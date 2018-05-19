package com.gionee.autommi;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.content.Context;
import android.media.MediaPlayer;
import com.gionee.util.DswLog;
import com.gionee.autommi.R;

//Gionee zhangke 20160411 modify for CR01664372 start
public class ReceiverDynaTest extends BaseActivity {
    private MediaPlayer mMediaPlayer;
    private int mLevel;
    private AudioManager mAudioManager;
    private static final String TAG = "ReceiverDynaTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        Intent it = this.getIntent();
        int maxVol = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        String aString = TestUtils.getStreamVoice("ReceiverTest");
        int i = Integer.valueOf(aString).intValue();
        mLevel = it.getIntExtra("level", maxVol-i);
        DswLog.i(TAG, "onCreate mLevel=" + mLevel+";maxVol="+maxVol+";i="+i);
        if(mLevel > maxVol){
            mLevel = maxVol;
        }else if(mLevel < 0){
            mLevel = 0;
        }


        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(this, R.raw.mute1s_plus3db_sweep_mono);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mMediaPlayer != null) {
            try {

                DswLog.i(TAG, "mMediaPlayer.release()");
                mMediaPlayer.release();
            } catch (Exception e) {
                DswLog.i(TAG, "Exception = " + e.getMessage());
            }
        }
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170323> modify for ID 91383 begin
	    mAudioManager.setSpeakerphoneOn(true);  
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170323> modify for ID 91383 end
        //mAudioManager.setParameters("ADBMMIReceiver=0");
        this.finish();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //mAudioManager.setParameters("ADBMMIReceiver=1");
        DswLog.i(TAG, "onResume:set mLevel=" + mLevel);
        //Gionee <GN_BSP_AUTOMMI> <chengq> <20170323> modify for ID 91383 begin	
		mAudioManager.setSpeakerphoneOn(false);
		mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		//Gionee <GN_BSP_AUTOMMI> <chengq> <20170323> modify for ID 91383 end
	    mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mLevel, 0);
		
		
        if (mMediaPlayer != null) {
            try {
                // DswLog.i(TAG, "mMediaPlayer.prepare()");
                // mMediaPlayer.prepare();
				mMediaPlayer.setLooping(false);
				mMediaPlayer.setVolume(1, 1);
                DswLog.i(TAG, "mMediaPlayer.start()");
                mMediaPlayer.start();
            } catch (Exception e) {
                DswLog.i(TAG, "onResume Exception = " + e.getMessage());
            }
        }
    }
}
//Gionee zhangke 20160411 modify for CR01664372 end

