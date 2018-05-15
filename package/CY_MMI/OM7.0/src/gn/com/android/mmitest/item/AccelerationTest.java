
package gn.com.android.mmitest.item;

import java.util.Timer;

import java.util.TimerTask;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
//Gionee zhangke 20151017 delete for platform start
//Platform
//import amigo.R.integer;
//Gionee zhangke 20151017 delete for platform end
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.app.Dialog;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.GnMMITest;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import gn.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.item.SensorUserCal;

public class AccelerationTest extends BaseActivity implements SensorEventListener, OnClickListener {
    private TextView mTitleTv;

    private TextView mContentTv;

    private ImageView mArrowView;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static String TAG = "AccelerationTest";

    private SensorManager mSensorMgr;

    private Sensor mSensor;

    private float[] mValues;

    private Timer mTimer;

    //Gionee zhangke 20151127 modify for CR01599589 start
    private static final int DELAY_TIME = 250;
    //Gionee zhangke 20151127 modify for CR01599589 end

    private int mSuccessNum;
    private Integer calib_ret;

    private boolean mIsAccRight;

    private boolean mIsLeftSuccess, mIsRightSuccess, mIsTopSuccess, mIsBottomSuccess, mIsFront, mIsback;

    private boolean calibsucc = true;
    private boolean isPause = false;
    private static final int CAL_FAIL11 = 2;
    private static final int CAL_FAIL = 0;
    private static final int CAL_SUCCESS = 1;
    private static final int CAL_SUCCESS_OK = 3;
    private static final int CAL_FAIL_FAIL = 4;
    private static final int CAL_FAIL_FAIL11 = 5;

    private int code = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开加速仪 @" + Integer.toHexString(hashCode()));
        // Gionee xiaolin 20120921 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120921 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.acceleration_test);

        mArrowView = (ImageView) findViewById(R.id.arraw);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mTitleTv.setText(R.string.acceleration_note);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mSensor = mSensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出加速仪 @" + Integer.toHexString(hashCode()));
    }


    @Override
    protected void onResume() {
        super.onResume();
        DswLog.e(TAG, " AccelerationTest  onResume");
        mIsAccRight = mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (false == mIsAccRight) {
            try {
                Thread.sleep(300);
                mIsAccRight = mSensorMgr.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } catch (InterruptedException e) {

            }
        }

        mSuccessNum = 0;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (mValues != null) {
                            AccelerationTest.this.showArrow(
                                    mValues[SensorManager.DATA_X],
                                    mValues[SensorManager.DATA_Y],
                                    mValues[SensorManager.DATA_Z]);
                        }
                    }
                });
            }
        }, 0, DELAY_TIME);
        DswLog.e(TAG, "gsensor calibrating...");
        new Thread(new Runnable() {
            public void run() {
                try {
                    mUiHandler.sendEmptyMessage(CAL_FAIL_FAIL11);
                    Context c = createPackageContext("com.mediatek.engineermode", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                    Class clazz = c.getClassLoader().loadClass("com.mediatek.engineermode.sensor.EmSensor");
                    GnReflectionMethods ReflectionMethods = new GnReflectionMethods(clazz, "doGsensorCalibration", new Class[]{int.class}, new Object[]{code}); //包名 方法名 方法参数类型 参数

                    //Gionee zhangke 20160322 modify for CR01656207 start

                    try{
                        Thread.sleep(1000);
                    }catch(InterruptedException e){
                    }

                    int calib_ret = (Integer)ReflectionMethods.getInvokeResult1();
                    /*	calib_ret = SensorUserCal.performUserCal((byte) 0,
								(byte) 0);*/
                    DswLog.e(TAG, "calib_ret Number = " + calib_ret + ";isPause="+isPause);
                    if (!isPause) {
                        if (calib_ret == 1) {
                            mUiHandler.sendEmptyMessage(CAL_SUCCESS_OK);
                        } else {
                            mUiHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                        }
                    }
                    //Gionee zhangke 20160322 modify for CR01656207 end
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CAL_FAIL_FAIL11:
                    showDialog(CAL_FAIL11);
                    break;
                case CAL_SUCCESS_OK:
                    removeDialog(CAL_FAIL11);
                    showDialog(CAL_SUCCESS);
                    mWrongBtn.setEnabled(true);
                    mRestartBtn.setEnabled(true);
                    break;
                case CAL_FAIL_FAIL:
                    calibsucc = false;
                    removeDialog(CAL_FAIL11);
                    showDialog(CAL_FAIL);
                    mWrongBtn.setEnabled(true);
                    mRestartBtn.setEnabled(true);
                    break;

            }

        }
    };


    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case CAL_FAIL11:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(
                        AccelerationTest.this);
                builder2.setMessage(R.string.acceler_cal).setCancelable(false)
                        .setPositiveButton(R.string.acceler_caling, null);
                dialog = builder2.create();
                break;
            case CAL_FAIL:
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        AccelerationTest.this);
                builder.setMessage(R.string.acceler).setCancelable(false)
                        .setPositiveButton(R.string.acceler_fail, null);
                dialog = builder.create();
                break;
            case CAL_SUCCESS:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(
                        AccelerationTest.this);
                builder1.setMessage(R.string.acceler).setCancelable(false)
                        .setPositiveButton(R.string.acceler_ok, null);
                dialog = builder1.create();
                break;
        }
        return dialog;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        StringBuffer sb = new StringBuffer();
        sb.append("x = " + event.values[SensorManager.DATA_X] + "\n");
        sb.append("y = " + event.values[SensorManager.DATA_Y] + "\n");
        sb.append("z = " + event.values[SensorManager.DATA_Z] + "\n");
