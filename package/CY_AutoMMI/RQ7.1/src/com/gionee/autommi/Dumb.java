package com.gionee.autommi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.os.SystemProperties;
import android.view.WindowManager;
import android.content.Intent;
import static  com.gionee.autommi.BaseActivity.PERSIST_RADIO_DISPATCH_ALL_KEY;

//Gionee zhangke 20160325 add for CR01660596 start
import android.os.PowerManager;
import android.content.Context;
//Gionee zhangke 20160325 add for CR01660596 end

public class Dumb extends Activity {

	private static final String TAG = "Dumb";
	//Gionee zhangke 20160325 add for CR01660596 start
	private PowerManager.WakeLock mWakeLock;
	private PowerManager mPowerManager;
	//Gionee zhangke 20160325 add for CR01660596 end

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Gionee zhangke 20160325 delete for CR01660596 start
        /*
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; 
        getWindow().setAttributes(lp);
        */
        //Gionee zhangke 20160325 delete for CR01660596 end
    }

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		SystemProperties.set(PERSIST_RADIO_DISPATCH_ALL_KEY, "false");	
        //Gionee zhangke 20160226 delete for CR01632945 start
        /*
        Intent stopi = new Intent();
        stopi.setAction("com.mediatek.mtklogger.ADB_CMD");
        Bundle bundle = new Bundle();
        bundle.putString("cmd_name", "stop");
        bundle.putInt("cmd_target", 7);
        stopi.putExtras(bundle);
        sendBroadcast(stopi);
        Log.e(TAG, "stop mtk mmi logcat "); 
        */
        //Gionee zhangke 20160226 delete for CR01632945 end
        //Gionee zhangke 20160325 delete for CR01660596 start
        Log.i(TAG, "onCreate:start wake and unlock screen");
        mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "AutoMMI"); 
        mWakeLock.setReferenceCounted(false);
        mWakeLock.acquire(5000); 
        sendBroadcast(new Intent("com.gionee.action.DISABLE_KEYGUARD"));
        //Gionee zhangke 20160325 delete for CR01660596 end
	}
}
