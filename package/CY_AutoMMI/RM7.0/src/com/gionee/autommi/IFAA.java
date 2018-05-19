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
import android.content.Context;
import java.util.Timer;
import java.util.TimerTask;


public class IFAA extends BaseActivity {
    public static final String TAG = "IFAA";

    private Context mContext;

    private TextView mTip = null;
    private TextView mTip2 = null;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip);
        mTip = (TextView)findViewById(R.id.tip);
        mTip2 = (TextView)findViewById(R.id.t2);
        mTip2.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        showResult();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        finish();
    } 
    private void showResult(){ 
        String textTip = "";
        String textTip2 = "";
        String nvTag = "F";
        String testResult = "0";       
        testResult = "1";
        nvTag = "P";
        TestResult tr = new TestResult();
        byte[] sn_buff = new byte[64];
        System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, 64);
        sn_buff = tr.getNewSN(TestResult.MMI_IFAA_KEY_TAG, nvTag, sn_buff);
        tr.writeToProductInfo(sn_buff);
        textTip = getString(R.string.write_ifaa_key);
        textTip += getString(R.string.success);       
        mTip.setText(textTip);
        Log.e(TAG,"sn_buff = " + sn_buff);
        Log.i(TAG, "sn_buff[29]="+sn_buff[29]);
        ((AutoMMI) getApplication()).recordResult(TAG, "", testResult);
    }
}
