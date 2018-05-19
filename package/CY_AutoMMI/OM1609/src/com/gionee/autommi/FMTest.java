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

import android.graphics.Color;
import com.gionee.autommi.utils.SysPropUtil;


public class FMTest extends BaseActivity  {

    private String TAG = "FMTest";
    private IFmRadioService mService;
 //   private RemoteServiceConnection mConnection;
    private boolean mIsBind;
	private static final String EXTRA_FM = "fm";
    private AudioManager mAM;
    float frequency;
    private Intent intent;
    private com.mediatek.fmradio.IFmRadioService mBLUService;
    private ServiceConnection mConnection;
    private class RemoteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            try {
                mService = IFmRadioService.Stub.asInterface(service);
                if (null == mService) {
                    Log.e(TAG, "Error: null interface");
                } else {

                  //  if (false == mService.isServiceInit()) {
		//	Log.e(TAG, "false == mService.isServiceInit()");
                        mService.initService(107);
               //     }
                    if (true == mService.isDeviceOpen() || true == mService.openDevice()) {
                        if (true == mService.isPowerUp() || true == mService.powerUp(frequency)){
			Log.e(TAG, " mService.tune(frequency)");
                            mService.tune(frequency);
                        } else {
                            Log.e(TAG, "fm power up fail");
                        }
                    } else {
                        Log.e(TAG, "fm device open fail");
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "IFMRadioService RemoteException");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    }

    private class BLUServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mBLUService = com.mediatek.fmradio.IFmRadioService.Stub.asInterface(service);
            if (null == mBLUService) {
                Log.e(TAG, "Error: null interface");
            } else {
                try {
                    Log.i(TAG,"#A0");
                    mBLUService.initService(107);
                    Log.i(TAG,"#A1");
                    if (mBLUService.isDeviceOpen()) {
                        Log.i(TAG,"#A2");
                        if (mBLUService.isPowerUp()) {
                            Log.i(TAG,"#A3");

                        } else {
                            Log.e(TAG, "fm power up fail");
                        }
                        try {
                            mBLUService.tuneStationAsync(frequency);
                        } catch (Exception e) {
                            Log.e(TAG, "mBLUService.tune:Exception=" + e.getMessage());
                        }

                    } else {
                        Log.e(TAG, "fm device open fail");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "mBLUService:Exception=" + e.getMessage());

                }

            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mBLUService = null;
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
        if (SysPropUtil.FM_IS_BLU) {
            mConnection = new BLUServiceConnection();
        }else {
            mConnection = new RemoteServiceConnection();
        }
        Log.i(TAG,"#1");
        if (null != mAM) {
            int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol , 0);
        }
        if (false == mIsBind) {
            if (SysPropUtil.FM_IS_BLU) {
                intent = new Intent("android.intent.action.FMRADIOSERVICE");
            }else {
                intent = new Intent("com.android.fmradio.IFmRadioService");
            }
        	intent.setComponent( new ComponentName("com.android.fmradio", "com.android.fmradio.FmService") );
        	   mIsBind = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        	/*   mIsBind = bindService(new Intent("com.mediatek.FmRadio.IFmRadioService"),
                    mConnection, Context.BIND_AUTO_CREATE);*/
            if (true == mIsBind) {

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
            if (true == mIsBind) {
                unbindService(mConnection);
                mIsBind = false;
            }

            if (SysPropUtil.FM_IS_BLU) {
                if (null != mBLUService) {
                    if (true == mBLUService.isPowerUp()) {
                        mBLUService.powerDownAsync();
                    }
                    mBLUService = null;
                }
            }else {
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
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,"message="+e.getMessage());
        }

        try {
            Thread.sleep(200);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
	    FMTest.this.finish();
    }
}
