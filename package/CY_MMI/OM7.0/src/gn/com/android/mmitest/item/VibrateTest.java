package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
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
import gn.com.android.mmitest.utils.DswLog;

//Gionee zhangke 20151031 modify for CR01578276 start 
import android.os.Handler;
import android.os.Message;
//Gionee zhangke 20151031 modify for CR01578276 end 


public class VibrateTest extends BaseActivity implements Button.OnClickListener {
    private Vibrator mVibrator;
    private Button mRightBtn, mWrongBtn, mStopBtn, mRestartBtn;
    private static final String TAG = "VibrateTest";
    //Gionee zhangxiaowei 20130905 add for CR00888208 start
    private boolean mIsVibrator = false;
    private boolean mVibratorStatus = false;
    //Gionee zhangxiaowei 20130905 add for CR00888208 start

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开震动 @" + Integer.toHexString(hashCode()));

        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.common_textview);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //Gionee zhangxiaowei 20130905 add for CR00888208 start
        updateSettings();
        if (mIsVibrator == false) {
            //Platform
            //AmigoSettings.putInt(this.getContentResolver(),
            //		AmigoSettings.SWITCH_VIBRATION_ENABLED, 1);
            mVibratorStatus = true;

        }
        //Gionee zhangxiaowei 20130905 add for CR00888208 end
        // Gionee xiaolin delete for CR00732837 start
        //mVibrator.vibrate(new long[] {1000, 2000}, 0);
        // Gionee xiaolin delete for CR00732837 end

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
                mRightBtn.setEnabled(true);
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(VibrateTest.this);
                mWrongBtn.setOnClickListener(VibrateTest.this);
                mRestartBtn.setOnClickListener(VibrateTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
        // Gionee xiaolin 20121203 add for CR00738326 start
        //TestUtils.muteAudio(this, false);
        // Gionee xiaolin 20121203 add for CR00738326 end
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出震动 @" + Integer.toHexString(hashCode()));
    }

    //Gionee zhangxiaowei 20130905 add for CR00888208 start
    public void updateSettings() {
        mIsVibrator = isRespirationLampNotificationOn();
    }

    public boolean isRespirationLampNotificationOn() {
        boolean result = false;
        //Platform
        //result = AmigoSettings.getInt(this.getContentResolver(),
        //		AmigoSettings.SWITCH_VIBRATION_ENABLED, 0) != 0;
        return result;
    }

    // Gionee xiaolin 20121120 add for CR00732837 start
    @Override
    public void onStart() {
        super.onStart();
        mVibrator.cancel();
        //Gionee zhangke 20151031 modify for CR01578276 start 
        /*
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mVibrator.vibrate(new long[] {1000, 2000}, 0);//前一个参数为设置震动的效果的数组，第二个参数为 -1表示只震动一次，为0则震动会一直持续
        */
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
    }

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

    //Gionee zhangke 20151031 modify for CR01578276 start
    private static final int START_VIBRATE = 0;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case START_VIBRATE:
                    DswLog.i(TAG, "START_VIBRATE");
                    mVibrator.vibrate(new long[]{1000, 2000}, 0);
                    break;
            }
        }
    };
    //Gionee zhangke 20151031 modify for CR01578276 end


}
