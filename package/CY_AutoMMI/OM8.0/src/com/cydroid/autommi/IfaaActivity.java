package com.cydroid.autommi;

import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import com.cydroid.autommi.BaseActivity;
import com.cydroid.autommi.R;
import com.cydroid.autommi.AutoMMI;
import android.view.View;
import com.cydroid.autommi.TestResult;
import android.content.SharedPreferences;
import android.content.Context;
import java.util.Timer;
import java.util.TimerTask;


public class IfaaActivity extends BaseActivity {
    public static final String TAG = "IfaaKeyTest";
    static {
        System.loadLibrary("teetestjni");
    }
    private Context mContext;
    native byte[] processCmd(Context context, byte[] bytes);
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
        DswLog.i(TAG, "onResume");
        
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
            DswLog.i(TAG,"writeIfaaKey");
            byte[] getWriteBytes = processCmd(mContext, writeBytes);
            if(getWriteBytes != null){
                for(int i=0; i<getWriteBytes.length; i++){
                    DswLog.i(TAG, "getWriteBytes["+i+"]="+getWriteBytes[i]);
                }
                mIsWriteSuc = true;
            }else{
                DswLog.i(TAG, "getWriteBytes is null");
                mIsWriteSuc = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void readIfaaKey(){
        byte[] readBytes = new byte[]{0,0,0,0, 0x07,0x00,0x60,0x00};
        try{
            DswLog.i(TAG,"readIfaaKey");
            byte[] getReadBytes = processCmd(mContext, readBytes);
        	//DswLog.i(TAG, "readIfaaKey ==== "+bytesToInt2(getReadBytes,0));
            DswLog.i(TAG, "readIfaaKey ====getReadBytes[0] "+getReadBytes[0]);
            DswLog.i(TAG, "readIfaaKey ====getReadBytes[1] "+getReadBytes[1]);
            DswLog.i(TAG, "readIfaaKey ====getReadBytes[2] "+getReadBytes[2]);
            DswLog.i(TAG, "readIfaaKey ====getReadBytes[3] "+getReadBytes[3]);
            if(getReadBytes != null){
				DswLog.i(TAG, "readIfaaKey = "+getReadBytes.toString().trim());
                //Gionee ningsy 20160715 begin 
			    if(getReadBytes[0] == 0){
					DswLog.d(TAG,"----------getReadBytes[0] == 0x00-");
					mIsReadSuc = true;
				}else{
					mIsReadSuc = false;
				}
				//Gionee ningsy 20160715 end 
                //mIsReadSuc = true;
            }else{
                DswLog.i(TAG, "getReadBytes is null");
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
		System.arraycopy(tr.readNvramInfo(), 0, sn_buff, 0, 64);
        sn_buff = tr.getNewSN(TestResult.MMI_IFAA_KEY_TAG, nvTag, sn_buff);
		tr.writeToNvramInfo(sn_buff);
        DswLog.i(TAG, "showResult:mIsWriteSuc="+mIsWriteSuc+";mIsReadSuc="+mIsReadSuc+";snNumber="+new String(sn_buff));
    }
}
