
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.util.List;
import java.lang.Object;

import android.app.Activity;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class GyroscopeTest extends Activity implements SensorEventListener, OnClickListener {
    private TextView mTitleTv;

    private TextView mContentTv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private boolean mIsAccuracyRight;

    private static String TAG = "GyroscopeTest";

    SensorManager mSensorMgr;

    Sensor mSensor;
    Resources mRs;
    StringBuilder mBuilder;

    private static final int EVENT_CALING = 0;
    private static final int EVENT_CHECK_RESULT = 1;
    private static final int EVENT_SUCCESS = 2;
    private static final int EVENT_ERROR = 3;
	private static final int SHOW_RESULT = 4;
    private Dialog mDialog = null;
    private boolean mIsCalSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);

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
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mRs = this.getResources();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Gionee xiaolin 20120531 modify for CR00613896 start
        if (null != mSensorMgr && null != mSensor) {
            boolean isTrue = mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        // Gionee xiaolin 20120531 modify for CR00613896 end
        mHandler.sendEmptyMessage(EVENT_CALING); 
		
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;

            switch (what) {
                case EVENT_CALING:
                    showCalibratingDialog(); 
                    sendEmptyMessageDelayed(EVENT_CHECK_RESULT, 1000); 
                    break;

                case EVENT_CHECK_RESULT:
		//Gionee <MMI><lifeilong><20161020> modify for 11856 bedin
		     new Thread(){
			public void run(){
				int ret = SensorUserCal.performUserCal((byte) 10,(byte) 0);
                 		Log.v(TAG, "doGyroscopeCalibration result: " + ret);
				Message msg = Message.obtain();
				msg.obj = ret;
				msg.what = SHOW_RESULT;
				sendMessage(msg);
		             	}
			}.start();
                //Gionee <MMI><lifeilong><20161020> modify for 11856 end                  
                    break;

                case EVENT_SUCCESS:
                    showSuccessDialog();
                    break;

                case EVENT_ERROR:
                    showErrorDialog();
                    break;
		//Gionee <MMI><lifeilong><20161020> modify for 11856 bedin
		case SHOW_RESULT:
		    int ret = (int)msg.obj;
		    if (ret == 0) {
                        sendEmptyMessage(EVENT_SUCCESS);
                        mIsCalSuccess = true;
                    } else {
                        sendEmptyMessage(EVENT_ERROR);
                        mIsCalSuccess = false;
                    }
                    Log.i(TAG, "EVENT_CHECK_RESULT:mIsAccuracyRight="+mIsAccuracyRight+";mIsCalSuccess="+mIsCalSuccess);
                    if(mIsAccuracyRight && mIsCalSuccess){
                        mTitleTv.setText(mRs.getString(R.string.gyroscope) + " --- " + mRs.getString(R.string.test_right));
                        mRightBtn.setEnabled(true);
                    }else{
                        mTitleTv.setText(mRs.getString(R.string.gyroscope) + " --- " + mRs.getString(R.string.test_wrong));
                        mRightBtn.setEnabled(false);
                    } 
		    break;
		//Gionee <MMI><lifeilong><20161020> modify for 11856 bedin
                default:
                    break;
            }
        }
    };

    private void showCalibratingDialog() {
        removeDialog(); 
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
	//Gionee <GN_MMI><lifeilong><20161024> modify for 12608 begin
	if(GyroscopeTest.this.isFinishing()){
		Log.e(TAG,"== Activity is finished. dialog will be not release ==");
		return;
	}
	//Gionee <GN_MMI><lifeilong><20161024> modify for 12608 end
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        Log.i(TAG, "onSensorChanged:event.accuracy="+event.accuracy);
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
        Log.i(TAG, "onAccuracyChanged:accuracy="+accuracy);
        if (2 <= accuracy) {
            mIsAccuracyRight = true;
        } else {
            mIsAccuracyRight = false;
        }
        Log.i(TAG, "mIsAccuracyRight="+mIsAccuracyRight+";mIsCalSuccess="+mIsCalSuccess);
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
