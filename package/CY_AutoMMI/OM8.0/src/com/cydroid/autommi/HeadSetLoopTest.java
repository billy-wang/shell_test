package com.cydroid.autommi;

import java.lang.Thread.State;

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
import com.cydroid.util.DswLog;
import android.view.KeyEvent;
import android.view.WindowManager;
//Gionee zhangke 20151026 modify for CR01574984 start
import android.content.Intent;
//Gionee zhangke 20151026 modify for CR01574984 start

public class HeadSetLoopTest extends BaseActivity {

    private static String TAG = "EarphoneLoopbackTest_billy";
    private final static int STATE_UNPLUGGIN = 0;
    private final static int STATE_PLUGGED = 1;
    private int mPluginState = STATE_UNPLUGGIN;

    // Gionee zhangke 20160411 modify for CR01664372 start
    private AudioManager mAudioManager;
    private int mLevel;
    // Gionee zhangke 20160411 modify for CR01664372 end

    //private RecordThread mRecThread;
    // Gionee zhangke 20151026 modify for CR01574984 start
    private boolean mIsRunning = true;
    // Gionee zhangke 20151026 modify for CR01574984 end
    private String aString;
    private BroadcastReceiver mEarphonePluginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 0);
                mPluginState = state;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        // lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);

        //mRecThread = new RecordThread();
        // Gionee zhangke 20160411 modify for CR01664372 start
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        registerReceiver(mEarphonePluginReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
       /* Intent it = this.getIntent();
        int maxVol = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mLevel = it.getIntExtra("level", maxVol);
        DswLog.i(TAG, "onCreate mLevel=" + mLevel);
        // Gionee zhangke 20160411 modify for CR01664372 end
        if(mLevel > maxVol){
            mLevel = maxVol;
        }else if(mLevel < 0){
            mLevel = 0;
        }*/

    }

    @Override
    public void onStart() {
        super.onStart();
        if (null != mAudioManager) {
            // Gionee zhangke 20151026 modify for CR01574984 start

            // Gionee zhangke 20160411 modify for CR01664372 start
            //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mLevel, 0);
            // Gionee zhangke 20160411 modify for CR01664372 end
            //DswLog.i(TAG, " set stream vol-->AudioManager.STREAM_MUSIC. Mode-->MMIMic=2");
            //DswLog.i(TAG, " set stream vol-->AudioManager.STREAM_MUSIC. Mode-->  SET_LOOPBACK_TYPE=22,2  ");
            //mAudioManager.setParameters("MMIMic=2");

            //Gionee <GN_BSP_AutoMMI> <chengq> <20170410> modify for ID 100178 begin
            aString = TestUtils.getStreamVoice("lb_autommi_HeadSetLoopTest");
            if (aString != null && aString.length() > 0 ) {
                mAudioManager.setParameters("SET_LOOPBACK_TYPE=22,2,"+aString);
            }else {
                mAudioManager.setParameters("SET_LOOPBACK_TYPE=22,2");
            }
            DswLog.e(TAG, "onStart: setParameters SET_LOOPBACK_TYPE=22,2,"+aString);
            //Gionee <GN_BSP_AutoMMI> <chengq> <20170410> modify for ID 100178 end

            // Gionee zhangke 20151026 modify for CR01574984 end
        }
        /*if (mRecThread != null && mRecThread.getState() == State.NEW) {
            DswLog.e(TAG, "--- RecordThread onStart---");
            mRecThread.start();
        }*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        cleanState();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mEarphonePluginReceiver);
    }

    private void cleanState() {
        //mAudioManager.setMode(AudioManager.MODE_NORMAL); /* disable by Billy.Wang */
        //mAudioManager.setParameters("MMIMic=0");
        mAudioManager.setParameters("SET_LOOPBACK_TYPE=0,0");
        //DswLog.e(TAG, "cleanState: setParameters MMIMic=0");
        DswLog.e(TAG, "cleanState: setParameters SET_LOOPBACK_TYPE=0,0");
        // Gionee zhangke 20151026 modify for start
        mIsRunning = false;
        // Gionee zhangke 20151026 modify for end
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    /*class RecordThread extends Thread {
        private AudioRecord mRecord;
        private AudioTrack mTrack;
        private int mRecBuffSize = 0;
        private int mTrackBuffSize = 0;

        public RecordThread() {
			//Gionee <GN_AutoMMI><lifeilong><20161109> modify for 21279 begin
            mRecBuffSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mTrackBuffSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mRecBuffSize);
            mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mTrackBuffSize, AudioTrack.MODE_STREAM);
            mRecBuffSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mTrackBuffSize = AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mRecBuffSize);
            mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mTrackBuffSize, AudioTrack.MODE_STREAM);
			//Gionee <GN_AutoMMI><lifeilong><20161109> modify for 21279 begin
        }

        public void run() {
            try {
                byte[] buffer = new byte[mRecBuffSize];
                //mRecord.startRecording();
                //mTrack.play();
                // Gionee zhangke 20151026 modify for start
                while (mIsRunning) {
                    if (mPluginState == STATE_UNPLUGGIN) {
                        continue;
                    }
                    int bufferReadResult = mRecord.read(buffer, 0, mRecBuffSize);
                    if (bufferReadResult > 0 && bufferReadResult % 2 == 0) {
                        DswLog.e(TAG, "bufferReadResult = " + bufferReadResult);
                        byte[] tmpBuf = new byte[bufferReadResult];
                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                        mTrack.write(tmpBuf, 0, bufferReadResult);
                    }
                }
                // Gionee zhangke 20151026 modify for end
            } catch (Throwable t) {
            } finally {
                //mTrack.release();
                //mRecord.release();
            }
        }
    }*/

}
