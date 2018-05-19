package com.cydroid.autommi;

import android.os.Bundle;
import com.cydroid.util.DswLog;
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
    private static final int EVENT_RESPONSE_CMD = 1;
    private TextView mTip = null;
	private Phone mGsmPhone;
    private int mCmdId = -1;
    private String mAtCmd = null;
    private static final int MAIN_ANTENNA_ID = 0;
    private static final int BRANCH_ANTENNA_ID = 1;
    private static final int NORMAL_ANTENNA_ID = 2;
    private static final int QUERY_ANTENNA_ID = 3;

    private static final String CMD_MAIN_ANTENNA = "AT+ETXANT=1,1,0";
    private static final String CMD_BRANCH_ANTENNA = "AT+ETXANT=1,1,1";
    private static final String CMD_NORMAL_ANTENNA = "AT+ETXANT=0,1,0";
    private static final String CMD_QUERY_ANTENNA = "AT+ETXANT=2,1,255";

    private static final String RESULT_TITLE_MAIN_ANTENNA = "AntennaMajor";
    private static final String RESULT_TITLE_BRANCH_ANTENNA = "AntennaSec";
    private static final String RESULT_TITLE_NORMAL_ANTENNA = "AntennaNormal";
    private static final String RESULT_TITLE_QUERY_ANTENNA = "AntennaQuery";
    private String mResultTitle;



    private int mRat_id = -1;
    private int mAnt_idx = -1;

    private String mTestResult = "ok";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tip);

        Intent intent = getIntent();
        if(intent != null){
            mCmdId = intent.getIntExtra("cmd", -1);
        }
        DswLog.i(TAG, "onCreate mCmdId=" + mCmdId);
        if(mCmdId == MAIN_ANTENNA_ID){
            mAtCmd = CMD_MAIN_ANTENNA;
        }else if(mCmdId == BRANCH_ANTENNA_ID){
            mAtCmd = CMD_BRANCH_ANTENNA;
        }else if(mCmdId == NORMAL_ANTENNA_ID){
            mAtCmd = CMD_NORMAL_ANTENNA;
        }else if(mCmdId == QUERY_ANTENNA_ID){
            mAtCmd = CMD_QUERY_ANTENNA;
        }else{
            DswLog.e(TAG,"error cmdid");
            //(AutoMMI) getApplication()).recordResult(TAG, "", "0");
            return;
        }
        mTip = (TextView) findViewById(R.id.tip);
        //Gionee zhangke 20160428 modify for CR01687748 start
        //mGsmPhone = PhoneFactory.getDefaultPhone();
        int majorPhoneId = SystemProperties.getInt("persist.radio.simswitch", 1) - 1;
        DswLog.i(TAG, "majorPhoneId="+majorPhoneId);
        mGsmPhone = PhoneFactory.getPhone(majorPhoneId);
        //Gionee zhangke 20160428 modify for CR01687748 end


        execCmd(mAtCmd);
        //(AutoMMI) getApplication()).recordResult(TAG, "", "0");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    private void execCmd(String  cmd){
        try {
            byte[] rawData = mAtCmd.getBytes();
            byte[] cmdByte = new byte[rawData.length + 1];
            System.arraycopy(rawData, 0, cmdByte, 0, rawData.length);
            cmdByte[cmdByte.length - 1] = 0;
            mGsmPhone.invokeOemRilRequestRaw(cmdByte,
                    mUiHandler.obtainMessage(EVENT_RESPONSE_CMD));
        } catch (NullPointerException ee) {
            DswLog.e(TAG,"send fail:"+ee.getMessage());
        }

    }
	
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            ar = (AsyncResult) msg.obj;
            switch (msg.what) {
                case EVENT_RESPONSE_CMD: {
                    if (ar.exception == null) {
                        try {
                            byte[] rawData = (byte[]) ar.result;
                            DswLog.i(TAG, "HexDump:" + HexDump.dumpHexString(rawData));
                            String txt = new String(rawData, "UTF-8");
                            DswLog.i(TAG, "The resopnse is " + txt);
                            if(txt.contains("OK")){
                                DswLog.i(TAG, "The resopnse contains OK ");
                                mTestResult = "ok";
                                if(!mAtCmd.equals(CMD_QUERY_ANTENNA)){
                                    mAtCmd = CMD_QUERY_ANTENNA;
                                    execCmd(mAtCmd);
                                    break;
                                }
                                if(txt.contains("ETXANT")){
                                    DswLog.i(TAG, "The resopnse contains ETXANT ");
                                    String rat_s = txt.substring(txt.indexOf(":") + 1, txt.indexOf(","));
                                    String ant_idx_s = txt.substring(txt.indexOf(",") + 1,txt.indexOf("O"));
                                    DswLog.i(TAG,"rat_s="+rat_s+";ant_idx_s="+ant_idx_s);
                                    try {
                                        mRat_id = Integer.valueOf(rat_s.trim());
                                        mAnt_idx = Integer.valueOf(ant_idx_s.trim());
                                    }catch(Exception e){
                                        mRat_id = -1;
                                        mAnt_idx = -1;
                                        DswLog.e(TAG,"string to int error:"+e.getMessage());
                                    }
                                    
                                }else if(txt.contains("ERROR")){
                                    mTestResult = txt.substring(txt.indexOf("<") + 1, txt.indexOf(">"));
                                    DswLog.i(TAG, "The resopnse contains ERROR "+ mTestResult);
                                }
                            }

                            //showInfo("AT command is sent:" + txt);
                        } catch (NullPointerException e) {
                            //showInfo("NullPointerException");
                            DswLog.e(TAG,"NullPointerException="+e.getMessage());
                        } catch (UnsupportedEncodingException ee) {
                            //showInfo("UnsupportedEncodingException");
                            DswLog.e(TAG,"UnsupportedEncodingException="+ee.getMessage());
                        }


                    } else {
                        DswLog.e(TAG, "EVENT_RESPONSE_TO_MAINANTENNA fail:" + ar.exception);
						//showInfo("receiver error");
                    }
                }
                showResult();
                break;
            }
        }
    };


    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        finish();
    }

    private void showInfo(String info) {
        if (isFinishing()) return;
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setTitle("showInfo");
        infoDialog.setMessage(info);
        infoDialog.setIcon(android.R.drawable.ic_dialog_alert);
        infoDialog.show();
    }

    private void showResult(){ 
        String result = "";
        String testResult = "0";
        if(mCmdId == MAIN_ANTENNA_ID){
           result = getString(R.string.to_main_antenna);
		   mResultTitle = RESULT_TITLE_MAIN_ANTENNA;
        }else if(mCmdId == BRANCH_ANTENNA_ID){
           result = getString(R.string.to_branch_antenna);
		   mResultTitle = RESULT_TITLE_BRANCH_ANTENNA;
        }else if(mCmdId == NORMAL_ANTENNA_ID){
           result = getString(R.string.to_normal_status);
		   mResultTitle = RESULT_TITLE_NORMAL_ANTENNA;
        }else if(mCmdId == QUERY_ANTENNA_ID){
           result = getString(R.string.query_antenna_status);
		   mResultTitle = RESULT_TITLE_QUERY_ANTENNA;
        }
        if("ok".equalsIgnoreCase(mTestResult)){
            result += getString(R.string.success);
            testResult = "1";
        }else{
            result += getString(R.string.fail);
            testResult = "0";
        }
        result += "\n";
        result += getString(R.string.current_status);
        if(mRat_id == 1 && mAnt_idx ==0){
            result += getString(R.string.main_antenna);
        }else if(mRat_id == 1 && mAnt_idx ==1){
            result += getString(R.string.branch_antenna);
        }else if(mRat_id == 1 && mAnt_idx ==255){
            result += getString(R.string.normal_status);
        }else {
            result += getString(R.string.error_status);
        }
		DswLog.i(TAG, "showResult:mResultTitle="+mResultTitle+";mRat_id="+mRat_id
			+";mAnt_idx="+mAnt_idx+";mTestResult="+mTestResult+";testResult="+testResult);

		mTip.setText(result);
        ((AutoMMI) getApplication()).recordResult(mResultTitle, mRat_id+"|"+mAnt_idx+"|"+mTestResult, testResult);

    }

}
