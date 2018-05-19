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
import android.nfc.NfcAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.SystemProperties;

public class RecovryNfc
        extends BaseActivity
{

    private TextView mTv1;
    private TextView mTv2;
    private TextView mTv3;
    private Handler mHandler;
    private String mCplc;
    private String startCplcTest;
    private String cplcResult;
    private static String TAG = "RecovryNfc";
    private OMAUtil o;
    private int flag;
    private NfcAdapter mNfcAdapter;
    private boolean mNfcOn = false;
    private boolean mNfcOff = false;
    private boolean mNfcturn = false;
    private boolean mRestartNfc = false;
    private boolean passFlag ,cplcFlag= false;
    private boolean RecovryNfcFlag = false;
    private String resultNum = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cplc_main);
        registerReceiver(mBroadcastReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        initNfc();
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
            Log.i(TAG, "Nfc is enable, begin to enable ");
            mNfcAdapter.enable();
        } else {
            Log.i(TAG, "Nfc is enable, try to disabe and then enable");
            mRestartNfc = true;
            Log.i(TAG, "begin to disable first 1, mRestartNfc=" + mRestartNfc);
            mNfcAdapter.disable(true);
            //TODO ?
        }
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170712> modify for ID 169191 end

    private void initView() {
        mTv1 = (TextView) findViewById(R.id.tv1);
        mTv2 = (TextView) findViewById(R.id.tv2);
        mTv3 = (TextView) findViewById(R.id.tv3);
        startCplcTest = getResources().getString(R.string.startCplcTest);
        cplcResult = getResources().getString(R.string.cplcResult);
        //mTv1.setText(startCplcTest);
        //mTv2.setText(cplcResult);
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
        Log.d(TAG,"RecovryNfc beginning");
        //mTv2.setText(mTv2.getText() + " : " + mCplc);
        if(RecovryNfcFlag){
            mTv3.setText(getResources().getString(R.string.success) + " , result  num = " + resultNum );
            Log.d(TAG,"show result == success");
            flag = 1;
            //((AutoMMI)getApplication()).recordResult(TAG, "", "1");
        } else {
            flag = 0;
            mTv3.setText(getResources().getString(R.string.fail) + " , result  num = " + resultNum);
            Log.d(TAG,"show result == fail");
            //((AutoMMI)getApplication()).recordResult(TAG, "", "0");
        }
        ((AutoMMI) getApplication()).recordResult(TAG, ""+mCplc, ""+flag);
    }

    Thread mThread = new Thread(){
        @Override
        public void run() {
            super.run();
            Log.d(TAG,"RecovryNfc beginning");
            int count = 0;
            try{
                while(mRestartNfc){   //if mRestartNfc true, need to wait above disable action complete.
                    Thread.sleep(1000);

                    if((mNfcOff)){
                        Log.i(TAG, "Nfc Disable ok now, net restart nfc, mRestartNfc=" + mRestartNfc);
                        //    Log.i(TAG, "Need to restart NFC for the eSE recovery check action")
                        //	mNfcAdapter.enable();
                        break;
                    }

                    count += 1;
                    Log.d(TAG,"mNfcAdapter.isEnabled()" + mNfcAdapter.isEnabled() + " ,  count = " + count);
                }

                if((mNfcOff || (!mNfcAdapter.isEnabled())) && mRestartNfc){

                Log.i(TAG, "begin to reenable nfc 2 for the eSE recovery check action ,mRestartNfc= " + mRestartNfc);

                    mNfcAdapter.enable();
                }
            }catch (Exception e) {
                 e.printStackTrace();
                 Log.d(TAG,e.getMessage());
            }

            count = 0;//To enable 
            try {
                if(!mNfcAdapter.isEnabled()){  
                    while(true){
                    Thread.sleep(1000);
                    count ++;

                    if(mNfcOn || count == 10){  //nfc is enable or timeout
                        Log.i(TAG, "Nfc enable ok or timeout");
                        break;
                    }
                    //mNfcAdapter.enable();
                    Log.d(TAG,"mNfcAdapter.isEnabled()" + mNfcAdapter.isEnabled() + "  ,  count = " + count);
                    }
                }
              } catch (Exception e) {
                 e.printStackTrace();
                 Log.d(TAG,e.getMessage());
            }
             count = 0;
             String eseRecoveryStatus;

            try{
                 if(mNfcAdapter.isEnabled()){  //if enable, will wait the ese recover result
                    //TODO: wait eSe recovery complete
                    do{
                        //eseRecoveryStatus = mNfcAdapter.getNfcProperty("debug.nxp.eseRecovery.status", "0");//check result
                        eseRecoveryStatus = SystemProperties.get("debug.nxp.eseRecovery.status", "0");

                        if(eseRecoveryStatus.equals("1")){
                            Log.e(TAG, "eSE recovery complete: eseRecoveryStatus=" + eseRecoveryStatus +"(0-not do, 1-ese recovery complete, 2-ese recovery fail)");
                            mRestartNfc = false;
                            RecovryNfcFlag = true;
                            resultNum = "1";
                            break;
                        } else {
                            Log.e(TAG, "eSE recovery complete: eseRecoveryStatus=" + eseRecoveryStatus +"(0-not do, 1-ese recovery complete, 2-ese recovery fail)");
                            mRestartNfc = false;
                            RecovryNfcFlag = false;
                            if(eseRecoveryStatus.equals("2")){
                                resultNum = "2";
                            }else if (eseRecoveryStatus.equals("3")){
                                resultNum = "3";
                            }
                            
                        }

                        Thread.sleep(1000);
                        count++;
                        if(count == 100){ //Wait Max 100s
                            resultNum = "time out ";
                            Log.e(TAG, "eseRecoveryStatus= time out ");
                            break;
                        }

                        Log.d(TAG,"Doing eSE recovery action, count = " + count);
                        if(eseRecoveryStatus.equals("1") || eseRecoveryStatus.equals("2") ||eseRecoveryStatus.equals("3")){
                            break;
                        }
                    }while(count < 100);

                    Log.e(TAG, "eseRecoveryStatus=" + eseRecoveryStatus +"(0-not do, 1-ese recovery complete, 2-ese recovery fail)");
                    mHandler.sendEmptyMessageDelayed(0,50);/////////////////////////
                 }
            }catch (Exception e) {
                 e.printStackTrace();
                 Log.d(TAG,e.getMessage());
            }
            }

    };

    @Override
    protected void onStart() {
        super.onStart();
        mThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mThread.close();
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
