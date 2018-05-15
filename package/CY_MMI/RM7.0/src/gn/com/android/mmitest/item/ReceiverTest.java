
package gn.com.android.mmitest.item;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ReceiverTest extends Activity implements OnClickListener {
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
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.common_textview);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.receiver_note);
        Log.i(TAG,"onCreate");

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
    public void onResume() {
        super.onResume();
        Log.i(TAG,"onResume 0");
        // Gionee xiaolin 20121017 modify for CR00715318 start
        mAM.setMode(AudioManager.MODE_NORMAL);
        Log.i(TAG,"onResume 1");
        //mAM.setParameters("MMIReceiver=1");
        Log.i(TAG,"onResume 2");
        new Thread() {
            public void run() {
                Log.i(TAG,"Thread.run begin");
                aString = TestUtils.setStreamVoice("ReceiverTest");
                Log.e(TAG, "aString="+aString);
                int i = Integer.valueOf(aString).intValue();
                Log.e(TAG, "i = " + i);
                if (null != mAM) {
                    int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                    Log.e(TAG, " set mode  = mode STREAM_VOICE_CALL , MODE_IN_COMMUNICATION ");
                    Log.e(TAG, " set stream  = STREAM_VOICE_CALL ");
                    mAM.setSpeakerphoneOn(false);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170525> modify for ID 147507 143964 begin
                    mAM.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170525> modify for ID 147507 143964 end
                    mAM.setStreamVolume(AudioManager.STREAM_VOICE_CALL, (maxVol - i), 0);
                    int max = mAM.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL );
                    int current = mAM.getStreamVolume(AudioManager.STREAM_VOICE_CALL );
                    Log.e(TAG, "maxVol = " + maxVol + " setStreamVolume = " + (maxVol -i));
                    Log.e(TAG, "max = " + max + " current = " + current);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 90352 end
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170316> modify for ID 86338 end
                }
                Log.i(TAG,"Thread.run end");
            }
        }.start();
        // Gionee xiaolin 20121017 modify for CR00715318 end

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                mHandler.post(new Runnable() {
                    public void run() {
                        Log.i(TAG,"playSound begin");
                        playSound();
                        Log.i(TAG,"playSound end");
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
        //mAM.setParameters("MMIReceiver=0");
        //Log.e(TAG, " set setParameters : MMIReceiver=0");
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
        Log.e(TAG, " set mode  = mode normal ");
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
        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
        mAudioTrack.play();
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
