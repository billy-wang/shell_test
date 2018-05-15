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
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
import android.os.SystemClock;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end

public class WIFITest extends Activity implements View.OnClickListener {
    public static final String FACTORY_WIFI = "wifi";
    WifiManager mWifiMgr;
    TextView mTitleTv;
    Button mScanBtn;
    BroadcastReceiver mReceiver;
    ArrayAdapter mArrayAdapter;
    Handler mUiHandler;
    ListView mContentLv;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "WIFITest";
    boolean mIsPass;
	//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
	private Timer timer;
	private TimerTask timerTask;
	//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end

    static final int INIT_WIFI_FAIL = 0;
    static final int BEGIN_TO_SCAN = 1, EVENT_RESPONSE_SN_WRITE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);

        mWifiMgr = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        setContentView(R.layout.wifi_test);
        mTitleTv = (TextView) findViewById(R.id.wifi_title);
        mContentLv = (ListView) findViewById(R.id.wifi_content);
        mScanBtn = (Button) findViewById(R.id.scan_wifi);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);

        mScanBtn.setOnClickListener(this);
        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        mContentLv.setAdapter(mArrayAdapter);
        //Gionee <GN_BSP_MMI><lifeilong><20161212> add for ID 43494 begin
        // Disable tethering if enabling Wifi
        int wifiApState = mWifiMgr.getWifiApState();
        if ((wifiApState == mWifiMgr.WIFI_AP_STATE_ENABLING) ||
        (wifiApState == mWifiMgr.WIFI_AP_STATE_ENABLED)) {
        mWifiMgr.setWifiApEnabled(null, false);

        }		
		//Gionee <GN_BSP_MMI><lifeilong><20161212> add for ID 43494 end

		//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
	    timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
				mUiHandler.sendEmptyMessage(BEGIN_TO_SCAN);
            }
        };
		//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case INIT_WIFI_FAIL:
                    mTitleTv.setText(R.string.init_wifi_fail);
                    break;
                case BEGIN_TO_SCAN:
					mScanBtn.setEnabled(true);
					//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
					Log.e(TAG," BEGIN_TO_SCAN ");
					mTitleTv.setText(R.string.scanning_wifi);
					//mArrayAdapter.clear();
					mTitleTv.setText(WIFITest.this.getResources().getString(R.string.find_wifi_num,
							String.valueOf(mArrayAdapter.getCount())));
					mWifiMgr.startScan();
					//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end
                    break;
                }
            }
        };
        if (mWifiMgr == null) {
            finish();
        }
    }

	

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mWifiMgr) {
            this.unregisterReceiver(mReceiver);
        }
		//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
		if(timer != null){
           timer.cancel();
		}
		//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG," BEGIN_TO  onStart ");
        mScanBtn.setEnabled(false);
        if (false == mWifiMgr.isWifiEnabled()) {
            mTitleTv.setText(R.string.opening_wifi);
            mWifiMgr.setWifiEnabled(true);
            new Thread(new Runnable() {
                public void run() {
                    int i = 0;
                    try {
                        while (false == mWifiMgr.isWifiEnabled()) {
                            Thread.sleep(1000);
                            i++;
                            if (i > 20) {
                                mUiHandler.sendEmptyMessage(INIT_WIFI_FAIL);
                                return;
                            }
                        }
                        //Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
                        Log.e(TAG," BEGIN_TO_SCAN ");
                        //Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end
                        //Gionee <GN_BSP_MMI><lifeilong><20161212> add for ID 43494 begin
                        //mUiHandler.sendEmptyMessage(BEGIN_TO_SCAN);
                        timer.schedule(timerTask,3000,4000);
                        //Gionee <GN_BSP_MMI><lifeilong><20161212> add for ID 43494 end
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            mTitleTv.setText(R.string.scanning_wifi);
            mWifiMgr.startScan();
            mScanBtn.setEnabled(true);
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
            timer.schedule(timerTask,3000,4000);  
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end
        }
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mWifiMgr.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                    List<ScanResult> results = mWifiMgr.getScanResults();
                    mArrayAdapter.clear();
                    for (int i = 0; i < results.size(); i++) {
                        mArrayAdapter.add(
                                "SSID: " + results.get(i).SSID + "\nBSSID: " + results.get(i).BSSID + "\ncapabilities: "
                                        + results.get(i).capabilities + "\nlevel: " + results.get(i).level);
                    }
                    int count = mArrayAdapter.getCount();
                    if (count > 0) {
                        mTitleTv.setText(
                                WIFITest.this.getResources().getString(R.string.find_wifi_num, String.valueOf(count)));
                        mRightBtn.setEnabled(true);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(mWifiMgr.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.scan_wifi: {
            mArrayAdapter.clear();
            mTitleTv.setText(WIFITest.this.getResources().getString(R.string.find_wifi_num,
                    String.valueOf(mArrayAdapter.getCount())));
            mWifiMgr.startScan();
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
			if(timer != null){
			   timer.cancel();
			   Log.e(TAG,"== timer.cancel() ==");
			}
			timer = new Timer();
			timerTask = new TimerTask() {
				@Override
				public void run() {
					mUiHandler.sendEmptyMessage(BEGIN_TO_SCAN);
				}
			};
            timer.schedule(timerTask,3000,4000);
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end
            break;
        }

        case R.id.right_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            if (TestUtils.mIsAutoMode) {
                SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                editor.putString(TestUtils.factoryFlag.get(FACTORY_WIFI), "P");
                editor.commit();
            }
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
			if(timer != null){
			   timer.cancel();
			   Log.e(TAG,"== timer.cancel() ==");
			}
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end
            TestUtils.rightPress(TAG, WIFITest.this);
            // }
            break;
        }

        case R.id.wrong_btn: {

            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            if (TestUtils.mIsAutoMode) {
                SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                editor.putString(TestUtils.factoryFlag.get(FACTORY_WIFI), "F");
                editor.commit();
            }
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
			if(timer != null){
			   timer.cancel();
			   Log.e(TAG,"== timer.cancel() ==");
			}
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end
            TestUtils.wrongPress(TAG, WIFITest.this);
            break;
        }

        case R.id.restart_btn: {
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 begin
			if(timer != null){
			   timer.cancel();
			   Log.e(TAG,"== timer.cancel() ==");
			}
			//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID38422 end
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
