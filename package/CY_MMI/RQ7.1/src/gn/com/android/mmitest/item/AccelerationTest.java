package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import gn.com.android.mmitest.item.SensorTest;

public class AccelerationTest extends Activity implements SensorEventListener, OnClickListener {
    private TextView mTitleTv;

    private TextView mContentTv;

    private ImageView mArrowView;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static String TAG = "AccelerationTest";

    private SensorManager mSensorMgr;

    private Sensor mSensor;

    private float[] mValues;

    private Timer mTimer;

    private static final int DELAY_TIME = 500;

    private int mSuccessNum;
    private Integer calib_ret;
    private boolean mIsSuccefully;
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
    private boolean mIsStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        setContentView(R.layout.acceleration_test);

        mArrowView = (ImageView) findViewById(R.id.arraw);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mTitleTv.setText(R.string.acceleration_note);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        // Gionee zhangke 20160628 modify for CR01724239 start
        try {
            mSensor = mSensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: no accelerometer sensor", Toast.LENGTH_LONG).show();
            TestUtils.wrongPress(TAG, this);
            return;
        }
        // Gionee zhangke 20160628 modify for CR01724239 end

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mIsStop = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        mIsAccRight = mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (false == mIsAccRight) {
            try {
                Thread.sleep(300);
                mIsAccRight = mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
                            AccelerationTest.this.showArrow(mValues[SensorManager.DATA_X],
                                    mValues[SensorManager.DATA_Y], mValues[SensorManager.DATA_Z]);
                        }
                    }
                });
            }
        }, 0, DELAY_TIME);
        Log.e(TAG, "gsensor calibrating...");
        new Thread(new Runnable() {
            public void run() {
                try {
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170308> modify for ID 80758 begin
                    mUiHandler.sendEmptyMessage(CAL_FAIL_FAIL11);
                    calib_ret = SensorUserCal.performUserCal((byte) 0,(byte) 0);
                    Log.e(TAG, "calib_ret Number = " + calib_ret);
                    if (!isPause) {
                        if (calib_ret == 0) {
                            mUiHandler.sendEmptyMessage(CAL_SUCCESS_OK);
                        } else {
                            mUiHandler.sendEmptyMessage(CAL_FAIL_FAIL);
                        }
                    }
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170308> modify for ID 80758 end
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();

    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        mIsStop = true;
    }


    // 主线程显示画面
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            if(mIsStop){
                Log.d(TAG,"mIsStop == true");
                return ;
            }
            switch (msg.what) {
            case CAL_FAIL_FAIL11:
                showDialog(CAL_FAIL11);
                break;
            case CAL_SUCCESS_OK:
                removeDialog(CAL_FAIL11);
                showDialog(CAL_SUCCESS);
                break;
            case CAL_FAIL_FAIL:
                calibsucc = false;
                removeDialog(CAL_FAIL11);
                showDialog(CAL_FAIL);
                break;

            }

        }
    };

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case CAL_FAIL11:
            AlertDialog.Builder builder2 = new AlertDialog.Builder(AccelerationTest.this);
            builder2.setMessage(R.string.acceler_cal).setCancelable(false).setPositiveButton(R.string.acceler_caling,
                    null);
            dialog = builder2.create();
            break;
        case CAL_FAIL:
            AlertDialog.Builder builder = new AlertDialog.Builder(AccelerationTest.this);
            builder.setMessage(R.string.acceler).setCancelable(false).setPositiveButton(R.string.acceler_fail, null);
            dialog = builder.create();
            break;
        case CAL_SUCCESS:
            AlertDialog.Builder builder1 = new AlertDialog.Builder(AccelerationTest.this);
            builder1.setMessage(R.string.acceler).setCancelable(false).setPositiveButton(R.string.acceler_ok, null);
            dialog = builder1.create();
            break;
        }
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
        getWindow().getDecorView().getSystemUiVisibility());        
        return dialog;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        StringBuffer sb = new StringBuffer();
        sb.append("x = " + event.values[SensorManager.DATA_X] + "\n");
        sb.append("y = " + event.values[SensorManager.DATA_Y] + "\n");
        sb.append("z = " + event.values[SensorManager.DATA_Z] + "\n");
        Log.e(TAG, "X=" + event.values[SensorManager.DATA_X]);
        Log.e(TAG, "Y=" + event.values[SensorManager.DATA_Y]);
        Log.e(TAG, "Z=" + event.values[SensorManager.DATA_Z]);
        mContentTv.setText(sb.toString());
        //Gionee <GN_BSP_MMI> <lifeilong> <20170308> modify for ID 80758 begin
        mValues = event.values;
        //Gionee <GN_BSP_MMI> <lifeilong> <20170308> modify for ID 80758 end
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

    private void showArrow(float x, float y, float z) {
        if (x > 9) {
            mArrowView.setImageResource(R.drawable.arrow_right);
            if (false == mIsLeftSuccess) {
                mSuccessNum++;
                mIsLeftSuccess = true;
            }
        } else if (x < -9) {
            mArrowView.setImageResource(R.drawable.arrow_left);
            if (false == mIsRightSuccess) {
                mSuccessNum++;
                mIsRightSuccess = true;
            }
        } else if (y > 9) {
            mArrowView.setImageResource(R.drawable.arrow_up);
            if (false == mIsBottomSuccess) {
                mSuccessNum++;
                mIsBottomSuccess = true;
            }
        } else if (y < -9) {
            mArrowView.setImageResource(R.drawable.arrow_down);
            if (false == mIsTopSuccess) {
                mSuccessNum++;
                mIsTopSuccess = true;
            }
        } else if (z > 9) {
            mArrowView.setImageResource(R.drawable.arrow);
            if (false == mIsFront) {
                mSuccessNum++;
                mIsFront = true;
            }
        } else if (z < -9) {
            mArrowView.setImageResource(R.drawable.arrow);
            if (false == mIsback) {
                mSuccessNum++;
                mIsback = true;
            }
        }

        if (6 == mSuccessNum && calibsucc) {
            mRightBtn.setEnabled(true);
            mSuccessNum++;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
