package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
//Gionee zhangke 20151016 add for CR01568021 start
import android.util.Log;
//Gionee zhangke 20151016 add for CR01568021 end
import android.content.Intent;


public class WIFITest extends Activity implements View.OnClickListener {
    WifiManager mWifiMgr;
    TextView mTitleTv;
    Button mScanBtn;
    ArrayAdapter mArrayAdapter;
    Handler mUiHandler;
    ListView mContentLv;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "WIFITest";
    TelephonyManager mTeleMgr;
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 end

    static final int INIT_WIFI_FAIL = 0;
    static final int BEGIN_TO_SCAN = 1, EVENT_RESPONSE_SN_WRITE = 2;
     //Gionee <GN_BSP_MMI> <lifeilong> <20170609> modify for ID 154572 being
    private int scanTime = 10000;
     //Gionee <GN_BSP_MMI> <lifeilong> <20170609> modify for ID 154572 end
     private boolean wifiFlag = false;
     private Intent it;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        it = this.getIntent();
        if(it != null){
            wifiFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"wifiFlag = " + wifiFlag);

        mWifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        setContentView(R.layout.wifi_test);
        mTitleTv = (TextView) findViewById(R.id.wifi_title);
        mContentLv = (ListView) findViewById(R.id.wifi_content);
        mScanBtn = (Button) findViewById(R.id.scan_wifi);

        mScanBtn.setOnClickListener(this);
        mTeleMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        mContentLv.setAdapter(mArrayAdapter);
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case INIT_WIFI_FAIL:
                        //Gionee zhangke 20151016 add for CR01568021 start
                        Log.i(TAG, "mUiHandler INIT_WIFI_FAIL");
                        //Gionee zhangke 20151016 add for CR01568021 end 

                        mTitleTv.setText(R.string.init_wifi_fail);
                        break;
                    case BEGIN_TO_SCAN:
                        
                        //Gionee zhangke 20151016 add for CR01568021 start
                        Log.i(TAG, "mUiHandler BEGIN_TO_SCAN");
                        //Gionee zhangke 20151016 add for CR01568021 end 

