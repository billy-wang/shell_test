package com.gionee.autommi;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Filter;
//Gionee zhangke 20151027 add for CR01575479 start
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
//Gionee zhangke 20151027 add for CR01575479 end

public class BluetoothTest extends BaseActivity {
	private static final String EXTRA_MAC = "mac";
	public static final String TAG = "BluetoothTest";
	private BluetoothAdapter btAdapter;
	private String expDev;
	private boolean found;
	//Gionee zhangke 20151027 add for CR01575479 start
	private ListView mBtLv;
	private TextView mTv;
	private ArrayAdapter mArrayAdapter;
	//Gionee zhangke 20151027 add for CR01575479 end

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
 		//Gionee zhangke 20151027 add for CR01575479 start
 		setContentView(R.layout.bluetooth_test);
		mTv = (TextView) findViewById(R.id.bt_title);
        mBtLv = (ListView) findViewById(R.id.bt_content);
		mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
		mBtLv.setAdapter(mArrayAdapter);
 		//Gionee zhangke 20151027 add for CR01575479 end
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		Intent it = this.getIntent();
		expDev = it.getStringExtra(EXTRA_MAC);
        Log.i(TAG, "expDev="+expDev);
	}

	private BroadcastReceiver resultProc = new BroadcastReceiver() {
	
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			//Gionee zhangke add for CR01569170 start
			String action = intent.getAction();
			Log.i(TAG, "action = " + action);
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
				if ( rssi >= 128) {
					rssi -= 256;
				}
				Log.i(TAG, "address = " + dev.getAddress()+";rssi=" + rssi);
				if(!found){
					if(expDev.equalsIgnoreCase(dev.getAddress())) {
						found = true;
						Log.i(TAG, "Bluetooth is found!!!");
						((AutoMMI)getApplication()).recordResult(TAG, "" + rssi,"1");
						Toast.makeText(context, getString(R.string.search_success), Toast.LENGTH_SHORT).show();
					}
				}
				//Gionee zhangke 20151027 add for CR01575479 start
				String name = dev.getName();
				String address = dev.getAddress();
				if (null != name)
					mArrayAdapter.add(name + "\n" + address);
				else
					mArrayAdapter.add(address + "\n" + address);
				mTv.setText(BluetoothTest.this.getResources().getString(
						R.string.find_bluetooth_device_num,
						String.valueOf(mArrayAdapter.getCount())));
				//Gionee zhangke 20151027 add for CR01575479 end
	
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				if(!found){
					Log.e(TAG, "Bluetooth is not found!!!");
					((AutoMMI)getApplication()).recordResult(TAG, "", "0");
					//Gionee zhangke 20151027 add for CR01575479 start
					Toast.makeText(context, getString(R.string.search_fail), Toast.LENGTH_SHORT).show();
					//Gionee zhangke 20151027 add for CR01575479 end
				}
			}
			//Gionee zhangke add for CR01569170 end
		}		
	};

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		this.registerReceiver(resultProc, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		this.registerReceiver(resultProc, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		//Gionee zhangke add for CR01569170 start
		if (false == btAdapter.isEnabled()) {
			btAdapter.enable();
			new Thread(new Runnable() {
				public void run() {
					int i = 0;
					try {
						while (!btAdapter.isEnabled()) {
							Thread.sleep(1000);
							i++;
							if (i > 20) {
								Log.e(TAG, "Bluetooth cannot open!!!");
								((AutoMMI)getApplication()).recordResult(TAG, "", "0");
								return;
							}
						}
						//Gionee zhangke 20151027 add for CR01575479 start
                        btAdapter.startDiscovery();
						//Gionee zhangke 20151027 add for CR01575479 end
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
		} else {
			btAdapter.startDiscovery();
		}
		//Gionee zhangke 20151027 add for CR01575479 start
		mTv.setText(R.string.scanning_bluetooth_device);
		//Gionee zhangke 20151027 add for CR01575479 start
		//Gionee zhangke add for CR01569170 end
		
	}
    
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//this.unregisterReceiver(sentinel);
		this.unregisterReceiver(resultProc);
		btAdapter.cancelDiscovery();
		this.finish();
	}


}
