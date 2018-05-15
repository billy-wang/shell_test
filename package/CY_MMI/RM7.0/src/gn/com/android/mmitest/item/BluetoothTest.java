
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class BluetoothTest extends Activity implements View.OnClickListener {
    private BluetoothAdapter mBluetoothAdapter;

    private TextView mTv;

    private BroadcastReceiver mReceiver;

    private ArrayAdapter mArrayAdapter;

    private Handler mUiHandler;

    private ListView mBtLv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private boolean mIsDiscovery;

    private static final int INIT_BT_FAIL = 0;

    private static final int BEGIN_TO_SCAN = 1, EVENT_RESPONSE_SN_WRITE = 2;

    private static String TAG = "BluetoothTest";
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 start

    private boolean btFlag = false;
    private Intent it;


/*
 * http://www.open-open.com/lib/view/open1335146166780.html
 * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        it = this.getIntent();
        if(it != null){
            btFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"btFlag = " + btFlag);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setContentView(R.layout.bluetooth_test);
        mTv = (TextView) findViewById(R.id.bt_title);
        mBtLv = (ListView) findViewById(R.id.bt_content);

        mArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        mBtLv.setAdapter(mArrayAdapter);
        mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case INIT_BT_FAIL:
                        mTv.setText(R.string.init_bluetooth_fail);
                        break;
                    case BEGIN_TO_SCAN:
                        mTv.setText(R.string.scanning_bluetooth_device);
                        mBluetoothAdapter.startDiscovery();
                        mIsDiscovery = true;
                        break;
                }
            }
        };
        if (mBluetoothAdapter == null) {
            finish();
        }

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setVisibility(View.INVISIBLE);
        if(btFlag){
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
                    mRightBtn.setVisibility(View.VISIBLE);
                }
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(BluetoothTest.this);
                mWrongBtn.setOnClickListener(BluetoothTest.this);
                mRestartBtn.setOnClickListener(BluetoothTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if (null != mBluetoothAdapter) {
            if (true == mIsDiscovery) {
                mBluetoothAdapter.cancelDiscovery();
            }
            this.unregisterReceiver(mReceiver);
        }
        // Gionee xiaolin 20121023 add for CR00717119 start
        exceptionHd.removeMessages(TRY_AGAIN);
        // Gionee xiaolin 20121023 add for CR00717119 end*/
        if(btFlag){
            this.finish();
            Log.d(TAG,"onStop as_record_finish_self");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsDiscovery = false;
        if (false == mBluetoothAdapter.isEnabled()) {
            mTv.setText(R.string.opening_bluetooth);
            mBluetoothAdapter.enable();
            new Thread(new Runnable() {
                public void run() {
                    int i = 0;
                    try {
                        while (!mBluetoothAdapter.isEnabled()) {
                            Thread.sleep(1000);
                            i++;
                            if (i > 20) {
                                mUiHandler.sendEmptyMessage(INIT_BT_FAIL);
                                return;
                            }
                        }
                        mUiHandler.sendEmptyMessage(BEGIN_TO_SCAN);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            mTv.setText(R.string.scanning_bluetooth_device);
            mBluetoothAdapter.startDiscovery();
            mIsDiscovery = true;
        }
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("lich", "intent " + intent.toString());
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Gionee xiaolin 20120710 modify for CR00637228 start
                    String name = device.getName();
                    String address = device.getAddress();
                    if (null != name)
                        mArrayAdapter.add(name + "\n" + address);
                    else
                        mArrayAdapter.add(address + "\n" + address);
                    // Gionee xiaolin 20120710 modify for CR00637228 end
                    mTv.setText(BluetoothTest.this.getResources().getString(
                            R.string.find_bluetooth_device_num,
                            String.valueOf(mArrayAdapter.getCount())));
                    //Gionee zhangke 20160428 modify for CR01687958 start
                    Log.d(TAG,"name = " + name + "  , address  = " + address);
                    mIsPass = true;
                    if(mIsTimeOver){
                        mRightBtn.setEnabled(true);
                        mRightBtn.setVisibility(View.VISIBLE);
                    }
                    //Gionee zhangke 20160428 modify for CR01687958 end

                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        // Gionee xiaolin 20121023 add for CR00717119 start
        exceptionHd.sendEmptyMessageDelayed(TRY_AGAIN, 12000);
        // Gionee xiaolin 20121023 add for CR00717119 end
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                if(btFlag){
                    TestUtils.asResult(TAG,"","1");
                }
                releaseBluetooth();
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("51", "P");
                    editor.commit();
                }
                TestUtils.rightPress(TAG, BluetoothTest.this);
                break;
            }

            case R.id.wrong_btn: {
                if(btFlag){
                    TestUtils.asResult(TAG,"","0");
                }
                releaseBluetooth();
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("51", "F");
                    editor.commit();
                }
                    TestUtils.wrongPress(TAG, BluetoothTest.this);
//                }
                break;
            }
            
            case R.id.restart_btn: {
                  mRightBtn.setEnabled(false);
                  mRightBtn.setVisibility(View.INVISIBLE);
                  mArrayAdapter.clear();
                  mBluetoothAdapter.cancelDiscovery();
                  // Gionee xiaolin 20120618 modify for CR00625304 start
                  try {
                      Thread.sleep(300);
                  } catch (InterruptedException e) {
                     e.printStackTrace();
                  }
                  // Gionee xiaolin 20120618 modify for CR00625304 end
                  mUiHandler.sendEmptyMessage(BEGIN_TO_SCAN);
                break;
            }
            
        }

    }
	//Gionee <xuna><2012-11-29> add for CR00735904 begin
	private void releaseBluetooth(){
		if (null != mBluetoothAdapter) {
		if (true == mIsDiscovery) {
		mBluetoothAdapter.cancelDiscovery();
		}
		mBluetoothAdapter.cancelDiscovery();
		this.unregisterReceiver(mReceiver);
		}
		Log.i("aaaa","releaseBluetooth");
		exceptionHd.removeMessages(TRY_AGAIN);
	}
	//Gionee <xuna><2012-11-29> add for CR00735904 begin

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    
    // Gionee xiaolin 20121023 add for CR00717119 start
    private final int TRY_AGAIN = 9;

    Handler exceptionHd = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRY_AGAIN:
                    if (mArrayAdapter.isEmpty()) {
                        Log.e(TAG, "mArrayAdapter.isEmpty(), so disable BT then open and discover again");
                        mBluetoothAdapter.disable();
                        registerReceiver(btRec, new IntentFilter(
                                BluetoothAdapter.ACTION_STATE_CHANGED));
                    }
                    break;
            }
        }
    };

    BroadcastReceiver btRec = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int state = intent
                    .getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            Log.d(TAG, BluetoothAdapter.ACTION_STATE_CHANGED + ":" + state);
            if (BluetoothAdapter.STATE_OFF == state)
                mBluetoothAdapter.enable();
            if (BluetoothAdapter.STATE_ON == state) {
                mBluetoothAdapter.startDiscovery();
                unregisterReceiver(this);
            }
        }
    };
    // Gionee xiaolin 20121023 add for CR00717119 end
}
