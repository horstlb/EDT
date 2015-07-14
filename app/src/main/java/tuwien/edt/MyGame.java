package tuwien.edt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class MyGame extends Activity implements
        SensorEventListener {

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
    private static final int NUM_SECONDS = 3;
    //DelayedConfirmationView.DelayedConfirmationListener listener;
    public DelayedConfirmationView delayedConfirmationView;
    ImageView player;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub_game);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                player = (ImageView) findViewById(R.id.player);
                startGame();
            }
        });
    }



    public void setConfirmationActivity(boolean win){
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if(win) {
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.SUCCESS_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                getString(R.string.conf_posmsg));
        }else{
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.FAILURE_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                    getString(R.string.conf_posmsgfailure));
        }
        startActivity(intent);
    }
    public void startGame(){
        final int width = 150;
        int height = 150;
        final int top = 75;
        final int bottom = 75;
        final int[] left = {75};
        final int right = 75;
        final int[] newLeft = {75};
        final int[] newRight = {75};
        boolean win = true;
        final LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(width, height);
        layoutParams.setMargins(left[0],  top, right,  bottom);
        int i = 0;
       while(i <= 20){
           try{
               int newX = (int)(mSensorX*10);
               int newY = (int)(mSensorY*10);

               newLeft[0] += newX;
               newRight[0] = width-newLeft[0];

               layoutParams.setMargins(newLeft[0],  top, newRight[0],  bottom);
               player.setLayoutParams(layoutParams);

               Log.v("setParams", "X-Coordinate:" + String.valueOf(newLeft[0]));

               Thread.sleep(2000);
           }catch(InterruptedException e){
               e.printStackTrace();
           }
           i++;

       }
        setConfirmationActivity(win);
        startActivity(new Intent(MyGame.this, WearableActivity.class));
        finish();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
// can be safely ignored for this demo
    }

    public void onSensorChanged(SensorEvent event){
        //moveTextView = (TextView) findViewById(R.id.textView);
        //mTextView.setText("Movement!");

        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        switch (mDisplay.getRotation()) {
            case Surface.ROTATION_0:
                mSensorX = event.values[0];
                mSensorY = event.values[1];
                //Log.v("mSensorX", "Rotation_0 " + String.valueOf(mSensorX));
                break;
            case Surface.ROTATION_90:
                mSensorX = -event.values[1];
                mSensorY = event.values[0];
                //Log.v("mSensorX", "Rotation_90 " + String.valueOf(mSensorX));
                break;
            case Surface.ROTATION_180:
                mSensorX = -event.values[0];
                mSensorY = -event.values[1];
                //Log.v("mSensorX", "Rotation_180 " + String.valueOf(mSensorX));
                break;
            case Surface.ROTATION_270:
                mSensorX = event.values[1];
                mSensorY = -event.values[0];
                //Log.v("mSensorX", "Rotation_270 " + String.valueOf(mSensorX));
                break;
        }
    }

}
