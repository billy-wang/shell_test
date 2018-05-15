package gn.com.android.mmitest.item;

import java.lang.Thread.State;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;
import android.content.Intent;

public class EarphoneLoopbackTest extends Activity implements OnClickListener {
    private static String TAG = "EarphoneLoopbackTest";

    private final static int STATE_UNPLUGGIN = 0;
    private final static int STATE_PLUGGED = 1;
    private int mPluginState = STATE_UNPLUGGIN;

    private TextView mContentTv, mTitleTv;
    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private int mStreamVol, mMaxVol;
    private int mHifiState;
    private boolean ear_flag = true;
    private AudioManager mAM;
    //Gionee zhangke 20151012 add for CR01567500 start
    private boolean mRecordThread = true;
    //Gionee zhangke 20151012 add for CR01567500 end
    String aString = null;
    private int earCount = 0;
    private BroadcastReceiver mEarphonePluginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 0);
                mPluginState = state;
                Log.i(TAG, "mEarphonePluginReceiver mPluginState = " + state);
                //Gionee <GN_BSP_MMI> <lifeilong> <20170805> modify for ID 181363 begin
                if (STATE_UNPLUGGIN == state && earCount == 1) {
                    mContentTv.setText(R.string.insert_earphone);
                    mContentTv.setTextColor(Color.RED);
                    ear_flag = false;
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170524> modify for ID 146803 begin 
                    mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
                    Log.e(TAG, "cleanState: setParameters SET_LOOPBACK_TYPE=0,0");
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170524> modify for ID 146803 end
                    Log.e(TAG," ear_flag  = " + ear_flag);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20171009> modify for ID 231906 begin 
                    if(earCount == 1){
                        mRightBtn.setEnabled(true);
                        mRightBtn.setVisibility(View.VISIBLE);
                    }
                    earCount = 0;
                }
                if (STATE_PLUGGED == state) {
                    earCount = 1;
                    mRightBtn.setVisibility(View.INVISIBLE);
                    mTitleTv.setText(R.string.headsethook_note);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20171009> modify for ID 231906 begin 
                //Gionee <GN_BSP_MMI> <lifeilong> <20170805> modify for ID 181363 end
                    mContentTv.setText(R.string.inserted_earphone);
                    mContentTv.setTextColor(Color.YELLOW);
                    ear_flag = true;
                    Log.e(TAG," ear_flag  = " + ear_flag);
                }
            }
        }
    };


    private boolean earPhoneloopbackFlag = false;
    private Intent it;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TestUtils.checkToContinue(this);

        TestUtils.setWindowFlags(this);
        setContentView(R.layout.common_textview);
        it = this.getIntent();
        if(it != null){
            earPhoneloopbackFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"earPhoneloopbackFlag = " + earPhoneloopbackFlag);    

        mContentTv = (TextView) findViewById(R.id.test_content);
        mContentTv.setGravity(Gravity.CENTER);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mTitleTv.setText(R.string.headsethook_note);

        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        registerReceiver(mEarphonePluginReceiver, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setVisibility(View.INVISIBLE);
        if(earPhoneloopbackFlag){
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

                mRightBtn.setOnClickListener(EarphoneLoopbackTest.this);
                mWrongBtn.setOnClickListener(EarphoneLoopbackTest.this);
                mRestartBtn.setOnClickListener(EarphoneLoopbackTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(earPhoneloopbackFlag){
           this.finish();
           Log.d(TAG,"onStop as_record_finish_self");
        }        
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        earCount = 0;
        unregisterReceiver(mEarphonePluginReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_btn: {
                if(earPhoneloopbackFlag){
                    TestUtils.asResult(TAG,"","1");
                }                  
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(earPhoneloopbackFlag){
                    TestUtils.asResult(TAG,"","0");
                }                  
                TestUtils.wrongPress(TAG, this);
                break;
            }

            case R.id.restart_btn: {
                TestUtils.restart(this, TAG);
                break;
            }
        }
        cleanState();
    }

    private void cleanState() {
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);
        mAM.setMode(AudioManager.MODE_NORMAL);
        if (mHifiState == 1) {
            TestUtils.openOrcloseHifi(this, true);
        }
        mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
        Log.e(TAG, "cleanState: setParameters SET_LOOPBACK_TYPE=0,0");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_CALL:
                case KeyEvent.KEYCODE_ENDCALL:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    mTitleTv.setText(R.string.headsethook_press);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170522> modify for ID 144848 begin
                    if(ear_flag){
                        Log.e(TAG, "Set streamvol to AudioManager.STREAM_MUSIC");
                        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, (mMaxVol - mStreamVol), 0);
                        //Gionee <GN_BSP_MMI> <lifeilong> <20170316> modify for ID 86338 begin
                        aString = TestUtils.getStreamVoice("lb_mmi_EarphoneLoopbackTest");
                        if (aString != null && aString.length() > 0 ) {
                            mAM.setParameters("SET_LOOPBACK_TYPE=22,2"+aString);
                        }else {
                            mAM.setParameters("SET_LOOPBACK_TYPE=22,2");
                        }
                        Log.e(TAG, "onStart: setParameters SET_LOOPBACK_TYPE=22,2"+aString);
                        //Gionee <GN_BSP_MMI> <lifeilong> <20170316> modify for ID 86338 end
                        ear_flag = false;
                        Log.e(TAG, "   ear_flag = false;  ");
                    }else {
                        Log.e(TAG, "   ear_flag = false already SET_LOOPBACK_TYPE=22,2  " + aString);
                    }
                     //Gionee <GN_BSP_MMI> <lifeilong> <20170522> modify for ID 144848 end
                    break;
            }
        }
        return true;
    }

}
