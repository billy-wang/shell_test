
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import gn.com.android.mmitest.item.EmSensor.EmSensor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
/* Gionee huangjianqiang 20160125 add for CR01625818 begin  */
import android.content.Intent;
/* Gionee huangjianqiang 20160125 add for CR01625818 end  */
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Message;

//Gionee <xuna><2012-11-02> modify for CR00724154 begin

public class AlsAndPsTest extends BaseActivity implements OnClickListener {
    private TextView tv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "AlsAndPsTest";

    private SensorManager mSensorMgr;

    private Sensor mLSensor;
    private Sensor mPSensor;
    private Sensor mDisSensor;
    private Timer mTimer;

    //Gionee liss 20111215 add mLightProTitle for CR00478802 start    
    private TextView mLightNum, mProximityNum, mLightProTitle, mCrntDistanse, colourNum;
    //Gionee liss 20111215 add mLightProTitle for CR00478802 end

    private RelativeLayout mParent;

    private boolean mIsClose, mIsFar;

    private boolean mIsDark;

    private boolean mIsLightRight, mIsProximityRight, mDisSensorRight;
    private boolean mFarTag = false;

    private static final int CAL_FAIL = 0;
    private static final int CAL_SUCCESS = 1;
    //Gionee zhangke 20160113 add for CR01623114 start
    private static final int CAL_ING = 2;
    //Gionee zhangke 20160113 add for CR01623114 end

    String a = null;

    //Gionee zhangxiaowei 20131109 add for CR00946202 start
    private boolean mIsScreenBright = false;
    private boolean mIsScreenBrightStatus = false;
    //Gionee zhangxiaowei 20131109 add for CR00946202 end

    // Gionee wujj 2015-09-09 add for CR01543944 begin
    EmSensor mEmSensor = null;
    //Handler mHandler = new Handler();
    float mPrivValue = 0;
    float mDelta = 0.0f;
    private int mCalSuccess = CAL_FAIL;
    // Gionee wujj 2015-09-09 add for CR01543944 end

    //Gionee zhangke 20151225 add for CR01613440 start
    private static final int MESSAGE_SHOW_FAIL_DIALOG = 0;
    //Gionee zhangke 20160113 add for CR01623114 start
    private static final int MESSAGE_SHOW_CALING_DIALOG = 1;
    private static final int MESSAGE_REMOVE_CAL_ING_DIALOG = 2;
    //Gionee zhangke 20160113 add for CR01623114 end
    /* Gionee huangjianqiang 20160125 add for CR01625818 begin  */
    private boolean mAlsAndPs = false;
    /* Gionee huangjianqiang 20160125 add for CR01625818 end  */

    //Gionee zhangke 20160419 add for CR01680501 start
    private boolean mIsCalSuccess = false;
    //Gionee zhangke 20160419 add for CR01680501 end

    private boolean mIsStoped = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_FAIL_DIALOG:
                    removeDialog(CAL_ING);
                    showDialog(CAL_FAIL);
                    break;
                //Gionee zhangke 20160113 add for CR01623114 start
                case MESSAGE_SHOW_CALING_DIALOG:
                    showDialog(CAL_ING);
                    break;
                case MESSAGE_REMOVE_CAL_ING_DIALOG:
                    removeDialog(CAL_ING);
                    break;

                //Gionee zhangke 20160113 add for CR01623114 end
            }
        }
    };

    //Gionee zhangke 20151225 add for CR01613440 end

    private SensorEventListener mLightListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // Gionee wujj 2015-09-09 add for CR01543944 begin
            // Do not change the TextView too frequently
            mDelta = event.values[0] - mPrivValue;
            if (Math.abs(mDelta) > 5f || mIsClose) {
                mLightNum.setText(event.values[0] + "");
            }
            mPrivValue = event.values[0];
            // Gionee wujj 2015-09-09 add for CR01543944 end

            Log.e(TAG, "LightNum = " + event.values[0]);
            if (event.values[0] < 50) {
                mIsDark = true;
            } else {
                mIsDark = false;
            }

            //Gionee liss 20111215 add for CR00478802 start
            //if (true == mIsFar && true == mIsClose && event.values[0] < 5) {
            //Gionee xiaolin 20120227 modify for CR00534606 start
            //Gionee zhangke 20160419 add for CR01680501 start
            if (true == mIsClose && true == mIsDark && mFarTag && mIsCalSuccess) {
                //Gionee zhangke 20160419 add for CR01680501 end
                //Gionee xiaolin 20120227 modify for CR00534606 end
                //Gionee liss 20111215 add for CR00478802 end
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

    SensorEventListener mProximityListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            int i = (int) event.values[0];
            Log.e(TAG, "ProximityNum = " + i);
            mIsClose = (i == 0 ? true : false);
            //Gionee xiaolin 20120227 modify for CR00534606 start
            if (i != 0) {
                mFarTag = true;
                Log.e(TAG, "i2222 = mFarTag = true;");
            }
            //Gionee zhangke 20160419 add for CR01680501 start
            if (true == mIsDark && true == mIsClose && mFarTag && mIsCalSuccess) {
                //Gionee zhangke 20160419 add for CR01680501 end
                //Gionee xiaolin 20120227 modify for CR00534606 end
                mParent.setBackgroundColor(Color.GREEN);
                mRightBtn.setEnabled(true);
            } else {
                mParent.setBackgroundColor(Color.BLACK);
            }

            if (0 != i)
                i = 1;
            mProximityNum.setText(Integer.toString(i));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Gionee huangjianqiang 20160125 add for CR01625818 begin */
        mAlsAndPs = getIntent().getBooleanExtra("AlsAndPs", false);
        if (mAlsAndPs) {
            setContentView(R.layout.gn_oversea_light_proximity);
            setTitle(R.string.gn_oversea_light_proximity);
        } else {
            setContentView(R.layout.light_proximity);
        }
        /* Gionee huangjianqiang 20160125 add for CR01625818 end */

        // Gionee liss 20111215 add for CR00478802 start
        mLightProTitle = (TextView) findViewById(R.id.light_proximity_title);
        mLightNum = (TextView) findViewById(R.id.light_num);
        mProximityNum = (TextView) findViewById(R.id.proximity_num);
        mCrntDistanse = (TextView) findViewById(R.id.crnt_distanse_num);
        //  colourNum  =(TextView) findViewById(R.id.colournum);
        mParent = (RelativeLayout) findViewById(R.id.light_proximity_rl);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);

        //Gionee zhangxiaowei 20131109 add for CR00946202 start
