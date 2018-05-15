package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;

import java.lang.Thread.State;

import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

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
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;

public class EarphoneLoopbackTest extends BaseActivity implements OnClickListener {
    private static String TAG = "EarphoneLoopbackTest_billy";

    private final static int STATE_UNPLUGGIN = 0;
    private final static int STATE_PLUGGED = 1;
    private int mPluginState = STATE_UNPLUGGIN;

    private TextView mContentTv, mTitleTv;
    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private int mStreamVol, mMaxVol;
    private int mHifiState;

    private AudioManager mAM;
    //private RecordThread mRecThread = null;
    //Gionee zhangke 20151012 add for CR01567500 start
    private boolean mRecordThread = true;
    //Gionee zhangke 20151012 add for CR01567500 end
    /*Gionee huangjianqiang 20160411 add CR01671381 for begin*/
    private boolean mIsPluginWithMic;
    private boolean mIsPluginWithoutMic;
    /*Gionee huangjianqiang 20160411 add for CR01671381 end*/
    private String aString;
    
    /* add by Billy.Wang */
    /* 监听系统广播ACTION_HEADSET_PLUG，耳机拔出时暂停播放，然而实际处理时会有延迟，耳机拔出并没有立即暂停播放，而是延迟几秒之后才暂停
     * 弊端：监测不到耳机插入事件
     * */
    private BroadcastReceiver mEarphonePluginReceiver_ = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)){
	       DswLog.d(TAG, "headset disconnected===> PlugOut");
	   }
	}
    };

    private BroadcastReceiver mEarphonePluginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
	   
	   /* add by Billy.Wang */
	   int state_ = intent.getIntExtra("state", -1);
           int mic_ = intent.getIntExtra("microphone", -1);
           if(state_==0 && mic_ ==0){ 
                DswLog.d(TAG, "headset no microphone not connected");
           }else if (state_==0 && mic_ ==1) { 
                DswLog.d(TAG, "headset with microphone not connected");
           }else if(state_==1 && mic_ ==0){ 
                DswLog.d(TAG, "headset no microphone connected");
           }else if (state_==1 && mic_ ==1) {
                DswLog.d(TAG, "headset with microphone connected");
           } 
     
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 0);
                int mic = intent.getIntExtra("microphone", -1);
                DswLog.d(TAG, "mEarphonePluginReceiver: state:" + state + " mic: " + mic);

                /*Gionee huangjianqiang 20160411 modify CR01671381 for begin*/

                if (STATE_PLUGGED == state) {
                    DswLog.d(TAG, "mPluginState " + state + " mic: " + mic);
                    mPluginState = STATE_PLUGGED;
                    mContentTv.setText(R.string.inserted_earphone);
                    mContentTv.setTextColor(Color.YELLOW);
                    if (mic == 1) {
                        mIsPluginWithMic = true;
                    } else {
                        mIsPluginWithoutMic = true;
                    }
                } else {
                    DswLog.d(TAG, "mPluginState " + state + " mic: " + mic);
                    if ((mic == 0) && (mIsPluginWithMic) && (mIsPluginWithoutMic)) {
                        mIsPluginWithMic = false;
                        mIsPluginWithoutMic = false;
//                        mPluginState = STATE_PLUGGED;
                        mContentTv.setTextColor(Color.YELLOW);
                        mContentTv.setText(R.string.inserted_earphone);
                    } else {
                        mContentTv.setTextColor(Color.RED);
                        mContentTv.setText(R.string.insert_earphone);
                        mIsPluginWithMic = false;
                        mIsPluginWithoutMic = false;
                        mPluginState = STATE_UNPLUGGIN;
                    }
		    mic = 0;
                }
                /*Gionee huangjianqiang 20160411 modify CR01671381 for end*/
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        DswLog.d(TAG, "\n\n\n****************打开耳机回路 @" + Integer.toHexString(hashCode()));
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        wavesEnable = true;
        setContentView(R.layout.common_textview);

        mContentTv = (TextView) findViewById(R.id.test_content);
        mContentTv.setGravity(Gravity.CENTER);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mTitleTv.setText(R.string.headsethook_note);

        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
	registerReceiver(mEarphonePluginReceiver, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));
	/* add by Billy.Wang */
        registerReceiver(mEarphonePluginReceiver_, new IntentFilter(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY));

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

                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(EarphoneLoopbackTest.this);
                mWrongBtn.setOnClickListener(EarphoneLoopbackTest.this);
                mRestartBtn.setOnClickListener(EarphoneLoopbackTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (null != mAM) {
            mHifiState = TestUtils.getHifiState(this);
            if (mHifiState == 1) {
                DswLog.d(TAG, "openOrcloseHifi false");
                TestUtils.openOrcloseHifi(this, false);
            }
            //mAM.setParameters("MMIMic=2");
            //DswLog.e(TAG, " onStart:Set Paremeter --> MMIMic=2");
        }
    }

    @Override
    protected void onDestroy() {
        DswLog.d(TAG, "\n****************退出耳机回路 @" + Integer.toHexString(hashCode()));
        super.onDestroy();
        unregisterReceiver(mEarphonePluginReceiver);
	/* add by Billy.Wang */
        unregisterReceiver(mEarphonePluginReceiver_);
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170316> modify for ID 86338 begin
    /*class RecordThread extends Thread {
        private AudioRecord mRecord;
        private AudioTrack mTrack;
        private int mRecBuffSize, mTrackBuffSize;

        public RecordThread() {
            // Gionee <xuna><2013-06-03> delete for CR00873055 begin
            mRecBuffSize = AudioRecord.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mTrackBuffSize = AudioTrack.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mRecBuffSize);
            mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mTrackBuffSize,
                    AudioTrack.MODE_STREAM);
        }

        public void run() {
            try {
                byte[] buffer = new byte[mRecBuffSize];
                //mRecord.startRecording();
                //mTrack.play();
                DswLog.e(TAG, "mPluginState = " + mPluginState);
                //Gionee zhangke 20151012 modify for CR01567500 start
                while (mRecordThread) {
                    //if (interrupted())
                    //	break;
                    //Gionee zhangke 20151012 add for CR01567500 end

                    if (mPluginState == STATE_UNPLUGGIN) {
                        continue;
                    }

                    int bufferReadResult = mRecord.read(buffer, 0, mRecBuffSize);
                    if (bufferReadResult > 0 && bufferReadResult % 2 == 0) {
                        //DswLog.e(TAG, "bufferReadResult = " + bufferReadResult);
                        byte[] tmpBuf = new byte[bufferReadResult];
                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                        mTrack.write(tmpBuf, 0, bufferReadResult);
                    }
                }
                buffer = null;
                // mAM.setMode(AudioManager.MODE_NORMAL);
            } catch (Throwable t) {
                DswLog.v(TAG, DswLog.getStackTraceString(t));
            } finally {
                //mTrack.release();
                //mRecord.release();
            }
        }
    }*/
    //Gionee <GN_BSP_MMI> <lifeilong> <20170316> modify for ID 86338 end

    @Override
    public void onResume() {
        super.onResume();
        DswLog.d(TAG, "打开耳机测试界面 onResume: mmi_setParameters SET_LOOPBACK_TYPE=0,0");
        mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
    }

    @Override
    public void onPause() {
        super.onPause();
        DswLog.d(TAG, "离开耳机测试界面 onPause: mmi_setParameters SET_LOOPBACK_TYPE=0,0");
        mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_btn: {
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
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
      //  mAM.setMode(AudioManager.MODE_NORMAL);
        if (mHifiState == 1) {
            DswLog.d(TAG, "openOrcloseHifi true");
            TestUtils.openOrcloseHifi(this, true);
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_CALL:
                case KeyEvent.KEYCODE_ENDCALL:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    DswLog.d(TAG, "KeyEvent keycode " + event.getKeyCode());

                    aString = TestUtils.setStreamVoice("lb_mmi_EarphoneLoopbackTest");
                    if (aString != null && aString.length() > 0 ) {
                        DswLog.d(TAG, "耳机按键，下发参数 :mmi_setParameters --> SET_LOOPBACK_TYPE=22,2,"+aString);
                        mAM.setParameters("SET_LOOPBACK_TYPE=22,2,"+aString);
                    }else {
                        DswLog.w(TAG, "耳机按键，下发参数 :mmi_setParameters --> SET_LOOPBACK_TYPE=22,2,");
                        mAM.setParameters("SET_LOOPBACK_TYPE=22,2");
                    }

                    mTitleTv.setText(R.string.headsethook_press);
                    mRightBtn.setEnabled(true);
                    break;
            }
        }
        return true;
    }

}
