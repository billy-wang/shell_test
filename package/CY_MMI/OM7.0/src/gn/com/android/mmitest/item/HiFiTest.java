
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;

import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebView.PrivateAccess;
import android.widget.Button;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.Color;

import gn.com.android.mmitest.BaseActivity;

public class HiFiTest extends BaseActivity implements OnClickListener {

    private boolean mIsRecording, mIsStop, mIsGetState;
    private TextView mContentTv, mTitleTv;
    private AudioRecord mRecord;

    private AudioTrack mTrack;

    private AudioManager mAM;

    private int mRecBuffSize, mTrackBuffSize;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static String TAG = "HiFiTest";

    private RecordThread mRecThread;
    private Handler mLpHander;
    String aString = null;
    private int i, maxVol;
    private int hifiState;
    private boolean hifi;
    private EarphonePluginReceiver mEarphonePluginReceiver;

    private int mPluginState = 0;//0,HEADSET_UNPLUG; 1, plug

    private class EarphonePluginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                mPluginState = intent.getIntExtra("state", 0);
                if (0 == mPluginState) {
                    mContentTv.setText(R.string.insert_earphone);
                    mContentTv.setTextColor(Color.RED);
                }
                if (1 == mPluginState) {
                    mContentTv.setText(R.string.inserted_earphone);
                    mContentTv.setTextColor(Color.YELLOW);
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开HIFI @" + Integer.toHexString(hashCode()));

        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.common_textview);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mContentTv.setGravity(Gravity.CENTER);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mTitleTv.setText(R.string.headsethook_note);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRecBuffSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mTrackBuffSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mRecBuffSize);
        mTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                mTrackBuffSize, AudioTrack.MODE_STREAM);
        mEarphonePluginReceiver = new EarphonePluginReceiver();

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
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(HiFiTest.this);
                mWrongBtn.setOnClickListener(HiFiTest.this);
                mRestartBtn.setOnClickListener(HiFiTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出HIFI @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.right_btn: {
                cleanState();
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                cleanState();
                TestUtils.wrongPress(TAG, this);
                break;
            }

            case R.id.restart_btn: {
                cleanState();
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }

    private void cleanState() {
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);
        mIsStop = true;
        try {
            mTrack.stop();
            mRecord.stop();
            mTrack.release();
            mRecord.release();
        } catch (IllegalStateException ex) {

        }
        mAM.setMode(AudioManager.MODE_NORMAL);
        //turn off HIFI
        if (hifi) {
            TestUtils.openOrcloseHifi(this, false);
        }

        DswLog.e(TAG, " onstop   set mode -->mode_normal ,setParameters HIFI_SWITCH=0");
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(mEarphonePluginReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        if (null != mAM) {
            new Thread() {
                public void run() {
                    maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    aString = TestUtils.setStreamVoice("EarphoneLoopbackTest");
                    i = Integer.valueOf(aString).intValue();
                    DswLog.e(TAG, "i = " + i);
                }
            }.start();
            //turn on HIFI
            hifiState = TestUtils.getHifiState(this);
            if (hifiState == 0) {
                TestUtils.openOrcloseHifi(this, true);
                hifi = true;
            }

            DswLog.e(TAG, " onStart  no set mode --> ,setParameters HIFI_SWITCH=1");
        }
        mIsRecording = false;
        DswLog.d(TAG, "---onStart---");
    }

    class RecordThread extends Thread {
        public void run() {
            try {
                byte[] buffer = new byte[mRecBuffSize];
                mRecord.startRecording();
                mTrack.play();
                DswLog.e(TAG, "mPluginState = " + mPluginState);
                while (false == mIsStop) {
                    if (mPluginState == 1)// do it only when headset plug
                    {
                        int bufferReadResult = mRecord.read(buffer, 0, mRecBuffSize);
                        if (bufferReadResult > 0 && bufferReadResult % 2 == 0) {
                            DswLog.e(TAG, " zhangxaiowei bufferReadResult = " + bufferReadResult);
                            byte[] tmpBuf = new byte[bufferReadResult];
                            System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                            mTrack.write(tmpBuf, 0, bufferReadResult);
                        }
                    }
                }

                mIsRecording = false;
                // mAM.setMode(AudioManager.MODE_NORMAL);
            } catch (Throwable t) {
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == event.ACTION_DOWN) {
            DswLog.d(TAG, "KEYCOD = " + event.getKeyCode());
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_CALL:
                case KeyEvent.KEYCODE_ENDCALL:
                case KeyEvent.KEYCODE_HEADSETHOOK:

                    mTitleTv.setText(R.string.headsethook_press);
                    mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - i, 0);
                    DswLog.e(TAG, " set stream  = music ");
                    DswLog.e(TAG, "maxVol = " + maxVol + " setStreamVolume = " + (maxVol - i));
                    ;

                    mRightBtn.setEnabled(true);
                    if (!mIsRecording) {
                        DswLog.d(TAG, "--- RecordThread weiwei onStart---");
                        new RecordThread().start();
                        mIsRecording = true;
                    }
                    break;
            }
        }
        return true;
    }

}
