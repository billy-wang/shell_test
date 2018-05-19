package com.gionee.autommi;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.R;
import com.gionee.autommi.AutoMMI;
import android.view.View;
import com.gionee.autommi.TestResult;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import java.util.Arrays;
import android.content.res.Resources;


public class AtmActivity extends BaseActivity{
    private static final String TAG = "AtmActivity";
    private TextView mTip = null;
    private TextView mTip2 = null;
    private boolean flag = false;
    private static final int TAG_LENGTH=8;
    private String echoString="31232700";
    private String deleteString = "00000000";
    private byte[] echoByte;
    private byte[] deleteByte;
    private String cmd = "";
    private Resources mRs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.tip);
        init();
        ((AutoMMI) getApplication()).recordResult(TAG, "", "2");
    }

    public void init(){
        Intent intent = getIntent();
        if(intent != null){
          flag = intent.getBooleanExtra("flag", false);
          cmd = intent.getStringExtra("cmd");
        }
        mRs = getResources();
        Log.e(TAG,"flag  = " + flag + " , cmd = " + cmd);
        mTip = (TextView)findViewById(R.id.tip);
        mTip2 = (TextView)findViewById(R.id.t2);
        mTip2.setVisibility(View.VISIBLE);
        echoByte= echoString.getBytes();
        deleteByte= deleteString.getBytes();
        if("3".equals(cmd)){
            mTip.setText(mRs.getString(R.string.echo));
        } else if ("0".equals(cmd)){
            mTip.setText(mRs.getString(R.string.delete));
        }
    }

    public byte[] addTagInfo(byte[] sn_buff,String cmd){
        Log.d(TAG,"add cmd = " + cmd);
        if("3".equals(cmd)){
            System.arraycopy(echoByte, 0, sn_buff, 527, TAG_LENGTH);
        }else if ("0".equals(cmd)){
            System.arraycopy(deleteByte, 0, sn_buff, 527, TAG_LENGTH);
        }
        return sn_buff;
    }

    private void showResult(){
        TestResult tr = new TestResult();
        String testResult = "0";
        String Result = "";
        byte[] sn_buff = new byte[TestResult.SN_LENGTH];
        sn_buff = addTagInfo(tr.getProductInfo(), cmd);
        tr.writeToProductInfo(sn_buff);
        byte[] sn = tr.getProductInfo();
        byte[] sn1 = new byte[TAG_LENGTH];
        System.arraycopy(sn, 527, sn1, 0, TAG_LENGTH);
        boolean result = Arrays.equals(sn1,echoByte);
        boolean result1 = Arrays.equals(sn1,deleteByte);
        Log.i(TAG,"result = " + result  + "  , result1 = " + result1);
        if("3".equals(cmd) && result){
            mTip2.setText(mRs.getString(R.string.echo) + " : " + mRs.getString(R.string.success));
        }else if ("0".equals(cmd) && result1 ){
            mTip2.setText(mRs.getString(R.string.delete) + " : " + mRs.getString(R.string.success));
        }
        if(result){
            testResult = "1";
            Result = echoString;
        } else if (result1){
            testResult = "1";
            Result = deleteString;
        }
        ((AutoMMI) getApplication()).recordResult(TAG, Result, testResult);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        showResult();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
    }

}
