package gn.com.android.mmitest.item;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import gn.com.android.mmitest.OMAUtil;
import gn.com.android.mmitest.R;
import android.view.KeyEvent;
import android.widget.Button;
import gn.com.android.mmitest.TestUtils;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.nfc.NfcAdapter;
import gn.com.android.mmitest.TestResult;
import com.gionee.esemanager.esemanagerAPI;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import gn.com.android.mmitest.GnMMITest;

public class CplcActivity
        extends Activity implements View.OnClickListener
{

    private TextView mTv1;
    private TextView mTv2;
    private TextView mTv3;
    private TextView mTv4;
    private TextView mTv5;
    private TextView mTv6;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private Handler mHandler;
    private String mCplc;
    private String mSpi;
    private static String TAG = "CplcActivity";
    private OMAUtil o;
    private boolean flag;
    private NfcAdapter mNfcAdapter;
    private boolean mNfcOn = false;
    private boolean mNfcOff = false;

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
                } else if (intExtra == 1){
                    mNfcOff = true;
                    mNfcOn = false;
                    mRestartBtn.setEnabled(true);
                }
            }
        }
    };    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cplc_main);
        registerReceiver(mBroadcastReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        Log.d(TAG,"registerReceiver");
        initNfc();    
        o = new OMAUtil(this);
        TestUtils.setWindowFlags(this);
        initView();
        Log.e(TAG,"CplcActivity  oncreate  ");
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170712> modify for ID 169191 begin
    private void initNfc(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);        
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
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170712> modify for ID 169191 end
    private void initView() {
        mTv1 = (TextView) findViewById(R.id.tv1);
        mTv2 = (TextView) findViewById(R.id.tv2);
        mTv3 = (TextView) findViewById(R.id.tv3);
        mTv4 = (TextView) findViewById(R.id.tv4);
        mTv5 = (TextView) findViewById(R.id.tv5);
        mTv6 = (TextView) findViewById(R.id.tv6);
        mTv3.setText("");
        mTv6.setText("");
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setEnabled(false);  
        mRightBtn.setVisibility(View.INVISIBLE);
        String startCplcTest = getResources().getString(R.string.startCplcTest);
        String cplcResult = getResources().getString(R.string.cplcResult);
        String startSPI = getResources().getString(R.string.startSPI);
        String spiResult = getResources().getString(R.string.spiResult);
        mTv1.setText(startCplcTest);
        mTv2.setText(cplcResult);
        mTv4.setText(startSPI);
        mTv5.setText(spiResult); 
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case 0:
                        shouResult();
                        break;
                }
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);
                mRightBtn.setOnClickListener(CplcActivity.this);
                mWrongBtn.setOnClickListener(CplcActivity.this);
                mRestartBtn.setOnClickListener(CplcActivity.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);        
    }

    private void shouResult() {
        String nvTag;
        Log.d(TAG,"shouResult beginning");
        TestResult tr = new TestResult();
        byte[] sn_buff = new byte[TestResult.SN_LENGTH];
        System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, TestResult.SN_LENGTH);
        Toast.makeText(this,mCplc,Toast.LENGTH_SHORT).show();
        mTv2.setText(mTv2.getText() + " : " + mCplc);
        mTv5.setText(mTv5.getText() + " : " + mCplc);
        if(mCplc != null && mSpi != null){
            mTv3.setText(getResources().getString(R.string.success));
            flag = true;
            nvTag = "P";
            mRightBtn.setEnabled(true);
            mRightBtn.setVisibility(View.VISIBLE);
        } else {
            nvTag = "F";
            mTv6.setText(getResources().getString(R.string.fail));
        }
        sn_buff = tr.getNewSN(TestResult.MMI_CPLC_TAG, nvTag, sn_buff);
        tr.writeToProductInfo(sn_buff);
    }
    Thread mThread = new Thread(){
        @Override
        public void run() {
            super.run();
            Log.d(TAG,"thread beginning");
            //Gionee <GN_BSP_MMI> <lifeilong> <20170811> modify for ID 185660 begin
            int count = 0;
            try {
                if(!mNfcAdapter.isEnabled()){
                    while(!GnMMITest.mNfcOn){
                        Thread.sleep(1000);
                        //mNfcAdapter.enable();
                        Log.d(TAG,"mNfcAdapter.isEnabled()" + mNfcAdapter.isEnabled() + "  ==  count  = " + count );
                        count++;
                        if(count == 13){
                            break;
                        }                    
                    }
                }
                //mCplc = getCplc();
                if(GnMMITest.mNfcOn || mNfcAdapter.isEnabled()){
                    byte[] i2c_id = o.getCPLC();
                    byte[] spi_id = esemanagerAPI.native_eseGetCplc();
                    Log.d(TAG, " i2c_id = " + i2c_id + "  , spi_id = " + spi_id );
                    mCplc = getCplc(i2c_id);
                    mSpi = getCplc(spi_id);                    
                }
                mHandler.sendEmptyMessageDelayed(0,100);
                //mHandler.sendEmptyMessageDelayed(0,500);
           } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG,e.getMessage());
           }
           //Gionee <GN_BSP_MMI> <lifeilong> <20170811> modify for ID 185660 end
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onstart and thread beginning");
        mThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBroadcastReceiver);
        Log.d(TAG,"unregisterReceiver");
    }    

    private String getCplc(byte[] id) {
        Log.d(TAG,"getCplc beginning");
        String result = null ;
        StringBuilder builder = new StringBuilder(id.length * 2);
        try {
            Log.d(TAG,"  getCPLC  = " + id);            
            if(id != null && id.length != 0){
                builder.append(bytesToHexString(id));
                result =  builder.toString().substring(6,builder.toString().length() - 4);
                //Gionee <GN_BSP_MMI> <lifeilong> <20170711> modify for ID 168538 begin
                Log.d(TAG,"result = " + result + "  , result.length() = " + result.length() );
                //Gionee <GN_BSP_MMI> <lifeilong> <20170711> modify for ID 168538 end
            }else {
                Log.d(TAG,"Cplc = null ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG," e = " + e.getMessage());
        }        
        return result;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (byte b : bytes){
            sb.append(String.format("%02X",b));
        }
        return sb.toString();
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                TestUtils.rightPress(TAG, CplcActivity.this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, CplcActivity.this);
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
