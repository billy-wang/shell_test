
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.io.BufferedReader;
import java.io.FileReader;
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
    String a = null;

    private boolean mIsScreenBright = false;
    private boolean mIsScreenBrightStatus = false;

    private SensorEventListener mLightListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            mLightNum.setText(event.values[0] + "");
            Log.e(TAG, "LightNum = " + event.values[0]);
            if (event.values[0] < 50) {
                mIsDark = true;
            } else {
                mIsDark = false;
            }


            if (true == mIsClose && true == mIsDark && mFarTag) {
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
            if (true == mIsDark && true == mIsClose && mFarTag) {
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
            Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
            mIsScreenBrightStatus = true;

        }
    }

    public void updateSettings() {
        mIsScreenBright = isRespirationLampNotificationOn();
    }

    public boolean isRespirationLampNotificationOn() {
        boolean result = false;
        result = Settings.System.getInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
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
            e.printStackTrace();
            return null;
        }

        return line2;
    }

    private String readPsValue2() {
        BufferedReader bufferReader = null;
        String mFileName = "/persist/ps_calib";
        String line, line1, line2, line3;
        try {
            bufferReader = new BufferedReader(new FileReader(mFileName));
            line = bufferReader.readLine();
            line1 = bufferReader.readLine();
            line2 = bufferReader.readLine();
            line3 = bufferReader.readLine();
            Log.e(TAG, "disnum = " + line3);
            Log.e(TAG, "colse = " + line);
            Log.e(TAG, "far = " + line1);
            mCrntDistanse.setText(line3 + ", " + line + ", " + line1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return line3;
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case CAL_FAIL:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("fail to calibrate!").setCancelable(false).setPositiveButton("ok", null);
            dialog = builder.create();
            break;
        }
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

                    int result = SensorTest.runNativeSensorTest(40, 0, 5, true, true);
                    Log.e(TAG, "result = " + result);

                    if (result == 0) {
                        showDialog(CAL_SUCCESS);
                    } else {
                        showDialog(CAL_FAIL);
                    }
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
    public void onPause() {
        super.onPause();
        if (true == mIsLightRight) {
            mSensorMgr.unregisterListener(mLightListener);
        }
        if (true == mIsProximityRight) {
            mSensorMgr.unregisterListener(mProximityListener);
        }
        if (mIsScreenBrightStatus) {
            Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 1);

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
}
