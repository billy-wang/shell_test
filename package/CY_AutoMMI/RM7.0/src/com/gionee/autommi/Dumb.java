package com.gionee.autommi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.os.SystemProperties;
import android.view.WindowManager;
import android.content.Intent;
import static  com.gionee.autommi.BaseActivity.PERSIST_RADIO_DISPATCH_ALL_KEY;
import static  com.gionee.autommi.BaseActivity.PERSIST_RUNTIME_AUTOMMI;

//Gionee zhangke 20160325 add for CR01660596 start
import android.os.PowerManager;
import android.content.Context;
//Gionee zhangke 20160325 add for CR01660596 end
import android.provider.Settings;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.nfc.NfcAdapter;
import com.gionee.autommi.AutoMMI;
import android.os.Process;

public class Dumb extends Activity {

    private static final String TAG = "Dumb";
    //Gionee zhangke 20160325 add for CR01660596 start
    private PowerManager.WakeLock mWakeLock;
    private PowerManager mPowerManager;
    //Gionee zhangke 20160325 add for CR01660596 end
    private static int SCREENTIMEOUT = 1800000;
    
    public static boolean AutoMMiIsRunning = false;
    public String autommiIsRunning;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 begin
    public static boolean mNfcOn = false;
    public static boolean mNfcOff = false;
    public static boolean mNfcTurn = false;
    private NfcAdapter mNfcAdapter;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 end
    private static String resetScreenTimeOut = "com.gionee.autommi.resetScreenTimeOut";
    private static String CLOSE = "com.gionee.autommi.close";
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        registerReceiver(mNfcReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        try{
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (mNfcAdapter == null) {
                Log.i(TAG, "mNfcAdapter == null");
                return;
            } else {
                Log.i(TAG, "mNfcAdapter != null");
            }
            if (!mNfcAdapter.isEnabled()) {
                Log.i(TAG, "mNfcAdapter != isEnable");
                mNfcAdapter.enable();
            } else {
                Log.i(TAG, "mNfcAdapter = isEnable");
            }
        }catch(Exception e){
            Log.i(TAG, " Exception="+e.getMessage());
        }
        Log.d(TAG,"Dumb  onCreate !");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170826> modify for ID 197122 begin
        SystemProperties.set(PERSIST_RADIO_DISPATCH_ALL_KEY, "false");
        //Gionee zhangke 20160325 delete for CR01660596 start
        mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if(mWakeLock == null){
            Log.i(TAG, "onCreate:start wake and unlock screen");
            autommiIsRunning = SystemProperties.get(PERSIST_RADIO_DISPATCH_ALL_KEY, "0");
            Log.d(TAG,"autommiIsRunning  onStart =  " + autommiIsRunning);
            if(!"1".equals(autommiIsRunning)){
                SystemProperties.set(PERSIST_RADIO_DISPATCH_ALL_KEY, "1");
            }
            mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "AutoMMI"); 
        }
        Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, SCREENTIMEOUT);
        mWakeLock.setReferenceCounted(false);
        mWakeLock.acquire(5000); 
        sendBroadcast(new Intent("com.gionee.action.DISABLE_KEYGUARD"));
        //Gionee zhangke 20160325 delete for CR01660596 end
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"  onResume  ");
        autommiIsRunning = SystemProperties.get(PERSIST_RADIO_DISPATCH_ALL_KEY, "0");
        Log.d(TAG,"autommiIsRunning  onResume 1 =  " + autommiIsRunning);
        if(!"1".equals(autommiIsRunning)){
            SystemProperties.set(PERSIST_RADIO_DISPATCH_ALL_KEY, "1");
        }
        autommiIsRunning = SystemProperties.get(PERSIST_RADIO_DISPATCH_ALL_KEY, "0");
        Log.d(TAG,"autommiIsRunning  onResume 2 =  " + autommiIsRunning);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.d(TAG,"Dumb onStop  !");
        autommiIsRunning = SystemProperties.get(PERSIST_RADIO_DISPATCH_ALL_KEY, "false");
        Log.d(TAG,"autommiIsRunning  onStop 1 =  " + autommiIsRunning);
        if("1".equals(autommiIsRunning)){
            SystemProperties.set(PERSIST_RADIO_DISPATCH_ALL_KEY, "false");
        }
        autommiIsRunning = SystemProperties.get(PERSIST_RADIO_DISPATCH_ALL_KEY, "false");
        Log.d(TAG,"autommiIsRunning  onStop 2 =  " + autommiIsRunning);
        if(!AutoMMiIsRunning){
            AutoMMiIsRunning = false;
        }
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170826> modify for ID 197122 end
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"Dumb onDestroy  !");
        this.unregisterReceiver(mNfcReceiver);
    }

    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 begin
    BroadcastReceiver mNfcReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)){
                int intExtra = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, 0);
                Log.d(TAG,"intextra = " + intExtra);
                if(intExtra == 3){
                    mNfcOn = true;
                    mNfcOff = false;
                } else if (intExtra == 1){
                    mNfcOff = true;
                    mNfcOn = false;
                } else if (intExtra == 2){
                    mNfcTurn = true;
                    mNfcOff = false;
                    mNfcOn = false;
                }
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 end
}
