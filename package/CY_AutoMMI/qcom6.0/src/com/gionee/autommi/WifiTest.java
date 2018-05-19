package com.gionee.autommi;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
//Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 begin
import android.os.SystemClock;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.os.Message;
//Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 end

public class WifiTest extends BaseActivity {
	private static final String EXTRA_BSSID = "bssid";
	public static final String TAG = "WifiTest";
	String targetBssid;
	WifiManager wifiManager;
	private boolean found;
	private String level;
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 begin
	private Timer timer;
	private TimerTask timerTask;
	private static final int START_SCAN = 0;
	private Handler mUiHandler;
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 end
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Intent it = this.getIntent();
		targetBssid = it.getStringExtra(EXTRA_BSSID);
		if( null != targetBssid) {
			Toast.makeText(this, "bssid : " + targetBssid, Toast.LENGTH_LONG).show();
		}
                //Gionee <GN_BSP_AUTOMMI><lifeilong><20161230> add for ID 57639 begin
                Log.e(TAG,"bssid : " + targetBssid );
                //Gionee <GN_BSP_AUTOMMI><lifeilong><20161230> add for ID 57639 end
        
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		//Gionee zhangke 20160217 delete for CR01634523 start
		/*
		resRec = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Log.i(TAG,"onReceive action found="+found);
				if (found == true)
					return;
				if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
					if(analyzeScanResults()) {
						found = true;
						((AutoMMI)getApplication()).recordResult(TAG, level, "1");
					} else {
						((AutoMMI)getApplication()).recordResult(TAG, "", "0");
					};
				}
			}
			
		};
        */
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 begin
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case START_SCAN:
                        wifiManager.startScan();
                        break; 
                }
            }
        };

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.e(TAG,"restart Scan");
                mUiHandler.sendEmptyMessage(START_SCAN);
            }
        };
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 end

        Log.e(TAG,"targetBssid = " + targetBssid);
        Log.e(TAG,"Begin Wifitest and reset Result ");        
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
	
		//Gionee zhangke 20160217 delete for CR01634523 end
	}

	//Gionee zhangke 20160217 modify for CR01634523 start
	private BroadcastReceiver resRec = new BroadcastReceiver() {
	
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.i(TAG,"onReceive action found="+found);
			if (found == true)
				return;
			if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				if(analyzeScanResults()) {
					found = true;
                    Log.e(TAG,"Find targetBssid"+ targetBssid);
					((AutoMMI)getApplication()).recordResult(TAG, level, "1");
                    //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 begin
                    if(timer != null){
                        timer.cancel();
                        timer = null;
                        Log.e(TAG,"== timer.cancel() ==");
                     }                    
				} else {
                    Log.e(TAG,"Not Find targetBssid"+ targetBssid);
	            //((AutoMMI)getApplication()).recordResult(TAG, "", "0");
                    
                    analyzeScanResults();
                    //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 end
				};
			}
		}
		
	};
	//Gionee zhangke 20160217 modify for CR01634523 end

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		this.registerReceiver(resRec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		//Gionee zhangke 20160217 modify for CR01634523 start
		Log.i(TAG,"onStart mWifiMgr.isWifiEnabled()="+wifiManager.isWifiEnabled());
		if (false == wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
			new Thread(
				new Runnable() {
					public void run() {
					int i = 0;
					try {

						while (false == wifiManager.isWifiEnabled()) {
							Thread.sleep(1000);
							i++;
							if (i > 20) {
								return;
							}
						}
			//wifiManager.startScan();
                        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 begin
                        timer.schedule(timerTask,0,2000);
                        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 end
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						Log.e(TAG, "onStart InterruptedException=" + e.getMessage());
					}
				}
			}).start();

		}else{
			Log.i(TAG,"onStart wifiManager.startScan()");
			//wifiManager.startScan();
                        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 begin
                        timer.schedule(timerTask,0,2000);
                        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 end
		}
		//Gionee zhangke 20160217 modify for CR01634523 end
	}

	private boolean analyzeScanResults() {
		// TODO Auto-generated method stub
		List<ScanResult>  rs = wifiManager.getScanResults();
		for (ScanResult i : rs) {
			//Log.d(TAG,i.BSSID);
                        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 begin
			//Log.d(TAG, ""+i.level);
			//Log.i(TAG,"analyzeScanResults:targetBssid="+targetBssid+";BSSID="+i.BSSID);
			if(targetBssid.equalsIgnoreCase(i.BSSID)) {
				level = String.valueOf(i.level);
				return true;
			}
		}
		Log.i(TAG,"analyzeScanResults return false ");
        
		//Gionee zhangke 20160217 modify for CR01634523 start
		/*if(rs == null || rs.size() == 0){
			Log.i(TAG,"analyzeScanResults getScanResults=null");
			wifiManager.startScan();
		}*/
                //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 end
		//Gionee zhangke 20160217 modify for CR01634523 end
		return false;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.unregisterReceiver(resRec);
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 begin
        if(timer != null){
           timer.cancel();
           timer = null;
           Log.e(TAG,"== timer.cancel() ==");
        }
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 end
		this.finish();
	}
}
