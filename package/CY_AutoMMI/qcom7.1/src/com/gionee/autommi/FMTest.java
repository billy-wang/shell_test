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
import com.caf.fmradio.IFMRadioService;
import android.media.AudioManager;

import android.graphics.Color;
import android.app.ActivityManager;
import android.os.PowerManager;
import android.content.Context;

public class FMTest extends BaseActivity {

	private String TAG = "FMTest";
	private IFMRadioService mService;
	private RemoteServiceConnection mConnection;
	private boolean mIsBind;
	private static final String EXTRA_FM = "fm";
	private AudioManager mAM;
	float frequency = 107.5f;
    PowerManager.WakeLock mWakeLock;
	private class RemoteServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			try {
				mService = IFMRadioService.Stub.asInterface(service);
				if (null == mService) {
					Log.e(TAG, "Error: null interface");
				} else {
					mService.fmOn();
					mService.tune((int) (frequency * 1000));
					Log.d(TAG, " ---fmOn()---");
                    uiHandler.sendEmptyMessageDelayed(1,500);
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		uiHandler.sendEmptyMessageDelayed(0,100);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GN_MMI");
        mWakeLock.acquire();
	}
	
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Intent intent = new Intent("com.caf.fmradio.FMRADIO_ACTIVITY");
                    intent.addFlags(intent.FLAG_ACTIVITY_SINGLE_TOP);
                    FMTest.this.startActivity(intent);
                    break;
                case 1:
                    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170328> modify for ID 96617 begin
                    try {
                        if(mService != null){
                            mService.tune((int) (107.5f * 1000));
                    }else {
                        uiHandler.sendEmptyMessageDelayed(1,500);
                        }
                    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170328> modify for ID 96617 end
                    } catch (RemoteException e) {
                         Log.e(TAG,"mService.tune((int) (107.5f * 1000))  failed !!");
                    }
                    break;
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 74557 end


	@Override
	public void onResume() {
		super.onResume();
		Intent it = this.getIntent();
		String freq = it.getStringExtra(EXTRA_FM);
		Log.i(TAG, "freq =" + freq);
		if (freq != null) {
			frequency = Float.parseFloat(freq);
		}
		Log.i(TAG, "frequency =" + frequency);
		mConnection = new RemoteServiceConnection();
        uiHandler.sendEmptyMessageDelayed(1,500);
		if (null != mAM) {
//			int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_FM);
//			mAM.setStreamVolume(AudioManager.STREAM_FM, maxVol, 0);
		}
		if (false == mIsBind) {
			//Gionee <GN_AutoMMI><lifeilong><20161102> modify for 17477 begin
			/*mIsBind = bindService(
					new Intent("com.caf.fmradio.IFMRadioService"), mConnection,
					Context.BIND_AUTO_CREATE);*/
			Intent intent = new Intent("com.caf.fmradio.IFMRadioService");
			intent.setComponent(new ComponentName("com.caf.fmradio", "com.caf.fmradio.FMRadioService"));
			mIsBind = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			//Gionee <GN_AutoMMI><lifeilong><20161102> modify for 17477 end

			if (true == mIsBind) {

			} else {
				Log.e(TAG, "bindService fail");
			}
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause(); ");
	}
    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170328> modify for ID 96617 begin
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (null != mService && true == mIsBind) {
                Log.e(TAG,"unbindService(mConnection);");
                unbindService(mConnection);
                mIsBind = false;
                mService = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "onDestroy onDestroy onDestroy onDestroy ");
	    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    try{
		    activityManager.forceStopPackage("com.caf.fmradio");
	    }catch(Exception e){
		    Log.i(TAG, "forceStopPackage Exception="+e.getMessage());
	    }
    }
    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170328> modify for ID 96617 end


	/*@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return true;
	}*/

}
