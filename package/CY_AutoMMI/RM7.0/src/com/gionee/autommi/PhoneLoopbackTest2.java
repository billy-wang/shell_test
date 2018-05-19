
package com.gionee.autommi;

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
public class PhoneLoopbackTest2 extends BaseActivity {

    boolean mIsRecording, mIsStop, mIsGetState;
    TextView mContentTv;
    AudioRecord mRecord;

    AudioTrack mTrack;

    AudioManager mAM;

    int mRecBuffSize, mTrackBuffSize;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static String TAG = "PhoneLoopbackTest2";
    String aString = null;
    RecordThread mRecThread;
    private int mLevel;
	//Gionee <GN_BSP_AutoMMI><lifeilong><20161111> modify for 21994 begin
	private int singleInt = 8000;
	//Gionee <GN_BSP_AutoMMI><lifeilong><20161111> modify for 21994 end
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        /*mRecBuffSize = AudioRecord.getMinBufferSize(singleInt, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mTrackBuffSize = AudioTrack.getMinBufferSize(singleInt, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, singleInt, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, mTrackBuffSize, AudioTrack.MODE_STREAM);*/
        /*Intent it = this.getIntent();
        int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        String aString = TestUtils.getStreamVoice("PhoneLoopbackTest2");
        int i = Integer.valueOf(aString).intValue();
        mLevel = it.getIntExtra("level", maxVol-i);
        Log.i(TAG, "onCreate mLevel=" + mLevel+";maxVol="+maxVol+";i="+i);
        if(mLevel > maxVol){
            mLevel = maxVol;
        }else if(mLevel < 0){
            mLevel = 0;
        }*/


    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
        mAM.setMode(AudioManager.MODE_NORMAL);
        //mAM.setParameters("MMIMic=3");
        //mAM.setParameters("SET_LOOPBACK_TYPE=3,2");
        //Log.i(TAG, " set stream vol-->AudioManager.STREAM_MUSIC. Mode-->  SET_LOOPBACK_TYPE= 3,2  ");
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <lifeilong> modify for ID 142337 begin
        aString = TestUtils.getStreamVoice("lb_autommi_PhoneLoopbackTest2");
        if (aString != null && aString.length() > 0 ) {
            mAM.setParameters("SET_LOOPBACK_TYPE=3,2,"+aString);
        }else {
            mAM.setParameters("SET_LOOPBACK_TYPE=3,2");
        }
        Log.e(TAG, "onStart: setParameters SET_LOOPBACK_TYPE=3,2,"+aString);
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <lifeilong> modify for ID 142337 end

        /*new Thread() {
            public void run() {
                //Gionee <GN_BSP_MMI><zhangke><20161105> add for ID19105 begin
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    mRecBuffSize);
                //Gionee <GN_BSP_MMI><zhangke><20161105> add for ID19105 end
                
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!mIsRecording) {
                    //new RecordThread().start();
                }
                if (null != mAM) {
                    mAM.setStreamVolume(AudioManager.STREAM_MUSIC, mLevel, 0);
                }
                Log.e(TAG, "run end");
            }
        }.start();*/
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

    }

    @Override
    public void onPause() {
        mIsStop = true;
        super.onPause();
        // Gionee zhangke 20151218 add for CR01611271 start
        /*try {
            Thread.sleep(500);
        } catch (Exception e) {
            System.out.println(e);
        }*/
        //mAM.setParameters("MMIMic=0");
        mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
        Log.e(TAG, " onPause set mode --> mode_normal,setParameters SET_LOOPBACK_TYPE=0,0");
        // Gionee zhangke 20151218 add for CR01611271 end
        Log.e(TAG, "onPause set mode --> mode_normal");
        mAM.setMode(AudioManager.MODE_NORMAL);
    }

    class RecordThread extends Thread {
        public void run() {
            try {
                //byte[] buffer = new byte[mRecBuffSize];
                //mRecord.startRecording();
                //mTrack.play();
                mIsRecording = true;
                /*while (false == mIsStop) {
                    int bufferReadResult = mRecord.read(buffer, 0, mRecBuffSize);
                    if (bufferReadResult > 0 && bufferReadResult % 2 == 0) {
                        //Log.e(TAG, "bufferReadResult = " + bufferReadResult);
                        byte[] tmpBuf = new byte[bufferReadResult];
                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                        mTrack.write(tmpBuf, 0, bufferReadResult);
                    }
                }*/
                // Gionee xiaolin 20120613 modify for CR00624109 start
                //mTrack.stop();
                //mRecord.stop();
                //mTrack.release();
                //mRecord.release();
                // Gionee xiaolin 20120613 modify for CR00624109 end
                mIsRecording = false;
            } catch (Throwable t) {
            }
        }
    }

}
// Gionee zhangke 20160411 modify for CR01664372 end
