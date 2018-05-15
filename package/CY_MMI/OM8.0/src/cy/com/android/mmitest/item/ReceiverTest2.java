
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;
import cy.com.android.mmitest.BaseActivity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import cy.com.android.mmitest.utils.DswLog;
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
import cy.com.android.mmitest.item.EmSensor.EmSensor;
import java.util.Timer;
import java.util.TimerTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import android.os.Message;
import java.io.FileNotFoundException;
import android.os.ServiceManager;

//Gionee <GN_BSP_MMI> <chengq> <20170214> modify for ID 68645 begin
public class ReceiverTest2 extends BaseActivity implements OnClickListener {
//Gionee <GN_BSP_MMI> <chengq> <20170214> modify for ID 68645 end
    Button mToneBt;

    private ToneGenerator mToneGenerator;

    private int TONE_LENGTH_MS = 3000;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private AudioManager mAM;

    private static final String TAG = "ReceiverTest2_billy";

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
        DswLog.d(TAG, "\n\n\n****************打开听筒2 @" + Integer.toHexString(hashCode()));

        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        wavesEnable = true;
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.receiver2_proximity);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
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
        if (isFileExisted("/sys/bus/platform/drivers/als_ps/high_threshold")) {
            calibratePs();
        }
    }

    @Override
    protected void onDestroy() {
        DswLog.d(TAG, "\n****************退出听筒2 @" + Integer.toHexString(hashCode()));
        super.onDestroy();
    }

    private void calibratePs() {
        IBinder binder = null;
        binder = ServiceManager.getService("NvRAMBackupAgent");
        if (binder == null) {
            DswLog.e(TAG, "binder is  NULL");
            return;
        }

        try {
            NvRAMBackupAgent agent = null;
            agent = NvRAMBackupAgent.Stub.asInterface(binder);
            int[] calData = null;
            calData = agent.readFile();
            DswLog.e(TAG, "calibrate  result is  =  " + shouldSaveToNv(calData));
            if (shouldSaveToNv(calData)) {
                /*Gionee huangjianqiang 20160606 add modify for CR01675907 begin */
                mIsCalSuccess = true;
                /*Gionee huangjianqiang 20160606 add modify for CR01675907 end */
                // GIONEE removed for GBL7320 begin
                // In some case, SN is lost. So don't backup NV too frequently
                /**
                 agent.writeFile(calData);
                 agent.backupFile();
                 */
                // GIONEE removed for GBL7320 end
                DswLog.v(TAG, "Test!!! don't backup PSensor cal data!");
                DswLog.e(TAG, "backup is ok  and  calibrate is success! ");
            } else {
                showDialog(CAL_FAIL);
            }
        } catch (RemoteException re) {
            DswLog.e(TAG, re.toString());
        }
    }

    private boolean shouldSaveToNv(int[] data) {
        if (data == null || data.length != 3 || data[2] == 0)
            return false;
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        testReceiver2();
        testPSensor();
    }

    private void testReceiver2() {
	/* disable by Billy.Wang */
        //DswLog.d(TAG, "setMode AudioManager.MODE_NORMAL");
        //mAM.setMode(AudioManager.MODE_NORMAL);

        aString = TestUtils.setStreamVoice("ReceiverTest2");
        int i = Integer.valueOf(aString).intValue();
        DswLog.d(TAG, "i = " + i);
        if (null != mAM) {
            int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
	    /* disable by Billy.Wang */
            //mAM.setSpeakerphoneOn(false);
            //mAM.setMode(AudioManager.MODE_IN_COMMUNICATION);
            //DswLog.d(TAG, "set mode  = mode MODE_IN_COMMUNICATION");
            DswLog.d(TAG, "STREAM_VOICE_CALL maxVol = " + maxVol + " setStreamVolume = " + (maxVol - i));
            mAM.setStreamVolume(AudioManager.STREAM_VOICE_CALL, maxVol - i, 0);
       //Gionee <GN_BSP_MMI> <chengq> <20170423> modify for ID 113555 end
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
                if(mIsCalSuccess != true) {
                    if (!mIsStoped) {
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_CALING_DIALOG);
                    }
                    int result = mEmSensor.doPsensorCalibration();
                    DswLog.i(TAG, "Thread doPsensorCalibration=" + result);
                    if (result == EmSensor.RET_ERROR && !mIsStoped) {
                        mHandler.sendEmptyMessage(MESSAGE_SHOW_FAIL_DIALOG);
                    } else {
                        mIsCalSuccess = true;
                        mHandler.sendEmptyMessage(MESSAGE_REMOVE_CAL_ING_DIALOG);
                    }
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
            DswLog.e(TAG, "ProximityNum = " + i);
            mIsClose = (i == 0 ? true : false);
            // Gionee xiaolin 20120227 modify for CR00534606 start
            if (i != 0) {
                mFarTag = true;
            }
            // Gionee zhangke 20160419 add for CR01680501 start
            DswLog.i(TAG, "mIsClose=" + mIsClose + ";mFarTag=" + mFarTag + ";mIsCalSuccess=" + mIsCalSuccess + mIsTimeOver
                    + ";mIsAudioPass=" + mIsAudioPass);
            if (true == mIsClose && mFarTag && mIsCalSuccess && mIsAudioPass) {
                mIsPass = true;

                mRightBtn.setEnabled(true);
            }
            if (mIsPass && mIsTimeOver) {
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

        if (null != mAudioTrack) {
            DswLog.d(TAG, "release");
            mAudioTrack.stop();
            mAudioTrack.release();
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DswLog.d(TAG, "B set mode  = mode normal ");
        mAM.setMode(AudioManager.MODE_NORMAL);
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
	
        /* add by Billy.Wang */
        DswLog.d(TAG, "setMode AudioManager.MODE_IN_CALL");
        mAM.setMode(AudioManager.MODE_IN_CALL);

        DswLog.d(TAG, "new AudioTrack");
        mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples, AudioTrack.MODE_STATIC);
        //Gionee <GN_BSP_MMI> <chengq> <20170420> modify for ID 102000 begin
        mAudioTrack.write(generatedSnd, 0, generatedSnd.length);
        try {
            Thread.sleep(50);
            DswLog.d(TAG, "play");
            mAudioTrack.play();
        } catch (InterruptedException e) {
            DswLog.i(TAG, "ReceiverTest InterruptedException ");
            e.printStackTrace();
        }catch (IllegalStateException e) {
            DswLog.i(TAG, "ReceiverTest audio is broken");
            e.printStackTrace();
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170420> modify for ID 102000 end
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
            //TestUtils.rightPress(TAG, this);
            TestUtils.rightPress("ReceiverTest2", this);
            break;
        }

        case R.id.wrong_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            //TestUtils.wrongPress(TAG, this);
            TestUtils.wrongPress("ReceiverTest2", this);
            break;
        }

        case R.id.restart_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            //TestUtils.restart(this, TAG);
            TestUtils.restart(this, "ReceiverTest2");
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

            //DswLog.e(TAG, "disnum = " + distanceValue);
            //DswLog.e(TAG, "colse = " + highThreshold);
            //DswLog.e(TAG, "far = " + lowThreshold);
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
        //DswLog.d(TAG, "rightDistanse=" + rightDistanse);
        return rightDistanse;
    }

    boolean isFileExisted(String fileName) {
        File file = new File(fileName);
        if (file != null && file.exists())
            return true;

        //DswLog.v(TAG, fileName + "isn't existed!");
        return false;
    }

}
