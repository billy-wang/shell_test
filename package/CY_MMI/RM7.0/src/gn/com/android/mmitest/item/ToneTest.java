
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
import android.content.Intent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ToneTest extends Activity implements OnClickListener {
    Button mToneBt;

    private ToneGenerator mToneGenerator;

    private int TONE_LENGTH_MS = 3000;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "ToneTest";

    private boolean mIsToneOn;
    private final int duration = 30; // seconds

    private final int sampleRate = 8000;

    private final int numSamples = duration * sampleRate;

    private final double sample[] = new double[numSamples];

    private final double freqOfTone = 1000; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    private Handler mHandler = new Handler();

    private AudioTrack mAudioTrack;
    private AudioManager mAM; 
    String aString = null;
    //Gionee zhangke 20160401 add for CR01661121 start
    private boolean mIsPause = false;
    //Gionee zhangke 20160401 add for CR01661121 end
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean toneFlag = false;
    private Intent it;
    private String dumpKey = "/etc/rt5509_dump.sh";
    private boolean dumpFlag;
    private File record = new File(dumpKey);
    private Process process = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TestUtils.checkToContinue(this);
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.common_textview);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.tone_note);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //Gionee zhangke 20160401 add for CR01661121 start
        Log.i(TAG,"onCreate");
        //mAM.setParameters("SetPlayToneL");//left 
        //Gionee zhangke 20160401 add for CR01661121 end
        it = this.getIntent();
        if(it != null){
            toneFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"toneFlag = " + toneFlag);
        
        if (record.exists()) {
            Log.e(TAG, " chmod 666 /etc/rt5509_dump.sh");
            try{
                Runtime.getRuntime().exec("chmod 666 " + dumpKey);
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
        }
        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        if(toneFlag){
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }

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

                mRightBtn.setOnClickListener(ToneTest.this);
                mWrongBtn.setOnClickListener(ToneTest.this);
                mRestartBtn.setOnClickListener(ToneTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
        dumpFlag = true;
    }
    Thread shThead = new Thread(){
            @Override
            public void run() {
                try{
                    /*while(dumpFlag){
                        String shellResult = "";
                        
                        Log.e(TAG, "shellResult " + shellResult);
                    }*/
                    //String shellResult = shellExec(dumpKey);
                } catch (Exception e) {
                    Log.e(TAG, " e =  " + e.getMessage());
                    e.printStackTrace();
                }
            }
    };

    @Override
    public void onResume() {
        super.onResume();
        //mAM.setParameters("MMITone=1");
        //Gionee zhangke 20160401 add for CR01661121 start
        Log.e(TAG, "onResume set setParameters :MMITone=1");
        new Thread() {      
            public void run() {
                aString = TestUtils.setStreamVoice("ToneTest");
                int i=Integer.valueOf(aString).intValue();
                Log.e(TAG, "i = " + i);
                if (null != mAM) {
                    int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    Log.e(TAG, " set stream  = music ");
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170324> modify for ID 97231 begin
                    mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - i , 0);
                    Log.e(TAG, "maxVol = " + maxVol + " setStreamVolume = " + (maxVol - i) );
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170324> modify for ID 97231 end
                }  
                Log.i(TAG, "thread mIsPause1="+mIsPause);
                if(!mIsPause){
                    genTone();
                }
                Log.i(TAG, "thread mIsPause2="+mIsPause);
                if(!mIsPause){
                    mHandler.post(new Runnable() {                    
                        public void run() {
                            Log.i(TAG, "thread mIsPause3="+mIsPause);
                            if(!mIsPause){
                                playSound();
                            }
                        }
                    });
                }

            }
        }.start();
        //Gionee zhangke 20160401 add for CR01661121 end
        shThead.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170523> modify for ID 147184 begin
        //mAM.setParameters("MMITone=0");
        //Log.e(TAG, " set setParameters :MMITone=0");
         //Gionee <GN_BSP_MMI> <lifeilong> <20170523> modify for ID 147184 end
        //Gionee zhangke 20160401 add for CR01661121 start
        mIsPause = true;
        //Gionee zhangke 20160401 add for CR01661121 end

        if (null != mAudioTrack) {
            mAudioTrack.stop();
            mAudioTrack.release();
            Log.d(TAG, "mAudioTrack.release()");
        }
        
    }

    @Override
    public void onStop() {
        super.onStop();
         if(toneFlag){
            this.finish();
            Log.d(TAG,"onStop as_record_finish_self");
         }
        try{
            dumpFlag = false;
            shThead.interrupt();
        } catch (Exception e) {
            Log.e(TAG, " e =  " + e.getMessage());
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
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);//使用music流，默认从喇叭发出
        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
        mAudioTrack.play();
        Log.d(TAG, "mAudioTrack.play()");
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
                if(toneFlag){
                    TestUtils.asResult(TAG,"","1");
                }
                if(process != null){
                    process.destroy();
                }
                dumpFlag = false;
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(toneFlag){
                    TestUtils.asResult(TAG,"","0");
                }
                if(process != null){
                    process.destroy();
                }
                dumpFlag = false;
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }
            
            case R.id.restart_btn: {
                dumpFlag = false;
                if(process != null){
                    process.destroy();
                }
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }


    public String shellExec(String cmd){
        Log.i("GN_LOG","shellExec "+cmd);
        String result = "";
        String[] args = new String[]{"sh", cmd};
        result = do_exec(args);
        return result;
    }

    public String do_exec(String[] args) {
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read = -1;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            int wait = process.waitFor();
            Log.d(TAG, "  process.waitFor  = " + wait);
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (errIs != null) {
                    errIs.close();
                }
                if (inIs != null) {
                    inIs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (process != null) {
                process.destroy();
            }
        }
        Log.i("GN_LOG","do_exec "+result);
        return result;
    }  

    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        return true;
    }
}
