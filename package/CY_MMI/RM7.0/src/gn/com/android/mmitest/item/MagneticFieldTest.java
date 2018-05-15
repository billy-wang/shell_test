package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
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
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Handler;
import java.lang.NullPointerException;
import android.widget.Toast;
import android.app.StatusBarManager;
import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import android.os.SystemClock;
import android.content.Context;
import android.content.Intent;

public class MagneticFieldTest extends Activity implements SensorEventListener, OnClickListener {
    private TextView mTitleTv;
    private TextView mContentTv;
    private ImageView mImageView;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
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
    private StatusBarManager sbm;
    private String resultRecord = "/storage/emulated/0/mtklog/sensor_log/";
    private int testResultCount;
    private String testTime;
    private BufferedReader mBufferedReader;
    private BufferedWriter mBufferedWriter;
    private StringBuffer s ;
    private boolean magnFlag = false;
    private Intent it;
    private String magn;
    private StringBuffer magnResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TestUtils.checkToContinue(this);
        TestUtils.setWindowFlags(this);
        it = this.getIntent();
        if(it != null){
            magnFlag=  it.getBooleanExtra("as", false);
        }
        magnResult = new StringBuffer();
        Log.d(TAG,"magnFlag = " + magnFlag);         
        sbm = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.disable(StatusBarManager.DISABLE_RECENT);        
        Log.e(TAG, "onCreate ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        testTime = sdf.format(System.currentTimeMillis());
        createMmiResultFolder();
        String testData = new String();
        testData = "utc : " + testTime + "\n" + "   x      y     z   " + "\n";
        s = new StringBuffer();
        getAccResult(testData); 

        //Gionee zhangke 20160811 add for CR01744854 start
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170616> modify for ID 158577 begin
        try {
            mSensor = mSensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
            if (null != mSensorMgr){
                mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        } catch (Exception ex) {
            Log.e(TAG,"no such Sensor ! ");
            Toast.makeText(this, "\u6ca1\u6709\u5730\u78c1\u4f20\u611f\u5668\uff01", Toast.LENGTH_LONG).show();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            TestUtils.wrongPress(TAG, this);
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170616> modify for ID 158577 end
        //Gionee zhangke 20160811 add for CR01744854 end

        // Gionee xiaolin 20120522 modify for CR00601846 start   
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
            mRightBtn.setVisibility(View.INVISIBLE);
            if(magnFlag){
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

            mIsRomVersion = false;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG,ex.getMessage());
        }
    }

    //Gionee zhangke 20160518 modify for CR01702402 start
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart ");
        /*MagneticFieldTest.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        MagneticFieldTest.this.getWindow().getDecorView().setSystemUiVisibility(
        getWindow().getDecorView().getSystemUiVisibility());*/
    }
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop ");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy  --> mSensorMgr.unregisterListener(this) --> sbm.disable(StatusBarManager.DISABLE_NONE) ");
        sbm.disable(StatusBarManager.DISABLE_NONE);        
        if (null != mSensorMgr){
            mSensorMgr.unregisterListener(this);
        }
        if(magnFlag){
            this.finish();
            Log.d(TAG,"onStop as_record_finish_self");
        }
    }
    //Gionee zhangke 20160518 modify for CR01702402 end

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
                mRightBtn.setVisibility(View.VISIBLE);
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
        s.setLength(0);
        magnResult.setLength(0);
        mBuilder.append("x = " + event.values[SensorManager.DATA_X] + "\n");
        mBuilder.append("y = " + event.values[SensorManager.DATA_Y] + "\n");
        mBuilder.append("z = " + event.values[SensorManager.DATA_Z] + "\n");
        mBuilder.append("accuracy = " + event.accuracy + "\n");
        mContentTv.setText(mBuilder.toString());
        s.append(""+event.values[SensorManager.DATA_X]+","+event.values[SensorManager.DATA_Y]+","
                        +event.values[SensorManager.DATA_Z]+","+event.timestamp+"\n");
        magnResult.append(""+event.values[SensorManager.DATA_X]+"|"+event.values[SensorManager.DATA_Y]+"|"+event.values[SensorManager.DATA_Z]);        
        getAccResult(s.toString());

    }
    // Gionee xiaolin 20120522 modify for CR00601846 end

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.right_btn: {
                if(magnFlag){
                    TestUtils.asResult(TAG,""+magnResult.toString(),"1");
                }                
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(magnFlag){
                    TestUtils.asResult(TAG,""+magnResult.toString(),"0");
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

     //Gionee <GN_BSP_MMI> <lifeilong> <20170817> modify for ID 189673 begin
    private void createMmiResultFolder(){
        File accDataFolder = new File(resultRecord);
        if (!accDataFolder.exists()) {
            try {
                accDataFolder.mkdir(); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File record = new File(resultRecord+testTime+ "_Sensor.TYPE_MAGNETIC_FIELD" );
        if (!record.exists()) {
            try{
                record.createNewFile();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
        if (record.exists()) {
            Log.e(TAG, resultRecord + testTime + "   _Sensor.TYPE_MAGNETIC_FIELD" );
            try{
                Runtime.getRuntime().exec("chmod 666 " + record);
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void getAccResult(String str){
        try {
            mBufferedReader = new BufferedReader(new FileReader(new File(resultRecord+testTime+"_Sensor.TYPE_MAGNETIC_FIELD")));
            mBufferedWriter = new BufferedWriter(new FileWriter(new File(resultRecord+testTime+"_Sensor.TYPE_MAGNETIC_FIELD"),true));
            mBufferedWriter.write(str);
            mBufferedReader.close();
            mBufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                mBufferedReader.close();
                mBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170817> modify for ID 189673 end

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
