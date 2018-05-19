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
import android.content.SharedPreferences;
import android.content.Context;
import java.util.Timer;
import java.util.TimerTask;
//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 23333 begin
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 23333 end



public class IfaaActivity extends BaseActivity implements QcRilHookCallback {
    public static final String FACTORY_IF = "IfaaKeyTest";
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
    //Gionee <GN_BSP_AUTOMMI><lifeilong><20161124> modify for ID32386 begin
    private boolean mIsRead = false;
    //Gionee <GN_BSP_AUTOMMI><lifeilong><20161124> modify for ID32386 end
    //Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 23333 begin
    private SharedPreferences mResultSP;
    private SharedPreferences mSNResultSP;
    private SharedPreferences.Editor mSNEditor;
    private int mCount;
    private QcNvItems nvItems = null;
    private byte[] getReadBytes;
    //Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 23333 end


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip);
        mTip = (TextView)findViewById(R.id.tip);
        mTip2 = (TextView)findViewById(R.id.t2);
        mTip2.setVisibility(View.VISIBLE);
        nvItems = new QcNvItems(this, this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

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
        nvItems.dispose();
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
                /*for(int i=0; i<getWriteBytes.length; i++){
                    Log.i(TAG, "getWriteBytes["+i+"]="+getWriteBytes[i]);
                }*/
                //Gionee <GN_BSP_AUTOMMI><lifeilong><20161124> modify for ID32386 begin
                readIfaa();
                if(mIsRead){
                    mIsWriteSuc = true;
                }else{
                    mIsWriteSuc =false;
                }
                //Gionee <GN_BSP_AUTOMMI><lifeilong><20161124> modify for ID32386 end
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
            getReadBytes = processCmd(mContext, readBytes);
        //Log.i(TAG, "readIfaaKey ==== "+bytesToInt2(getReadBytes,0));
            Log.i(TAG, "readIfaaKey ====getReadBytes[0] "+getReadBytes[0]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[1] "+getReadBytes[1]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[2] "+getReadBytes[2]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[3] "+getReadBytes[3]);
            if(getReadBytes != null){
                Log.i(TAG, "readIfaaKey = "+getReadBytes.toString().trim());
                //Gionee ningsy 20160715 begin 
                if(getReadBytes[0] == 0){
                    Log.d(TAG,"----------getReadBytes[0] == 0x00-");
                    mIsReadSuc = true;
                }else{
                    mIsReadSuc = false;
                }
                //Gionee ningsy 20160715 end 
                //mIsReadSuc = true;
            }else{
                Log.i(TAG, "getReadBytes is null");
                mIsReadSuc = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

	//Gionee <GN_BSP_AUTOMMI><lifeilong><20161124> modify for ID32386 begin
	private void readIfaa(){
        byte[] readBytes = new byte[]{0,0,0,0, 0x07,0x00,0x60,0x00};
        try{
            Log.i(TAG,"readIfaaKey"); 
            getReadBytes = processCmd(mContext, readBytes);
        	//Log.i(TAG, "readIfaaKey ==== "+bytesToInt2(getReadBytes,0));
            Log.i(TAG, "readIfaaKey ====getReadBytes[0] "+getReadBytes[0]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[1] "+getReadBytes[1]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[2] "+getReadBytes[2]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[3] "+getReadBytes[3]);
            if(getReadBytes != null){
                Log.i(TAG, "readIfaaKey = "+getReadBytes.toString().trim());
                //Gionee ningsy 20160715 begin 
                if(getReadBytes[0] == 0){
                    Log.d(TAG,"----------getReadBytes[0] == 0x00-");
                    mIsRead= true;
                }else{
                    mIsRead = false;
                }
                //Gionee ningsy 20160715 end 
                //mIsReadSuc = true;
            }else{
                Log.i(TAG, "getReadBytes is null");
                mIsReadSuc = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    //Gionee <GN_BSP_AUTOMMI><lifeilong><20161124> modify for ID32386 end

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
        if(mIsRead){
           textTip2 += getString(R.string.success);
        }else{
           textTip2 += getString(R.string.fail);
        }
        if(mIsWriteSuc){
            testResult = "1";
            nvTag = "P";
        }
        mTip.setText(textTip2);

        mTip2.setText(textTip);

        ((AutoMMI) getApplication()).recordResult(TAG, "", testResult);

        Log.i(TAG, "showResult:mIsWriteSuc="+mIsWriteSuc+";mIsReadSuc="+mIsReadSuc);


    }

	//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 23333 begin
	public void onQcRilHookReady() {
            Log.e(TAG,"onQcRilHookReady  start ");
            String factoryResult = "";
            //Gionee <GN_BSP_MMI> <lifeilong> <20170620> modify for ID 159911 begin
            try {
                String oFS = nvItems.getFactoryResult();
                Log.e(TAG, "oFS= " + oFS);
                Log.e(TAG, "oFS.CharAt(33) = " + oFS.charAt(33));
                if('P' == oFS.charAt(33)){
                    Log.e(TAG, "already success ! skip read write ");
                    mIsWriteSuc = true;
                    mIsRead = true;
                    mIsReadSuc = true;
                    showResult();
                } else{
                    Log.e(TAG, "not success ! need to  read and write !");
                    readIfaaKey();
                    writeIfaaKey();
                    showResult();
                    String nFS = getNewFactorySet(oFS);
                    Log.e(TAG, "nFS= " + nFS);
                    nvItems.setFactoryResult(nFS + "0");
                }
                //Gionee <GN_BSP_MMI> <lifeilong> <20170620> modify for ID 159911 end
                factoryResult = nvItems.getFactoryResult();
                Log.d(TAG, "factoryResult = " + factoryResult + " : " + factoryResult.length());
                String factoryResult11 = nvItems.getFactoryResult();
                Log.d(TAG, "factoryResul1 = " + factoryResult11 + " : " + factoryResult11.length());
            } catch (Exception e) {
                Log.e(TAG,"fail");
                e.printStackTrace();
            }


            }
    private String getNewFactorySet(String old) {
        StringBuilder sb = new StringBuilder(old);
        Log.e(TAG,"old = " + old );
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161129> modify for ID35255 begin
        //if(mIsWriteSuc && mIsReadSuc){
        if(mIsWriteSuc){
            //Gionee <GN_BSP_AutoMMI><lifeilong><20161129> modify for ID35255 end
            sb.setCharAt(33,'P');
        }else{
            sb.setCharAt(33,'F');
        }
        return sb.toString();
    }
	//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 23333 end

	
}

