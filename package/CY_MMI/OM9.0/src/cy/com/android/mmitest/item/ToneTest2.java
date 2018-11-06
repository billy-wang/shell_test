
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ToneTest2 extends BaseActivity implements OnClickListener {
    Button mToneBt;

    private ToneGenerator mToneGenerator;

    private int TONE_LENGTH_MS = 3000;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "ToneTest2_billy";

    private boolean mIsToneOn;
    private final int duration = 30; //15 seconds

    private final int sampleRate = 8000;

    private final int numSamples = duration * sampleRate;

    private final double sample[] = new double[numSamples];

    private final double freqOfTone = 1000; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    private Handler mHandler = new Handler();

    private AudioTrack mAudioTrack;
    private AudioManager mAM;
    String aString = null;
    //Gionee <GN_BSP_MMI> <chengq> <20170227> modify for ID 74495 begin
    //Gionee zhangke 20160401 add for CR01661121 start
    private boolean mIsPause = false;
    //Gionee zhangke 20160401 add for CR01661121 end
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 start

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开扬声器2 @" + Integer.toHexString(hashCode()));

        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        wavesEnable = true;
        setContentView(R.layout.common_textview);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.tone_note);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAM.setParameters("SetPlayToneR");

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

                mRightBtn.setOnClickListener(ToneTest2.this);
                mWrongBtn.setOnClickListener(ToneTest2.this);
                mRestartBtn.setOnClickListener(ToneTest2.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_MORETIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
    }

    @Override
    protected void onDestroy() {
        DswLog.d(TAG, "\n****************退出扬声器2 @" + Integer.toHexString(hashCode()));
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread() {
            public void run() {
                mAM.setParameters("MMITone=2");
                DswLog.e(TAG, " set setParameters :MMITone=2");

                aString = TestUtils.setStreamVoice("ToneTest2");
                DswLog.e(TAG, "run end");
                int i = Integer.valueOf(aString).intValue();
                DswLog.e(TAG, "i = " + i);
                if (null != mAM) {
                    int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    DswLog.e(TAG, " set stream  = music ");
                    mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - i, 0);
                    DswLog.e(TAG, "maxVol = " + maxVol + " setStreamVolume = " + (maxVol - i));
                }
                DswLog.i(TAG, "thread mIsPause1="+mIsPause);
                if(!mIsPause){
                    genTone();
                }
                DswLog.i(TAG, "thread mIsPause2="+mIsPause);
                if(!mIsPause){
                    mHandler.postDelayed(new Runnable() {

                        public void run() {
                            DswLog.i(TAG, "thread mIsPause3="+mIsPause);
                            if(!mIsPause){
                                playSound();
                            }
                        }
                    },1000);
                }

            }
        }.start();
        //Gionee zhangke 20160401 add for CR01661121 end
    }

    @Override
    public void onPause() {
        super.onPause();
        mAM.setParameters("MMITone=0");
        DswLog.e(TAG, " set setParameters :MMITone=0");
        //Gionee zhangke 20160401 add for CR01661121 start
        mIsPause = true;
        //Gionee zhangke 20160401 add for CR01661121 end
        //Gionee <GN_BSP_MMI> <chengq> <20170516> modify for ID 142248 begin
        try {
            if (null != mAudioTrack) {
                mAudioTrack.stop();
                mAudioTrack.release();
                DswLog.d(TAG, "mAudioTrack.release()");
            }
        }catch (Exception e) {
            DswLog.i(TAG, "ToneTest2 onPause audio is broken");
            e.printStackTrace();
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170516> modify for ID 142248 end

    }

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
        DswLog.d(TAG, "new AudioTrack STREAM_MUSIC");
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);//使用music流，默认从喇叭发出
        //Gionee <GN_BSP_MMI> <chengq> <20170420> modify for ID 102000 begin
        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
        try {
            DswLog.d(TAG, "play");
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
    //Gionee <GN_BSP_MMI> <chengq> <20170227> modify for ID 74495 end

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                //TestUtils.rightPress(TAG, this);
                TestUtils.rightPress("ToneTest2", this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                //TestUtils.wrongPress(TAG, this);
                TestUtils.wrongPress("ToneTest2", this);
                break;
            }

            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                //TestUtils.restart(this, TAG);
                TestUtils.restart(this, "ToneTest2");
                break;
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
