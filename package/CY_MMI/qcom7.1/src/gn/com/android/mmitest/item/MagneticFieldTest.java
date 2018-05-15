package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
//Gionee zhangke 20161114 add for ID16169 begin
import android.os.PowerManager;
import android.content.Context;
//Gionee zhangke 20161114 add for ID16169 end

public class MagneticFieldTest extends Activity implements SensorEventListener, OnClickListener {
    private TextView mTitleTv;
    private TextView mContentTv;
    private ImageView mImageView;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private boolean mIsAccuracyRight;
    private static String TAG = "MagneticFieldTest";
    SensorManager mSensorMgr;
    Sensor mSensor;
    StringBuilder mBuilder;
    Resources mRs;

	private boolean mIsRomVersion = false;
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20161114 add for ID16169 begin
    PowerManager.WakeLock mWakeLock;
    //Gionee zhangke 20161114 add for ID16169 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
		//Gionee <MMI><lifeilong><2016-10-14> modify for 8596  begin
		mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		mSensor = mSensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
		//Gionee <MMI><lifeilong><2016-10-14> modify for 8596  end
        //Gionee zhangke 20161114 add for ID16169 begin
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GN_MMI");	
        mWakeLock.acquire();  
        //Gionee zhangke 20161114 add for ID16169 end

        // Gionee xiaolin 20120522 modify for CR00601846 start
        try {
            int accuracy = 3;
            Intent intent = getPackageManager().getLaunchIntentForPackage("jlzn.com.android.compass");
            intent.putExtra("referenceValue", accuracy);
            startActivity(intent);
            MagneticFieldTest.this.setContentView(R.layout.common_textview);	

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
                    //Gionee zhangke 20160811 add for CR01744854 start
					mIsTimeOver = true;
					if(mIsPass){
						mRightBtn.setEnabled(true);
                        mWrongBtn.setEnabled(false);
					}else{
                        mRightBtn.setEnabled(false);
                        mWrongBtn.setEnabled(true);
                    }
                    //Gionee zhangke 20160811 add for CR01744854 end
					mRestartBtn.setEnabled(true);

                    mRightBtn.setOnClickListener(MagneticFieldTest.this);
                    mWrongBtn.setOnClickListener(MagneticFieldTest.this);
                    mRestartBtn.setOnClickListener(MagneticFieldTest.this);
                }
            }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
            //Gionee zhangke 20160428 modify for CR01687958 end
            mIsRomVersion = true;
        } catch (Exception ex) {
            setContentView(R.layout.acceleration_test);
            mTitleTv = (TextView) findViewById(R.id.test_title);
            mTitleTv.setText(R.string.magnetic_field_test);
            mContentTv = (TextView) findViewById(R.id.test_content);
            mImageView = (ImageView) findViewById(R.id.arraw);
            mImageView.setImageResource(R.drawable.calibration_8);
            mContentTv.setText(R.string.magnetic_field_test);
            mBuilder = new StringBuilder();
            mRs = this.getResources();

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
                    //Gionee zhangke 20160811 add for CR01744854 start
					mIsTimeOver = true;
					if(mIsPass){
						mRightBtn.setEnabled(true);
                        mWrongBtn.setEnabled(false);
					}else{
                        mRightBtn.setEnabled(false);
                        mWrongBtn.setEnabled(true);
                    }
                    //Gionee zhangke 20160811 add for CR01744854 end

					mRestartBtn.setEnabled(true);

                    mRightBtn.setOnClickListener(MagneticFieldTest.this);
                    mWrongBtn.setOnClickListener(MagneticFieldTest.this);
                    mRestartBtn.setOnClickListener(MagneticFieldTest.this);
                }
            }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
            //Gionee zhangke 20160428 modify for CR01687958 end

        }
    }

    protected void onStart() {
		Log.e(TAG, "onStart ");
        super.onStart();
        if (null != mSensorMgr)
            mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
	//Gionee <MMI><lifeilong><2016-10-14> modify for begin
	protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop ");
        if (null != mSensorMgr)
            mSensorMgr.unregisterListener(this);
    }
	//Gionee <MMI><lifeilong><2016-10-14> modify for end

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        Log.e(TAG, "event.accuracy =  " + event.accuracy);
        //Gionee zhangke 20160811 add for CR01744854 start
        if (3 == event.accuracy) {
            if(mIsTimeOver){
                mRightBtn.setEnabled(true);
                mWrongBtn.setEnabled(false);
            }
            mIsPass = true;
        }else{
            if(mIsTimeOver){
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(true);
            }
            mIsPass = false;
        }

        if(mIsRomVersion){
            return;
        }

        if(mIsPass){
            if(mTitleTv != null){
                mTitleTv.setText(mRs.getString(R.string.magnetic_field) + " --- " + mRs.getString(R.string.test_right));
            }
            if(mImageView != null){
                mImageView.setVisibility(View.INVISIBLE);
            }
            
        }else{
            if(mTitleTv != null){
                mTitleTv.setText(R.string.magnetic_field_test);
            }
            if(mImageView != null){
                mImageView.setVisibility(View.VISIBLE);
            }
        }

        if (mBuilder.length() > 1) {
            mBuilder.delete(0, mBuilder.length() - 1);
        }
        //Gionee zhangke 20160811 add for CR01744854 end
        mBuilder.append("x = " + event.values[SensorManager.DATA_X] + "\n");
        mBuilder.append("y = " + event.values[SensorManager.DATA_Y] + "\n");
        mBuilder.append("z = " + event.values[SensorManager.DATA_Z] + "\n");
        mContentTv.setText(mBuilder.toString());
    }
    // Gionee xiaolin 20120522 modify for CR00601846 end

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.right_btn: {
			//Gionee zhangke 20161114 add for ID16169 begin
			if(mWakeLock != null){
				mWakeLock.release();
			}
			//Gionee zhangke 20161114 add for ID16169 end

            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.rightPress(TAG, this);
            break;
        }

        case R.id.wrong_btn: {
			//Gionee zhangke 20161114 add for ID16169 begin
			if(mWakeLock != null){
				mWakeLock.release();
			}
			//Gionee zhangke 20161114 add for ID16169 end

            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.wrongPress(TAG, this);
            break;
        }

        case R.id.restart_btn: {
			//Gionee zhangke 20161114 add for ID16169 begin
			if(mWakeLock != null){
				mWakeLock.release();
			}
			//Gionee zhangke 20161114 add for ID16169 end

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
