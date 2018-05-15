
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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.AlertDialog;
import android.app.Dialog;
import java.io.IOException;
import java.io.InputStreamReader;
import gn.com.android.mmitest.item.EmSensor.EmSensor;
import java.util.Timer;
import java.util.TimerTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import android.os.Message;
import java.io.FileNotFoundException;

public class ReceiverTest2 extends Activity implements OnClickListener {
    Button mToneBt;

    private ToneGenerator mToneGenerator;

    private int TONE_LENGTH_MS = 3000;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private AudioManager mAM;

    private static final String TAG = "ReceiverTest2";

    private boolean mIsToneOn;
    private final int duration = 30; // seconds

    private final int sampleRate = 8000;

    private final int numSamples = duration * sampleRate;

    private final double sample[] = new double[numSamples];

    private final double freqOfTone = 500; // hz

    private final byte generatedSnd[] = new byte[2 * numSamples];

    private AudioTrack mAudioTrack;
    String aString = null;
    // Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    // Gionee zhangke 20160428 modify for CR01687958 start
    private TextView mProximityNum, mCrntDistanse;
    EmSensor mEmSensor = null;
    private SensorManager mSensorMgr;
    private Sensor mPSensor;
    private Sensor mDisSensor;
    private Timer mTimer;
    private boolean mDisSensorRight;
    private boolean mIsProximityRight;
    private boolean mIsCalSuccess;
    private boolean mIsFar;
    private boolean mIsClose;
    private boolean mFarTag = false;
    private boolean mIsStoped = false;
    private boolean mIsAudioPass = false;
    private static final int CAL_FAIL = 0;
    private static final int CAL_SUCCESS = 1;
    private static final int CAL_ING = 2;

