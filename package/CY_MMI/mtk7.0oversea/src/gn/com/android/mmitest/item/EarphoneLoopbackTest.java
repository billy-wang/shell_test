package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;

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

public class EarphoneLoopbackTest extends BaseActivity implements OnClickListener {
    private static String TAG = "EarphoneLoopbackTest";

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
    private BroadcastReceiver mEarphonePluginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 0);
                int mic = intent.getIntExtra("microphone", -1);
                Log.e(TAG, "mEarphonePluginReceiver: state:" + state);

                /*Gionee huangjianqiang 20160411 modify CR01671381 for begin*/

                if (STATE_PLUGGED == state) {
                    mPluginState = STATE_PLUGGED;
                    mContentTv.setText(R.string.inserted_earphone);
                    mContentTv.setTextColor(Color.YELLOW);
                    if (mic == 1) {
                        mIsPluginWithMic = true;
                    } else {
                        mIsPluginWithoutMic = true;
                    }
                } else {
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
                }
                /*Gionee huangjianqiang 20160411 modify CR01671381 for end*/
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        wavesEnable = true;
        setContentView(R.layout.common_textview);

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
        if (null != mAM) {

            mHifiState = TestUtils.getHifiState(this);
            if (mHifiState == 1) {
                TestUtils.openOrcloseHifi(this, false);
            }
			


            //mAM.setParameters("MMIMic=2");
            //Log.e(TAG, " onStart:Set Paremeter --> MMIMic=2");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mEarphonePluginReceiver);
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
                Log.e(TAG, "mPluginState = " + mPluginState);
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
                        //Log.e(TAG, "bufferReadResult = " + bufferReadResult);
                        byte[] tmpBuf = new byte[bufferReadResult];
                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                        mTrack.write(tmpBuf, 0, bufferReadResult);
                    }
                }
                buffer = null;
                // mAM.setMode(AudioManager.MODE_NORMAL);
            } catch (Throwable t) {
                Log.v(TAG, Log.getStackTraceString(t));
            } finally {
                //mTrack.release();
                //mRecord.release();
            }
        }
    }*/
    //Gionee <GN_BSP_MMI> <lifeilong> <20170316> modify for ID 86338 end

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
        mAM.setMode(AudioManager.MODE_NORMAL);
        if (mHifiState == 1) {
            TestUtils.openOrcloseHifi(this, true);
        }
        //mAM.setParameters("MMIMic=0");
        //Log.e(TAG, "cleanState: setParameters MMIMic=0");
        mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
        Log.e(TAG, "cleanState: setParameters SET_LOOPBACK_TYPE=0,0");


        // RecordThread should end when exiting
        //Gionee zhangke 20151012 add for CR01567500 start
        /*mRecordThread = false;
        //Gionee zhangke 20151012 add for CR01567500 end
        if (mRecThread != null && mRecThread.isAlive()) {
            mRecThread.interrupt();
            mRecThread = null;
        }*/
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_CALL:
                case KeyEvent.KEYCODE_ENDCALL:
                case KeyEvent.KEYCODE_HEADSETHOOK:

                    mTitleTv.setText(R.string.headsethook_press);
                    mRightBtn.setEnabled(true);
                    //Log.e(TAG, "Set streamvol to AudioManager.STREAM_MUSIC");
					//mAM.setStreamVolume(AudioManager.STREAM_MUSIC, (mMaxVol - mStreamVol), 0);
                    //Gionee <GN_BSP_MMI> <chengq> <20170325> modify for ID 93627 begin

                    //Gionee <GN_BSP_MMI> <chengq> <20170410> modify for ID 104490 begin
                    aString = TestUtils.setStreamVoice("lb_mmi_EarphoneLoopbackTest");
                    if (aString != null && aString.length() > 0 ) {
                        mAM.setParameters("SET_LOOPBACK_TYPE=22,2,"+aString);
                    }else {
                        mAM.setParameters("SET_LOOPBACK_TYPE=22,2");
                    }
					Log.e(TAG, " onStart:Set Paremeter --> SET_LOOPBACK_TYPE=22,2,"+aString);
                    //Gionee <GN_BSP_MMI> <chengq> <20170410> modify for ID 104490 end

                    //Initial record thread here to prevent AudioTrack & RecordRecord
                    // having not release during PhoneLoopbackTest
                    /*if (mRecThread == null) {
                        mRecThread = new RecordThread();
                    }
                    if (mRecThread != null && mRecThread.getState() == State.NEW) {
                        Log.e(TAG, "--- RecordThread onStart---");                        
                    }*/
                    //Gionee <GN_BSP_MMI> <chengq> <20170325> modify for ID 93627 end
                    break;
            }
        }
        return true;
    }

}
