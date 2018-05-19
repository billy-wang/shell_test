package com.gionee.autommi;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class SpeakerDynaTest extends BaseActivity {
    private MediaPlayer mMediaPlayer;
    private int mLevel;
    protected AudioManager mAudioManager;
    private static final String TAG = "SpeakerDynaTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        Intent it = this.getIntent();
        int maxVol = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        String aString = TestUtils.getStreamVoice("ToneTest");
        int i = Integer.valueOf(aString).intValue();
        mLevel = it.getIntExtra("level", maxVol - i);
        Log.i(TAG, "onCreate mLevel=" + mLevel + ";maxVol=" + maxVol + ";i=" + i);
        if(mLevel > maxVol){
            mLevel = maxVol;
        }else if(mLevel < 0){
            mLevel = 0;
        }

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(this, R.raw.spk);
        chooseSpeaker();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mMediaPlayer != null) {
            try {
                Log.i(TAG, "mMediaPlayer.release()");
                mMediaPlayer.release();
            } catch (Exception e) {
                Log.i(TAG, "Exception = " + e.getMessage());
            }
        }

        this.finish();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        int maxVol = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.i(TAG, "onResume:mLevel=" + mLevel);
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170113> modify for ID 224866 begin
        mAudioManager.setSpeakerphoneOn(true);
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170113> modify for ID 224866 end
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mLevel, 0);
        if (mMediaPlayer != null) {
            try {
                // Log.i(TAG, "mMediaPlayer.prepare()");
                // mMediaPlayer.prepare();
                Log.i(TAG, "mMediaPlayer.start()");
                mMediaPlayer.start();
            } catch (Exception e) {
                Log.i(TAG, "onResume Exception = " + e.getMessage());
            }
        }
    }

    protected void chooseSpeaker() {

    }
}
