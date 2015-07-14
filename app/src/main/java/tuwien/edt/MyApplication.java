package tuwien.edt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Random;


public class MyApplication extends Activity implements
        DelayedConfirmationView.DelayedConfirmationListener, SensorEventListener {

    private static final int PROGRESS = 0x1;

    private ProgressBar mProgress;
    private int mProgressStatus = 0;

    private Handler mHandler = new Handler();


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
    private static final int NUM_SECONDS = 5;
    //DelayedConfirmationView.DelayedConfirmationListener listener;
    DelayedConfirmationView delayedConfirmationView;
    ImageView player;
    boolean flag = true;
    int counter = 0;
    TextView attackMsg;
    TextView attackInfo;
    LinearLayout linLayout;
    int gameMode = 0;
    int maxMode = 3;
    long startTime,endTime,startTimeX;
    boolean gameModeFlag = false;
    boolean duellStart=false;
    int shoots=0;
    boolean duellEnd=false;
    boolean vibrate=true;
    boolean shot=false;
    int rand=3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rect_activity_attack);
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // Get an instance of the PowerManager
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);

        // Get an instance of the WindowManager
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        /*final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub_attack);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
                delayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
                delayedConfirmationView.start();
                delayedConfirmationView.setListener(MyApplication.this);
                player = (ImageView) findViewById(R.id.player);
            }
        });*/

        delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
        delayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
        delayedConfirmationView.start();
        delayedConfirmationView.setListener(MyApplication.this);

        player = (ImageView) findViewById(R.id.player);
        attackMsg = (TextView) findViewById(R.id.attackMsg);
        attackInfo = (TextView) findViewById(R.id.attackInfo);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        linLayout = (LinearLayout) findViewById(R.id.linLayout);
        endTime = delayedConfirmationView.getDrawingTime();
    }

    public void onStartTimer(View v) {
        delayedConfirmationView.start();
        delayedConfirmationView.setListener(this);
    }

    @Override
    public void onTimerSelected(View v) {
        v.setPressed(true);
        ((DelayedConfirmationView) v).setListener(null);
        finish();
    }

    @Override
    public void onTimerFinished(View v) {
        //delayedConfirmationView.setVisibility(View.GONE);

        //Log.v("infoMSG", "visible");
        boolean win = false;
        if(flag) {
            startGame();

        }else{
            flag=true;
            /*if(counter<150 && shoots == 1000){
                win = true;
                int wins = Integer.parseInt(WearableActivity.scoreBox.getText().toString());
                wins ++;
                WearableActivity.scoreBox.setText(Integer.toString(wins));
                WearableActivity.scoreBox.setVisibility(View.VISIBLE);
                WearableActivity.scoreLost.setVisibility(View.VISIBLE);
            }else
            */

            if(shoots > 0 && (endTime > startTimeX) && (( endTime - startTimeX) < 2000)) { //&& shoots != 1000
                win = true;
                int wins = Integer.parseInt(WearableActivity.scoreBox.getText().toString());
                wins ++;
                WearableActivity.scoreBox.setText(Integer.toString(wins));
                WearableActivity.scoreBox.setVisibility(View.VISIBLE);
                WearableActivity.scoreLost.setVisibility(View.VISIBLE);
            }else{
                int losses = Integer.parseInt(WearableActivity.scoreLost.getText().toString());
                losses ++;
                WearableActivity.scoreLost.setText(Integer.toString(losses));
                WearableActivity.scoreLost.setVisibility(View.VISIBLE);
                WearableActivity.scoreBox.setVisibility(View.VISIBLE);
            }
            setConfirmationActivity(win);
            WearableActivity.setAttackState(false);
            finish();
        }
    }

    public void setConfirmationActivity(boolean win){
        Intent intent = new Intent(this, ConfirmationActivity.class);
        if(win) {
            /*intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.SUCCESS_ANIMATION);*/
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.FAILURE_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                getString(R.string.conf_posmsg));
        }else{
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                    ConfirmationActivity.FAILURE_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                    getString(R.string.conf_posmsgfailure));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void startGame(){
        //gameMode = randInt(0,maxMode);
        gameMode = 3;
        attackMsg.setVisibility(View.VISIBLE);
        delayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 3000);
        delayedConfirmationView.start();
        delayedConfirmationView.setListener(MyApplication.this);
        Log.v("GameMode","GameMode: " + gameMode);

        if(gameMode==2){
            startTime = delayedConfirmationView.getDrawingTime();
            gameModeFlag=true;
            gameMode=0;
            attackMsg.setText("Random-Mode!!");
        }
        if(gameMode==3){
            attackMsg.setText("Duell!!");
            attackInfo.setText("Ziehe deine Waffe!!");
            startTime = delayedConfirmationView.getDrawingTime();
            rand = randInt(0,NUM_SECONDS);
            Log.v("Rand", "Rand:  " + String.valueOf(rand));
        }
        flag = false;
    }
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
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

        mSensorX = event.values[1];
        mSensorY = -event.values[0];
        if(flag==false){
            switch (gameMode){
                case 0:
                    shoots=1000;
                    counter += Math.abs(mSensorX + mSensorY);
                    attackInfo.setText("Waagrecht");
                    if(gameModeFlag) {
                        long currTimeX = delayedConfirmationView.getDrawingTime();
                        if (currTimeX - startTime > NUM_SECONDS * 1000) {
                            startTime = currTimeX;
                            gameMode = 1;
                        }
                    }
                    break;
                case 1:
                    shoots=1000;
                    counter+=Math.abs(10-Math.abs(mSensorX)-Math.abs(mSensorY));
                    attackInfo.setText("Senkrecht");
                    if(gameModeFlag) {
                        long currTimeY = delayedConfirmationView.getDrawingTime();
                        if (currTimeY - startTime > NUM_SECONDS * 1000) {
                            startTime = currTimeY;
                            gameMode = 0;
                        }
                    }
                    break;
                case 3:
                    /*Start
                    X -2 bis -4
                    Y 7 bis 9

                    ENDE
                    X -7 bis -9
                    Y -1 bis 1*/

                    long currTimeX = delayedConfirmationView.getDrawingTime();
                    if(currTimeX - startTime > rand * 1000 && shot != true) {
                        if(vibrate) {
                            startTimeX = delayedConfirmationView.getDrawingTime();
                            vibrateWatch();
                            vibrate=false;
                        }
                        //counter++;
                        if (mSensorX < -3 && mSensorX > -7 && mSensorY > 7 && duellStart == false) {
                            duellStart = true;
                            //vibrateWatch();
                        } else if (duellStart == true && mSensorX < -7 && mSensorY > -1 && mSensorY < 7) {
                            duellEnd = true;
                            duellStart = false;
                        }
                        if (duellStart == false && duellEnd == true) {
                            endTime = delayedConfirmationView.getDrawingTime();
                            shot=true;
                            shoots++;
                            duellEnd = false;
                            vibrateWatch();
                            Log.v("Time", "StartTime:  " + String.valueOf(startTimeX) + " EndTime:  " + String.valueOf(endTime) + " Diff:  " + String.valueOf(endTime-startTimeX));
                            Log.v("Shoots", "Shoots:  " + String.valueOf(shoots));
                        }
                    }
                    break;
            }
            //Log.v("counter", "counter " + String.valueOf(counter) + "drawing time: " + delayedConfirmationView.getDrawingTime());
            //Log.v("X/Y", "X:  " + String.valueOf(mSensorX) + "Y: " + String.valueOf(mSensorY));
            mProgressStatus = counter * 2 / 3;
            mProgress.setProgress(mProgressStatus);
            if (counter > 150){ linLayout.setBackgroundColor(0xffff0000);vibrateWatch();}
        }
    }

    public void vibrateWatch(){
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = {0, 500};
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
    }

    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
