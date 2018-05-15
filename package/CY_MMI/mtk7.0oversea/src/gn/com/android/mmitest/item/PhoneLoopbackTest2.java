
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
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
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class PhoneLoopbackTest2 extends BaseActivity implements OnClickListener {

    boolean mIsRecording, mIsStop, mIsGetState;
    TextView mContentTv;
    AudioRecord mRecord;

    AudioTrack mTrack;

    AudioManager mAM;

    int mRecBuffSize, mTrackBuffSize;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static String TAG = "PhoneLoopbackTest2";
    String aString = null;
  //  RecordThread mRecThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        wavesEnable = true;
        setContentView(R.layout.common_textview);
        Log.e(TAG, "onCreate");
        mContentTv = (TextView) findViewById(R.id.test_content);
        mContentTv.setText(R.string.mic2test);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        //Gionee <xuna><2013-06-03> delete for CR00873055 begin
        /*mRecBuffSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mTrackBuffSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
		//Gionee <Gn_MMI><lifeilong><20161024> modify for 12952 begin
		*//*mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
		AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
		mRecBuffSize);*//*
		//Gionee <GN_MMI><lifeilong><20161024> modify for 12952 end

        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mTrackBuffSize, AudioTrack.MODE_STREAM);
        //Gionee <xuna><2013-06-03> delete for CR00873055 end*/

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
                mRightBtn.setEnabled(true);
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(PhoneLoopbackTest2.this);
                mWrongBtn.setOnClickListener(PhoneLoopbackTest2.this);
                mRestartBtn.setOnClickListener(PhoneLoopbackTest2.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.right_btn: {
                mIsStop = true;
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mIsStop = true;
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }

            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAM.setMode(AudioManager.MODE_NORMAL);
        //Gionee <BP_BSP_MMI> <chengq> <20170328> modify for ID 96660 begin
        //mAM.setParameters("MMIMic=3");
        //Gionee <BP_BSP_MMI> <chengq> <20170330> modify for ID 100222 begin

        //Gionee <GN_BSP_MMI> <chengq> <20170410> modify for ID 104490 begin
        aString = TestUtils.setStreamVoice("lb_mmi_PhoneLoopbackTest2");
        if (aString != null && aString.length() > 0 ) {
            mAM.setParameters("SET_LOOPBACK_TYPE=25,3,"+aString);
        }else {
            mAM.setParameters("SET_LOOPBACK_TYPE=25,3");
        }
        Log.e(TAG, " onStart:Set Paremeter --> SET_LOOPBACK_TYPE=25,3,"+aString);
        //Gionee <GN_BSP_MMI> <chengq> <20170410> modify for ID 104490 end

        //Gionee <BP_BSP_MMI> <chengq> <20170330> modify for ID 100222 end
        //Gionee <BP_BSP_MMI> <chengq> <20170328> modify for ID 96660 end
		/*new Thread(){
			public void run(){
				Log.e(TAG,"onStart == sleep ");
                try {

                Thread.sleep(200);
				mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mRecBuffSize);

                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

			}
		}.start();*/
		//Gionee <Gn_MMI><lifeilong><20161024> modify for 12952  end

        //mAM.setMode(AudioManager.MODE_IN_CALL);//通话模式

       /* new Thread() {
            public void run() {
                aString = TestUtils.setStreamVoice("PhoneLoopbackTest2");
                Log.e(TAG, "run end");
                int i = Integer.valueOf(aString).intValue();
                Log.e(TAG, "i = " + i);
                Log.e(TAG, " onStart set mode --> mode_normal,setParameters MMIMic3=1");
                try {
                    //Gionee zhangke 20151218 add for CR01611271 start
                    Thread.sleep(800);
                    //Gionee zhangke 20151218 add for CR01611271 start
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!mIsRecording) {
                    new RecordThread().start();
                }
                if (null != mAM) {
                    int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    Log.e(TAG, "maxVol = " + maxVol);
                    Log.e(TAG, " use Vol = " + (maxVol - i));
                    mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - i, 0);
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
        //Gionee <BP_BSP_MMI> <chengq> <20170328> modify for ID 96660 begin
        mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
        Log.e(TAG, " onPause setParameters SET_LOOPBACK_TYPE=0,0");
        //Gionee <BP_BSP_MMI> <chengq> <20170328> modify for ID 96660 end
        // Gionee xiaolin 20120613 modify for CR00624109 start
        // Gionee zhangke 20160223 modify for CR01639494 start
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			System.out.println(e);
		}
        //mAM.setParameters("MMIMic=0");
        // Gionee xiaolin 20120613 modify for CR00624109 end
        // mAM.setMode(AudioManager.MODE_NORMAL);
        // Gionee zhangke 20160223 modify for CR01639494 end
        Log.e(TAG, "onPause set mode --> mode_normal");
        //mAM.setMode(AudioManager.MODE_NORMAL);

    }

    /*class RecordThread extends Thread {
        public void run() {
            try {
                byte[] buffer = new byte[mRecBuffSize];
                mRecord.startRecording();
                mTrack.play();
                mIsRecording = true;
                while (false == mIsStop) {
                    int bufferReadResult = mRecord.read(buffer, 0, mRecBuffSize);
                    if (bufferReadResult > 0 && bufferReadResult % 2 == 0) {
                        Log.e("lich", "bufferReadResult = " + bufferReadResult);
                        byte[] tmpBuf = new byte[bufferReadResult];
                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                        mTrack.write(tmpBuf, 0, bufferReadResult);
                    }
                }
                // Gionee xiaolin 20120613 modify for CR00624109 start
                mTrack.stop();
                mRecord.stop();
                mTrack.release();
                mRecord.release();
                // Gionee xiaolin 20120613 modify for CR00624109  end
                mIsRecording = false;
            } catch (Throwable t) {
            }
        }
    }*/

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

}
