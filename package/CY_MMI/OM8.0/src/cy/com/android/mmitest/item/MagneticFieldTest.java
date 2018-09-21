package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
//Gionee <GN_BSP_MMI> <chengq> <20170315> modify for ID 80014 begin
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.ComponentName;
//Gionee <GN_BSP_MMI> <chengq> <20170315> modify for ID 80014 end

public class MagneticFieldTest extends BaseActivity implements SensorEventListener, OnClickListener {
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
    //Gionee zhangke 20160811 add for CR01744854 start
    private boolean mIsRomVersion = false;
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160811 add for CR01744854 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开地磁测试 @" + Integer.toHexString(hashCode()));

        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        DswLog.e(TAG, "onCreate ");
        //Gionee zhangke 20160811 add for CR01744854 start

        //Gionee zhangke 20160811 add for CR01744854 end

        // Gionee xiaolin 20120522 modify for CR00601846 start   
        try {
            // Gionee huangjianqiang 20160517 modify ,请勿动，固定为2，驱动要求！！
            int accuracy = 2;
            //Gionee <GN_BSP_MMI> <chengq> <20170315> modify for ID 80014 begin
            IntentFilter filter = new IntentFilter("com.gionee.magneticfield.MMITEST");
            registerReceiver(mBroadcastReceiver, filter);
            //Gionee <GN_BSP_MMI> <chengq> <20170315> modify for ID 80014 end
            //Intent intent = getPackageManager().getLaunchIntentForPackage("com.cydroid.compass");
            Intent intent = new Intent();
            ComponentName name = new ComponentName("com.cydroid.compass"
                    ,"com.cydroid.compass.JLZN_CompassActivity");
            intent.putExtra("referenceValue", accuracy);
            intent.setComponent(name);
            //Gionee zhangke 20151125 add for CR01592841 start
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //Gionee zhangke 20151125 add for CR01592841 end

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
            handler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    //Gionee zhangke 20160811 add for CR01744854 start
                    mIsTimeOver = true;
                    if(mIsPass){
                        mRightBtn.setEnabled(true);
                        //Gionee tanbotao 20160912 add for CR01753184 start
                        mWrongBtn.setEnabled(true);
                        //Gionee tanbotao 20160912 add for CR01753184 end
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
            });
            //Gionee zhangke 20160428 modify for CR01687958 end
            mIsRomVersion = true;
        } catch (Exception ex) {
            mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
            mSensor = mSensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);

            setContentView(R.layout.acceleration_test);
            mTitleTv = (TextView) findViewById(R.id.test_title);
            mTitleTv.setText(R.string.magnetic_field_test);
            mContentTv = (TextView) findViewById(R.id.test_content);
            mImageView = (ImageView) findViewById(R.id.arraw);
            mImageView.setImageResource(R.drawable.calibration_8);
            mContentTv.setText(R.string.magnetic_field_test);
            mBuilder = new StringBuilder();
            mRs = this.getResources();//Gionee zhangke 20160428 modify for CR01687958 start
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
                        //Gionee tanbotao 20160912 add for CR01753184 start
                        mWrongBtn.setEnabled(true);
                        //Gionee tanbotao 20160912 add for CR01753184 end
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
//Gionee <GN_BSP_MMI> <chengq> <20170315> modify for ID 80014 begin
    //Gionee zhangke 20160518 modify for CR01702402 start
    //Gionee <GN_BSP_MMI> <chengq> <20170228> modify for ID 76831 begin
    @Override
    protected void onStart() {
        super.onStart();
        DswLog.e(TAG, "iscalibration onStart ");
        if (!mIsRomVersion && null != mSensorMgr)
            mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170228> modify for ID 76831 end
    @Override
    protected void onStop() {
        super.onStop();
        DswLog.e(TAG, "iscalibration onStop ");
        if (!mIsRomVersion && null != mSensorMgr)
            mSensorMgr.unregisterListener(this);
    }
    //Gionee zhangke 20160518 modify for CR01702402 end

//    protected void onResume() {
//        super.onResume();
//        DswLog.e(TAG, "onResume ");
//        if (null != mSensorMgr)
//            mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
//    }
    @Override
    protected void onDestroy() {
        DswLog.e(TAG, "iscalibration  onDestroy");
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出地磁感应 @" + Integer.toHexString(hashCode()));
    }
//Gionee <GN_BSP_MMI> <chengq> <20170315> modify for ID 80014 end

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        // TODO Auto-generated method stub
        DswLog.e(TAG, "event.accuracy =  " + event.accuracy);
        //Gionee zhangke 20160811 add for CR01744854 start
        if (2 <= event.accuracy) {
            if(mIsTimeOver){
                mRightBtn.setEnabled(true);
                //Gionee tanbotao 20160912 add for CR01753184 start
                mWrongBtn.setEnabled(true);
                //Gionee tanbotao 20160912 add for CR01753184 end
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
//Gionee <GN_BSP_MMI> <chengq> <20170315> modify for ID 80014 begin
    private BroadcastReceiver mBroadcastReceiver =new BroadcastReceiver (){
        @Override
        public void onReceive(Context context, Intent intent) {

            DswLog.e(TAG, "mBroadcastReceiver onReceive ");
            if (intent.getAction().equals("com.gionee.magneticfield.MMITEST")) {
                boolean calibration = intent.getBooleanExtra("calibration", false);
                DswLog.e(TAG, "mBroadcastReceiver intent.getAction()+ calibration ="+calibration);

                mRightBtn.setEnabled(calibration);
            }

        }
    };
//Gionee <GN_BSP_MMI> <chengq> <20170315> modify for ID 80014 end
}
