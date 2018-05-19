package com.gionee.autommi;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.gionee.autommi.EmSensor.EmSensor;
import android.os.Handler;
import android.os.Message;

public class SensorsCalTest extends BaseActivity implements SensorEventListener {
    public static String TAG = "SensorsCalTest";
    private SensorManager mSensorMgr;
    private boolean mIsPSensorSuccess;
    private boolean mIsMSensorSuccess;
    private boolean mIsGSensorSuccess;
    private boolean mIsGyroscopeSuccess;
    private TextView tip;
    private EmSensor mEmSensor = null;
    private Sensor mMSensor;
    private static final int MSG_SHOW_RESULT = 0;
    private static final String TEST_FAIL = "0";
    private static final String TEST_SUCCESS = "1";
    private static final String TEST_NOT_SUPPORT = "2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tip);
        tip = (TextView) this.findViewById(R.id.tip);
        mSensorMgr = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        ((AutoMMI) getApplication()).recordResult(TAG, "", "0");
        mEmSensor = EmSensor.getInstance(this);
        if(FeatureOption.GN_RW_GN_MMI_SENSOR_COMPASS_SUPPORT){
            mMSensor = mSensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if(FeatureOption.GN_RW_GN_MMI_SENSOR_COMPASS_SUPPORT){
            //Gionee <GN_BSP_MMI> <chengq> <20170303> modify for ID 68621 begin
            mSensorMgr.registerListener(this, mMSensor, SensorManager.SENSOR_DELAY_GAME);
            //Gionee <GN_BSP_MMI> <chengq> <20170303> modify for ID 68621 end
        }
        new Thread(new Runnable() {
            public void run() {
                if(FeatureOption.GN_RW_GN_MMI_SENSOR_LIGHT_SUPPORT){
                    doPSensorCalibration();
                }
                if(FeatureOption.GN_RW_GN_MMI_SENSOR_ACC_SUPPORT){
                    doGSensorCalibration();
                }
                if(FeatureOption.GN_RW_GN_MMI_SENSOR_GYRO_SUPPORT){
                    doGyroscopeCalibration();
                }
                mHandler.sendEmptyMessage(MSG_SHOW_RESULT);
            }
        }).start();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SHOW_RESULT:
                String pSensorResult;
                String gSensorResult;
                String gyroResult;
                String mSensorResult;
				
                StringBuilder sb = new StringBuilder();
                sb.append(getString(R.string.sensor_p_cal_result));
                if (FeatureOption.GN_RW_GN_MMI_SENSOR_LIGHT_SUPPORT) {
                    if (mIsPSensorSuccess) {
                        sb.append(getString(R.string.success));
                        pSensorResult = TEST_SUCCESS;
                    } else {
                        sb.append(getString(R.string.fail));
                        pSensorResult = TEST_FAIL;
                    }
                } else {
                    sb.append(getString(R.string.not_support));
                    pSensorResult = TEST_NOT_SUPPORT;
                }

                sb.append("\n"+getString(R.string.sensor_g_cal_result));
                if (FeatureOption.GN_RW_GN_MMI_SENSOR_ACC_SUPPORT) {
                    if (mIsGSensorSuccess) {
                        sb.append(getString(R.string.success));
                        gSensorResult = TEST_SUCCESS;
                    } else {
                        sb.append(getString(R.string.fail));
                        gSensorResult = TEST_FAIL;
                    }
                } else {
                    sb.append(getString(R.string.not_support));
                    gSensorResult = TEST_NOT_SUPPORT;
                }

                sb.append("\n"+getString(R.string.sensor_gyro_cal_result));
                if (FeatureOption.GN_RW_GN_MMI_SENSOR_GYRO_SUPPORT) {
                    if (mIsGyroscopeSuccess) {
                        sb.append(getString(R.string.success));
                        gyroResult = TEST_SUCCESS;
                    } else {
                        sb.append(getString(R.string.fail));
                        gyroResult = TEST_FAIL;
                    }
                } else {
                    sb.append(getString(R.string.not_support));
                    gyroResult = TEST_NOT_SUPPORT;
                }

                sb.append("\n"+getString(R.string.sensor_m_cal_result));
                if (FeatureOption.GN_RW_GN_MMI_SENSOR_COMPASS_SUPPORT) {
                    if (mIsMSensorSuccess) {
                        sb.append(getString(R.string.success));
                        mSensorResult = TEST_SUCCESS;
                    } else {
                        sb.append(getString(R.string.fail));
                        mSensorResult = TEST_FAIL;
                    }
                } else {
                    sb.append(getString(R.string.not_support));
                    mSensorResult = TEST_NOT_SUPPORT;
                }
                tip.setText(sb.toString());
                String preTest = pSensorResult + "|" + gSensorResult + "|" + gyroResult;
                String content = "";
                if(preTest.contains(TEST_FAIL)){
                    content = TEST_FAIL + "|" + preTest;
                }else{
                    content = TEST_SUCCESS + "|" + preTest;
                }
                content += "|" + mSensorResult;
                Log.i(TAG, "MSG_SHOW_RESULT:content="+content);
                if(content.contains(TEST_FAIL)){
                    ((AutoMMI) getApplication()).recordResult(TAG, content, TEST_FAIL);
                }else{
                    ((AutoMMI) getApplication()).recordResult(TAG, content, TEST_SUCCESS);
                }
                break;
            }
        }
    };


    private void doPSensorCalibration() {
        int result = mEmSensor.doPsensorCalibration();
        if (result == 0) {
            mIsPSensorSuccess = false;
        } else {
            mIsPSensorSuccess = true;
        }
        Log.i(TAG, "doPSensorCalibration result=" + mIsPSensorSuccess);
    }

    private void doGSensorCalibration() {
        int result = mEmSensor.doGSensorCalibration();
        if (result == 1) {
            mIsGSensorSuccess = true;
        } else {
            mIsGSensorSuccess = false;
        }

        Log.i(TAG, "doGSensorCalibration result=" + mIsGSensorSuccess);
    }

    private void doGyroscopeCalibration() {
        int result = mEmSensor.doGyroscopeCalibration();

        if (result == 1) {
            mIsGyroscopeSuccess = true;
        } else {
            mIsGyroscopeSuccess = false;
        }
        Log.i(TAG, "doGyroscopeCalibration result=" + mIsGyroscopeSuccess);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if(FeatureOption.GN_RW_GN_MMI_SENSOR_COMPASS_SUPPORT){
            mSensorMgr.unregisterListener(this);
        }
        this.finish();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if(mIsMSensorSuccess){
            return;
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170303> modify for ID 68621 begin
        if (2 <= event.accuracy) {
        //Gionee <GN_BSP_MMI> <chengq> <20170303> modify for ID 68621 end
            mIsMSensorSuccess = true;
            mHandler.sendEmptyMessage(MSG_SHOW_RESULT);
        } else {
            mIsMSensorSuccess = false;
        }
	    Log.i(TAG, "mIsMSensorSuccess="+mIsMSensorSuccess+";mIsPSensorSuccess="+mIsPSensorSuccess+";mIsGSensorSuccess="+mIsGSensorSuccess
			+";mIsGyroscopeSuccess="+mIsGyroscopeSuccess);

        if (mIsMSensorSuccess && mIsPSensorSuccess && mIsGSensorSuccess && mIsGyroscopeSuccess) {
            ((AutoMMI) getApplication()).recordResult(TAG, "", "1");
        }
    }

}
