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
import java.io.File;
import java.io.IOException;

//Gionee zhangke 20160411 modify for CR01664372 start
public class ReceiverDynaTest extends BaseActivity {
    private MediaPlayer mMediaPlayer;
    private int mLevel;
    private AudioManager mAudioManager;
    private static final String TAG = "ReceiverDynaTest";
    private int maxVol;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
        initMedia();
    }
    //Gionee <GN_BSP_AUTOMMI> <lifeilong> <20171104> modify for ID 252939 begin
    public void init(){
        mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        Intent it = this.getIntent();
        maxVol = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        String aString = TestUtils.getStreamVoice("ReceiverTest");
        int i = Integer.valueOf(aString).intValue();
        mLevel = it.getIntExtra("level", maxVol-i);
        Log.i(TAG, "onCreate mLevel=" + mLevel+";maxVol="+maxVol+";i="+i);
        if(mLevel > maxVol){
            mLevel = maxVol;
        }else if(mLevel < 0){
            mLevel = 0;
        }
    }
    public void initMedia(){
        mMediaPlayer = new MediaPlayer();
        String sdPath ;//= TestUtils.getSdCardPath(this);
        File file = new File("/sdcard/rdy.wav");
        sdPath = file.getAbsolutePath();
        Log.d(TAG,"file exists =" + file.exists() + "  , real Path = " + file.getAbsolutePath());//file.getAbsolutePath()
        if(file.exists()){
            try {
                Log.i(TAG,"setDataSource");
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(file.getAbsolutePath());
                mMediaPlayer.prepare();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            Log.i(TAG,"MediaPlayer.create");
            mMediaPlayer = MediaPlayer.create(this, R.raw.mute1s_plus3db_sweep_mono);
        }
    }
    //Gionee <GN_BSP_AUTOMMI> <lifeilong> <20171104> modify for ID 252939 end

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mMediaPlayer != null) {
            try {
                Log.i(TAG, "mMediaPlayer.release()");
                mMediaPlayer.release();
                //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170325> modify for ID 92029 begin
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                mAudioManager.setSpeakerphoneOn(true);
                //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170325> modify for ID 92029 end
            } catch (Exception e) {
                Log.i(TAG, "Exception = " + e.getMessage());
            }
        }

        //mAudioManager.setParameters("ADBMMIReceiver=0");
        this.finish();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.i(TAG, "onResume:set mLevel=" + mLevel);
        mAudioManager.setSpeakerphoneOn(false);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170706> modify for ID 165354 begin
        mAudioManager.setStreamVolume(AudioManager.MODE_IN_COMMUNICATION, mLevel, 0);
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170706> modify for ID 165354 begin
        if (mMediaPlayer != null) {
            try {
                // Log.i(TAG, "mMediaPlayer.prepare()");
                // mMediaPlayer.prepare();
                Log.i(TAG, "mMediaPlayer.start()");
                //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170325> modify for ID 92029 begin
                mMediaPlayer.setLooping(false);
                mMediaPlayer.setVolume(1,1);
                //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170325> modify for ID 92029 end
                mMediaPlayer.start();
            } catch (Exception e) {
                Log.i(TAG, "onResume Exception = " + e.getMessage());
            }
        }
    }
}
//Gionee zhangke 20160411 modify for CR01664372 end

