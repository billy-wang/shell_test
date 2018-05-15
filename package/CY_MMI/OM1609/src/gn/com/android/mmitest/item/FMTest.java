
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.fmradio.IFmRadioService;


public class FMTest extends BaseActivity implements SeekBar.OnSeekBarChangeListener,
        View.OnClickListener {
    private SeekBar mSeekBar;

    private Button mMinusBtn, mPlusBtn, mDefaultBtn, mRightBtn, mWrongBtn;

    private TextView mCurrentHz, mEarphoneTv;

    private String TAG = "FMTest";

    private IFmRadioService mService;
    /*Gionee huangjianqiang 20160624 add for CR01722202 begin*/
    private com.mediatek.fmradio.IFmRadioService mBLUService;
    /*Gionee huangjianqiang 20160624 add for CR01722202 end*/

    public static final int HIGHEST_STATION = 1080;

    public static final int LOWEST_STATION = 875, EVENT_RESPONSE_SN_WRITE = 0;

    private int miCurrentStation;

    private ServiceConnection mConnection;

    private boolean mIsPlud;

    private boolean mIsBind;

    private AudioManager mAM;

    private Intent intent;
    //gionee cuijiuyu 20120919 add for CR00696512 start
    float frequency = 107.5f;
    //gionee cuijiuyu 20120919 add for CR00696512 end

    private Handler mHandler = new Handler();

    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 start
    /*Gionee huangjianqiang 20160419 add CR01679960 for begin*/
    private boolean mIsPluginWithMic;
    private boolean mIsPluginWithoutMic;
    /*Gionee huangjianqiang 20160419 add for CR01679960 end*/

    /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
    private final float defaultFrequency = ((TestUtils.mIsForSale) && (TestUtils.mIsSKFlag)) ? 93.5f :
            (((TestUtils.mIsForSale) && (TestUtils.mIsGnIndia)) ? 91.1f : 107.5f);
    /*Gionee huangjianqiang 20160616 add for CR01715226 end*/

    private class RemoteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {

            mService = IFmRadioService.Stub.asInterface(service);
            if (null == mService) {
                Log.e(TAG, "Error: null interface");
            } else {
                // Gionee zhangke 20160224 modify for CR01640212 start
                // Gionee xiaolin 20120711 modify for CR00643088 start
                Log.i(TAG, "onServiceConnected start powerup thread");
                new Thread() {
                    public void run() {
                        try {
                            /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
                            mService.initService((int) (getCurrFrequencyBySeekBar()*10));
                            /*Gionee huangjianqiang 20160624 add for CR01722202 begin*/
                            if (true == mService.isDeviceOpen() || true == mService.openDevice()) {
                                if (true == mService.isPowerUp() || true == mService.powerUp(defaultFrequency)) {
                                    mHandler.post(new Runnable() {
                                        public void run() {
                                            try {
                                                mService.tune(defaultFrequency);
                            /*Gionee huangjianqiang 20160616 add for CR01715226 end*/
                                                enableWidget(true);
                                                Log.e(TAG, "fm power up success");
                                            } catch (Exception e) {
                                                Log.e(TAG, "mService.tune:Exception=" + e.getMessage());
                                            }
                                        }
                                    });
                                } else {
                                    Log.e(TAG, "fm power up fail");
                                }
                            } else {
                                Log.e(TAG, "fm device open fail");
                            }
                            /*Gionee huangjianqiang 20160624 add for CR01722202 end*/
                        } catch (Exception e) {
                            Log.e(TAG, "mService:Exception=" + e.getMessage());
                        }
                    }
                }.start();
                // Gionee xiaolin 20120711 modify for CR00643088 end
                // Gionee zhangke 20160224 modify for CR01640212 end
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120921 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120921 add for CR00693542 end
        wavesEnable = true;
        setContentView(R.layout.fm_test);

        mDefaultBtn = (Button) findViewById(R.id.default_btn);
        mCurrentHz = (TextView) findViewById(R.id.current_hz);
        mEarphoneTv = (TextView) findViewById(R.id.earphone_note);
        // Gionee xiaolin 20121106 add for CR00725238 start
        mEarphoneTv.setTextColor(Color.RED);
        // Gionee xiaolin 20121106 add for CR00725238 end

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setIndeterminate(false);
        mSeekBar.setMax(210);
        /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
        mSeekBar.setProgress(setSeekBarByFrequency(defaultFrequency));
        /*Gionee huangjianqiang 20160616 add for CR01715226 end*/
        mMinusBtn = (Button) findViewById(R.id.minus_btn);
        mPlusBtn = (Button) findViewById(R.id.plus_btn);

        mSeekBar.setEnabled(false);
        mMinusBtn.setEnabled(false);
        mPlusBtn.setEnabled(false);
        mDefaultBtn.setEnabled(false);
        /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
        mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(getCurrFrequencyBySeekBar())));
        /*Gionee huangjianqiang 20160616 add for CR01715226 end*/
        mMinusBtn.setOnClickListener(this);
        mPlusBtn.setOnClickListener(this);
        mDefaultBtn.setOnClickListener(this);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Gionee xiaolin 20120107 add for CR00742652 start
        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, 11, 0);
        // Gionee xiaolin 20120107 add for CR00742652 end

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);

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

                mRightBtn.setOnClickListener(FMTest.this);
                mWrongBtn.setOnClickListener(FMTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
    }

    @Override
    public void onStart() {
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);
        super.onStart();
        if(TestUtils.IS_BLU) {
            mConnection = new BLUServiceConnection();
        } else {
            mConnection = new RemoteServiceConnection();
        }

        if (null != mAM) {
            int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol - 4, 0);
            Log.e(TAG, "maxVol = " + maxVol + " setStreamVolume = " + (maxVol - 4));
        }
        if (mEarphonePluginReceiver != null) {
            try {
                registerReceiver(mEarphonePluginReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            } catch (Exception e) {
                // TODO: handle exception
                Log.e(TAG, "registerReceiver:error=" + e.getMessage());
            }
        }

        if (false == mIsBind) {
            if(TestUtils.IS_BLU) {
                intent = new Intent("android.intent.action.FMRADIOSERVICE");
            } else {
                intent = new Intent("com.android.fmradio.IFmRadioService");
            }


            intent.setComponent(new ComponentName("com.android.fmradio", "com.android.fmradio.FmService"));
            mIsBind = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            /*   mIsBind = bindService(new Intent("com.mediatek.FmRadio.IFmRadioService"),
                    mConnection, Context.BIND_AUTO_CREATE);*/
            Log.e(TAG, "onStart bindService : " + mIsBind);
            if (true == mIsBind) {
                //enableWidget(true);
            } else {
                Log.e(TAG, "bindService fail");
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        // this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);

        // Gionee xiaolin 20121222 delete for CR00742482 start
        /*
        if (null != mEarphonePluginReceiver) {
            unregisterReceiver(mEarphonePluginReceiver);
        }

        try {
            if (null != mService && true == mIsBind) {
                if (true == mService.isPowerUp()) {
                    mService.powerDown();
                }
                if (mService.isDeviceOpen()) {
                    mService.closeDevice();
                }
                unbindService(mConnection);
                mIsBind = false;
                mService = null;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        // Gionee xiaolin 20120621 add for CR00626768 start
        try {
            Thread.sleep(200);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        // Gionee xiaolin 20120621 add for CR00626768 end
		*/
        // Gionee xiaolin 20121222 delete for CR00742482 end
    }

    // Gionee xiaolin 20121222 add for CR00742482 start
    private void stopFm() {

        if (null != mEarphonePluginReceiver) {
            // Gionee <xiaolin><2013-3-18> modify for CR00785399 start
            try {
                unregisterReceiver(mEarphonePluginReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "unregisterReceiver error=" + e.getMessage());
            }
            // Gionee <xiaolin><2013-3-18> modify for CR00785399 end
        }

        //Gionee zhangke 20151015 modify for CR01568781 start
        if (true == mIsBind) {
            try {
                mIsBind = false;
                unbindService(mConnection);
            } catch (Exception e) {
                Log.e(TAG, "unbindService:error=" + e.getMessage());
            }
        }
        try {
            if(TestUtils.IS_BLU) {
                if (null != mBLUService) {
                    if (true == mBLUService.isPowerUp()) {
                        mBLUService.powerDownAsync();
                    }
                    mBLUService = null;
                }
            } else {
                if (null != mService) {
                    if (true == mService.isPowerUp()) {
                        mService.powerDown();
                    }
                    if (mService.isDeviceOpen()) {
                        mService.closeDevice();
                    }
                    mService = null;
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //Gionee zhangke 20151015 modify for CR01568781 end
        // Gionee xiaolin 20121238 add for CR00742482 start
        mAM.setParameters("AUDIO_PARAMETER_KEY_MMI=1");
        // Gionee xiaolin 20121238 add for CR00742482 end
    }
    // Gionee xiaolin 20121222 add for CR00742482 end

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // TODO Auto-generated method stub
        if (null != mCurrentHz) {
            mCurrentHz.setText(getResources().getString(R.string.current_hz,
                    String.valueOf(87 + progress * 0.1)));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        //gionee cuijiuyu 20120919 modify for CR00696512 start
        /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
        frequency = getCurrFrequencyBySeekBar();
        /*Gionee huangjianqiang 20160616 add for CR01715226 end*/
        //gionee cuijiuyu 20120919 modify for CR00696512 end
        mCurrentHz
                .setText(getResources().getString(R.string.current_hz, String.valueOf(frequency)));
        try {
            if(TestUtils.IS_BLU) {
                if (mBLUService != null) {
                    mBLUService.tuneStationAsync(frequency);
                }
            } else {
                if (mService != null) {
                    mService.tune(frequency);
                }
            }
            System.out.println("onStopTrackingTouchfrequency" + frequency);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.minus_btn: {
                if (mSeekBar.getProgress() > 0) {
                    mSeekBar.incrementProgressBy(-1);
                    //gionee cuijiuyu 20120919 modify for CR00696512 start
                    /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
                    frequency = getCurrFrequencyBySeekBar();
                    /*Gionee huangjianqiang 20160616 add for CR01715226 end*/
                    //gionee cuijiuyu 20120919 modify for CR00696512 end
                    mCurrentHz.setText(getResources().getString(R.string.current_hz,
                            String.valueOf(frequency)));
                    if (false == mIsBind) {
                        return;
                    }
                    try {
                        if(TestUtils.IS_BLU) {
                            mBLUService.tuneStationAsync(frequency);
                        } else {
                            mService.tune(frequency);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case R.id.plus_btn: {
                //gionee cuijiuyu 20120919 modify for CR00696512 start
                /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
                frequency = getCurrFrequencyBySeekBar();
                    /*Gionee huangjianqiang 20160616 add for CR01715226 end*/
                //gionee cuijiuyu 20120919 modify for CR00696512 end
                if (mSeekBar.getProgress() < mSeekBar.getMax()) {
                    mSeekBar.incrementProgressBy(1);
                    frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
                    mCurrentHz.setText(getResources().getString(R.string.current_hz,
                            String.valueOf(frequency)));
                    if (false == mIsBind) {
                        return;
                    }
                    try {
                        if(TestUtils.IS_BLU) {
                            mBLUService.tuneStationAsync(frequency);
                        } else {
                            mService.tune(frequency);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            case R.id.default_btn: {
                /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
                mSeekBar.setProgress(setSeekBarByFrequency(defaultFrequency));
                /*Gionee huangjianqiang 20160616 add for CR01715226 end*/
                mCurrentHz.setText(getResources().getString(R.string.current_hz,
                        String.valueOf(defaultFrequency)));
                if (false == mIsBind) {
                    return;
                }
                try {
                    if(TestUtils.IS_BLU) {
                        mBLUService.tuneStationAsync(defaultFrequency);
                    } else {
                        mService.tune(defaultFrequency);
                    }
                    /*Gionee huangjianqiang 20160616 add for CR01715226 end*/
                } catch (RemoteException e) {

                }
                break;
            }

            case R.id.right_btn: {
                // Gionee xiaolin 20121222 add for CR00742482 start
                stopFm();
                // Gionee xiaolin 20121222 add for CR00742482 end
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("52", "P");
                    editor.commit();
                }
                TestUtils.rightPress(TAG, FMTest.this);
                break;
            }

            case R.id.wrong_btn: {
                // Gionee xiaolin 20121222 add for CR00742482 start
                stopFm();
                // Gionee xiaolin 20121222 add for CR00742482 end
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("52", "F");
                    editor.commit();
                }
                TestUtils.wrongPress(TAG, FMTest.this);
                break;
            }
        }
    }

    private BroadcastReceiver mEarphonePluginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 0);
                Log.i(TAG, "onReceive:state=" + state);

                /*Gionee huangjianqiang 20160419 add CR01679960 for begin*/
                int mic = intent.getIntExtra("microphone", -1);
                if (1 == state) {
                    if (mic == 1) {
                        mIsPluginWithMic = true;
                    } else {
                        mIsPluginWithoutMic = true;
                    }

                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            turnOnFM();
                        }
                    }, 200);
                } else {
                    if ((mic == 0) && (mIsPluginWithMic) && (mIsPluginWithoutMic)) {
                        mIsPluginWithMic = false;
                        mIsPluginWithoutMic = false;
                        mEarphoneTv.setText(R.string.inserted_earphone);
                        mEarphoneTv.setTextColor(Color.YELLOW);
//                        mIsPlud = true;
                    } else {
                        mEarphoneTv.setText(R.string.insert_earphone);
                        // Gionee xiaolin 20121106 add for CR00725238 start
                        mEarphoneTv.setTextColor(Color.RED);
                        // Gionee xiaolin 20121106 add for CR00725238 end
                        mIsPlud = false;
                        mIsPluginWithMic = false;
                        mIsPluginWithoutMic = false;
                        enableWidget(false);
                    }
                /*Gionee huangjianqiang 20160419 add CR01679960 for end*/

                }
            }
        }
    };

    void turnOnFM() {
        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, 11, 0);
        mEarphoneTv.setText(R.string.inserted_earphone);
        mEarphoneTv.setTextColor(Color.YELLOW);
        mIsPlud = true;


        try {
            if(TestUtils.IS_BLU) {
                if (mBLUService != null)  mBLUService.tuneStationAsync(frequency);;
            } else {
                if (mService != null)  mService.tune(frequency);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (true == mIsBind) {
            Log.e(TAG, "bindService success");
            enableWidget(true);
        } else {
            Log.e(TAG, "bindService fail");
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private void enableWidget(boolean state) {

        //Gionee zhangke 20160428 modify for CR01687958 start
        if(state){
            mIsPass = true;
            if(mIsTimeOver){
                mRightBtn.setEnabled(state);
            }
        }else{
            mIsPass = false;
            mRightBtn.setEnabled(state);
        }
        //Gionee zhangke 20160428 modify for CR01687958 end
        mSeekBar.setEnabled(state);
        mMinusBtn.setEnabled(state);
        mPlusBtn.setEnabled(state);
        mDefaultBtn.setEnabled(state);
    }

    /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
    private float getCurrFrequencyBySeekBar(){
        return (float) (87 + mSeekBar.getProgress() * 0.1);
    }
    private int setSeekBarByFrequency(float frequency){
        if(frequency>87){
            return (int)(frequency*10-870);
        }else{
            return 87;
        }
    }
    /*Gionee huangjianqiang 20160616 add for CR01715226 end*/

    private class BLUServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBLUService = com.mediatek.fmradio.IFmRadioService.Stub.asInterface(service);
            if (null == mBLUService) {
                Log.e(TAG, "Error: null interface");
            } else {
                new Thread() {
                    public void run() {
                        try {
                            mBLUService.initService((int) (getCurrFrequencyBySeekBar()*10));
                            if (true == mBLUService.isDeviceOpen() ) {
                                if (true == mBLUService.isPowerUp()) {

                                } else {
                                    Log.e(TAG, "fm power up fail");
                                }
                                mHandler.post(new Runnable() {
                                    public void run() {
                                        try {
                                            mBLUService.tuneStationAsync(defaultFrequency);
                                            enableWidget(true);
                                        } catch (Exception e) {
                                            Log.e(TAG, "mService.tune:Exception=" + e.getMessage());
                                        }
                                    }
                                });
                            } else {
                                Log.e(TAG, "fm device open fail");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "mService:Exception=" + e.getMessage());
                        }
                    }
                }.start();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mBLUService = null;
        }

    }
}



