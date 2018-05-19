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

public class FMTest extends BaseActivity {

	private String TAG = "FMTest";
	private IFMRadioService mService;
	private RemoteServiceConnection mConnection;
	private boolean mIsBind;
	private static final String EXTRA_FM = "fm";
	private AudioManager mAM;
	float frequency = 107.5f;

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
	}

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
		try {
			if (null != mService && true == mIsBind) {
				unbindService(mConnection);
				mIsBind = false;
				mService = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		FMTest.this.finish();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return true;
	}

}
