
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import gn.com.android.mmitest.item.SensorUserCal;
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
import android.content.Intent;

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
    private String resultRecord = "/storage/emulated/0/mtklog/sensor_log/";
    private int code = 2;
    private int testResultCount;
    private String testTime;
    private BufferedReader mBufferedReader;
    private BufferedWriter mBufferedWriter;
    private StringBuffer s ;
    private boolean accFlag = false;
    private Intent it;
    private StringBuffer accResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "start  AccelerationTest  test ");
        //TestUtils.checkToContinue(this);
        setContentView(R.layout.acceleration_test);
        TestUtils.setWindowFlags(this);
        accResult = new StringBuffer();
        it = this.getIntent();
        if(it != null){
            accFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"accFlag = " + accFlag);        
        //Gionee zhangke 20151215 modify for CR01609753 end
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        testTime = sdf.format(System.currentTimeMillis());
        createMmiResultFolder();
        String testData = new String();
        testData = "utc : " + testTime + "\n" + "   x      y     z   " + "\n";
        s = new StringBuffer();
        getAccResult(testData);        
        mArrowView = (ImageView) findViewById(R.id.arraw);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mTitleTv.setText(R.string.acceleration_note);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        try {
            mSensor = mSensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "\u6ca1\u6709\u52a0\u901f\u4eea\u4f20\u611f\u5668", Toast.LENGTH_LONG).show();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            TestUtils.wrongPress(TAG, this);
            Log.d(TAG, " mSensor Exception ");
        }
        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        if(accFlag){
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }        
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);
        mRightBtn.setVisibility(View.INVISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mRightBtn.setOnClickListener(AccelerationTest.this);
                mWrongBtn.setOnClickListener(AccelerationTest.this);
                mRestartBtn.setOnClickListener(AccelerationTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

    }

    //Gionee zhangke 20160518 modify for CR01702402 start
    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, " AccelerationTest  onStart registerListener");
    //Gionee zhangke 20160518 modify for CR01702402 end
        mIsAccRight = mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        if (false == mIsAccRight) {
            try {
                Thread.sleep(300);
                mIsAccRight = mSensorMgr.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_UI);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        Log.e(TAG, "gsensor calibrating... mIsAccRight="+mIsAccRight);

    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500);
                    mUiHandler.sendEmptyMessage(CAL_FAIL_FAIL11);
                    Context c = createPackageContext("com.mediatek.engineermode", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
                    Class clazz = c.getClassLoader().loadClass("com.mediatek.engineermode.sensor.EmSensor");
                    GnReflectionMethods ReflectionMethods = new GnReflectionMethods(clazz, "doGsensorCalibration", new Class[]{int.class}, new Object[]{code}); //包名 方法名 方法参数类型 参数
                    //Gionee zhangke 20160322 modify for CR01656207 start
                    try{
                        Thread.sleep(500);
                    }catch(InterruptedException e){
                    }
                    int calib_ret = (Integer)ReflectionMethods.getInvokeResult1();
                    /*calib_ret = SensorUserCal.performUserCal((byte) 0,
                    (byte) 0);*/
                    Log.e(TAG, "calib_ret Number = " + calib_ret + ";isPause="+isPause);
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

        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
        getWindow().getDecorView().getSystemUiVisibility());
        
        return dialog;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        s.setLength(0);
        accResult.setLength(0);
        StringBuffer sb = new StringBuffer();
        sb.append("x = " + event.values[SensorManager.DATA_X] + "\n");
        sb.append("y = " + event.values[SensorManager.DATA_Y] + "\n");
        sb.append("z = " + event.values[SensorManager.DATA_Z] + "\n");
        //Log.e(TAG, "X=" + event.values[SensorManager.DATA_X]);
        //Log.e(TAG, "Y=" + event.values[SensorManager.DATA_Y]);
        //Log.e(TAG, "Z=" + event.values[SensorManager.DATA_Z]);
        mContentTv.setText(sb.toString());
        s.append(""+event.values[SensorManager.DATA_X]+","+event.values[SensorManager.DATA_Y]+","
                        +event.values[SensorManager.DATA_Z]+","+event.timestamp+"\n");
        getAccResult(s.toString());
        accResult.append(""+event.values[SensorManager.DATA_X]+"|"+event.values[SensorManager.DATA_Y]+"|"+event.values[SensorManager.DATA_Z]);
        mValues = event.values;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    //Gionee zhangke 20160518 modify for CR01702402 start
    @Override
    public void onStop() {
        super.onStop();
        //Gionee zhangke 20160322 modify for CR01656207 start
        Log.i(TAG, "onStop");
        //Gionee zhangke 20160518 modify for CR01702402 end
        removeDialog(CAL_FAIL11);
        //Gionee zhangke 20160322 modify for CR01656207 end
        if (null != mSensorMgr && null != mSensor) {
            Log.e(TAG, " onStop --> unregisterListener" );
            mSensorMgr.unregisterListener(this);
        }
        isPause = true;
        //Gionee zhangke 20151124 add for CR01595957 start
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
        if(accFlag){
            this.finish();
            Log.d(TAG,"onStop as_record_finish_self");
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                if(accFlag){
                    TestUtils.asResult(TAG,""+accResult.toString(),"1");
                }                
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(accFlag){
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

    //Gionee zhangke 20151124 modify for CR01595957 start
    private void showArrow(float x, float y, float z) {
        if (x > 9f) {
            mArrowView.setImageResource(R.drawable.arrow_right);
            if (false == mIsLeftSuccess) {
                Log.i(TAG, " Left Success");
                mSuccessNum++;
                mIsLeftSuccess = true;
            }
        } else if (x < -9f) {
            mArrowView.setImageResource(R.drawable.arrow_left);
            if (false == mIsRightSuccess) {
                Log.i(TAG, " Right Success");
                mSuccessNum++;
                mIsRightSuccess = true;
            }
        } else if (y > 9f) {
            mArrowView.setImageResource(R.drawable.arrow_up);
            if (false == mIsBottomSuccess) {
                Log.i(TAG, " Bottom Success");
                mSuccessNum++;
                mIsBottomSuccess = true;
            }
        } else if (y < -9f) {
            mArrowView.setImageResource(R.drawable.arrow_down);
            if (false == mIsTopSuccess) {
                Log.i(TAG, "Top Success");
                mSuccessNum++;
                mIsTopSuccess = true;
            }
        } else if ( z > 9f) {
            mArrowView.setImageResource(R.drawable.arrow);
            if (false == mIsFront) {
                Log.i(TAG, "Front Success");
                mSuccessNum++;
                mIsFront = true;
            }
        }else if ( z < -9f) {
            mArrowView.setImageResource(R.drawable.arrow);
            if (false == mIsback) {
                Log.i(TAG, "Back Success");
                mSuccessNum++;
                mIsback = true;
            }
        }

        Log.i(TAG, "mSuccessNum="+mSuccessNum+";calibsucc="+calibsucc);
        if (6 <= mSuccessNum && calibsucc) {
            mRightBtn.setEnabled(true);
            mRightBtn.setVisibility(View.VISIBLE);
            //mSuccessNum++;
        }
    }
    //Gionee zhangke 20151124 modify for CR01595957 end

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
        File record = new File(resultRecord+testTime+ "_Sensor.TYPE_ACCELEROMETER" );
        if (!record.exists()) {
            try{
                record.createNewFile();
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
        if (record.exists()) {
            Log.e(TAG, resultRecord + testTime + "   _Sensor.TYPE_ACCELEROMETER" );
            try{
                Runtime.getRuntime().exec("chmod 666 " + record);
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void getAccResult(String str){
        try {
            mBufferedReader = new BufferedReader(new FileReader(new File(resultRecord+testTime+ "_Sensor.TYPE_ACCELEROMETER")));
            mBufferedWriter = new BufferedWriter(new FileWriter(new File(resultRecord+testTime+ "_Sensor.TYPE_ACCELEROMETER"),true));
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
