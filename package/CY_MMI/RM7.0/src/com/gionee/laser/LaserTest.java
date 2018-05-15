package com.gionee.laser;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import gn.com.android.mmitest.R;
import android.app.Activity;
import android.view.View.OnClickListener;
import gn.com.android.mmitest.TestUtils;
import android.widget.Button;
import android.view.WindowManager;
import android.content.SharedPreferences;
import android.view.KeyEvent;

public class LaserTest extends Activity implements OnClickListener {
    public static final String TAG = "LaserTest";
    private static final int EVENT_RESPONSE_CMD = 1;
    private TextView mTitle = null;
    private TextView mContent = null;

    private int mCmdId = LASER_DISTANCE_ID;
    private static final int LASER_PCB_ID = 0;
    private static final int LASER_OFFSET_ID = 1;
    private static final int LASER_CROSSTALK_ID = 2;
    private static final int LASER_DISTANCE_ID = 3;

    private boolean mIsTest400Success = false;
    private boolean mIsTest100Success = false;
    private boolean mIsTestSuccess = false;
    private int mLaserOffsetCalDo = -1;
    private int mLaserCrosstalkCalDo = -1;
    private static final int DELAY_TIME = 5000;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private Button mOffsetBtn;
    private Button mCrosstalkBtn;
    private TextView mOffsetText;
    private TextView mCrosstalkText;
    private boolean mIsOffsetCaled = false;
    private boolean mIsCrosstalkCaled = false;
    private boolean mIsThreadRun = true;
    private boolean mOffsetBtnPressed = false;
    private boolean mCrosstalkBtnPressed = false;
    private static final int MESSAGE_SHOW_LASER_DISTANCE = 0;
    private static final int MESSAGE_REFRESH_STATUS = 1;
    private int mOffsetValue = -100;
    private int mCrosstalkValue = -100;
    private int mDistance = -100;
	
    // start calibrate offset,return 0 means fail,return 1 means success.
    public native int startLaserOffsetCalib();

    // get offset value.return -200 means no calibrate.
    public native int getLaserOffsetCalib();

    // is offset calibrate has been down. return 0 means fail,return 1 means
    // success.
    public native int isDoLaserOffsetCalib();

    // start calibrate crosstalk,return 0 means fail,return 1 means success.
    public native int startLaserXTalkCalib();

    // get crosstalk value.
    public native int getLaserXTalkCalib();

    // is crosstalk calibrate has been down. return 0 means fail,return 1 means
    // success.
    public native int isDoLaserXTalkCalib();

    // get distance,return 765 means nothing discoveried.
    public native int getLaserRange();

    static {
        System.loadLibrary("gnlaser_jni");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        TestUtils.checkToContinue(this);
        //Gionee zhangke 20151215 modify for CR01609753 start
        /*
        // Gionee xiaolin 20120924 add for CR00693542 end
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        View view = getWindow().getDecorView();
        int visFlags = View.STATUS_BAR_DISABLE_BACK | View.STATUS_BAR_DISABLE_HOME | View.STATUS_BAR_DISABLE_RECENT
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
        view.setSystemUiVisibility(visFlags);
        */
        TestUtils.setWindowFlags(this);
        //Gionee zhangke 20151215 modify for CR01609753 end
        setContentView(R.layout.laser_test);

        mOffsetBtn = (Button) findViewById(R.id.offset_button);
        mOffsetBtn.setOnClickListener(this);
        mCrosstalkBtn = (Button) findViewById(R.id.crosstalk_button);
        mCrosstalkBtn.setOnClickListener(this);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);

        mTitle = (TextView) findViewById(R.id.test_title);
        mContent = (TextView) findViewById(R.id.test_content);
        mContent.setVisibility(View.VISIBLE);
        mTitle.setText(getString(R.string.laser_distance_testing));

        mOffsetText = (TextView)findViewById(R.id.offset_text);
        mCrosstalkText = (TextView)findViewById(R.id.crosstalk_text);

