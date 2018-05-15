
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import gn.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ReceiverTest extends BaseActivity implements OnClickListener {
    Button mToneBt;

    private ToneGenerator mToneGenerator;

    private int TONE_LENGTH_MS = 3000;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private AudioManager mAM;

    private static final String TAG = "ReceiverTest";

    private boolean mIsToneOn;
    private final int duration = 30; // seconds

    private final int sampleRate = 8000;

    private final int numSamples = duration * sampleRate;

    private final double sample[] = new double[numSamples];

    private final double freqOfTone = 300; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    private Handler mHandler = new Handler();

    private AudioTrack mAudioTrack;
    String aString = null;
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开听筒 @" + Integer.toHexString(hashCode()));

        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        wavesEnable = true;
        setContentView(R.layout.common_textview);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.receiver_note);
        DswLog.i(TAG, "onCreate");

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
                mIsTimeOver = true;
                if(mIsPass){
                    mRightBtn.setEnabled(true);
                }
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(ReceiverTest.this);
                mWrongBtn.setOnClickListener(ReceiverTest.this);
                mRestartBtn.setOnClickListener(ReceiverTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出听筒 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
        DswLog.i(TAG, "onResume 0");
		// Gionee xiaolin 20121017 modify for CR00715318 start
        mAM.setMode(AudioManager.MODE_NORMAL);
        DswLog.i(TAG, "onResume");
        mAM.setParameters("MMIReceiver=1");
        DswLog.e(TAG, " set setParameters :MMIReceiver=1 ");
        new Thread() {
            public void run() {
                DswLog.i(TAG, "Thread.run begin");
                aString = TestUtils.setStreamVoice("ReceiverTest");
                DswLog.e(TAG, "aString=" + aString);
                int i = Integer.valueOf(aString).intValue();
                DswLog.e(TAG, "i = " + i);
                if (null != mAM) {
                    int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                    //Gionee <GN_BSP_MMI> <chengq> <20170423> modify for ID 113555 begin
                    mAM.setSpeakerphoneOn(false);
                    mAM.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    DswLog.e(TAG, " set mode  = mode MODE_IN_COMMUNICATION) ");
                    

                    mAM.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVol - i, 0);
                    DswLog.e(TAG, "maxVol = " + maxVol + " setStreamVolume = " + (maxVol - i));
                    //Gionee <GN_BSP_MMI> <chengq> <20170423> modify for ID 113555 end
                }
                DswLog.i(TAG, "Thread.run end");
            }
        }.start();
        // Gionee xiaolin 20121017 modify for CR00715318 end

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                mHandler.post(new Runnable() {

                    public void run() {
                        DswLog.i(TAG, "playSound begin");
                        playSound();
                        DswLog.i(TAG, "playSound end");
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Gionee zhangxiaowei 20130905 modify for CR00880893 start
        mAM.setParameters("MMIReceiver=0");
        DswLog.e(TAG, " set setParameters : MMIReceiver=0");
        if (null != mAudioTrack) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mAM.setMode(AudioManager.MODE_NORMAL);
        DswLog.e(TAG, " set mode  = mode normal ");
    }
    //Gionee zhangxiaowei 20130905 modify for CR00880893 end

    void genTone() {
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    void playSound() {
        // Gionee xiaolin 20121017 modify for CR00715318 start
        mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        // Gionee xiaolin 20121017 modify for CR00715318 end
        //Gionee <GN_BSP_MMI> <chengq> <20170420> modify for ID 102000 begin
        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
        try {
            mAudioTrack.play();
            Thread.sleep(50);
        } catch (InterruptedException e) {
            DswLog.i(TAG, "ReceiverTest InterruptedException ");
            e.printStackTrace();
        }catch (IllegalStateException e) {
            DswLog.i(TAG, "ReceiverTest audio is broken");
            e.printStackTrace();
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170420> modify for ID 102000 end
        //Gionee zhangke 20160428 modify for CR01687958 start
        mIsPass = true;
        if(mIsTimeOver){
            mRightBtn.setEnabled(true);
        }
        //Gionee zhangke 20160428 modify for CR01687958 end
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
