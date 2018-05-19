package com.gionee.laser;

import android.os.Bundle;
import com.gionee.util.DswLog;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.R;
import com.gionee.autommi.AutoMMI;
import android.view.View;
import com.gionee.autommi.TestResult;
import android.content.SharedPreferences;
import android.content.Context;
import java.util.Timer;
import java.util.TimerTask;


public class LaserTest extends BaseActivity {
    public static final String TAG = "LaserTest";
    private static final int EVENT_RESPONSE_CMD = 1;
    private TextView mTip = null;
    private TextView mTip2 = null;

    private int mCmdId = -1;
    private static final int LASER_PCB_ID = 0;
    private static final int LASER_OFFSET_ID = 1;
    private static final int LASER_CROSSTALK_ID = 2;
    private static final int LASER_DISTANCE_100_ID = 3;
    private static final int LASER_DISTANCE_400_ID = 4;

    private static final String RESULT_TITLE_LASER_PCB = "LaserPcb";
    private static final String RESULT_TITLE_LASER_OFFSET = "LaserOffset";
    private static final String RESULT_TITLE_LASER_CROSSTALK = "LaserCrosstalk";
    private static final String RESULT_TITLE_LASER_DISTANCE_100 = "LaserDistanse10";
    private static final String RESULT_TITLE_LASER_DISTANCE_400 = "LaserDistanse40";

    private boolean mIsTestSuccess = false;
    private int mTestValue = -1;
    private int mLaserOffsetCalDo = -1;
    private int mLaserCrosstalkCalDo = -1;
    
    //start calibrate offset,return 0 means fail,return 1 means success.
    public native int startLaserOffsetCalib();
    //get offset value.return -200 means no calibrate.
    public native int getLaserOffsetCalib();
    //is offset calibrate has been down. return 0 means fail,return 1 means success.
    public native int isDoLaserOffsetCalib();
    //start calibrate crosstalk,return 0 means fail,return 1 means success.
    public native int startLaserXTalkCalib();
    //get crosstalk value.
    public native int getLaserXTalkCalib();
    //is crosstalk calibrate has been down. return 0 means fail,return 1 means success.
    public native int isDoLaserXTalkCalib();
    //get distance,return 765 means nothing discoveried.
    public native int getLaserRange();

    //Gionee zhangke 20160222 add for CR01639016 start
    private int m765Times = 0;
    //Gionee zhangke 20160222 add for CR01639016 end
    
    static {
        System.loadLibrary("gnlaser_jni");
    }

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private int mSpec = 15;

