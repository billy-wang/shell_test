package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;


import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.provider.Settings;
//Platform
//import amigo.provider.AmigoSettings;
import android.util.Log;

//Gionee zhangke 20151031 modify for CR01578276 start 
import android.os.Handler;
import android.os.Message;
//Gionee zhangke 20151031 modify for CR01578276 end 
import android.content.Context;
import android.content.Intent;


public class VibrateTest extends Activity implements Button.OnClickListener {
    private Vibrator mVibrator;
    private Button mRightBtn, mWrongBtn, mStopBtn, mRestartBtn;
    private static final String TAG = "VibrateTest";
    //Gionee zhangxiaowei 20130905 add for CR00888208 start
    private boolean mIsVibrator = false;
    private boolean mVibratorStatus = false;
    //Gionee zhangxiaowei 20130905 add for CR00888208 start
    private boolean vibFlag = false;
    private Intent it;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        it = this.getIntent();
        if(it != null){
            vibFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"vibFlag = " + vibFlag);        
        setContentView(R.layout.common_textview);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //Gionee zhangxiaowei 20130905 add for CR00888208 start
        updateSettings();
        if (mIsVibrator == false) {
            mVibratorStatus = true;

        }

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        if(vibFlag){
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
                mRightBtn.setEnabled(true);
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(VibrateTest.this);
                mWrongBtn.setOnClickListener(VibrateTest.this);
                mRestartBtn.setOnClickListener(VibrateTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

    }

    //Gionee zhangxiaowei 20130905 add for CR00888208 start
    public void updateSettings() {
        mIsVibrator = isRespirationLampNotificationOn();
    }

    public boolean isRespirationLampNotificationOn() {
        boolean result = false;
        return result;
    }

    // Gionee xiaolin 20121120 add for CR00732837 start
    @Override
    public void onStart() {
        super.onStart();
        mVibrator.cancel();
        mHandler.sendEmptyMessageDelayed(START_VIBRATE, 600);
        //Gionee zhangke 20151031 modify for CR01578276 end
    }

    // Gionee xiaolin 20121120 add for CR00732837 end
    @Override
    public void onStop() {
        super.onStop();
        //Gionee zhangke 20151031 modify for CR01578276 start 
        mHandler.removeMessages(START_VIBRATE);
        //Gionee zhangke 20151031 modify for CR01578276 end 
        mVibrator.cancel();
        //Gionee zhangxiaowei 20130905 add for CR00888208 start
        if (mVibratorStatus) {
             //Platform
            //	AmigoSettings.putInt(this.getContentResolver(),
            //		AmigoSettings.SWITCH_VIBRATION_ENABLED, 0);

        }
        //Gionee zhangxiaowei 20130905 add for CR00888208 end
        //Gionee zhangxiaowei 20130704 add for CR00833201 start
        // mVibrator = null;
        //Gionee zhangxiaowei 20130704 add for CR00833201 end
        if(vibFlag){
            this.finish();
            Log.d(TAG,"onStop as_record_finish_self");
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                if(vibFlag){
                    TestUtils.asResult(TAG,"","1");
                }                
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(vibFlag){
                    TestUtils.asResult(TAG,"","0");
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    //Gionee zhangke 20151031 modify for CR01578276 start
    private static final int START_VIBRATE = 0;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case START_VIBRATE:
                    Log.i(TAG, "START_VIBRATE");
                    mVibrator.vibrate(new long[]{1000, 2000}, 0);
                    break;
            }
        }
    };
    //Gionee zhangke 20151031 modify for CR01578276 end


}
