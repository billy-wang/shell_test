
package com.gionee.autommi;

import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
//Gionee zhangke 20160411 modify for CR01664372 start
public class PhoneLoopbackTest extends BaseActivity {
    private static String TAG = "PhoneLoopbackTest";
    String aString = null;

    Button mRightBtn, mWrongBtn, mRestartBtn;
    TextView mContentTv;

    AudioManager mAM;
    RecordThread mRecThread;
    AtomicBoolean mIsRunning = new AtomicBoolean(false);
    private int mLevel;
	private Intent it;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRecThread = new RecordThread();
		it = this.getIntent();
        int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        String aString = TestUtils.getStreamVoice("PhoneLoopbackTest");
        int i = Integer.valueOf(aString).intValue();
        mLevel = it.getIntExtra("level", maxVol-i);
        Log.i(TAG, "onCreate mLevel=" + mLevel+";maxVol="+maxVol+";i="+i);
        if(mLevel > maxVol){
            mLevel = maxVol;
        }else if(mLevel < 0){
            mLevel = 0;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mAM.setMode(AudioManager.MODE_NORMAL);
        mIsRunning.set(true);
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161114> modify for 25264
        String s = it.toString();
		Log.e(TAG,"s = " + s.substring(32, s.length() - 1));
		if(s.substring(32, s.length() - 1).startsWith("al")){
			mAM.setParameters("ADBMMIMic=1");
			Log.e(TAG,"ADBMMIMic=1");
		}else if(s.substring(32, s.length() - 1).startsWith("la")){
			mAM.setParameters("MMIMic=1");
			Log.e(TAG,"MMIMic=1");
		}
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161114> modify for 25264


		
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (null != mAM) {
                    mAM.setStreamVolume(AudioManager.STREAM_MUSIC, mLevel, 0);
                }
                if (mRecThread != null && mRecThread.getState() == State.NEW) {
                    Log.v(TAG, "start RecordThread");
                    mRecThread.start();
                }
                Log.e(TAG, "run end");
            }
        }.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsRunning.set(false);

        Log.e(TAG, " onPause ");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    class RecordThread extends Thread {
        AudioRecord mRecord;
        AudioTrack mTrack;
        int mRecBuffSize, mTrackBuffSize = -1;

        public RecordThread() {
            // Gionee <xuna><2013-06-03> delete for CR00873055 begin
            mRecBuffSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mTrackBuffSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            // Gionee <xuna><2013-06-03> delete for CR00873055 end
            mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mRecBuffSize);
            mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mTrackBuffSize, AudioTrack.MODE_STREAM);
        }

        public void run() {
            try {
                Log.v(TAG, "Record thread beging running");
                mRecord.startRecording();
                mTrack.play();
                byte[] buffer = new byte[mRecBuffSize];
                while (mIsRunning.get()) {
                    int bufferReadResult = mRecord.read(buffer, 0, mRecBuffSize);
                    if (bufferReadResult > 0 && bufferReadResult % 2 == 0) {
                        Log.e(TAG, "bufferReadResult = " + bufferReadResult);
                        byte[] tmpBuf = new byte[bufferReadResult];
                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                        mTrack.write(tmpBuf, 0, bufferReadResult);
                    }
                }
            } catch (Throwable t) {
                Log.v(TAG, Log.getStackTraceString(t));
            } finally {
                mTrack.stop();
                mRecord.stop();
                mTrack.release();
                mRecord.release();
                mAM.setParameters("MMIMic=0");
                mAM.setMode(AudioManager.MODE_NORMAL);
            }
        }
    }
}
//Gionee zhangke 20160411 modify for CR01664372 end

