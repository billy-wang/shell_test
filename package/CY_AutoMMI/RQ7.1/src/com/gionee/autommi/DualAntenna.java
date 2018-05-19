package com.gionee.autommi;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import android.os.Message;
import android.os.AsyncResult;
import java.io.UnsupportedEncodingException;
import android.widget.Toast;
import com.android.internal.util.HexDump;
import android.app.AlertDialog;
import android.content.Intent;
//Gionee zhangke 20160428 modify for CR01687748 start
import android.os.SystemProperties;
//Gionee zhangke 20160428 modify for CR01687748 end

public class DualAntenna extends BaseActivity {
    public static final String TAG = "DualAntenna";
    //private static final int EVENT_RESPONSE_CMD = 1;
    private TextView mTip = null;
    private Phone mGsmPhone;
    private int mCmdId = -1;
    private String mAtCmd = null;
    //Gionee <GN_AutoMMI><lifielong><20161026> modify for 6697 begin
    private static final int MSG_DIAG_DISABLE = 4;
    private static final int MSG_DIAG_ENABLE = 2;
    private static final int MSG_DIAG_SET_PASS_MODE = 1;
    private static final int MSG_DIAG_SET_SWAP_MODE = 0;
    private static final int MSG_NFC_START_SHOW = 3;
	
    private static final String RESULT_TITLE_MAIN_ANTENNA = "AntennaMajor";
    private static final String RESULT_TITLE_BRANCH_ANTENNA = "AntennaSec";
    private static final String RESULT_TITLE_NORMAL_ANTENNA = "AntennaNormal";
    private static final String RESULT_TITLE_TEST_ANTENNA = "AntennaDISABLE";
    private static final String RESULT_TITLE_QUERY_ANTENNA = "AntennaQuery";
    private String mResultTitle;
    //Gionee <GN_AutoMMI><lifielong><20161026> modify for 6697 end

    private String mTestResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip);
		Log.e(TAG,"DualAntenna == DualAntenna");


        
		
	//Gionee <GN_AutoMMI><lifielong><20161026> modify for 6697 begin
        /*if(mCmdId == MSG_DIAG_DISABLE){
            mAtCmd = CMD_MAIN_ANTENNA;
        }else if(mCmdId == MSG_DIAG_ENABLE){
            mAtCmd = CMD_BRANCH_ANTENNA;
        }else if(mCmdId == MSG_DIAG_SET_PASS_MODE){
            mAtCmd = CMD_NORMAL_ANTENNA;
        }else if(mCmdId == MSG_DIAG_SET_SWAP_MODE){
            mAtCmd = CMD_QUERY_ANTENNA;
        }else if(mCmdId == MSG_NFC_START_SHOW){
		mAtCmd = 
	}else{
           Log.e(TAG,"error cmdid");
           //(AutoMMI) getApplication()).recordResult(TAG, "", "0");
           return;
        }*/
        //Gionee <GN_AutoMMI><lifielong><20161026> modify for 6697 end
        mTip = (TextView) findViewById(R.id.tip);
        //Gionee zhangke 20160428 modify for CR01687748 start
        Intent intent = getIntent();
        if(intent != null){
            mCmdId = intent.getIntExtra("cmd", -1);
        }
		Log.i(TAG, "onCreate mCmdId=" + mCmdId);
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161126> modify for ID34072 begin
		String cmd = null;
		switch(mCmdId){
			case 0 : cmd = "s";
				break;
			case 1:  cmd = "p";
				break;
			case 2:  cmd = "e";
				break;
			case 4:  cmd = "d";
				break;
		}
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161126> modify for ID34072 end
	boolean result = runNfcTestCommand(cmd);
	if(result){
		mTestResult = "ok";
	}else {
		mTestResult = "error";
	}
	showResult();
	//Gionee <GN_AutoMMI><lifielong><20161026> modify for 6697 end
        //(AutoMMI) getApplication()).recordResult(TAG, "", "0");
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
    //Gionee <GN_AutoMMI><lifielong><20161026> modify for 6697 begin
    private void showResult(){ 
        String result = "";
        String testResult = "0";
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161115> modify for 6697 begin
		Log.e(TAG,"mTestResult = " + mTestResult);
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161115> modify for 6697 end

        if(mCmdId == MSG_DIAG_SET_SWAP_MODE){
           result = getString(R.string.to_main_antenna);
	   mResultTitle = RESULT_TITLE_MAIN_ANTENNA;
        }else if(mCmdId == MSG_DIAG_SET_PASS_MODE){
           result = getString(R.string.to_branch_antenna);
	   mResultTitle = RESULT_TITLE_BRANCH_ANTENNA;
        }else if(mCmdId == MSG_DIAG_ENABLE){
           result = getString(R.string.to_normal_status);
	   mResultTitle = RESULT_TITLE_NORMAL_ANTENNA;
        }else if(mCmdId == MSG_DIAG_DISABLE){
           result = getString(R.string.to_test_status);
	   mResultTitle = RESULT_TITLE_TEST_ANTENNA;
        }else if(mCmdId == MSG_NFC_START_SHOW){
	       result = getString(R.string.to_query_status);
	   mResultTitle = RESULT_TITLE_QUERY_ANTENNA;
		}
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161115> modify for 6697 begin
        if("ok".equalsIgnoreCase(mTestResult)){
            result += getString(R.string.success);
            testResult = "1";
        }else{
            result += getString(R.string.fail);
            testResult = "0";
        }
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161115> modify for 6697 end



		//result += "\n";
	Log.i(TAG, "showResult:mResultTitle="+mResultTitle+";mRat_id="+"0"
		+";mAnt_idx="+"0"+";mTestResult="+mTestResult+";testResult="+testResult);

	mTip.setText(result);
        ((AutoMMI) getApplication()).recordResult(mResultTitle, "0"+"|"+"0"+"|"+mTestResult, testResult);

    }
	//Gionee <GN_AutoMMI><lifielong><20161026> modify for 6697 end

	//Gionee <GN_AutoMMI><lifielong><20161026> add for 6697 begin
        private boolean runNfcTestCommand(String cmd) {
     	   Process process = null;
       	   boolean result = false;
        
        try {
           // process = Runtime.getRuntime().exec("ps");
            process = Runtime.getRuntime().exec("/system/bin/diag_sendcmd -"+cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer output = new StringBuffer();
		int exeReturn = process.waitFor();
            if(exeReturn == 0){
            	result = true;
            }
            Log.d(TAG, "send command return:"+exeReturn);
        } catch (Exception e) {
            Log.d(TAG, "Unexpected error: " + e.getMessage());
            return false;
        } finally {
            process.destroy();
        }
        
        return result;
    }
	//Gionee <GN_AutoMMI><lifielong><20161026> add for 6697 end

}
