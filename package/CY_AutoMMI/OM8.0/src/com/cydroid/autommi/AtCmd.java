package com.cydroid.autommi;

import android.content.Intent;
import android.net.Uri;
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
import android.content.Context;
//Gionee zhangke 20160428 modify for CR01687748 start
import android.os.SystemProperties;
//Gionee zhangke 20160428 modify for CR01687748 end

//Gionee zhangke 20160123 modify for CR01630432 start
public class AtCmd extends BaseActivity {
    public static final String TAG = "AtCmd";
    private static final int EVENT_RESPONSE_CMD = 1;
    private TextView mTip = null;
    private Phone mGsmPhone;
    private int mCmdId = -1;
    private String mResultTitle;

    private int mRat_id = -1;
    private int mAnt_idx = -1;

    private String mTestResult = "ERROR";
    private Context mContext;
    private String mAtCmd = "AT+EOPS=4,2,\"00101\",0,62";
    private static final String CALL_TEST_TYPE = "CallTest";
    private String mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mAtCmd = intent.getStringExtra("at");
            mType = intent.getStringExtra("type");
        }
        DswLog.i(TAG, "mAtCmd=" + mAtCmd);
        //Gionee zhangke 20160428 modify for CR01687748 start
        //mGsmPhone = PhoneFactory.getDefaultPhone();
        int majorPhoneId = SystemProperties.getInt("persist.radio.simswitch", 1) - 1;
        DswLog.i(TAG, "majorPhoneId="+majorPhoneId);
        mGsmPhone = PhoneFactory.getPhone(majorPhoneId);
        //Gionee zhangke 20160428 modify for CR01687748 end

        if (mAtCmd != null && !mAtCmd.equals("")) {
            try {
                formatCmd(mAtCmd);
            } catch (Exception e) {
                DswLog.e(TAG, "formatCmd exception=" + e.getMessage());
            }
            execCmd(mAtCmd);
        } else {
            finish();
        }
        setContentView(R.layout.tip);
        mTip = (TextView) findViewById(R.id.tip);
        mContext = this;
    }

    private void formatCmd(String cmd) {
        if (mType != null && mType.equals(CALL_TEST_TYPE)) {
            if (!cmd.contains("\"")) {
                int p1 = cmd.indexOf(",", cmd.indexOf(",") + 1) + 1;
                String str1 = cmd.substring(0, p1);
                String str2 = cmd.substring(p1, p1 + 5);
                String str3 = cmd.substring(p1 + 5, cmd.length());
                mAtCmd = str1 + "\"" + str2 + "\"" + str3;
            }
        }
    }

    private void execCmd(String cmd) {
        try {
            DswLog.i(TAG, "execCmd:" + cmd);
            byte[] rawData = cmd.getBytes();
            byte[] cmdByte = new byte[rawData.length + 1];
            System.arraycopy(rawData, 0, cmdByte, 0, rawData.length);
            cmdByte[cmdByte.length - 1] = 0;
            mGsmPhone.invokeOemRilRequestRaw(cmdByte, mUiHandler.obtainMessage(EVENT_RESPONSE_CMD));
        } catch (NullPointerException ee) {
            DswLog.e(TAG, "send fail:" + ee.getMessage());
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
                            if (mType != null && mType.equals(CALL_TEST_TYPE)) {
                                String error = "+CME ERROR:";
                                if (txt.contains("OK")) {
                                    DswLog.i(TAG, "The resopnse contains OK ");
                                    mTestResult = "ok";
                                } else if (txt.contains(error)) {
                                    mTestResult = txt.substring(txt.indexOf(error) + error.length(),
                                            txt.indexOf(error) + error.length() + 5);
                                    DswLog.i(TAG, "The resopnse contains ERROR " + mTestResult);
                                }
                            }
    
                        } catch (NullPointerException e) {
                            DswLog.e(TAG, "NullPointerException=" + e.getMessage());
                        } catch (UnsupportedEncodingException ee) {
                            DswLog.e(TAG, "UnsupportedEncodingException=" + ee.getMessage());
                        }
    
                    } else {
                        DswLog.e(TAG, "EVENT_RESPONSE_TO_MAINANTENNA fail:" + ar.exception);
                    }
                    showResult();
                    break;
                }
            }
        }
    };

    private void showResult() {
        String result = "";
        String testResult = "0";

        if ("ok".equalsIgnoreCase(mTestResult)) {
            result += getString(R.string.success);
            testResult = "1";
        } else {
            result += getString(R.string.fail) + mTestResult;
            testResult = "0";
        }

        mTip.setText(mAtCmd + "|" + mTestResult);
        ((AutoMMI) getApplication()).recordResult(TAG, mAtCmd + "|" + mTestResult, testResult);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        finish();
    }
}
// Gionee zhangke 20160123 modify for CR01630432 end
