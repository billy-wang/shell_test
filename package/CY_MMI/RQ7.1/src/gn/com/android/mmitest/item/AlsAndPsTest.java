
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;

public class AlsAndPsTest extends Activity implements OnClickListener {
    private TextView tv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "AlsAndPsTest";

    private SensorManager mSensorMgr;

    private Sensor mLSensor;
    private Sensor mPSensor;
    private Sensor mDisSensor;
    private Timer mTimer;

    private TextView mLightNum, mProximityNum, mLightProTitle, mCrntDistanse, colourNum;

    private RelativeLayout mParent;

    private boolean mIsClose, mIsFar;

    private boolean mIsDark;

    private boolean mIsLightRight, mIsProximityRight;
    private boolean mFarTag = false;

    private static final int CAL_FAIL = 0;
    private static final int CAL_SUCCESS = 1;
    private static final int CAL_ING = 2;
    String a = null;
    private static final int MESSAGE_REMOVE_CAL_ING_DIALOG = 3;
    private boolean mIsCalSuccess = false;
    private boolean mIsStoped = false;
    private boolean mIsScreenBright = false;
    private boolean mIsScreenBrightStatus = false;
    private static final int MESSAGE_SHOW_CALING_DIALOG = 4;
    private static final int MESSAGE_SHOW_FAIL_DIALOG = 5;
    private int mCalSuccess = CAL_FAIL;
    private boolean mIsStop;
    private boolean writeResult = true;
    private SensorEventListener mLightListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            mLightNum.setText(event.values[0] + " ");
            Log.e(TAG, "LightNum = " + event.values[0]);
            if (event.values[0] < 50) {
                mIsDark = true;
            } else {
                mIsDark = false;
            }


            if (true == mIsClose && true == mIsDark && mFarTag && mIsCalSuccess) {
                mParent.setBackgroundColor(Color.GREEN);
                mRightBtn.setEnabled(true);
            } else {
                mParent.setBackgroundColor(Color.BLACK);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    };

    // \u74ba\u6fc8\ue787
    SensorEventListener mProximityListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            int i = (int) event.values[0];
            Log.e(TAG, "ProximityNum = " + i);
            mIsClose = (i == 0 ? true : false);
            // Gionee xiaolin 20120227 modify for CR00534606 start
            if (i != 0) {
                mFarTag = true;
                Log.e(TAG, "i2222 = mFarTag = true;");
            }
            Log.e(TAG, "mIsDark="+mIsDark+";mIsClose="+mIsClose+";mFarTag="+mFarTag+";mIsCalSuccess="+mIsCalSuccess);
            if (true == mIsDark && true == mIsClose && mFarTag && mIsCalSuccess ) {
                // Gionee xiaolin 20120227 modify for CR00534606 end
                mParent.setBackgroundColor(Color.GREEN);
                mRightBtn.setEnabled(true);
            } else {
                mParent.setBackgroundColor(Color.BLACK);
            }

            if (0 != i)
                i = 1;
            mProximityNum.setText(i + "");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 81053 begin
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(mIsStop){
                Log.d(TAG,"mIsStop == true");
                return ;
            }
            switch (msg.what) {
                case MESSAGE_SHOW_FAIL_DIALOG:
                    removeDialog(CAL_ING);
                    showDialog(CAL_FAIL);
                break;
                case MESSAGE_SHOW_CALING_DIALOG:
                    showDialog(CAL_ING);
                break;
                case MESSAGE_REMOVE_CAL_ING_DIALOG:
                    removeDialog(CAL_ING);
                break;
           }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 81053 end
    //Gionee <GN_BSP_MMI> <lifeilong> <20170413> modify for ID 112921 begin
    /*static {
        System.loadLibrary("sensor_test2"); 
    }*/
    //Gionee <GN_BSP_MMI> <lifeilong> <20170413> modify for ID 112921 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        setContentView(R.layout.light_proximity);

        mLightProTitle = (TextView) findViewById(R.id.light_proximity_title);

        try {
            a = readPsValue();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        Log.e(TAG, "ps_result = " + readPsValue());

        mLightNum = (TextView) findViewById(R.id.light_num);
        mProximityNum = (TextView) findViewById(R.id.proximity_num);
        mCrntDistanse = (TextView) findViewById(R.id.crnt_distanse_num);
        mParent = (RelativeLayout) findViewById(R.id.light_proximity_rl);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        updateSettings();
        if (mIsScreenBright == true) {
            //Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
            mIsScreenBrightStatus = true;

        }
        mIsStop = false;
    }