                        mTitleTv.setText(R.string.scanning_wifi);
                        mWifiMgr.startScan();
                        mScanBtn.setEnabled(true);
                        break;
                }
            }
        };
        if (mWifiMgr == null) {
            finish();
        }

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setVisibility(View.INVISIBLE);
        if(wifiFlag){
            TestUtils.asResult(TAG,"","2");
            mRestartBtn.setVisibility(View.INVISIBLE);
        } 

        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
        
        @Override
        public void run() {
                // TODO Auto-generated method stub
                mIsTimeOver = true;
                if(mIsPass){
                    mRightBtn.setEnabled(true);
                }

                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(WIFITest.this);
                mWrongBtn.setOnClickListener(WIFITest.this);
                mRestartBtn.setOnClickListener(WIFITest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mWifiMgr) {
            this.unregisterReceiver(mReceiver);
        }
        mUiHandler.removeMessages(BEGIN_TO_SCAN);
        if(wifiFlag){
            this.finish();
            Log.d(TAG,"onStop as_record_finish_self");
        }        
    }

    //Gionee zhangke 20151016 add for CR01568021 start
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mWifiMgr.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                List<ScanResult> results = mWifiMgr.getScanResults();
                mArrayAdapter.clear();
                for (int i = 0; i < results.size(); i++) {
                    if ((2400 <= results.get(i).frequency && results.get(i).frequency <= 2500)) {
                        mArrayAdapter.add("SSID: " + results.get(i).SSID + "\nBSSID: "
                                + results.get(i).BSSID + "\ncapabilities: "
                                + results.get(i).capabilities + "\nlevel: " + results.get(i).level + "\nfrequency: " + results.get(i).frequency);
                    }
                    Log.d(TAG,"SSID: " + results.get(i).SSID + "\nBSSID: "
                                + results.get(i).BSSID + "\ncapabilities: "
                                + results.get(i).capabilities + " \nlevel: " + results.get(i).level + "\nfrequency: " + results.get(i).frequency + "\n");
                }
                int count = mArrayAdapter.getCount();
                Log.i(TAG, "onReceive count=" + count);
                //Gionee <GN_BSP_MMI> <lifeilong> <20170609> modify for ID 154572 being
                if (count > 0) {
                    mTitleTv.setText(WIFITest.this.getResources().getString(R.string.find_wifi_num, String.valueOf(count)));
                    mIsPass = true;
                    if(mIsTimeOver){
                        mRightBtn.setEnabled(true);
                        mRightBtn.setVisibility(View.VISIBLE);
                    }
                } 
                Log.e(TAG,"scanTime = " + scanTime);
                mUiHandler.sendEmptyMessageDelayed(BEGIN_TO_SCAN,scanTime);
                //Gionee <GN_BSP_MMI> <lifeilong> <20170609> modify for ID 154572 end
            }
        }
    };
    //Gionee zhangke 20151016 add for CR01568021 end 

    @Override
    protected void onStart() {
        super.onStart();
        mScanBtn.setEnabled(false);
        //Gionee zhangke 20151016 add for CR01568021 start
        Log.i(TAG, "onStart mWifiMgr.isWifiEnabled()=" + mWifiMgr.isWifiEnabled());
        IntentFilter filter = new IntentFilter(mWifiMgr.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);
        Log.i(TAG, "registerReceiver");
        //Gionee zhangke 20151016 add for CR01568021 end 

        if (false == mWifiMgr.isWifiEnabled()) {
            mTitleTv.setText(R.string.opening_wifi);
            mWifiMgr.setWifiEnabled(true);
            new Thread(
                    new Runnable() {
                        public void run() {
                            int i = 0;
                            try {
                                //Gionee zhangke 20151016 add for CR01568021 start
                                Log.i(TAG, "onStart2 mWifiMgr.isWifiEnabled()=" + mWifiMgr.isWifiEnabled());
                                //Gionee zhangke 20151016 add for CR01568021 end 

                                while (false == mWifiMgr.isWifiEnabled()) {
                                    //Gionee zhangke 20151016 add for CR01568021 start
                                    Log.i(TAG, "onStart3 mWifiMgr.isWifiEnabled()=" + mWifiMgr.isWifiEnabled());
                                    //Gionee zhangke 20151016 add for CR01568021 end 

                                    Thread.sleep(1000);
                                    i++;
                                    if (i > 20) {
                                        mUiHandler.sendEmptyMessage(INIT_WIFI_FAIL);
                                        return;
                                    }
                                }
                                mUiHandler.sendEmptyMessage(BEGIN_TO_SCAN);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                //Gionee zhangke 20151016 add for CR01568021 start
                                Log.e(TAG, "onStart InterruptedException=" + e.getMessage());
                                //Gionee zhangke 20151016 add for CR01568021 end 

                            }
                        }
                    }).start();
        } else {
            mTitleTv.setText(R.string.scanning_wifi);
            // Gionee xiaolin 20120925 add for CR00693699 start
            if (TestUtils.mIsAutoMode) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.out.println(ex);
                }
            }
            // Gionee xiaolin 20120925 add for CR00693699 end
            mWifiMgr.startScan();
            mScanBtn.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.scan_wifi: {
                mArrayAdapter.clear();
                mTitleTv.setText(WIFITest.this.getResources().getString(R.string.find_wifi_num, String.valueOf(mArrayAdapter.getCount())));
                mWifiMgr.startScan();
                break;
            }

            case R.id.right_btn: {
                if(wifiFlag){
                    TestUtils.asResult(TAG,"","1");
                }

                mRightBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("53", "P");
                    editor.commit();
                }
                TestUtils.rightPress(TAG, WIFITest.this);
                break;
            }

            case R.id.wrong_btn: {
                if(wifiFlag){
                    TestUtils.asResult(TAG,"","0");
                }                
                mRightBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("53", "F");
                    editor.commit();
                }
                TestUtils.wrongPress(TAG, WIFITest.this);
                break;
            }

            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
