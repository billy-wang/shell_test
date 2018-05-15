
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import gn.com.android.mmitest.item.EmSensor.EmSensor;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import android.os.SystemClock;
import android.content.Context;
import android.content.Intent;

public class GyroscopeTest extends Activity implements SensorEventListener, OnClickListener {
    private TextView mTitleTv;

    private TextView mContentTv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private boolean mIsAccuracyRight;
    //Gionee zhangke 20151023 modify for CR01572458 start
    private boolean mIsCalSuccess;
    //Gionee zhangke 20151023 modify for CR01572458 end
    private static String TAG = "GyroscopeTest";

    SensorManager mSensorMgr;
    private String resultRecord = "/storage/emulated/0/mtklog/sensor_log/";
    private int testResultCount;
    private String testTime;
    private BufferedReader mBufferedReader;
    private BufferedWriter mBufferedWriter;
    private StringBuffer sb ;
    private FileOutputStream fos;
    private boolean gyroFlag = false;
    private Intent it;
    private String gyro;
    private StringBuffer accResult;
    Sensor mSensor;
    Resources mRs;
    StringBuilder mBuilder;

    private static final int EVENT_CALING = 0;
    private static final int EVENT_CHECK_RESULT = 1;
    private static final int EVENT_SUCCESS = 2;
    private static final int EVENT_ERROR = 3;
    private Dialog mDialog = null;
    private int accuracy;
    private boolean calFlag;
    private File record;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int what = msg.what;

