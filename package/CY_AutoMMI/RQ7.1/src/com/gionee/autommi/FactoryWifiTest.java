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
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import android.content.SharedPreferences;
import android.widget.TextView;

    public class FactoryWifiTest extends BaseActivity implements QcRilHookCallback {
        private static final String EXTRA_BSSID = "bssid";
        public static final String TAG = "FactoryWifiTest";
        private static final String EXTRA_MIN = "min";
        private static final String EXTRA_MAX = "max";
        private static final String EXTRA_MAC = "mac";
        private static final String FACTORY_WIFI = "wifi";
        private float mMin;
        private float mMax;
        private float mLevel;
        private TextView mTv;
        private String wifiTest;
        private String succ;
        private String fail;        
        private boolean mIsWifiOk = false ;
        private SharedPreferences mResultSP;
        private SharedPreferences mSNResultSP;
        private SharedPreferences.Editor mSNEditor;
        private int mCount;
        private QcNvItems nvItems = null;
        String targetBssid;
        WifiManager wifiManager;
        private boolean found;
        private String level = "0";
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 begin
        private Timer timer;
        private TimerTask timerTask;
        private static final int START_SCAN = 0;
        private Handler mUiHandler;
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161217> modify for ID 49101 end
        private int wifiCount = 0;
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
        if( null != it) {
            targetBssid = it.getStringExtra(EXTRA_MAC);
            mMin = Float.parseFloat(it.getStringExtra(EXTRA_MIN));
            mMax = Float.parseFloat(it.getStringExtra(EXTRA_MAX));
        }
        if( null != targetBssid) {
            Toast.makeText(this, "bssid : " + targetBssid, Toast.LENGTH_LONG).show();
        }
        Log.e(TAG,"bssid : " + targetBssid );
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
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
        ((AutoMMI)getApplication()).recordResult(TAG, "", "2");
        //Gionee zhangke 20160217 delete for CR01634523 end
    }

	//Gionee zhangke 20160217 modify for CR01634523 start
	private BroadcastReceiver resRec = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO Auto-generated method stub
                Log.i(TAG,"onReceive action found="+found);
                if (found == true){
                    return;
                }
                if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    if(analyzeScanResults()) {
                        found = true;
                        Log.e(TAG,"Find targetBssid"+ targetBssid);
                        if(timer != null){
                            timer.cancel();
                            timer = null;
                            Log.e(TAG,"== timer.cancel() ==");
                        }
                        nvItems = new QcNvItems(FactoryWifiTest.this, FactoryWifiTest.this);
                    } else {
                        Log.e(TAG,"Not Find targetBssid  =  "+ targetBssid);
                        wifiCount++;
                        if(wifiCount < 4){
                            analyzeScanResults();
                        } else {
                            Log.d(TAG,"wificount >= 4 not find target mac ");
                            if(timer != null){
                                timer.cancel();
                                timer = null;
                                Log.e(TAG,"== timer.cancel() ==");
                            }
                            nvItems = new QcNvItems(FactoryWifiTest.this, FactoryWifiTest.this);
                        }
                    }
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
                            timer.schedule(timerTask,0,2000);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                Log.e(TAG, "onStart InterruptedException=" + e.getMessage());
                            }
                        }
                }).start();
            }else{
                Log.i(TAG,"onStart wifiManager.startScan()");
                timer.schedule(timerTask,0,2000);
            }
            //Gionee zhangke 20160217 modify for CR01634523 end
	}

	private boolean analyzeScanResults() {
            // TODO Auto-generated method stub
            List<ScanResult>  rs = wifiManager.getScanResults();
            for (ScanResult i : rs) {
                Log.d(TAG,i.BSSID);
                Log.d(TAG, ""+i.level);
                Log.d(TAG,"analyzeScanResults:targetBssid="+targetBssid+";BSSID="+i.BSSID);
                if(targetBssid.equalsIgnoreCase(i.BSSID)) {
                    level = String.valueOf(i.level);
                    mLevel = mLevel =  Float.parseFloat(level);
                    if(mLevel >= mMin && mLevel <=mMax){
                        Log.d(TAG,"----wifi is ok");
                        mIsWifiOk = true ;
                    } else {
                        Log.d(TAG,"----wifi is fail" + "  , targetBssid = " + targetBssid + "  , mLevel = " + mLevel);
                        mIsWifiOk = false ;
                    }                    
                    return true;
                }
            }
            Log.i(TAG,"analyzeScanResults return false ");
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
            nvItems.dispose();
            this.finish();
	}
	public void onQcRilHookReady() {
            Log.e(TAG,"onQcRilHookReady  start ");
            mCount = 0;
            try {
                Context context = createPackageContext("gn.com.android.mmitest", Activity.CONTEXT_IGNORE_SECURITY);
                mResultSP = context.getSharedPreferences("gn_mmi_test", Context.MODE_WORLD_WRITEABLE);
                mSNResultSP = context.getSharedPreferences("gn_mmi_sn", Context.MODE_WORLD_WRITEABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String factoryResult = "";
            try {
                String oFS = nvItems.getFactoryResult();
                Log.e(TAG, "oFS= " + oFS);
                String nFS = getNewFactorySet(oFS);
                Log.e(TAG, "nFS= " + nFS);
                nvItems.setFactoryResult(nFS + "0");
                factoryResult = nvItems.getFactoryResult();
                Log.d(TAG, "factoryResult = " + factoryResult + " : " + factoryResult.length());
                String factoryResult11 = nvItems.getFactoryResult();
                Log.d(TAG, "factoryResul1 = " + factoryResult11 + " : " + factoryResult11.length());
            } catch (Exception e) {
                Log.e(TAG,"fail");
                e.printStackTrace();
            }
    }
    private String getNewFactorySet(String old) {
        StringBuilder sb = new StringBuilder(old);
        Log.e(TAG,"old = " + old );
        if(mIsWifiOk){
            mSNEditor = mSNResultSP.edit();
            mSNEditor.putString(TestUtils.factoryFlag.get(FACTORY_WIFI), "P");
            mSNEditor.commit();
            sb.setCharAt(15,'P');
            Log.d(TAG,"---write wifi factory flag is  P ");
            mTv.setText(wifiTest + " : " + succ + "  , mMax = " + mMax + "  , mMin " + mMin
                                    + "\n" + " targetBssid = " + targetBssid + "  , mLevel = " + mLevel);
            ((AutoMMI)getApplication()).recordResult(TAG, level, "1");
        }else{
            mSNEditor = mSNResultSP.edit();
            mSNEditor.putString(TestUtils.factoryFlag.get(FACTORY_WIFI), "F");
            mSNEditor.commit();
            sb.setCharAt(15,'F');
            Log.d(TAG,"---write wifi factory flag is  F ");
            mTv.setText(wifiTest + " : " + fail + "  , mMax = " + mMax + "  , mMin " + mMin
                                    + "\n" + " targetBssid = " + targetBssid + "  , mLevel = " + mLevel);
            ((AutoMMI)getApplication()).recordResult(TAG, level, "0");
        }
        return sb.toString();
    }

}
