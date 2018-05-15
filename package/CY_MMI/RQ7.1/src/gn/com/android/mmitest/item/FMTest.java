
package gn.com.android.mmitest.item;

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
//import com.android.internal.telephony.Phone;
//import com.android.internal.telephony.PhoneFactory;
//import android.os.AsyncResult;
//Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 begin
import android.app.ActivityManager;
import android.os.PowerManager;
import android.content.Context;
//Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 end
import android.app.StatusBarManager;

public class FMTest extends Activity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
    private SeekBar mSeekBar;

    private Button mMinusBtn, mPlusBtn, mDefaultBtn, mRightBtn, mWrongBtn;

    private TextView mCurrentHz, mEarphoneTv;

    private EarphonePluginReceiver mEarphonePluginReceiver;

    private String TAG = "FMTest";

    private IFMRadioService mService;

    public static final int HIGHEST_STATION = 1080;

    public static final int LOWEST_STATION = 875, EVENT_RESPONSE_SN_WRITE = 0;

    private int miCurrentStation;

    private RemoteServiceConnection mConnection;

    private boolean mIsPlud;

    private boolean mIsBind;

    private AudioManager mAM;

    // private Handler mUiHandler;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 begin
    PowerManager.WakeLock mWakeLock;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 end

    private boolean mIsPass;
    private boolean mIsFmRunning;
    private int state;
    private StatusBarManager sbm;
    private boolean mIsOnStop;
    //Gionee <GN_BSP_MMI> <lifeilong> <20171020> modify for ID 242507 begin
    private final float defaultFrequency = ((TestUtils.mIsForSale) && (TestUtils.mIsSKFlag)) ? 93.5f :(((TestUtils.mIsForSale) && (TestUtils.mIsGnIndia)) ? 91.1f : 107.5f);
    //private final float defaultFrequency = ((TestUtils.mIsForSale) && (TestUtils.mIsSKFlag)) ? 93.5f : (TestUtils.mIsGnIndia? 91.1f : 107.5f);
    //Gionee <GN_BSP_MMI> <lifeilong> <20171020> modify for ID 242507 end
    private class RemoteServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "onServiceConnected");
            try {
                mService = IFMRadioService.Stub.asInterface(service);
                if (null == mService) {
                    Log.e(TAG, "Error: null interface");
                } else {
                    Log.d(TAG, " ---fmOn()---");
                    if(!mService.isFmOn()){
                        //mService.fmOn();
                    }
                    if(mService.isFmOn()){
                        //mService.tune((int) (107.5f * 1000));
                    }
                    Log.d(TAG, " ---fmOn()---");
                }
            }  catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "IFMRadioService Exception:"+e.getMessage());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            try {
                //mService.fmOff();
                //mService = null;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "IFMRadioService Exception:"+e.getMessage());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        setContentView(R.layout.fm_test);

        mDefaultBtn = (Button) findViewById(R.id.default_btn);
        mCurrentHz = (TextView) findViewById(R.id.current_hz);
        mEarphoneTv = (TextView) findViewById(R.id.earphone_note);
        sbm = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.disable(StatusBarManager.DISABLE_RECENT);

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
        //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 begin
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GN_MMI");
        mWakeLock.acquire();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 end
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mEarphonePluginReceiver = new EarphonePluginReceiver();
        //uiHandler.sendEmptyMessageDelayed(0,100);

        //Gionee <GN_BSP_MMI> <lifeilong> <20170406> modify for ID 98079 begin
        mConnection = new RemoteServiceConnection();

        if (mEarphonePluginReceiver != null) {
            registerReceiver(mEarphonePluginReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170911> modify for ID 209382 begin
        if (false == mIsBind) {
            Intent intent = new Intent("com.caf.fmradio.IFMRadioService");
            intent.setComponent(new ComponentName("com.caf.fmradio", "com.caf.fmradio.FMRadioService"));
            mIsBind = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            if (true == mIsBind) {
                Log.e(TAG, "bindService is ok");
                enableWidget(true);
            } else {
                Log.e(TAG, "bindService fail");
            }
        }
        mIsOnStop = false;
        //Gionee <GN_BSP_MMI> <lifeilong> <20170911> modify for ID 209382 end
    }




    @Override
    public void onResume() {
        super.onResume();
        TestUtils.setWindowFlags(this);
    }

    //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 74557 begin
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Intent intent = new Intent("com.caf.fmradio.FMRADIO_ACTIVITY");
                    intent.addFlags(intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("fromMmi","fromMmi");
                    FMTest.this.startActivity(intent);
                    break;
                case 1:
                    try {
                         //Gionee <GN_BSP_MMI> <lifeilong> <20170911> modify for ID 209382 begin
                         if(mService != null && state == 1 && mIsOnStop){
                         //Gionee <GN_BSP_MMI> <lifeilong> <20170911> modify for ID 209382 end
                             mService.tune((int) (defaultFrequency * 1000));
                         }else {
                            uiHandler.sendEmptyMessageDelayed(1,500);
                         }
                    } catch (Exception e) {
                         Log.e(TAG,"mService.tune((int) (" +defaultFrequency +" * 1000))  failed !!");
                    }
                    break;
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 74557 end


    @Override
    public void onStart() {

        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);

        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, " onStop  ");
        mIsOnStop = true;
        // this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED);


    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 74557 begin
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93776 begin
        if (null != mEarphonePluginReceiver) {
            unregisterReceiver(mEarphonePluginReceiver);
            Log.e(TAG,"unregisterReceiver(mEarphonePluginReceiver)");
            Log.e(TAG, "onDestroy -- > sbm.disable(StatusBarManager.DISABLE_NONE) ");
            sbm.disable(StatusBarManager.DISABLE_NONE);
        }

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
        //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93776 end
        Log.i(TAG, "onDestroy onDestroy onDestroy onDestroy ");
	    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    try{
		    activityManager.forceStopPackage("com.caf.fmradio");
	    }catch(Exception e){
		    Log.i(TAG, "forceStopPackage Exception="+e.getMessage());
	    }
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 74557 end


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
        float frequency = (float) (87 + seekBar.getProgress() * 0.1);
        mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(frequency)));
        try {
            mService.tune((int) (frequency * 1000));
        } catch (RemoteException e) {

        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.minus_btn: {
            if (mSeekBar.getProgress() > 0) {
                mSeekBar.incrementProgressBy(-1);
                float frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
                mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(frequency)));
                if (false == mIsBind) {
                    return;
                }
                try {
                    mService.tune((int) (frequency * 1000));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
        case R.id.plus_btn: {
            float frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
            if (mSeekBar.getProgress() < mSeekBar.getMax()) {
                mSeekBar.incrementProgressBy(1);
                frequency = (float) (87 + mSeekBar.getProgress() * 0.1);
                mCurrentHz.setText(getResources().getString(R.string.current_hz, String.valueOf(frequency)));
                if (false == mIsBind) {
                    return;
                }
                try {
                    mService.tune((int) (frequency * 1000));
                } catch (RemoteException e) {
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
                mService.tune((int) (defaultFrequency * 1000));
            } catch (RemoteException e) {

            }
            break;
        }

        case R.id.right_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 begin
			if(mWakeLock != null){
				mWakeLock.release();
			}
            //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 end
            if (TestUtils.mIsAutoMode) {
                SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                editor.putString("52", "P");
                editor.commit();
            }
            TestUtils.rightPress(TAG, FMTest.this);
            // }
            break;
        }

        case R.id.wrong_btn: {

            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 begin
            if(mWakeLock != null){
                 mWakeLock.release();
            }
            //Gionee <GN_BSP_MMI> <lifeilong> <20170325> modify for ID 93789 end
            if (TestUtils.mIsAutoMode) {
                SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                editor.putString("52", "F");
                editor.commit();
            }
            TestUtils.wrongPress(TAG, FMTest.this);
            // }
            break;
        }
        }
    }

    private class EarphonePluginReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG");
                state = intent.getIntExtra("state", 0);
                Log.d(TAG, "HEADSET_PLUG_STAT : " + state);
                if (0 == state) {
                mEarphoneTv.setText(R.string.insert_earphone);
                //Gionee <GN_BSP_MMI> <lifeilong> <20170502> modify for ID 110849 begin
                    /*if (true == mIsBind) {
                        if(null != mService){
                            unbindService(mConnection);
                        }
                        mIsBind = false;
                    }                    
                    mIsPlud = false;
                    enableWidget(false);*/
                //Gionee <GN_BSP_MMI> <lifeilong> <20170502> modify for ID 110849 end
                } else if (1 == state) {
                    mEarphoneTv.setText(R.string.inserted_earphone);
                    uiHandler.sendEmptyMessageDelayed(0,100);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 74557 begin
                    uiHandler.sendEmptyMessageDelayed(1,2500);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170323> modify for ID 74557 end

                }
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private void enableWidget(boolean state) {
        mRightBtn.setEnabled(state);
        mSeekBar.setEnabled(state);
        mMinusBtn.setEnabled(state);
        mPlusBtn.setEnabled(state);
        mDefaultBtn.setEnabled(state);
    }
}

