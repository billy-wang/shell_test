
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.nfc.NfcAdapter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import gn.com.android.mmitest.GnMMITest;

public class NFCTest2 extends Activity implements OnClickListener {
    Button mToneBt;
    private NfcAdapter mNfcAdapter;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170828> modify for ID 198060 begin
    private boolean mNfcOn = false;
    private boolean mNfcOff = false;
    private boolean mNfcTurn = false;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170828> modify for ID 198060 end
    private static final String TAG = "NFCTest2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.common_textview);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.nfctest2_note);
        registerReceiver(mBroadcastReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //mRightBtn.setEnabled(true);
            mWrongBtn.setEnabled(true);
            //mRestartBtn.setEnabled(true);
            mRightBtn.setOnClickListener(NFCTest2.this);
            mWrongBtn.setOnClickListener(NFCTest2.this);
            mRestartBtn.setOnClickListener(NFCTest2.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170828> modify for ID 198060 begin
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)){
                int intExtra = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, 0);
                Log.d(TAG,"intextra = " + intExtra);
                if(intExtra == 3){
                    mNfcOn = true;
                    mNfcOff = false;
                    mRestartBtn.setEnabled(true);
                    mRightBtn.setEnabled(true);
                } else if (intExtra == 1){
                    mNfcOff = true;
                    mNfcOn = false;
                    mRestartBtn.setEnabled(true);
                } else if (intExtra == 2){
                    mNfcTurn = true;
                    mNfcOff = false;
                    mNfcOn = false;
                    mRestartBtn.setEnabled(false);
                    mRightBtn.setEnabled(false);                 
                }
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170828> modify for ID 198060 end

    @Override
    public void onResume() {
        super.onResume();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170828> modify for ID 198060 begin
        if (mNfcAdapter == null) {
            Log.i(TAG, "mNfcAdapter == null");
            TestUtils.wrongPress(TAG, this);
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
        if(mNfcAdapter.isEnabled() || GnMMITest.mNfcOn){
            mRestartBtn.setEnabled(true);
            mRightBtn.setEnabled(true);
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170828> modify for ID 198060 end
    }

    @Override
    public void onPause() {
        super.onPause();
    
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        unregisterReceiver(mBroadcastReceiver);
        switch (v.getId()) {
            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
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
    public boolean dispatchKeyEvent (KeyEvent event) {
        return true;
    }
}