    private int mTimes = 0;
    private static final int MAX_TIMES_100 = 5;
    private static final int MAX_TIMES_400 = 20;
    private int[] mTestValueArray;
    private Timer mTimer;
    private boolean mIsStoped = false;
    private String mTestValueString;
    private int mMaxTimes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip);

        mTip = (TextView)findViewById(R.id.tip);
        mTip2 = (TextView)findViewById(R.id.t2);
        mTip2.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        if(intent != null){
            mCmdId = intent.getIntExtra("cmd", -1);
        }
        if(mCmdId == LASER_PCB_ID){
            mTip.setText(getString(R.string.laser_pcb_testing));
        }else if(mCmdId == LASER_OFFSET_ID){
            mTip.setText(getString(R.string.laser_offset_testing));
        }else if(mCmdId == LASER_CROSSTALK_ID){
            mTip.setText(getString(R.string.laser_crosstalk_testing));
        }else if(mCmdId == LASER_DISTANCE_100_ID){
            mSpec = intent.getIntExtra("spec", 15);
            mMaxTimes = MAX_TIMES_100;
            mTestValueArray = new int[mMaxTimes];
            mTip.setText(getString(R.string.laser_distance_testing));
        }else if(mCmdId == LASER_DISTANCE_400_ID){
            mSpec = intent.getIntExtra("spec", 15);
            mMaxTimes = MAX_TIMES_400;
            mTestValueArray = new int[mMaxTimes];
            mTip.setText(getString(R.string.laser_distance_testing));
        }else{
            mTip.setText("Error cmd");
        }

        DswLog.i(TAG, "mCmdId="+mCmdId + ";mSpec="+mSpec);
        mSharedPreferences = getSharedPreferences("gn_autommi_test",
            Context.MODE_WORLD_WRITEABLE|Context.MODE_WORLD_READABLE);
        mEditor = mSharedPreferences.edit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        DswLog.i(TAG, "onResume");
        new Thread(new Runnable() {
            public void run() {
                execCmd(mCmdId);
            }
        }).start();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    private void execCmd(int  cmd){
        if(cmd == LASER_PCB_ID){
            mTestValue = getLaserRange();
            if(mTestValue > 0 && mTestValue != 765){
                mIsTestSuccess = true;
            }
            DswLog.i(TAG, "mTestValue = "+ mTestValue);
            mUiHandler.sendEmptyMessage(EVENT_RESPONSE_CMD);
        }else if(cmd == LASER_OFFSET_ID){
            int laserOffsetCalStart = startLaserOffsetCalib();
            int laserOffsetCalDo = isDoLaserOffsetCalib();
            
            if(1 == laserOffsetCalStart && 1 == laserOffsetCalDo){
                mTestValue = getLaserOffsetCalib();
                mIsTestSuccess = true;
            }
            DswLog.i(TAG, "laserOffsetCalStart = "+laserOffsetCalStart+";laserOffsetCalDo="+laserOffsetCalDo+";mTestValue="+mTestValue);
            mUiHandler.sendEmptyMessage(EVENT_RESPONSE_CMD);
        }else if(cmd == LASER_CROSSTALK_ID){
            int laserCrosstalkCalStart = startLaserXTalkCalib();
            int laserCrosstalkCalDo = isDoLaserXTalkCalib();
            
            if(1 == laserCrosstalkCalStart && 1 == laserCrosstalkCalDo){
                mTestValue = getLaserXTalkCalib();
                mIsTestSuccess = true;
            }
            DswLog.i(TAG, "laserCrosstalkCalStart = "+laserCrosstalkCalStart+";laserCrosstalkCalDo="+laserCrosstalkCalDo+";mTestValue="+mTestValue);
            mUiHandler.sendEmptyMessage(EVENT_RESPONSE_CMD);
        }else if(cmd == LASER_DISTANCE_100_ID){
            mLaserOffsetCalDo = isDoLaserOffsetCalib();
            mLaserCrosstalkCalDo = isDoLaserOffsetCalib();
            
            if(1 == mLaserOffsetCalDo && 1 == mLaserCrosstalkCalDo){
                mIsTestSuccess = true;
                getLaserTestValues(100);
            }else{
                mUiHandler.sendEmptyMessage(EVENT_RESPONSE_CMD);
            }

			DswLog.i(TAG, "mLaserOffsetCalDo = "+mLaserOffsetCalDo+";mLaserCrosstalkCalDo="+mLaserCrosstalkCalDo);
        }else if(cmd == LASER_DISTANCE_400_ID){
            mLaserOffsetCalDo = isDoLaserOffsetCalib();
            mLaserCrosstalkCalDo = isDoLaserOffsetCalib();
            
            if(1 == mLaserOffsetCalDo && 1 == mLaserCrosstalkCalDo){
                mIsTestSuccess = true;
                getLaserTestValues(400);
            }else{
                mUiHandler.sendEmptyMessage(EVENT_RESPONSE_CMD);
            }
			DswLog.i(TAG, "mLaserOffsetCalDo = "+mLaserOffsetCalDo+";mLaserCrosstalkCalDo="+mLaserCrosstalkCalDo);
        }

    }
	
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_RESPONSE_CMD: 
                showResult();
                break;
            }
        }
    };

    
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mIsStoped = true;
        finish();
    }

    private void showInfo(String info) {
        if (isFinishing()) return;
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setTitle("showInfo");
        infoDialog.setMessage(info);
        infoDialog.setIcon(android.R.drawable.ic_dialog_alert);
        infoDialog.show();
    }

    private void showResult(){ 
        String textTip = "";
        String textTip2 = "";
        String testTag = "";
        String testResult = "0";

        if(mCmdId == LASER_PCB_ID){
           textTip = getString(R.string.title_laser_pcb);
		   textTip2= getString(R.string.laser_distance);
		   testTag = RESULT_TITLE_LASER_PCB;
        }else if(mCmdId == LASER_OFFSET_ID){
           textTip = getString(R.string.title_laser_offset);
		   textTip2 = getString(R.string.laser_offset);
		   testTag = RESULT_TITLE_LASER_OFFSET;
        }else if(mCmdId == LASER_CROSSTALK_ID){
           textTip = getString(R.string.title_laser_crosstalk);
		   textTip2 = getString(R.string.laser_crosstalk);
		   testTag = RESULT_TITLE_LASER_CROSSTALK;
        }else if(mCmdId == LASER_DISTANCE_100_ID){
           textTip = getString(R.string.title_laser_distance);
		   testTag = RESULT_TITLE_LASER_DISTANCE_100;
        }else if(mCmdId == LASER_DISTANCE_400_ID){
           textTip = getString(R.string.title_laser_distance);
		   testTag = RESULT_TITLE_LASER_DISTANCE_400;
        }
        if(mIsTestSuccess){
            textTip += getString(R.string.success);
            testResult = "1";            
        }else{
            textTip += getString(R.string.fail);
            if(mCmdId == LASER_DISTANCE_100_ID || mCmdId == LASER_DISTANCE_400_ID){
                textTip += "(";
                if(mLaserOffsetCalDo != 1){
                    textTip += getString(R.string.laser_offset_no);
                }else if(mLaserCrosstalkCalDo != 1){
                    textTip += getString(R.string.laser_crosstalk_no);
                }else{
                    textTip += getString(R.string.laser_distance_error);
                }
                textTip += ")";

            }
            testResult = "0";
        }
        if(mCmdId != LASER_DISTANCE_100_ID && mCmdId != LASER_DISTANCE_400_ID){
            mTestValueString = mTestValue + "";
        }
        textTip2 += mTestValueString;
        
        mTip.setText(textTip);
        mTip2.setText(textTip2);
        ((AutoMMI) getApplication()).recordResult(testTag, mTestValueString, testResult);
        DswLog.i(TAG, "mCmdId="+mCmdId+";mTestValueString="+mTestValueString+";mIsTestSuccess="+mIsTestSuccess);
        if(mCmdId == LASER_DISTANCE_100_ID || mCmdId == LASER_DISTANCE_400_ID){
            TestResult tr = new TestResult();
            byte[] sn_buff = new byte[64];
			System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, 64);
            
            String tag = "F";
            String testDistance100 = mSharedPreferences.getString("100", "F");
            String testDistance400 = mSharedPreferences.getString("400", "F");
            if(testDistance100.equals("P") && testDistance400.equals("P")){
                tag = "P";
            }
            sn_buff = tr.getNewSN(TestResult.MMI_LASER_TAG, tag, sn_buff);

			tr.writeToProductInfo(sn_buff);
            DswLog.i(TAG, "testDistance100="+testDistance100+";testDistance400="+testDistance400+";snNumber="+new String(sn_buff));
        }
        
    }

    private void getLaserTestValues(final int targetValue) {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (mTimes < mMaxTimes) {
                            mTestValueArray[mTimes] = getLaserRange();
                            //Gionee zhangke 20160222 modify for CR01639016 start
                            if(mCmdId == LASER_DISTANCE_400_ID){
                                if (mTestValueArray[mTimes] == 765){
                                    if(++m765Times >=3){
                                        DswLog.i(TAG, "m765Times="+m765Times);
                                        mIsTestSuccess = false;
                                    }
                                }else{
                                    m765Times = 0;
                                    if (mTestValueArray[mTimes] < (targetValue * (100 - mSpec) / 100)
                                        || mTestValueArray[mTimes] > (targetValue * (100 + mSpec) / 100)) {
                                            mIsTestSuccess = false;
                                    }
                                }
                            }else{
                                if (mTestValueArray[mTimes] < (targetValue * (100 - mSpec) / 100)
                                    || mTestValueArray[mTimes] > (targetValue * (100 + mSpec) / 100)) {
                                        mIsTestSuccess = false;
                                }
                            }
                            //Gionee zhangke 20160222 modify for CR01639016 end

                            DswLog.i(TAG, "mTestValueArray[" + mTimes + "]=" + mTestValueArray[mTimes] + ";mIsTestSuccess="
                                + mIsTestSuccess);
                            if (++mTimes >= mMaxTimes) {
                                if (mIsTestSuccess) {
                                    mEditor.putString(targetValue + "", "P");
                                    mEditor.commit();
                                } else {
                                    mEditor.putString(targetValue + "", "F");
                                    mEditor.commit();
                                }
                                StringBuffer buffer = new StringBuffer();
                                for (int i = 0; i < mMaxTimes; i++) {
                                    buffer.append(mTestValueArray[i]);
                                    if (i != mMaxTimes - 1) {
                                        buffer.append("|");
                                    }
                                }
                                mTestValueString = buffer.toString();
                                if (!mIsStoped) {
                                    mUiHandler.sendEmptyMessage(EVENT_RESPONSE_CMD);
                                }
                                mTimer.cancel();
                                mTimer = null;
                            }
                        }
                    }
                });
            }
        }, 0, 100);
    }
}