            switch (what) {
                case EVENT_CALING:
                    showCalibratingDialog();
                    //Gionee zhangke 20151023 modify for CR01572458 start
                    sendEmptyMessageDelayed(EVENT_CHECK_RESULT, 1000);
                    //Gionee zhangke 20151023 modify for CR01572458 end
                    break;

                case EVENT_CHECK_RESULT:
                    int ret = EmSensor.getInstance(GyroscopeTest.this).doGyroscopeCalibration();
                    Log.v(TAG, "doGyroscopeCalibration result: " + ret);
                    //Gionee zhangke 20151023 modify for CR01572458 start
                    if (ret == EmSensor.RET_SUCCESS) {
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
                        mRightBtn.setVisibility(View.VISIBLE);
                    }else{
                        mTitleTv.setText(mRs.getString(R.string.gyroscope) + " --- " + mRs.getString(R.string.test_wrong));
                        mRightBtn.setEnabled(false);
                        //sendEmptyMessageDelayed(EVENT_CHECK_RESULT, 1000);
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
            mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            mDialog.getWindow().getDecorView().setSystemUiVisibility(
            getWindow().getDecorView().getSystemUiVisibility());
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
            mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            mDialog.getWindow().getDecorView().setSystemUiVisibility(
            getWindow().getDecorView().getSystemUiVisibility());
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
            mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            mDialog.getWindow().getDecorView().setSystemUiVisibility(
            getWindow().getDecorView().getSystemUiVisibility());
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
        //TestUtils.checkToContinue(this);
        Log.e(TAG,"  onCreate  ");
        TestUtils.setWindowFlags(this);
        setContentView(R.layout.common_textview);
        it = this.getIntent();
        if(it != null){
            gyroFlag=  it.getBooleanExtra("as", false);
        }
        accResult = new StringBuffer();
        Log.d(TAG,"gyroFlag = " + gyroFlag);        
        mTitleTv = (TextView) findViewById(R.id.test_title);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
        testTime = sdf.format(System.currentTimeMillis());
        createMmiResultFolder();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170822> modify for ID 190251 begin
        try {
            fos = new FileOutputStream(record);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170822> modify for ID 190251 end
        String testData = new String();
        sb = new StringBuffer();
        testData = "utc : " + testTime + "\n" + "   x   y   z   " + "\n";
        getGyroResult(testData);
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
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170324> modify for ID 87260 begin
                    SensorManager.SENSOR_DELAY_FASTEST);
                     //Gionee <GN_BSP_MMI> <lifeilong> <20170324> modify for ID 87260 end
        }
        // Gionee xiaolin 20120531 modify for CR00613896 end
        //Gionee zhangke 20160119 add for CR01625218 start
        mTitleTv.setText(mRs.getString(R.string.gyroscope));
        //Gionee zhangke 20160119 add for CR01625218 end

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setVisibility(View.INVISIBLE);
        if(gyroFlag){
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }        
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
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"  onResume  ");
        mHandler.sendEmptyMessage(EVENT_CALING);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG,"  onStop  ");
        if(gyroFlag){
            this.finish();
            Log.d(TAG,"onStop as_record_finish_self");
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        //Log.i(TAG, "onSensorChanged:event.accuracy="+event.accuracy);
        if (mBuilder.length() > 1) {
            mBuilder.delete(0, mBuilder.length() - 1);
        }
        sb.setLength(0);
        accResult.setLength(0);
        mBuilder.append("x = " + event.values[SensorManager.DATA_X] + "\n");
        mBuilder.append("y = " + event.values[SensorManager.DATA_Y] + "\n");
        mBuilder.append("z = " + event.values[SensorManager.DATA_Z] + "\n");
        mBuilder.append("accuracy = " + event.accuracy + "\n");
        mContentTv.setText(mBuilder.toString());
        accuracy = event.accuracy;
        sb.append(""+event.values[SensorManager.DATA_X]+","+event.values[SensorManager.DATA_Y]+","
                            +event.values[SensorManager.DATA_Z]+"," + event.timestamp+"\n");
        accResult.append(""+event.values[SensorManager.DATA_X]+"|"+event.values[SensorManager.DATA_Y]+"|"+event.values[SensorManager.DATA_Z]);        
        getGyroResult(sb.toString());
        //Log.e(TAG,"  onSensorChanged  --- accuracy --> " + accuracy);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        //Gionee zhangke 20160519 modify for CR01703205 start
        //Log.i(TAG, "onAccuracyChanged:accuracy="+accuracy);
        if (2 <= accuracy) {
            mIsAccuracyRight = true;
        } else {
            mIsAccuracyRight = false;
        }
        //Log.i(TAG, "mIsAccuracyRight="+mIsAccuracyRight+";mIsCalSuccess="+mIsCalSuccess);
        if(mIsAccuracyRight && mIsCalSuccess){
            mTitleTv.setText(mRs.getString(R.string.gyroscope) + " --- " + mRs.getString(R.string.test_right));
            mRightBtn.setEnabled(true);
            mRightBtn.setVisibility(View.VISIBLE);
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
            Log.e(TAG, " onPause --> unregisterListener" );
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
                if(gyroFlag){
                    TestUtils.asResult(TAG,""+accResult.toString(),"1");
                }                
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(gyroFlag){
                    TestUtils.asResult(TAG,""+accResult.toString(),"0");
                }                
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

    //Gionee <GN_BSP_MMI> <lifeilong> <20170822> modify for ID 190251 begin
    private void getGyroResult(String str){
        try {
            //FileOutputStream fos = openFileOutput(resultRecord+testTime + "_Sensor.TYPE_GYROSCOPE",Context.MODE_APPEND);
            fos.write(str.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170822> modify for ID 190251 end
    
    private void createMmiResultFolder(){
        File accDataFolder = new File(resultRecord);
        if (!accDataFolder.exists()) {
            try {
                accDataFolder.mkdir(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        record = new File(resultRecord+testTime+"   _Sensor.TYPE_GYROSCOPE");
        if (!record.exists()) {
            try{
                record.createNewFile();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
        if (record.exists()) {
            Log.e(TAG, "/storage/emulated/0/mtklog/" + testTime +"   _Sensor.TYPE_GYROSCOPE");
            try{
                Runtime.getRuntime().exec("chmod 666 " + record);
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
