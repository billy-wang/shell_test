
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;

import java.util.concurrent.atomic.AtomicBoolean;

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
import gn.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class PhoneLoopbackTest extends BaseActivity implements OnClickListener {
    private static String TAG = "PhoneLoopbackTest";
    String aString = null;

    Button mRightBtn, mWrongBtn, mRestartBtn;
    TextView mContentTv;

    AudioManager mAM;
    //RecordThread mRecThread;
    AtomicBoolean mIsRunning = new AtomicBoolean(false);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开音频回路 @" + Integer.toHexString(hashCode()));

        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        wavesEnable = true;
        setContentView(R.layout.common_textview);

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

                mRightBtn.setOnClickListener(PhoneLoopbackTest.this);
                mWrongBtn.setOnClickListener(PhoneLoopbackTest.this);
                mRestartBtn.setOnClickListener(PhoneLoopbackTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

        TextView recordTitle = (TextView) findViewById(R.id.test_title);

        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //mRecThread = new RecordThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出音频回路 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_btn: {
                mIsRunning.set(false);
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mIsRunning.set(false);
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
    //Gionee <GN_BSP_MMI> <chengq> <20170325> modify for ID 93627 begin
    // Gionee xiaolin 20120827 modify for CR00681574 start
    @Override
    public void onStart() {
        super.onStart();
        //mAM.setMode(AudioManager.MODE_IN_CALL);//在通话模式，
        mAM.setMode(AudioManager.MODE_NORMAL);
        //mAM.setParameters("MMIMic=1");
        //Gionee <GN_BSP_MMI> <chengq> <20170410> modify for ID 104490 begin
        aString = TestUtils.setStreamVoice("lb_mmi_PhoneLoopbackTest");
        if (aString != null && aString.length() > 0 ) {
            mAM.setParameters("SET_LOOPBACK_TYPE=21,1,"+aString);
        }else {
            mAM.setParameters("SET_LOOPBACK_TYPE=21,1");
        }
        DswLog.e(TAG, " onStart:Set Paremeter --> SET_LOOPBACK_TYPE=21,1,"+aString);
        //Gionee <GN_BSP_MMI> <chengq> <20170410> modify for ID 104490 end

        //Gionee zhangke 20151015 add for CR01568781 start
        mIsRunning.set(true);
        //Gionee zhangke 20151015 add for CR01568781 end
        /*new Thread() {
            public void run() {
                aString = TestUtils.setStreamVoice("PhoneLoopbackTest");
                int i = Integer.valueOf(aString).intValue();
                DswLog.e(TAG, "i = " + i);
                DswLog.e(TAG, "set mode --> mode_normal,setParameters SET_LOOPBACK_TYPE=21,1");
                *//*try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (null != mAM) {
                    int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    DswLog.e(TAG, "maxVol = " + maxVol);
                    DswLog.e(TAG, "use Vol = " + (maxVol - i));
                    mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - i, 0);
                }
                if (mRecThread != null && mRecThread.getState() == State.NEW) {
                    DswLog.v(TAG, "start RecordThread");
                    mRecThread.start();
                }*//*
                DswLog.e(TAG, "run end");
            }
        }.start();*/
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170325> modify for ID 93627 end

    @Override
    public void onPause() {
        super.onPause();
        mIsRunning.set(false);
        mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
        DswLog.e(TAG, " onPause setParameters SET_LOOPBACK_TYPE=0,0");
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
        DswLog.e(TAG, "onPause set mode --> mode_normal");
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

   /* class RecordThread extends Thread {
        AudioRecord mRecord;
        AudioTrack mTrack;
        int mRecBuffSize, mTrackBuffSize = -1;

        public RecordThread() {
            //Gionee <xuna><2013-06-03> delete for CR00873055 begin
            mRecBuffSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            mTrackBuffSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            //Gionee <xuna><2013-06-03> delete for CR00873055 end
            mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    mRecBuffSize);
            mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    mTrackBuffSize, AudioTrack.MODE_STREAM);
        }

        public void run() {
            try {
                DswLog.v(TAG, "Record thread beging running");
                //Gionee zhangke 20151015 delete for CR01568781 start
                //mIsRunning.set(true);
                //Gionee zhangke 20151015 delete for CR01568781 end
                //mRecord.startRecording();
                //mTrack.play();
                byte[] buffer = new byte[mRecBuffSize];
                while (mIsRunning.get()) {
                    int bufferReadResult = mRecord.read(buffer, 0, mRecBuffSize);
                    if (bufferReadResult > 0 && bufferReadResult % 2 == 0) {
                        DswLog.e(TAG, "bufferReadResult = " + bufferReadResult);
                        byte[] tmpBuf = new byte[bufferReadResult];
                        System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                        mTrack.write(tmpBuf, 0, bufferReadResult);
                    }
                }
            } catch (Throwable t) {
                DswLog.v(TAG, DswLog.getStackTraceString(t));
            } finally {
                // Gionee xiaolin 20120613 modify for CR00624109 start
                //Gionee zhangke 20151218 add for CR01611271 start
               // mTrack.stop();
                //mRecord.stop();
                //Gionee zhangke 20151218 add for CR01611271 end
                //mTrack.release();
                //mRecord.release();

                // Gionee zhangke 20160223 modify for CR01639494 start
                mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
                mAM.setMode(AudioManager.MODE_NORMAL);
                // Gionee zhangke 20160223 modify for CR01639494 end
                // Gionee xiaolin 20120613 modify for CR00624109  end
            }
        }
    }*/
}
