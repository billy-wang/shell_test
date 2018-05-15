
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import gn.com.android.mmitest.item.FmService;

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
import gn.com.android.mmitest.item.FeatureOption;
import com.android.fmradio.IFmRadioService;
import android.os.Message;

public class FMTest extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private SeekBar mSeekBar;

    private Button mMinusBtn, mPlusBtn, mDefaultBtn, mRightBtn, mWrongBtn;

    private TextView mCurrentHz, mEarphoneTv;

    private String TAG = "FMTest";
    private boolean new_fm_flag = false;
    private IFmRadioService mFmRadioService;
    private FmService mService;

    public static final int HIGHEST_STATION = 1080;

    public static final int LOWEST_STATION = 875, EVENT_RESPONSE_SN_WRITE = 0;

    private int miCurrentStation;

    private RemoteServiceConnection mConnection;

    private boolean mIsPlud;

    private boolean mIsBind;

    private AudioManager mAM;

    private Intent intent;
    // gionee cuijiuyu 20120919 add for CR00696512 start
    float frequency = 107.5f;
    // gionee cuijiuyu 20120919 add for CR00696512 end
    // Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    // Gionee zhangke 20160818 modify for CR01748530 start
    private boolean mIsPowerUping = false;
    private static final int MSG_REFRESH_STATUS = 0;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REFRESH_STATUS:
                refreshFMStatus(false);
                break;
            }
        }
    };
    // Gionee zhangke 20160818 modify for CR01748530 end
	
    private class RemoteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG,"new_fm_flag = " + new_fm_flag);
            mService = ((FmService.ServiceBinder) service).getService();
            if (null == mService) {
                Log.e(TAG, "onServiceConnected, mService is null");
                finish();
                return;
            }
            if (null == mService) {
                Log.e(TAG, "Error: null interface");
            } else {
            Log.i(TAG, "mService = ((FmService.ServiceBinder) service).getService()");
                new FmThread().start();
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    }
    class FmThread extends Thread {
        public void run() {
            try {               
                mService.initService((int) ((mSeekBar.getProgress() * 0.1 + 87) * 10));
                if (true == mService.isDeviceOpen() || true == mService.openDevice()) {
                    mHandler.post(new Runnable() {
                        public void run() {
                        try {
                            mService.tuneStation(frequency);
                            enableWidget(true);
                            Log.e(TAG, "fm power up success");
                        } catch (Exception e) {
                        Log.e(TAG, "mService.tuneStation:Exception=" + e.getMessage());
                        }
                        }
                    });
                } else {
                    Log.e(TAG, "fm device open fail");
                }
                mIsPowerUping = false;
                mHandler.sendEmptyMessage(MSG_REFRESH_STATUS);
            } catch (Exception e) {
            Log.e(TAG, "mService:Exception=" + e.getMessage());
            }
        }
    }

    class FmRunnable implements Runnable {
        @Override
        public void run(){
            try {               
                mService.tuneStation(frequency);
                enableWidget(true);
                Log.e(TAG, "fm power up success");
            } catch (Exception e) {
                Log.e(TAG, "mService.tuneStation:Exception=" + e.getMessage());
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120921 add for CR00693542 start
        TestUtils.checkToContinue(this);
        TestUtils.setWindowFlags(this);
        // Gionee zhangke 20151215 modify for CR01609753 end
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
        mSeekBar.setProgress(205);
        mMinusBtn = (Button) findViewById(R.id.minus_btn);
        mPlusBtn = (Button) findViewById(R.id.plus_btn);

        mSeekBar.setEnabled(false);
        mMinusBtn.setEnabled(false);
        mPlusBtn.setEnabled(false);
        mDefaultBtn.setEnabled(false);

        mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(107.5)));
        mMinusBtn.setOnClickListener(this);
        mPlusBtn.setOnClickListener(this);
        mDefaultBtn.setOnClickListener(this);

        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // Gionee xiaolin 20120107 add for CR00742652 start
        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, 11, 0);
        // Gionee xiaolin 20120107 add for CR00742652 end

        // Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setVisibility(View.INVISIBLE);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mIsTimeOver = true;
                if (mIsPass) {
                    mRightBtn.setEnabled(true);
                    mRightBtn.setVisibility(View.VISIBLE);
                }

                mWrongBtn.setEnabled(true);

                mRightBtn.setOnClickListener(FMTest.this);
                mWrongBtn.setOnClickListener(FMTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
    }

    @Override
    public void onStart() {
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);
        super.onStart();
        mConnection = new RemoteServiceConnection();
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
            //mIsPowerUping = true;				
            mIsBind = bindService(new Intent(FMTest.this, FmService.class),mConnection, Context.BIND_AUTO_CREATE);
            Log.e(TAG, "onStart bindService : " + mIsBind);
            if (true == mIsBind) {
                enableWidget(true);
            } else {
                Log.e(TAG, "bindService fail");
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
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
        //Gionee <GN_BSP_MMI> <lifeilong> <20170505> modify for ID 129417 begin
        try {
            if (null != mService) {
                if (true == mService.isPowerUp()) {
                    mService.powerDown();
                }
                if (mService.isDeviceOpen()) {
                    mService.closeDevice();
                }
                mService = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170505> modify for ID 129417 end
        // Gionee zhangke 20151015 modify for CR01568781 start
        if (true == mIsBind) {
            try {
                mIsBind = false;
                unbindService(mConnection);
            } catch (Exception e) {
                Log.e(TAG, "unbindService:error=" + e.getMessage());
            }
        }
        // Gionee zhangke 20151015 modify for CR01568781 end
        // Gionee xiaolin 20121238 add for CR00742482 start
        mAM.setParameters("AUDIO_PARAMETER_KEY_MMI=1");
        // Gionee xiaolin 20121238 add for CR00742482 end
    }
    // Gionee xiaolin 20121222 add for CR00742482 end

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        // TODO Auto-generated method stub
        if (null != mCurrentHz) {
            mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(87 + progress * 0.1)));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        // gionee cuijiuyu 20120919 modify for CR00696512 start
        frequency = (float) (87 + seekBar.getProgress() * 0.1);
        // gionee cuijiuyu 20120919 modify for CR00696512 end
        mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(frequency)));
        try {
            if (mService != null) {
                mService.tuneStation(frequency);
            }
            System.out.println("onStopTrackingTouchfrequency" + frequency);
        } catch (Exception e) {
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
                // gionee cuijiuyu 20120919 modify for CR00696512 start
                frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
                // gionee cuijiuyu 20120919 modify for CR00696512 end
                mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(frequency)));
                if (false == mIsBind) {
                    return;
                }
                try {
                    mService.tuneStation(frequency);                  
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        }
        case R.id.plus_btn: {
            // gionee cuijiuyu 20120919 modify for CR00696512 start
            frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
            // gionee cuijiuyu 20120919 modify for CR00696512 end
            if (mSeekBar.getProgress() < mSeekBar.getMax()) {
                mSeekBar.incrementProgressBy(1);
                frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
                mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(frequency)));
                if (false == mIsBind) {
                    return;
                }
                try {
                    mService.tuneStation(frequency);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
        }
        case R.id.default_btn: {
            mSeekBar.setProgress(205);
            mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(107.5)));
            if (false == mIsBind) {
                return;
            }
            try {
                mService.tuneStation(107.5f);
            } catch (Exception e) {

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
                if (0 == state) {
                        mIsPlud = false;
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170508> modify for ID 132971 begin
                    try {
                        if (null != mService) {
                            if (true == mService.isPowerUp()) {
                                mService.powerDown();
                            }
                            if (mService.isDeviceOpen()) {
                                mService.closeDevice();
                            }
                        }
                    } catch (Exception e) {
                    e.printStackTrace();
                    }
                } else if (1 == state) {
                    // Gionee zhangke 20160516 modify for CR01699963 start
                    mIsPlud = true;
                    // Gionee zhangke 20160516 modify for CR01699963 end
                try {
                    mService.initService((int) ((mSeekBar.getProgress() * 0.1 + 87) * 10));
                    if (true == mService.isDeviceOpen() || true == mService.openDevice()) {
                        mHandler.post(new FmRunnable());
                    } else {
                        Log.e(TAG, "fm device open fail");
                    }
                    mIsPowerUping = false;
                    mHandler.sendEmptyMessage(MSG_REFRESH_STATUS);
                } catch (Exception e) {
                    Log.e(TAG, "mService:Exception=" + e.getMessage());
                }
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170508> modify for ID 132971 end

                }
                refreshFMStatus(true);
            }
        }
    };

    void turnOnFM() {
        // Gionee zhangke 20160516 modify for CR01699963 start
        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, 11, 0);
        if (mService != null) {
            try {
                Log.e(TAG, "tuneStation fm: " + frequency);
                mService.tuneStation(frequency);              
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Gionee zhangke 20160516 modify for CR01699963 end
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private void enableWidget(boolean state) {
        // Gionee zhangke 20160428 modify for CR01687958 start
        if(state){
            mRightBtn.setVisibility(View.VISIBLE);
        }else {
            mRightBtn.setVisibility(View.INVISIBLE);
        }
        if (state) {
            mIsPass = true;
            if (mIsTimeOver) {
                mRightBtn.setEnabled(state);
            }
        } else {
            mIsPass = false;
            mRightBtn.setEnabled(state);
        }
        // Gionee zhangke 20160428 modify for CR01687958 end

        mSeekBar.setEnabled(state);
        mMinusBtn.setEnabled(state);
        mPlusBtn.setEnabled(state);
        mDefaultBtn.setEnabled(state);
    }

    // Gionee zhangke 20160818 modify for CR01748530 start
    private void refreshFMStatus(boolean isDelay) {
        Log.i(TAG, "refreshFMStatus mIsPowerUping=" + mIsPowerUping + ";mIsPlud=" + mIsPlud);
        if (mIsPlud) {
            mEarphoneTv.setText(R.string.inserted_earphone);
        }else{
            mEarphoneTv.setText(R.string.insert_earphone);
        }
        if (mIsPowerUping) {
            return;
        }
        if (mIsPlud) {
            mEarphoneTv.setTextColor(Color.YELLOW);
            boolean a = true == mIsBind && mService!= null;
            Log.e(TAG, mIsBind + " =  =  " + mService+ "  ----  " + a);
            if (true == mIsBind && mService!= null) {
                Log.e(TAG, "mEarphonePluginReceiver bindService success");
                enableWidget(true);
            } else {
                Log.e(TAG, "mEarphonePluginReceiver bindService fail");
            }
            if (isDelay) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsPlud) {
                            if (mIsPlud) {
                                turnOnFM();
                            }
                        }
                    }
                }, 500);
            } else {
                turnOnFM();
            }
        } else {
            mEarphoneTv.setTextColor(Color.RED);
            enableWidget(false);
        }
    }
    // Gionee zhangke 20160818 modify for CR01748530 end
}
