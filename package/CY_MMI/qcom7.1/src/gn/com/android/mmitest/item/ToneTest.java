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

public class ToneTest extends Activity implements OnClickListener {
    Button mToneBt;

    private ToneGenerator mToneGenerator;

    private int TONE_LENGTH_MS = 3000;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "ToneTest";

    private boolean mIsToneOn;
    private final int duration = 15; // seconds

    private final int sampleRate = 8000;

    private final int numSamples = duration * sampleRate;

    private final double sample[] = new double[numSamples];

    private final double freqOfTone = 1000; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    private Handler mHandler = new Handler();

    private AudioTrack mAudioTrack;
    private AudioManager mAM;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.common_textview);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.tone_note);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAM.setParameters("MMITone=1");
        Log.e(TAG, " set setParameters :MMITone=1");
        if (null != mAM) {
            int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            String aString = TestUtils.setStreamVoice("ToneTest");
            int i=Integer.valueOf(aString).intValue();
            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - i, 0);
            Log.e(TAG, "maxVol = " + maxVol + " setStreamVolume = " + (maxVol - i));
        }
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                genTone();
                mHandler.post(new Runnable() {

                    public void run() {
                        playSound();
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mAM.setParameters("MMITone=0");
        Log.e(TAG, " set setParameters :MMITone=0");
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
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples, AudioTrack.MODE_STATIC);
        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
        mAudioTrack.play();
        mRightBtn.setEnabled(true);
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
