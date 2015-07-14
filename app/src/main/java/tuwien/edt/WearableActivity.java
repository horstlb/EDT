package tuwien.edt;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

public class WearableActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float mSensorX;
    private float mSensorY;
    private Display mDisplay;
    private SensorManager sm;
    private PowerManager mPowerManager;
    private WindowManager mWindowManager;
    private float dist = 40;

    private static final String TAG = "WearableActivity";

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";
    public static boolean attack = false;

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;
    public static TextView scoreBox;
    public static TextView scoreLost;
    private TextView mTextView;
    TextView moveTextView;
    TextView gameInfo;
    ImageButton okBtn;
    ImageButton cancelBtn;
    ImageView statIcon;
    //private ParseObject testObject = new ParseObject("TestObject");
    /**
     * Called when the activity is starting. Restores the activity state.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wearable);


        // Enable Local Datastore.
        //Parse.enableLocalDatastore(this);
        //Parse.initialize(this, "H7l1EZ6aoMIkNcgJrh7ZRZL0kNRtqwmtBamrtm7d", "EkRor8syu9CaXpx2qP3lKaTnJrYCE6QWchp7Yzxm");

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                moveTextView = (TextView) stub.findViewById(R.id.moveTextView);
                okBtn = (ImageButton) stub.findViewById(R.id.okBtn);
                cancelBtn = (ImageButton) stub.findViewById(R.id.cancelBtn);
                statIcon = (ImageView) stub.findViewById(R.id.statIcon);
                gameInfo = (TextView) stub.findViewById(R.id.gameInfo);
                scoreBox = (TextView) findViewById(R.id.scoreBox);
                scoreLost = (TextView) findViewById(R.id.scoreLost);
            }
        });


    }

    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    /**
     * Called when activity gets invisible. Connection to Play Services needs to
     * be disconnected as soon as an activity is invisible.
     */
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Saves the resolution state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }



    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                retryConnecting();
                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");

        LocationRequest locationRequest;

        // Create a LocationRequest object
        locationRequest = LocationRequest.create();
        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 2 seconds
        locationRequest.setInterval(TimeUnit.SECONDS.toMillis(2));
        // Set the fastest update interval to 2 seconds
        locationRequest.setFastestInterval(TimeUnit.SECONDS.toMillis(2));
        // Set the minimum displacement
        locationRequest.setSmallestDisplacement(2);
        // Register for location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }

    @Override
    public void onLocationChanged(Location location){
        // Log the output for debugging
        Log.v("myTag", "Latitude: " + location.getLatitude() +
                ", Longitude: " + location.getLongitude());

        // Display latitude in UI in default wearable text view
        mTextView.setText("Latitude:  " + String.valueOf( location.getLatitude()) +
                "\nLongitude:  " + String.valueOf( location.getLongitude()));
        moveTextView.setText("Movement!");
        checkMarkers(location);

    }

    public void checkMarkers(Location location){
        float[] results = new float[1];
        boolean attackflag=false;
        double[] markerLat = new double[]{48.1831911,48.197791,48.198595,48.199056,48.199547,48.1980481};
        double[] markerLon = new double[]{15.6284965,16.371509,16.371635,16.369964,16.371153,16.3688539};
        //Zuhause, Seminarraum Argentinierstr.,vor der Karlskirche,Haupteingang TU,Denkmal J. Madersberger, Lernraum Paniglgasse

        for(int i=0; i<markerLat.length;i++) {
            Location.distanceBetween(markerLat[i], markerLon[i],
                    location.getLatitude(), location.getLongitude(), results);
            Log.v("Distance to Marker", String.valueOf(results[0]));
            if(results[0]< dist && attack == false){
                attackflag=true;
            }
        }

        //moveTextView.setText(String.valueOf(mSensorX));
        if(attackflag){
            gameInfo.setVisibility(View.GONE);
            attack = true;
            vibrateWatch();
            WearableActivity.scoreBox.setVisibility(View.GONE);
            WearableActivity.scoreLost.setVisibility(View.GONE);
            okBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
            statIcon.setImageResource(R.drawable.evil);
            //finish();
        }
    }
    public Boolean getAttackState() {
        return attack;
    }

    public static void setAttackState(Boolean newState) {
        attack = newState;
    }
    public void vibrateWatch(){
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 500, 50, 300};
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
    }
    public void defendAttack(View v){
        startActivity(new Intent(WearableActivity.this, MyApplication.class));
        statIcon.setImageResource(R.drawable.cool);
        okBtn.setVisibility(View.INVISIBLE);
        cancelBtn.setVisibility(View.INVISIBLE);

    }
    public void cancelAttack(View v){
        attack = false;
        statIcon.setImageResource(R.drawable.cool);
        okBtn.setVisibility(View.INVISIBLE);
        cancelBtn.setVisibility(View.INVISIBLE);
        gameInfo.setVisibility(View.VISIBLE);
    }
    public void simulateAttack(View v){
        attack = true;
        vibrateWatch();
        WearableActivity.scoreBox.setVisibility(View.GONE);
        WearableActivity.scoreLost.setVisibility(View.GONE);
        gameInfo.setVisibility(View.GONE);
        okBtn.setVisibility(View.VISIBLE);
        cancelBtn.setVisibility(View.VISIBLE);
        statIcon.setImageResource(R.drawable.evil);
    }
}
