
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;
import cy.com.android.mmitest.item.EmSensor.EmSensor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class GyroscopeTest extends BaseActivity implements SensorEventListener, OnClickListener {
    private TextView mTitleTv;

    private TextView mContentTv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private boolean mIsAccuracyRight;
    //Gionee zhangke 20151023 modify for CR01572458 start
    private boolean mIsCalSuccess;
    //Gionee zhangke 20151023 modify for CR01572458 end
    private static String TAG = "GyroscopeTest";

    SensorManager mSensorMgr;

    Sensor mSensor;
    Resources mRs;
    StringBuilder mBuilder;

    private static final int EVENT_CALING = 0;
    private static final int EVENT_CHECK_RESULT = 1;
    private static final int EVENT_SUCCESS = 2;
    private static final int EVENT_ERROR = 3;
    private Dialog mDialog = null;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;

            switch (what) {
                case EVENT_CALING:
                    showCalibratingDialog();
                    //Gionee zhangke 20151023 modify for CR01572458 start
                    sendEmptyMessageDelayed(EVENT_CHECK_RESULT, 2000);
                    //Gionee zhangke 20151023 modify for CR01572458 end
                    break;

                case EVENT_CHECK_RESULT:
                    int ret = EmSensor.getInstance(GyroscopeTest.this).doGyroscopeCalibration();
                    DswLog.v(TAG, "doGyroscopeCalibration result: " + ret);
                    //Gionee zhangke 20151023 modify for CR01572458 start
                    if (ret == EmSensor.RET_SUCCESS) {
                        sendEmptyMessage(EVENT_SUCCESS);
                        mIsCalSuccess = true;
                    } else {
                        sendEmptyMessage(EVENT_ERROR);
                        mIsCalSuccess = false;
                    }
                    DswLog.i(TAG, "EVENT_CHECK_RESULT:mIsAccuracyRight="+mIsAccuracyRight+";mIsCalSuccess="+mIsCalSuccess);
                    if(mIsAccuracyRight && mIsCalSuccess){
                        mTitleTv.setText(mRs.getString(R.string.gyroscope) + " --- " + mRs.getString(R.string.test_right));
                        mRightBtn.setEnabled(true);
                    }else{
                        mTitleTv.setText(mRs.getString(R.string.gyroscope) + " --- " + mRs.getString(R.string.test_wrong));
                        mRightBtn.setEnabled(false);
                    }
                    //Gionee zhangke 20151023 modify for CR01572458 end
                    break;

                case EVENT_SUCCESS:
                    showSuccessDialog();
                    break;

                case EVENT_ERROR:
                    showErrorDialog();
                    break;
                default:
                    break;
            }
        }
    };

    private void showCalibratingDialog() {
        removeDialog();
        // modify for CR01557430
        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    GyroscopeTest.this);
            builder.setMessage(R.string.acceler_cal);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.acceler_caling, null);
            mDialog = builder.create();
            mDialog.show();
        }
    }

    private void showSuccessDialog() {
        removeDialog();
        // modify for CR01557430
        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    GyroscopeTest.this);
            builder.setMessage(R.string.acceler_ok);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", null);
            mDialog = builder.create();
            mDialog.show();
        }
    }

    private void showErrorDialog() {
        removeDialog();
        // modify for CR01557430
        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    GyroscopeTest.this);
            builder.setMessage(R.string.acceler_fail);
            builder.setCancelable(false);
            builder.setPositiveButton("OK", null);
            mDialog = builder.create();
            mDialog.show();
        }
    }

    private void removeDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开陀螺仪 @" + Integer.toHexString(hashCode()));

        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.common_textview);

        mTitleTv = (TextView) findViewById(R.id.test_title);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        // Gionee xiaolin 20120531 modify for CR00613896 start
        if (null != mSensorMgr) {
            List<Sensor> sl = mSensorMgr.getSensorList(Sensor.TYPE_GYROSCOPE);
            if (null != sl) {
                if (sl.size() != 0) {
                    mSensor = sl.get(0);
                }
            }
        }
        // Gionee xiaolin 20120531 modify for CR00613896 end
        mBuilder = new StringBuilder();
        mRs = this.getResources();

        // Gionee xiaolin 20120531 modify for CR00613896 start
        if (null != mSensorMgr && null != mSensor) {
            boolean isTrue = mSensorMgr.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_UI);
        }
        // Gionee xiaolin 20120531 modify for CR00613896 end
        //Gionee zhangke 20160119 add for CR01625218 start
        mTitleTv.setText(mRs.getString(R.string.gyroscope));
        //Gionee zhangke 20160119 add for CR01625218 end

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(GyroscopeTest.this);
                mWrongBtn.setOnClickListener(GyroscopeTest.this);
                mRestartBtn.setOnClickListener(GyroscopeTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出陀螺仪 @" + Integer.toHexString(hashCode()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(EVENT_CALING);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        DswLog.e(TAG, "event.accuracy = " + event.accuracy);
        if (mBuilder.length() > 1) {
            mBuilder.delete(0, mBuilder.length() - 1);
        }
        mBuilder.append("x = " + event.values[SensorManager.DATA_X] + "\n");
        mBuilder.append("y = " + event.values[SensorManager.DATA_Y] + "\n");
        mBuilder.append("z = " + event.values[SensorManager.DATA_Z] + "\n");
        mContentTv.setText(mBuilder.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        //Gionee zhangke 20160519 modify for CR01703205 start
        DswLog.i(TAG, "onAccuracyChanged:accuracy="+accuracy);
        if (2 <= accuracy) {
            mIsAccuracyRight = true;
        } else {
            mIsAccuracyRight = false;
        }
        DswLog.i(TAG, "mIsAccuracyRight="+mIsAccuracyRight+";mIsCalSuccess="+mIsCalSuccess);
        if(mIsAccuracyRight && mIsCalSuccess){
            mTitleTv.setText(mRs.getString(R.string.gyroscope) + " --- " + mRs.getString(R.string.test_right));
            mRightBtn.setEnabled(true);
        }else{
            mTitleTv.setText(mRs.getString(R.string.gyroscope) + " --- " + mRs.getString(R.string.test_wrong));
            mRightBtn.setEnabled(false);
        }
        //Gionee zhangke 20160519 modify for CR01703205 end

    }

    @Override
    public void onPause() {
        super.onPause();
        // Gionee xiaolin 20120531 modify for CR00613896 start
        if (null != mSensorMgr && null != mSensor) {
            mSensorMgr.unregisterListener(this);
        }
        // Gionee xiaolin 20120531 modify for CR00613896 end
        removeDialog();
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