    public void updateSettings() {
        mIsScreenBright = isRespirationLampNotificationOn();
    }

    public boolean isRespirationLampNotificationOn() {
        boolean result = false;
        //result = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
        return result;
    }

    private String readPsValue() {
        BufferedReader bufferReader = null;
        String mFileName = "/persist/ps_calib";
        String line, line1, line2 = null;
        try {
            bufferReader = new BufferedReader(new FileReader(mFileName));
            line = bufferReader.readLine();
            line1 = bufferReader.readLine();
            line2 = bufferReader.readLine();
            Log.e(TAG, "readPsValue = " + line2);
        } catch (Exception e) {
        //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 84270 begin
            e.printStackTrace();
            try {
                bufferReader.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                bufferReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 84270 end
        return line2;
    }

    private String readPsValue2() {
        BufferedReader bufferReader = null;
        BufferedWriter bw = null;
        String mFileName = "/persist/ps_calib";
        String line, line1, line2, line3 = null;
        try {
            bufferReader = new BufferedReader(new FileReader(mFileName));
            //writeResult = true;
            //Gionee <GN_BSP_MMI> <lifeilong> <20170909> modify for ID 207632 begin
            Log.d(TAG,"writeResult  == " + writeResult);
            /*if(writeResult){
                bw = new BufferedWriter(new FileWriter(mFileName));
                String result = null;
                Log.d(TAG,"BufferedWriter ");
                int i = 0;
                while(i < 4){
                    bw.write("233");
                    bw.newLine();
                    bw.flush();
                    i++;
                    Log.d(TAG,"BufferedWriter  write 1 ");
                }
                writeResult = false;
            }*/
            //Gionee <GN_BSP_MMI> <lifeilong> <20170909> modify for ID 207632 end
            line = bufferReader.readLine();
            line1 = bufferReader.readLine();
            line2 = bufferReader.readLine();
            line3 = bufferReader.readLine();
            Log.e(TAG, "disnum = " + line3 + "   , colse = " + line + "  , far = " + line1 );
            //Log.e(TAG, "colse = " + line);
            //Log.e(TAG, "far = " + line1);
            mCrntDistanse.setText(line3 + ", " + line + ", " + line1 + " ");
            if(bufferReader != null) bufferReader.close();
            //if(bw != null ) bw.close(); 
        } catch (Exception e) {
            e.printStackTrace();
            //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 84270 begin
            try {
                bufferReader.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                bufferReader.close();
                //bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 84270 end
        return line3;
        
    }
    
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null; 
        switch (id) {
        case CAL_FAIL:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.psensor_cal_fail)).setCancelable(false).setPositiveButton("ok", null);
            dialog = builder.create();
            break;
        case CAL_ING:
           AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
           builder2.setMessage(R.string.psensor_caling)
               .setCancelable(false)
               .setPositiveButton(R.string.acceler_caling, null);
           dialog = builder2.create();
           break;
        }
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
        getWindow().getDecorView().getSystemUiVisibility());    
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);// \u934f\u590b\u5285
        if (mLSensor != null) {
            mIsLightRight = mSensorMgr.registerListener(mLightListener, mLSensor, SensorManager.SENSOR_DELAY_FASTEST);
            if (a != null) {
                if (a.equals("0")) {

                    //int result = SensorTest.runNativeSensorTest(40, 0, 5, true, true);
                    //Log.e(TAG, "result = " + result);
                    /*if (result == 0) {
                        showDialog(CAL_SUCCESS);
                    } else {
                        showDialog(CAL_FAIL);
                    }*/
                }
            }
            if (false == mIsLightRight) {
                try {
                    Thread.sleep(300);
                    mIsLightRight = mSensorMgr.registerListener(mLightListener, mLSensor,
                            SensorManager.SENSOR_DELAY_FASTEST);
                } catch (InterruptedException e) {

                }
                if (false == mIsLightRight) {
                    mLightNum.setText(R.string.init_light_sensor_fail);
                }
            }
        }
        mPSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mPSensor != null) {
            mIsProximityRight = mSensorMgr.registerListener(mProximityListener, mPSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
            if (false == mIsProximityRight) {
                try {
                    Thread.sleep(300);
                    mIsProximityRight = mSensorMgr.registerListener(mProximityListener, mPSensor,
                            SensorManager.SENSOR_DELAY_FASTEST);
                } catch (InterruptedException e) {

                }
                if (false == mIsProximityRight) {
                    mProximityNum.setText(R.string.init_proximity_sensor_fail);
                }
            }
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 81053 begin
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mCalSuccess != CAL_SUCCESS) {
					//Gionee <GN_BSP_MMI> <lifeilong> <20170508> modify for ID 134202 begin
                    if(!mIsStoped){
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_CALING_DIALOG);
                    }
					//Gionee <GN_BSP_MMI> <lifeilong> <20170508> modify for ID 134202 end
                    try {
                      Thread.sleep(1500);
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    }
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170413> modify for ID 112921 begin
                    //int result = SensorTest.runNativeSensorTest(40, 0, 5, true, true);
                    //Log.e(TAG, "SensorTest.runNativeSensorTest result = " + result);
                    //
                    //if(result == 0 ){
                      //  Log.e(TAG,"writeResult");
                        //writeResult = true;
                    //}
                    String calResult = readPsValue2();
                    Log.e(TAG,"= = calResult < 800 ? " + calResult);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170424> modify for ID 123461 begin
                    try {
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170508> modify for ID 134202 begin
                        //if (Integer.parseInt(calResult) < 800 && result == 0 && !mIsStoped) {
                        if (Integer.parseInt(calResult) < 800 && !mIsStoped) {
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170508> modify for ID 134202 end
                        //Gionee <GN_BSP_MMI> <lifeilong> <20170413> modify for ID 112921 end
                            mIsCalSuccess = true;
                            mHandler.sendEmptyMessage(MESSAGE_REMOVE_CAL_ING_DIALOG);
                        }else{
                            mIsCalSuccess = false;
                            mHandler.sendEmptyMessage(MESSAGE_SHOW_FAIL_DIALOG);
                        }
                    } catch (Exception e) {
                        Log.e(TAG,"= = calResult = = Exception " + calResult);
                        e.printStackTrace();
                        mIsCalSuccess = false;
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_FAIL_DIALOG);
                    }
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170424> modify for ID 123461 end
              }
           }
        }).start();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170329> modify for ID 81053 end

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        readPsValue2();
                    }
                });
            }
        }, 0, 100);

    }

    @Override
    //Gionee <GN_BSP_MMI> <lifeilong> <20170508> modify for ID 134202 begin
    public void onStop() {
        super.onStop();
        mIsStop = true;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170508> modify for ID 134202 end
        Log.e(TAG,"onStop");
        //int result = SensorTest.runNativeSensorTest(40, 0, 1, true, true);
        //Log.e(TAG,"onPause  === runNativeSensorTest  == " + result);
        mIsStoped = true;
        mHandler.removeMessages(MESSAGE_SHOW_FAIL_DIALOG);
        if (true == mIsLightRight) {
            mSensorMgr.unregisterListener(mLightListener);
        }
        if (true == mIsProximityRight) {
            mSensorMgr.unregisterListener(mProximityListener);
        }
        if (mIsScreenBrightStatus) {
            //Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 1);

        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

        case R.id.right_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.rightPress(TAG, this);
            break;
        }

        case R.id.wrong_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.wrongPress(TAG, this);
            break;
        }

        case R.id.restart_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.restart(this, TAG);
            break;
        }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    //Gionee <GN_BSP_MMI> <lifeilong> <20170413> modify for ID 112921 begin
      /**
     * Native function (ie. implemented in C) to run the self-test.
     *
     * Using primitive types through JNI is considerably simpler, less error-prone, and
     * requires less memory than passing complex objects.  Hence the reason for both a
     * non-native, compile-time argument type-enforcing function and a native, simple
     * JNI function.
     *
     * @param sensorID Sensor ID as defined by the SMGR API.
     * @param dataType Binary integer for primary/secondary data.  Int is used instead of
     *                 Boolean in case future data types are created.
     * @param testType The specific test to run.
     * @param saveToRegistry If applicable, whether to save bias calculations
     *                       to the registry as part of the test.
     * @param applyCalNow If applicable, whether to apply bias calculations immediately.
     * @return Error-code as returned from the sensor.  0 upon success.
     */
    static public native int runNativeSensorTest(int sensorID, int dataType, int testType,
          boolean saveToRegistry, boolean applyCalNow);
    //Gionee <GN_BSP_MMI> <lifeilong> <20170413> modify for ID 112921 end
}