    private static final int MESSAGE_SHOW_FAIL_DIALOG = 0;
    private static final int MESSAGE_SHOW_CALING_DIALOG = 1;
    private static final int MESSAGE_REMOVE_CAL_ING_DIALOG = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.receiver2_proximity);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setVisibility(View.INVISIBLE);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);
        mProximityNum = (TextView) findViewById(R.id.proximity_num);
        mCrntDistanse = (TextView) findViewById(R.id.crnt_distanse_num);
        mSensorMgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        mEmSensor = EmSensor.getInstance(this);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mIsTimeOver = true;
                if (mIsPass) {
                    mRightBtn.setEnabled(true);
                }
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(ReceiverTest2.this);
                mWrongBtn.setOnClickListener(ReceiverTest2.this);
                mRestartBtn.setOnClickListener(ReceiverTest2.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        // Gionee zhangke 20160428 modify for CR01687958 end

    }

    @Override
    public void onResume() {
        super.onResume();
        testReceiver2();
        testPSensor();
    }

    private void testReceiver2() {
        mAM.setMode(AudioManager.MODE_NORMAL);
        //mAM.setParameters("MMIReceiver=2");

        aString = TestUtils.setStreamVoice("ReceiverTest2");
        int i = Integer.valueOf(aString).intValue();
        Log.e(TAG, "i = " + i);
        if (null != mAM) {
            //Gionee <GN_BSP_MMI> <lifeilong> <20170316> modify for ID 86338 begin
            int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            mAM.setSpeakerphoneOn(false);
            //Gionee <GN_BSP_MMI> <lifeilong> <20170525> modify for ID 147507 143964 begin
            mAM.setMode(AudioManager.MODE_IN_COMMUNICATION);
            //Gionee <GN_BSP_MMI> <lifeilong> <20170525> modify for ID 147507 143964 end
            mAM.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVol - i, 0);
            Log.e(TAG, "maxVol = " + maxVol + " setStreamVolume = " + (maxVol - i));
            //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 90618 end
            //Gionee <GN_BSP_MMI> <lifeilong> <20170316> modify for ID 86338 end
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

    private void testPSensor() {
        mPSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mPSensor != null) {
            mIsProximityRight = mSensorMgr.registerListener(mProximityListener, mPSensor,
                    SensorManager.SENSOR_DELAY_FASTEST);
            if (false == mIsProximityRight) {
                try {
                    Thread.sleep(300);
                    mIsProximityRight = mSensorMgr.registerListener(mProximityListener, mPSensor,
                            SensorManager.SENSOR_DELAY_FASTEST);
                } catch (InterruptedException e) {

                }
                if (false == mIsProximityRight) {
                    mProximityNum.setText(R.string.init_proximity_sensor_fail);
                }
            }
        }
        // Gionee zhangke 20151225 modify for CR01613440 start
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (!mIsStoped) {
                    mHandler.sendEmptyMessage(MESSAGE_SHOW_CALING_DIALOG);
                }
                int result = mEmSensor.doPsensorCalibration();
                Log.i(TAG, "Thread doPsensorCalibration=" + result);
                if(FeatureOption.GN_RW_GN_MMI_SKIP_PSENSOR_SUPPORT){
                    try{
                        Log.d(TAG,"GN_RW_GN_MMI_SKIP_PSENSOR_SUPPORT  == true ");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e(TAG," e = " + e.getMessage());
                        e.printStackTrace(); 
                    }
                    result = EmSensor.RET_SUCCESS;
                }
                if (result == EmSensor.RET_ERROR && !mIsStoped) {
                    mHandler.sendEmptyMessage(MESSAGE_SHOW_FAIL_DIALOG);
                } else {
                    mIsCalSuccess = true;
                    mHandler.sendEmptyMessage(MESSAGE_REMOVE_CAL_ING_DIALOG);
                }
            }
        }).start();
        // Gionee zhangke 20151225 modify for CR01613440 end
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        readPsValue2();
                    }
                });
            }
        }, 0, 100);

    }

    SensorEventListener mProximityListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            int i = (int) event.values[0];
            //Log.e(TAG, "ProximityNum = " + i);
            mIsClose = (i == 0 ? true : false);
            // Gionee xiaolin 20120227 modify for CR00534606 start
            if (i != 0) {
                mFarTag = true;
            }
            // Gionee zhangke 20160419 add for CR01680501 start
            Log.i(TAG, "mIsClose=" + mIsClose + ";mFarTag=" + mFarTag + ";mIsCalSuccess=" + mIsCalSuccess + mIsTimeOver
                    + ";mIsAudioPass=" + mIsAudioPass);
            if (true == mIsClose && mFarTag && mIsCalSuccess && mIsAudioPass) {
                mIsPass = true;
                mRightBtn.setVisibility(View.VISIBLE);
                mRightBtn.setEnabled(true);
            }
            if (mIsPass && mIsTimeOver) {
                mRightBtn.setVisibility(View.VISIBLE);
                mRightBtn.setEnabled(true);
            }

            if (0 != i)
                i = 1;
            mProximityNum.setText(Integer.toString(i));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    };

    @Override
    public void onPause() {
        super.onPause();
        // Gionee zhangxiaowei 20130905 modify for CR00880893 start
        mIsStoped = true;
        mHandler.removeMessages(MESSAGE_SHOW_FAIL_DIALOG);
        if (true == mIsProximityRight) {
            mSensorMgr.unregisterListener(mProximityListener);
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

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
        mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples, AudioTrack.MODE_STATIC);
        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
        mAudioTrack.play();
        mIsAudioPass = true;
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

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SHOW_FAIL_DIALOG:
                removeDialog(CAL_ING);
                showDialog(CAL_FAIL);
                break;
            case MESSAGE_SHOW_CALING_DIALOG:
                showDialog(CAL_ING);
                break;
            case MESSAGE_REMOVE_CAL_ING_DIALOG:
                removeDialog(CAL_ING);
                break;
            }
        }
    };

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case CAL_FAIL:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Gionee zhangke 20160419 modify for CR01680501 start
            builder.setMessage(getString(R.string.psensor_cal_fail)).setCancelable(false).setPositiveButton("ok", null);
            // Gionee zhangke 20160419 modify for CR01680501 end
            dialog = builder.create();
            break;
        case CAL_ING:
            AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
            builder2.setMessage(R.string.psensor_caling).setCancelable(false).setPositiveButton(R.string.acceler_caling,
                    null);
            dialog = builder2.create();
            break;
        }
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
        getWindow().getDecorView().getSystemUiVisibility());       
            
        return dialog;
    }

    private String readPsValue2() {
        String highThreshold, lowThreshold, distanceValue;
        try {
            String fileName = "/sys/bus/platform/drivers/als_ps/high_threshold";
            if (isFileExisted(fileName)) {
                highThreshold = getRightDistanse(fileName);// \u93ba\u30e8\u7e4e\u95c3\u20ac\u934a?
            } else {
                highThreshold = Integer.toString(mEmSensor.getPsensorHighThreshold());
            }
            fileName = "/sys/bus/platform/drivers/als_ps/low_threshold";
            if (isFileExisted(fileName)) {
                lowThreshold = getRightDistanse(fileName);// \u6769\u6ec5\ue787\u95c3\u20ac\u934a?
            } else {
                lowThreshold = Integer.toString(mEmSensor.getPsensorLowThreshold());
            }
            fileName = "/sys/bus/platform/drivers/als_ps/pdata";
            if (isFileExisted("/sys/bus/platform/drivers/als_ps/pdata")) {
                distanceValue = getRightDistanse(fileName);// p-sensor\u7039\u70b4\u6902\u7035\u52eb\u74e8\u9363\u3125\u20ac?
            } else {
                distanceValue = Integer.toString(mEmSensor.getPsensorData());
            }

            //Log.e(TAG, "disnum = " + distanceValue);
            //Log.e(TAG, "colse = " + highThreshold);
            //Log.e(TAG, "far = " + lowThreshold);
            mCrntDistanse.setText(distanceValue + ", " + highThreshold + ", " + lowThreshold);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return distanceValue;
    }

    public String getRightDistanse(String fileName) {
        String rightDistanse = null;
        String mFileName = null;
        mFileName = fileName;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File voltageFilePath = new File(mFileName);
                if (voltageFilePath.exists()) {
                    fileInputStream = new FileInputStream(voltageFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        rightDistanse = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "rightDistanse=" + rightDistanse);
        return rightDistanse;
    }

    boolean isFileExisted(String fileName) {
        File file = new File(fileName);
        if (file != null && file.exists())
            return true;

        //Log.v(TAG, fileName + "isn't existed!");
        return false;
    }

}
