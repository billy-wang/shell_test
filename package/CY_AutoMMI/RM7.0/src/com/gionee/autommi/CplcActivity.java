package com.gionee.autommi;

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
import com.gionee.autommi.OMAUtil;
import android.nfc.NfcAdapter;
import com.gionee.esemanager.esemanagerAPI;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Intent;

public class CplcActivity
        extends BaseActivity
{

    private TextView mTv1;
    private TextView mTv2;
    private TextView mTv3;
    private Handler mHandler;
    private String mCplc;
    private String startCplcTest;
    private String cplcResult;
    private static String TAG = "CplcActivity";
    private OMAUtil o;
    private int flag;
    private NfcAdapter mNfcAdapter;
    private boolean mNfcOn = false;
    private boolean mNfcOff = false;
    private boolean mNfcturn = false;
    private String key = "";
    private String passKey = "GionEe_cPlC^EsE*TeSt";
    private boolean passFlag ,cplcFlag= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cplc_main);
        registerReceiver(mBroadcastReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170913> modify for ID 211902 begin
        Intent it = this.getIntent();
        if(it != null){
            cplcFlag = it.getBooleanExtra("gioNee", false);
            key = it.getStringExtra("key");
        }
        Log.d(TAG, "cplcFlag = " + cplcFlag);
        if(key != null && passKey.equals(key)){
            passFlag = true;
        }
        initNfc();
        o = new OMAUtil(this);
        initView();
        Log.e(TAG,"CplcActivity  oncreate  ");
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170913> modify for ID 211902 end
        ((AutoMMI)getApplication()).recordResult(TAG, "", "2");
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170712> modify for ID 169191 begin
    private void initNfc(){
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);        
        if (mNfcAdapter == null) {
            Log.i(TAG, "mNfcAdapter == null");
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
        startCplcTest = getResources().getString(R.string.startCplcTest);
        cplcResult = getResources().getString(R.string.cplcResult);
        mTv1.setText(startCplcTest);
        mTv2.setText(cplcResult);
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
    }

    private void shouResult() {
        Log.d(TAG,"shouResult beginning");
        Toast.makeText(this,mCplc,Toast.LENGTH_SHORT).show();
        mTv2.setText(mTv2.getText() + " : " + mCplc);
        if(mCplc != null){
            mTv3.setText(getResources().getString(R.string.success));
            Log.d(TAG,"  show result == success");
            flag = 1;
            //((AutoMMI)getApplication()).recordResult(TAG, "", "1");
        } else {
            flag = 0;
            mTv3.setText(getResources().getString(R.string.fail));
            Log.d(TAG,"  show result == fail");
            //((AutoMMI)getApplication()).recordResult(TAG, "", "0");
        }
        ((AutoMMI) getApplication()).recordResult(TAG, ""+mCplc, ""+flag);
    }


    public int cleanBinding(){
        byte[] buffer = {0x10, 0x20, 0x30};
        Log.d(TAG,"  cleanBinding begin");
        int flag = esemanagerAPI.native_eseSendCommandEx(esemanagerAPI.CLEAR_BINDING_INFO, buffer);
        Log.d(TAG,"  cleanBinding end flag = " + flag);
        return flag;
    }

    public int bindingEse(){
        Log.d(TAG , "  bindingEse begin");
        int flag =  esemanagerAPI.native_eseSendCommandEx(esemanagerAPI.START_BINDING, null);
        Log.d(TAG , "  bindingEse flag = " + flag);
        return flag;
    }

    Thread mThread = new Thread(){
        @Override
        public void run() {
            super.run();
            Log.d(TAG,"thread beginning");
             //Gionee <GN_BSP_MMI> <lifeilong> <20170811> modify for ID 185660 begin
             try {
                 int count = 0;
                 if(!mNfcAdapter.isEnabled()){
                     while(!Dumb.mNfcOn){
                         Thread.sleep(1000);
                         count ++;
                         if(count == 10){
                             break;
                         }
                         //mNfcAdapter.enable();
                         Log.d(TAG,"mNfcAdapter.isEnabled()" + mNfcAdapter.isEnabled() + "  ,  count = " + count);
                     }
                 }

                 
                 if(mNfcAdapter.isEnabled() || Dumb.mNfcOn){
                     int cleanFlag = -1;
                     int bindingFlag = -1;
                     flag = bindingEse();
                     if(flag != 0){
                        cleanFlag = cleanBinding();
                        if(cleanFlag == 0){
                            flag = bindingEse();
                        }
                     }
                     if(flag == 0){
                        mCplc = getCplc();
                     }
                 }
                 mHandler.sendEmptyMessageDelayed(0,50);
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
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170913> modify for ID 211902 begin
        if(!cplcFlag || !passFlag){
            Log.d(TAG,"passFlag = not continue , passFlag = " +passFlag + "  , cplcFlag = " + cplcFlag);
            mHandler.sendEmptyMessageDelayed(0,50);
        } else {
            Log.d(TAG,"onstart and thread beginning");
            mTv2.setText(cplcResult);
            mThread.start();
        }
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170913> modify for ID 211902 end
    }

    private String getCplc() {
        Log.d(TAG,"getCplc beginning");
        String result = null ;
        try {
            byte[] a = o.getCPLC();
            Log.d(TAG,"  getCPLC  = " + a);
            StringBuilder builder;
            if(a != null && a.length != 0){
                builder = new StringBuilder(a.length * 2);
                for(byte b : a){
                    builder.append(String.format("%02X",b));
                }
                result =  builder.toString().substring(6,builder.toString().length() - 4);
                Log.d(TAG,"result = " + result + "  , result.length() = " + result.length() );
            }else {
                Log.d(TAG,"Cplc = null ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG,e.getMessage());
        }        
        return result;
    }


    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 begin
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
                    mNfcturn = false;
                } else if (intExtra == 1){
                    mNfcOff = true;
                    mNfcOn = false;
                    mNfcturn = false;
                } else if (intExtra == 2){
                    mNfcOff = false;
                    mNfcOn = false;
                    mNfcturn = true;
                }
            }
        }
    };    
    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 end

    /*@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }*/
}
