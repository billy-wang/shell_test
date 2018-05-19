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
        Log.i(TAG, "onCreate mLevel=" + mLevel+";maxVol="+maxVol+";i="+i);
        if(mLevel > maxVol){
            mLevel = maxVol;
        }else if(mLevel < 0){
            mLevel = 0;
        }


        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(this, R.raw.rec);
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

        mAudioManager.setParameters("ADBMMIReceiver=0");
        this.finish();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mAudioManager.setParameters("ADBMMIReceiver=1");
        Log.i(TAG, "onResume:set mLevel=" + mLevel);
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
}
//Gionee zhangke 20160411 modify for CR01664372 end

