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
import com.gionee.util.DswLog;
import android.widget.Toast;

public class WifiTest extends BaseActivity {
	private static final String EXTRA_BSSID = "bssid";
	public static final String TAG = "WifiTest";
	String targetBssid;
	WifiManager wifiManager;
	private boolean found;
	private String level;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Intent it = this.getIntent();
		targetBssid = it.getStringExtra(EXTRA_BSSID);
		if( null != targetBssid) {
			Toast.makeText(this, "bssid : " + targetBssid, Toast.LENGTH_LONG).show();
		} 
        
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		//Gionee zhangke 20160217 delete for CR01634523 start
		/*
		resRec = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				DswLog.i(TAG,"onReceive action found="+found);
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
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
	
		//Gionee zhangke 20160217 delete for CR01634523 end
	}

	//Gionee zhangke 20160217 modify for CR01634523 start
	private BroadcastReceiver resRec = new BroadcastReceiver() {
	
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			DswLog.i(TAG,"onReceive action found="+found);
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
	//Gionee zhangke 20160217 modify for CR01634523 end

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		this.registerReceiver(resRec, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		//Gionee zhangke 20160217 modify for CR01634523 start
		DswLog.i(TAG,"onStart mWifiMgr.isWifiEnabled()="+wifiManager.isWifiEnabled());
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
						wifiManager.startScan();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						DswLog.e(TAG, "onStart InterruptedException=" + e.getMessage());
					}
				}
			}).start();

		}else{
			DswLog.i(TAG,"onStart wifiManager.startScan()");
			wifiManager.startScan();
		}
		//Gionee zhangke 20160217 modify for CR01634523 end
	}

	private boolean analyzeScanResults() {
		// TODO Auto-generated method stub
		List<ScanResult>  rs = wifiManager.getScanResults();
		for (ScanResult i : rs) {
			//DswLog.d(TAG,i.BSSID);
			DswLog.d(TAG, ""+i.level);
			DswLog.i(TAG,"analyzeScanResults:targetBssid="+targetBssid+";BSSID="+i.BSSID);
			if(targetBssid.equalsIgnoreCase(i.BSSID)) {
				level = String.valueOf(i.level);
				return true;
			}
		}
		DswLog.i(TAG,"analyzeScanResults return false ");
		//Gionee zhangke 20160217 modify for CR01634523 start
		if(rs == null || rs.size() == 0){
			DswLog.i(TAG,"analyzeScanResults getScanResults=null");
			wifiManager.startScan();
		}
		//Gionee zhangke 20160217 modify for CR01634523 end
		return false;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.unregisterReceiver(resRec);
		this.finish();
	}
}