        if(isDoLaserOffsetCalib() == 1){
            mOffsetValue = getLaserOffsetCalib();
            mIsOffsetCaled = true;
        }
        if(isDoLaserXTalkCalib() == 1){
            mCrosstalkValue = getLaserXTalkCalib();
            mIsCrosstalkCaled = true;
        }

        refreshStatus();
        //Gionee zhangke 20160309 delete for CR01649484 start
        //mOffsetBtn.setEnabled(true);
        //Gionee zhangke 20160309 delete for CR01649484 end

        Log.i(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        startDistanceThread();
    }

    private void startDistanceThread(){
        mIsThreadRun = true;
        new Thread(new Runnable() {
            public void run() {
                while (mIsThreadRun) {
                    mIsTestSuccess = false;
                    execCmd(LASER_DISTANCE_ID);
                    mUiHandler.sendEmptyMessage(MESSAGE_SHOW_LASER_DISTANCE);
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
            }
        }).start();

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        mIsThreadRun = false;
    }

    private void refreshStatus() {
        /*
         * if(isDoLaserOffsetCalib() == 1){ mIsOffsetCaled = true; }else{
         * mIsOffsetCaled = false; } if(isDoLaserXTalkCalib() == 1){
         * mIsCrosstalkCaled = true; }else{ mIsCrosstalkCaled = false; }
         */
        String offsetText = "";
        String crosstalkText = "";

        if(mIsOffsetCaled){
            offsetText += getString(R.string.title_laser_offset) + mOffsetValue;
        }else{
            offsetText += getString(R.string.laser_offset_no);
        }
        mOffsetText.setText(offsetText);
        if(mIsCrosstalkCaled){
            crosstalkText += getString(R.string.title_laser_crosstalk) + mCrosstalkValue;
        }else{
            crosstalkText += getString(R.string.laser_crosstalk_no);
        }

        if(mOffsetBtnPressed && mIsOffsetCaled){            
			mOffsetBtn.setEnabled(false);
            if(mCrosstalkBtnPressed && mIsCrosstalkCaled){
                mCrosstalkBtn.setEnabled(false);
            }else{
                mCrosstalkBtn.setEnabled(true);
            }
        }
        mCrosstalkText.setText(crosstalkText);

        mWrongBtn.setEnabled(true);
        mRestartBtn.setEnabled(true);

        showResult();
        Log.i(TAG, "refreshStatus:mIsOffsetCaled=" + mIsOffsetCaled + ";mIsCrosstalkCaled=" + mIsCrosstalkCaled
            +";mOffsetBtnPressed="+mOffsetBtnPressed+";mCrosstalkBtnPressed="+mCrosstalkBtnPressed
			+";offsetText="+offsetText+";crosstalkText="+crosstalkText);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        String tag = "LaserPreTest";
        switch (v.getId()) {
        case R.id.offset_button:
            mOffsetBtn.setEnabled(false);
            mCrosstalkBtn.setEnabled(false);
            mIsThreadRun = false;
	        mOffsetBtnPressed = true;
            mOffsetText.setText(getString(R.string.laser_offset_testing));
            new Thread(new Runnable() {
                public void run() {
                    execCmd(LASER_OFFSET_ID);
                }
            }).start();
            break;
        case R.id.crosstalk_button:
            mOffsetBtn.setEnabled(false);
            mCrosstalkBtn.setEnabled(false);
            mIsThreadRun = false;
            mCrosstalkBtnPressed = true;
            mCrosstalkText.setText(getString(R.string.laser_crosstalk_testing));
            new Thread(new Runnable() {
                public void run() {
                    execCmd(LASER_CROSSTALK_ID);
                }
            }).start();
            break;
        case R.id.right_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            if(TestUtils.mIsAutoMode){
                SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                editor.putString("32", "P");
                editor.commit();
            }
            TestUtils.rightPress(tag, this);
            break;
        }

        case R.id.wrong_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            if(TestUtils.mIsAutoMode){
                SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                editor.putString("32", "F");
                editor.commit();
            }

            TestUtils.wrongPress(tag, this);
            break;
        }

        case R.id.restart_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.restart(this, tag);
            break;
        }
        }

    }

    private void execCmd(int cmd) {
        if (cmd == LASER_OFFSET_ID) {
            int laserOffsetCalStart = startLaserOffsetCalib();
            if (isDoLaserOffsetCalib() == 1) {
                mIsOffsetCaled = true;
                mOffsetValue = getLaserOffsetCalib();
                mUiHandler.sendEmptyMessage(MESSAGE_REFRESH_STATUS);
            } else {
                mIsOffsetCaled = false;
            }

            Log.i(TAG, "execCmd LASER_OFFSET_ID:laserOffsetCalStart = " + laserOffsetCalStart + ";mIsOffsetCaled=" + mIsOffsetCaled+";mOffsetValue="+mOffsetValue);
        } else if (cmd == LASER_CROSSTALK_ID) {
            int laserCrosstalkCalStart = startLaserXTalkCalib();
            if (isDoLaserXTalkCalib() == 1) {
                mIsCrosstalkCaled = true;
                mCrosstalkValue = getLaserXTalkCalib();
                mUiHandler.sendEmptyMessage(MESSAGE_REFRESH_STATUS);
                startDistanceThread();
            } else {
                mIsCrosstalkCaled = false;
            }
            Log.i(TAG, "execCmd LASER_CROSSTALK_ID:laserCrosstalkCalStart = " + laserCrosstalkCalStart + ";mIsCrosstalkCaled="
                    + mIsCrosstalkCaled+";mCrosstalkValue="+mCrosstalkValue);
        } else if (cmd == LASER_DISTANCE_ID) {
            if (mIsOffsetCaled && mIsCrosstalkCaled) {
                mDistance = getLaserRange();
                //400+-15%:340~460, 100+-15%:85~115
                if (mDistance > 340 && mDistance < 460) {
                    mIsTest400Success = true;
                }else if (mDistance > 85 && mDistance < 115) {
                    mIsTest100Success = true;
                } 
                if (mIsTest400Success && mIsTest100Success) {
                    mIsTestSuccess = true;
                } 
            }
            Log.i(TAG, "execCmd LASER_DISTANCE_ID:mIsOffsetCaled = " + mIsOffsetCaled + ";mIsCrosstalkCaled=" + mIsCrosstalkCaled
                    + ";mDistance=" + mDistance+";mIsTestSuccess="+mIsTestSuccess);
        }
    }

    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_REFRESH_STATUS:
                refreshStatus();
                break;
            case MESSAGE_SHOW_LASER_DISTANCE:
                showResult();
                break;
            }
        }
    };

    private void showResult() {
        Log.i(TAG, "showResult" + ";mDistance=" + mDistance + ";mIsTestSuccess=" + mIsTestSuccess+";mIsOffsetCaled="+mIsOffsetCaled
            +";mIsCrosstalkCaled="+mIsCrosstalkCaled);
        String textTip = "";
        String textTip2 = getString(R.string.laser_distance);

        if (mIsTestSuccess) {
            textTip += getString(R.string.success);
        } else {
            textTip += getString(R.string.fail);
            textTip += "(";
            if (!mIsOffsetCaled) {
                textTip += getString(R.string.laser_offset_no);
            } else if (!mIsCrosstalkCaled) {
                textTip += getString(R.string.laser_crosstalk_no);
            } else {
                if(!mIsTest100Success){
                    textTip += getString(R.string.laser_distance_100_error);
                } 
                if(!mIsTest400Success){
                    if(!mIsTest100Success){
                        textTip += ",";
                    }
                    textTip += getString(R.string.laser_distance_400_error);
                }
            }
            textTip += ")";
        }
        mRightBtn.setEnabled(mIsTestSuccess);
        textTip2 += mDistance;

        mTitle.setText(textTip);
        mContent.setText(textTip2);
        
    }
}
