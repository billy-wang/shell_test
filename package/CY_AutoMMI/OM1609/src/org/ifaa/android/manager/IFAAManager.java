package org.ifaa.android.manager;

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


public class IFAAManager extends BaseActivity {
    public static final String TAG = "IfaaKeyTest";
    static {
        System.loadLibrary("teeclientjni");
    }
    private Context mContext;
    static native byte[] processCmd(Context context, byte[] bytes);
    private TextView mTip = null;
    private TextView mTip2 = null;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private boolean mIsWriteSuc = false;
    private boolean mIsReadSuc = false;

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
        
        writeIfaaKey();
        readIfaaKey();
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

    private void writeIfaaKey(){
		byte[] writeBytes = new byte[4096];
		writeBytes[4] = 0x03;
		writeBytes[5] = 0x00;
		writeBytes[6] = 0x60;
		writeBytes[7] = 0x00;

        try{
            Log.i(TAG,"writeIfaaKey"); 
            byte[] getWriteBytes = processCmd(mContext, writeBytes);
            if(getWriteBytes != null){
                for(int i=0; i<getWriteBytes.length; i++){
                    Log.i(TAG, "getWriteBytes["+i+"]="+getWriteBytes[i]);
                }
                mIsWriteSuc = true;
            }else{
                Log.i(TAG, "getWriteBytes is null");
                mIsWriteSuc = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void readIfaaKey(){
        byte[] readBytes = new byte[]{0,0,0,0, 0x07,0x00,0x60,0x00};
        try{
            Log.i(TAG,"readIfaaKey"); 
            byte[] getReadBytes = processCmd(mContext, readBytes);
            if(getReadBytes != null){
				Log.i(TAG, "readIfaaKey = "+getReadBytes.toString().trim());

                mIsReadSuc = true;
            }else{
                Log.i(TAG, "getReadBytes is null");
                mIsReadSuc = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

		
    private void showResult(){ 
        String textTip = "";
        String textTip2 = "";
        String nvTag = "F";
        String testResult = "0";
        textTip = getString(R.string.write_ifaa_key);
        if(mIsWriteSuc){
           textTip += getString(R.string.success);
        }else{
           textTip += getString(R.string.fail);
        }
        textTip2 = getString(R.string.read_ifaa_key);
        if(mIsReadSuc){
           textTip2 += getString(R.string.success);
        }else{
           textTip2 += getString(R.string.fail);
        }
        if(mIsWriteSuc && mIsReadSuc){
            testResult = "1";
            nvTag = "P";
        }

        mTip.setText(textTip);
        mTip2.setText(textTip2);
        ((AutoMMI) getApplication()).recordResult(TAG, "", testResult);

        TestResult tr = new TestResult();
        byte[] sn_buff = new byte[64];
		System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, 64);
        sn_buff = tr.getNewSN(TestResult.MMI_IFAA_KEY_TAG, nvTag, sn_buff);
		tr.writeToProductInfo(sn_buff);
        Log.i(TAG, "showResult:mIsWriteSuc="+mIsWriteSuc+";mIsReadSuc="+mIsReadSuc+";snNumber="+new String(sn_buff));
    }
}