//        updateSettings();
//        if (mIsScreenBright == true) {
//            Settings.System.putInt(this.getContentResolver(),
//                    Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
//            mIsScreenBrightStatus = true;
//
//        }
        //Gionee zhangxiaowei 20131109 add for CR00946202 end

        // Gionee xiaolin 20121012 add for CR00711174 start
        if (isFileExisted("/sys/bus/platform/drivers/als_ps/high_threshold")) {
            calibratePs();
        }
        // Gionee xiaolin 20121012 add for CR00711174 end

        // Gionee wujj 2015-09-09 add for CR01543944 begin
        mEmSensor = EmSensor.getInstance(this);
        // Gionee wujj 2015-09-09 add for CR01543944 end
    }

    //Gionee zhangxiaowei 20131109 add for CR00946202 start
    public void updateSettings() {
        mIsScreenBright = isRespirationLampNotificationOn();
    }

    public boolean isRespirationLampNotificationOn() {
        boolean result = false;
        result = Settings.System.getInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
        return result;
    }
    //Gionee zhangxiaowei 20131109 add for CR00946202 end

    // Gionee xiaolin 20121012 add for CR00711174 start
    private void calibratePs() {
        IBinder binder = null;
        binder = ServiceManager.getService("NvRAMBackupAgent");
        if (binder == null) {
            Log.e(TAG, "binder is  NULL");
            return;
        }

        try {
            NvRAMBackupAgent agent = null;
            agent = NvRAMBackupAgent.Stub.asInterface(binder);
            int[] calData = null;
            calData = agent.readFile();
            Log.e(TAG, "calibrate  result is  =  " + shouldSaveToNv(calData));
            if (shouldSaveToNv(calData)) {
                mCalSuccess = CAL_SUCCESS;
                /*Gionee huangjianqiang 20160606 add modify for CR01675907 begin */
                mIsCalSuccess = true;
                /*Gionee huangjianqiang 20160606 add modify for CR01675907 end */
                // GIONEE removed for GBL7320 begin
                // In some case, SN is lost. So don't backup NV too frequently
                /**
                 agent.writeFile(calData);
                 agent.backupFile();
                 */
                // GIONEE removed for GBL7320 end
                Log.v(TAG, "Test!!! don't backup PSensor cal data!");
                Log.e(TAG, "backup is ok  and  calibrate is success! ");
            } else {
                showDialog(CAL_FAIL);
            }
        } catch (RemoteException re) {
            Log.e(TAG, re.toString());
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case CAL_FAIL:
                //Gionee <GN_BSP_MMI> <chengq> <20170210> modify for ID 68006 begin
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.psensor_cal_fail)
                        .setCancelable(false)
                        .setPositiveButton("ok", null);
                dialog = builder.create();
                //Gionee <GN_BSP_MMI> <chengq> <20170210> modify for ID 68006 end
                break;
            case CAL_ING:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setMessage(R.string.psensor_caling)
                        .setCancelable(false)
                        .setPositiveButton(R.string.acceler_caling, null);
                dialog = builder2.create();
                break;
        }
        return dialog;
    }

    private boolean shouldSaveToNv(int[] data) {
        if (data == null || data.length != 3 || data[2] == 0)
            return false;
        return true;
    }
    // Gionee xiaolin 20121012 add for CR00711174 end

    // Gionee wujj 2015-09-09 add for CR01543944 begin
    private String readPsValue2() {
        String highThreshold, lowThreshold, distanceValue;
        try {
            String fileName = "/sys/bus/platform/drivers/als_ps/high_threshold";
            if (isFileExisted(fileName)) {
                highThreshold = getRightDistanse(fileName);//接近阀值
            } else {
                highThreshold = Integer.toString(mEmSensor.getPsensorHighThreshold());
            }
            fileName = "/sys/bus/platform/drivers/als_ps/low_threshold";
            if (isFileExisted(fileName)) {
                lowThreshold = getRightDistanse(fileName);//远离阀值
            } else {
                lowThreshold = Integer.toString(mEmSensor.getPsensorLowThreshold());
            }
            fileName = "/sys/bus/platform/drivers/als_ps/pdata";
            if (isFileExisted("/sys/bus/platform/drivers/als_ps/pdata")) {
                distanceValue = getRightDistanse(fileName);//p-sensor实时寄存器值
            } else {
                distanceValue = Integer.toString(mEmSensor.getPsensorData());
            }

            Log.e(TAG, "disnum = " + distanceValue);
            Log.e(TAG, "colse = " + highThreshold);
            Log.e(TAG, "far = " + lowThreshold);
            mCrntDistanse.setText(distanceValue + ", " + highThreshold + ", "
                    + lowThreshold);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return distanceValue;
    }
    // Gionee wujj 2015-09-09 add for CR01543944 end


    @Override
    protected void onResume() {
        super.onResume();


        mLSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);//光感
        if (mLSensor != null) {
            mIsLightRight = mSensorMgr.registerListener(mLightListener, mLSensor, SensorManager.SENSOR_DELAY_FASTEST);

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
            mIsProximityRight = mSensorMgr.registerListener(mProximityListener, mPSensor, SensorManager.SENSOR_DELAY_FASTEST);
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
        // Gionee zhangke 20151225 modify for CR01613440 start
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mCalSuccess != CAL_SUCCESS) {
                    //Gionee zhangke 20160113 add for CR01623114 start
                    if (!mIsStoped) {
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_CALING_DIALOG);
                    }
                    //Gionee zhangke 20160113 add for CR01623114 end
                    int result = mEmSensor.doPsensorCalibration();
                    Log.i(TAG, "Thread doPsensorCalibration=" + result);
                    if (result == EmSensor.RET_ERROR && !mIsStoped) {
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_FAIL_DIALOG);
                    } else {//Gionee zhangke 20160113 add for CR01623114 start
                        //Gionee zhangke 20160419 add for CR01680501 start
                        mIsCalSuccess = true;
                        //Gionee zhangke 20160419 add for CR01680501 end
                        mHandler.sendEmptyMessage(MESSAGE_REMOVE_CAL_ING_DIALOG);
                    }//Gionee zhangke 20160113 add for CR01623114 end
                } else {
                /*Gionee huangjianqiang 20160606 add modify for CR01675907 begin */
                    mIsCalSuccess = true;
                /*Gionee huangjianqiang 20160606 add modify for CR01675907 end */
                }
            }
        }).start();
        //Gionee zhangke 20151225 modify for CR01613440 end


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
    public void onStop() {
        super.onStop();
        mIsStoped = true;
        mHandler.removeMessages(MESSAGE_SHOW_FAIL_DIALOG);
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
        //Gionee zhangxiaowei 20131109 add for CR00946202 start
        if (mIsScreenBrightStatus) {
            Settings.System.putInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 1);

        }
        //Gionee zhangxiaowei 20131109 add for CR00946202 end

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
                /* Gionee huangjianqiang 20160125 modify for CR01625818 begin */
                if (mAlsAndPs) {
                    restartbtn();
                } else {
                    TestUtils.restart(this, TAG);
                }
                /* Gionee huangjianqiang 20160125 modify for CR01625818 end */
                break;
            }
        }

    }

    public String getRightDistanse(String fileName) {
        String rightDistanse = null;
        String mFileName = null;
        mFileName = fileName;
//	 	if(i == 1){
//	    mFileName = "/sys/bus/platform/drivers/als_ps/high_threshold";}
//		else if(i == 2){
//		mFileName = "/sys/bus/platform/drivers/als_ps/low_threshold";}
//		else if(i == 3){
//		mFileName = "/sys/bus/platform/drivers/als_ps/pdata";}
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File voltageFilePath = new File(mFileName);
                if (voltageFilePath.exists()) {
                    fileInputStream = new FileInputStream(voltageFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        rightDistanse = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "rightDistanse=" + rightDistanse);
        return rightDistanse;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    // Gionee wujj 2015-09-09 add for CR01543944 begin
    boolean isFileExisted(String fileName) {
        File file = new File(fileName);
        if (file != null && file.exists())
            return true;

        Log.v(TAG, fileName + "isn't existed!");
        return false;
    }
    // Gionee wujj 2015-09-09 add for CR01543944 end


    /* Gionee huangjianqiang 20160125 add for CR01625818 begin */
    public void restartbtn() {
        try {
            finish();
            Intent it = new Intent(this, Class
                    .forName("gn.com.android.mmitest.item." + TAG));
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            it.putExtra("AlsAndPs", true);
            startActivity(it);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    /* Gionee huangjianqiang 20160125 add for CR01625818 end */
}