//        DswLog.e(TAG, "X=" + event.values[SensorManager.DATA_X]);
//        DswLog.e(TAG, "Y=" + event.values[SensorManager.DATA_Y]);
//        DswLog.e(TAG, "Z=" + event.values[SensorManager.DATA_Z]);
        mContentTv.setText(sb.toString());
        mValues = event.values;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPause() {
        super.onPause();
        isPause = true;
        if (true == mIsAccRight) {
            mSensorMgr.unregisterListener(this);
        }
        //Gionee zhangke 20151124 add for CR01595957 start
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        //Gionee zhangke 20151124 add for CR01595957 end

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    //Gionee zhangke 20151124 modify for CR01595957 start
    private void showArrow(float x, float y, float z) {
        if (x > 9f) {
            mArrowView.setImageResource(R.drawable.arrow_right);
            if (false == mIsLeftSuccess) {
                DswLog.i(TAG, " Left Success");
                mSuccessNum++;
                mIsLeftSuccess = true;
            }
        } else if (x < -9f) {
            mArrowView.setImageResource(R.drawable.arrow_left);
            if (false == mIsRightSuccess) {
                DswLog.i(TAG, " Right Success");
                mSuccessNum++;
                mIsRightSuccess = true;
            }
        } else if (y > 9f) {
            mArrowView.setImageResource(R.drawable.arrow_up);
            if (false == mIsBottomSuccess) {
                DswLog.i(TAG, " Bottom Success");
                mSuccessNum++;
                mIsBottomSuccess = true;
            }
        } else if (y < -9f) {
            mArrowView.setImageResource(R.drawable.arrow_down);
            if (false == mIsTopSuccess) {
                DswLog.i(TAG, "Top Success");
                mSuccessNum++;
                mIsTopSuccess = true;
            }
        } else if (z > 9f) {
            mArrowView.setImageResource(R.drawable.arrow);
            if (false == mIsFront) {
                DswLog.i(TAG, "Front Success");
                mSuccessNum++;
                mIsFront = true;
            }
        } else if (z < -9f) {
            mArrowView.setImageResource(R.drawable.arrow);
            if (false == mIsback) {
                DswLog.i(TAG, "Back Success");
                mSuccessNum++;
                mIsback = true;
            }
        }

        DswLog.i(TAG, "mSuccessNum=" + mSuccessNum + ";calibsucc=" + calibsucc);
        if (6 <= mSuccessNum && calibsucc) {
            mRightBtn.setEnabled(true);
            //mSuccessNum++;
        }
    }
    //Gionee zhangke 20151124 modify for CR01595957 end


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
