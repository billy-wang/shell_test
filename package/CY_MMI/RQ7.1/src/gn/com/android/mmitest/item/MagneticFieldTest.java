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
import java.lang.NullPointerException;
import android.widget.Toast;

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
        try {
            mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            mSensor = mSensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
            if (null != mSensorMgr){
                mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        } catch (Exception ex) {
            Log.e(TAG,"no such Sensor ! ");
            t.start();
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170921> modify for ID 219930 begin
        try {
            setContentView(R.layout.acceleration_test);
            mTitleTv = (TextView) findViewById(R.id.test_title);
            mTitleTv.setText(R.string.magnetic_field_test);
            mContentTv = (TextView) findViewById(R.id.test_content);
            mImageView = (ImageView) findViewById(R.id.arraw);
            mImageView.setImageResource(R.drawable.calibration_8);
            mBuilder = new StringBuilder();
            mContentTv.setText(mBuilder.toString());
            mRs = this.getResources();
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
                    mIsTimeOver = true;
                    if(mIsPass){
                        mRightBtn.setEnabled(true);
                        mWrongBtn.setEnabled(false);
                    }else{
                        mRightBtn.setEnabled(false);
                        mWrongBtn.setEnabled(true);
                    }
                    mRestartBtn.setEnabled(true);
                    mRightBtn.setOnClickListener(MagneticFieldTest.this);
                    mWrongBtn.setOnClickListener(MagneticFieldTest.this);
                    mRestartBtn.setOnClickListener(MagneticFieldTest.this);
                }
            }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170921> modify for ID 219930 end
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170705> modify for ID 163517 begin
    Thread t = new Thread(){
        @Override
        public void run() {
            super.run();
            try {
                Log.d(TAG,"thread begin ");
                Thread.sleep(1000);
                Log.d(TAG,"thread begin 1000");
                mSensorMgr = (SensorManager) MagneticFieldTest.this.getSystemService(SENSOR_SERVICE);
                mSensor = mSensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
                if (null != mSensorMgr){
                    mSensorMgr.registerListener(MagneticFieldTest.this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
            } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG,"no MagneticFieldTest Sensor ! ");
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170706> modify for ID 166104 begin
                    MagneticFieldTest.this.runOnUiThread(new Runnable(){
                        @Override
                        public void run(){
                            Toast.makeText(MagneticFieldTest.this, "\u6ca1\u6709\u5730\u78c1\u4f20\u611f\u5668\uff01", Toast.LENGTH_LONG).show();
                        }
                    });
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170706> modify for ID 166104 end
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170705> modify for ID 163517 end

    protected void onStart() {
        Log.e(TAG, "onStart ");
        super.onStart();
    }

    //Gionee <GN_BSP_MMI> <lifeilong> <20170713> modify for ID 168709 begin
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop ");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy  --> mSensorMgr.unregisterListener(this);");
        if (null != mSensorMgr)
            mSensorMgr.unregisterListener(this);
    }
     //Gionee <GN_BSP_MMI> <lifeilong> <20170713> modify for ID 168709 end

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
        mBuilder.append("accuracy = " + event.accuracy + "\n");
        mContentTv.setText(mBuilder.toString());
    }
    // Gionee xiaolin 20120522 modify for CR00601846 end

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
