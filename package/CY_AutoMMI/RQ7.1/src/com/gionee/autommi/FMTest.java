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
    private int state;
    private EarphonePluginReceiver mEarphonePluginReceiver;
        private class RemoteServiceConnection implements ServiceConnection {
            public void onServiceConnected(ComponentName className, IBinder service) {
                try {
                    mService = IFMRadioService.Stub.asInterface(service);
                    if (null == mService) {
                        Log.e(TAG, "Error: null interface");
                    } else {
                        //Gionee <GN_BSP_AutoMMI> <lifeilong> <2170508> modify for ID 124769 begin
                        uiHandler.sendEmptyMessageDelayed(0,1500);
                       // uiHandler.sendEmptyMessageDelayed(1,1500);
                        //Gionee <GN_BSP_AutoMMI> <lifeilong> <2170508> modify for ID 124769 begin
                    }
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
        Intent it = this.getIntent();
        String freq = it.getStringExtra(EXTRA_FM);
        Log.i(TAG, "freq =" + freq);
        if (freq != null) {
            frequency = Float.parseFloat(freq);
        }
        Log.i(TAG, "frequency =" + frequency);
        mConnection = new RemoteServiceConnection();
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        uiHandler.sendEmptyMessageDelayed(0,100);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GN_MMI");
        mWakeLock.acquire();
        mEarphonePluginReceiver = new EarphonePluginReceiver();
        if (mEarphonePluginReceiver != null) {
            registerReceiver(mEarphonePluginReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        }
        if (false == mIsBind) {
            Intent intent = new Intent("com.caf.fmradio.IFMRadioService");
            intent.setComponent(new ComponentName("com.caf.fmradio", "com.caf.fmradio.FMRadioService"));
            mIsBind = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            if (true == mIsBind) {

            } else {
                Log.e(TAG, "bindService fail");
            }
        }
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
                    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170926> modify for ID 223712 begin
                    try {
                        if(mService != null && state == 1 ){
                            Log.d(TAG, " ---fmOn()---");
                            mService.fmOn();
                            mService.fmRadioReset();
                            mService.tune((int) (frequency * 1000));
                            Log.d(TAG, " ---fmOn()---");
                        }else {
                            uiHandler.sendEmptyMessageDelayed(1,2500);
                        }
                        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170926> modify for ID 223712 end
                    } catch (RemoteException e) {
                         Log.e(TAG,"mService.tune((int) (107.5f * 1000))  failed !!");
                    }
                    break;
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 74557 end

    private class EarphonePluginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG");
                state = intent.getIntExtra("state", 0);
                Log.d(TAG, "HEADSET_PLUG_STAT : " + state);
                if (0 == state) {
                    
                } else if (1 == state) {
                    uiHandler.sendEmptyMessageDelayed(0,100);
                    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170511> modify for ID 124769 begin
                    uiHandler.sendEmptyMessageDelayed(1,2500);
                    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170511> modify for ID 124769 end
                }
            }
        }

    }

	@Override
	public void onResume() {
            super.onResume();
	}

    @Override
    public void onPause() {
            super.onPause();
            Log.i(TAG, "onPause(); ");
    }

    @Override
    public void onStop() {
            super.onStop();
            Log.i(TAG, "onStop(); ");
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

}
