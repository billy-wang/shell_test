package com.gionee.autommi.composition;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.FMTest;
import com.android.fmradio.IFmRadioService;
import com.gionee.autommi.MagneticFieldTest;

public class MagFmTest extends BaseActivity implements SensorEventListener{

	private String TAG = "FMTest";
	private IFmRadioService mService;
	private RemoteServiceConnection mConnection;
	private boolean mIsBind;
	private static final String EXTRA_FM = "fm";
	private AudioManager mAM;
	float frequency;
	
    private SensorManager sensorManager;
    private Sensor mFSensor;
    private boolean flag;


	private class RemoteServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			try {
				mService = IFmRadioService.Stub.asInterface(service);
				if (null == mService) {
					Log.e(TAG, "Error: null interface");
				} else {
				//	if (false == mService.isServiceInit()) {
						Log.e(TAG, "false == mService.isServiceInit()");
						mService.initService(107);
					//}
					if (true == mService.isDeviceOpen()
							|| true == mService.openDevice()) {
						if (true == mService.isPowerUp()
								|| true == mService.powerUp(frequency)) {
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		mFSensor = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
		((AutoMMI)getApplication()).recordResult(MagneticFieldTest.TAG, "", "0");
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent it = this.getIntent();
		String fmDev = it.getStringExtra(EXTRA_FM);
		Log.i(TAG, "fmDev =" + fmDev);
		if (fmDev != null) {
			frequency = Float.parseFloat(fmDev);
		}
		Log.i(TAG, "frequency =" + frequency);
		mConnection = new RemoteServiceConnection();
		if (null != mAM) {
			int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol, 0);
		}
		if (false == mIsBind) {
			//Gionee zhangke 20160128 modify for CR01633186 start
            Intent intent = new Intent("com.android.fmradio.IFmRadioService");

            intent.setComponent(new ComponentName("com.android.fmradio", "com.android.fmradio.FmService"));
            mIsBind = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
			//Gionee zhangke 20160128 modify for CR01633186 end
			if (true == mIsBind) {

			} else {
				Log.e(TAG, "bindService fail");
			}
		}
		
		sensorManager.registerListener(this, mFSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause(); ");
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

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sensorManager.unregisterListener(this);
		this.finish();
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (!flag) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			if(!isZero(x) || !isZero(y) || !isZero(z)){
				flag = true;
				String res = "" + x + "|" + y + "|" + z;
				((AutoMMI) getApplication()).recordResult(TAG, res, "1");
				Toast.makeText(this, res, Toast.LENGTH_LONG).show();
			}

		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

    private boolean isZero(float f){
    	return (f<0.000001&&f>-0.000001);
    }

}
