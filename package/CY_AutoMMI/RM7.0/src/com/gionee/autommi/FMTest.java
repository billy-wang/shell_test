package com.gionee.autommi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import com.android.fmradio.IFmRadioService;
import android.content.Intent;
import android.media.AudioManager;
import com.gionee.autommi.FmService;

import android.graphics.Color;



public class FMTest extends BaseActivity  {

    private String TAG = "FMTest";
    private IFmRadioService mFmRadioService;
    private FmService mService;
    private RemoteServiceConnection mConnection;
    private boolean mIsBind;
	private static final String EXTRA_FM = "fm";
    private AudioManager mAM;
    float frequency;
    private Intent intent;
    private class RemoteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            try {
                //mService = IFmRadioService.Stub.asInterface(service);
                mService = ((FmService.ServiceBinder) service).getService();
                if (null == mService) {
                    Log.e(TAG, "Error: null interface");
                } else {
                    mService.initService(107);
                    if (true == mService.isDeviceOpen() || true == mService.openDevice()) {
                        if (true == mService.isPowerUp() || true == mService.powerUp(frequency)){
			            Log.e(TAG, " mService.tune(frequency)");
                            mService.tuneStation(frequency);
                        } else {
                            Log.e(TAG, "fm power up fail");
                        }
                    } else {
                        Log.e(TAG, "fm device open fail");
                    }
                }
            }  catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }
    @Override
    public void onResume() {
        super.onResume();
        Intent it = this.getIntent();
		String fmDev = it.getStringExtra(EXTRA_FM);
		Log.i(TAG,"fmDev ="+fmDev);
        if(fmDev!= null){
          frequency = Float.parseFloat(fmDev);
        }
		Log.i(TAG,"frequency ="+frequency);
        mConnection = new RemoteServiceConnection();
        if (null != mAM) {
            int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol , 0);
        }
        if (false == mIsBind) {
        	/*intent = new Intent("com.android.fmradio.IFmRadioService");        
        	intent.setComponent( new ComponentName("com.android.fmradio", "com.android.fmradio.FmService") );
        	   mIsBind = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);*/
            mIsBind = bindService(new Intent(FMTest.this, FmService.class),mConnection, Context.BIND_AUTO_CREATE);
        	/*   mIsBind = bindService(new Intent("com.mediatek.FmRadio.IFmRadioService"),
                    mConnection, Context.BIND_AUTO_CREATE);*/
            if (true == mIsBind) {
                Log.e(TAG, "bindService ok");
            } else {
                Log.e(TAG, "bindService fail");
            }
        }

    }
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG,"onPause(); ");
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(200);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
	    FMTest.this.finish();
    }
}
