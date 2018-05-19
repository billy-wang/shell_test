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
import android.widget.TextView;

public class WifiTest extends BaseActivity {
	private static final String EXTRA_BSSID = "bssid";
	public static final String TAG = "WifiTest";
        String targetBssid;
        WifiManager wifiManager;
	private boolean found;
	private String level;
        private boolean flag = false;
        private TestResult tr;
        private TextView mTv;
        private String wifiTest;
        private String succ;
        private String fail;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            setContentView(R.layout.wifi_test);
            mTv = (TextView) findViewById(R.id.wifi_title);
            wifiTest = getResources().getString(R.string.wifiStart);
            mTv.setText(wifiTest);
            succ = getResources().getString(R.string.success);
            fail = getResources().getString(R.string.fail);
            Intent it = this.getIntent();
            if(it != null){
                flag = it.getBooleanExtra("as", false);
            }
            Log.d(TAG, "wifi flag = " + flag);
            targetBssid = it.getStringExtra(EXTRA_BSSID);
            if( null != targetBssid) {
                Toast.makeText(this, "bssid : " + targetBssid, Toast.LENGTH_LONG).show();
            }
            tr = new TestResult();
            wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            if(flag){
                ((AutoMMI)getApplication()).asResult(TAG, "", "2");
            } else {
                ((AutoMMI)getApplication()).recordResult(TAG, "", "0");
            }
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
                            if(flag){
                                ((AutoMMI)getApplication()).asResult(TAG, level, "1");
                            } else {
                                showResult("P");
                                ((AutoMMI)getApplication()).recordResult(TAG, level, "1");
                            }
                            mTv.setText(wifiTest + " : " + succ);
                        } else {
                            if(flag){
                                ((AutoMMI)getApplication()).asResult(TAG, "", "0");
                            } else {
                                showResult("F");
                                ((AutoMMI)getApplication()).recordResult(TAG, "", "0");
                            }
                            mTv.setText(wifiTest + " : " + fail);
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
                                wifiManager.startScan();
                            } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        Log.e(TAG, "onStart InterruptedException=" + e.getMessage());
                        }
                        }
                    }).start();
                }else{
                Log.i(TAG,"onStart wifiManager.startScan()");
                    wifiManager.startScan();
            }
                //Gionee zhangke 20160217 modify for CR01634523 end
	}

	private boolean analyzeScanResults() {
            // TODO Auto-generated method stub
            List<ScanResult>  rs = wifiManager.getScanResults();
            for (ScanResult i : rs) {
                //Log.d(TAG,i.BSSID);                
                Log.d(TAG, ""+i.level);
                Log.i(TAG,"analyzeScanResults:targetBssid="+targetBssid+";BSSID="+i.BSSID);
                if(targetBssid.equalsIgnoreCase(i.BSSID)) {
                    level = String.valueOf(i.level);
                    return true;
                }
            }
            Log.i(TAG,"analyzeScanResults return false ");
            //Gionee zhangke 20160217 modify for CR01634523 start
            if(rs == null || rs.size() == 0){
                Log.i(TAG,"analyzeScanResults getScanResults=null");
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


    private void showResult(String s) {
        byte[] sn_buff = new byte[TestResult.SN_LENGTH];
        System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, TestResult.SN_LENGTH);
        sn_buff = tr.getNewSN(TestResult.MMI_WIFI_TAG, s, sn_buff);
        tr.writeToProductInfo(sn_buff);
        System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, TestResult.SN_LENGTH);
        Log.d(TAG,"  sn_buff[TestResult.MMI_WIFI_TAG]  = " +  sn_buff[TestResult.MMI_WIFI_TAG] + "  , s = " + s);
    }    
}
