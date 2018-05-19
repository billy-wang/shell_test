package com.gionee.autommi;

import android.content.Intent;
import android.net.Uri;
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
import android.content.Context;
import android.security.KeyStore;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


//Gionee zhangke 20160123 modify for CR01630432 start
public class WChatATTK extends BaseActivity {
    public static final String TAG = "WChatATTK";
    private static final int EVENT_RESPONSE_CMD = 1;
    private TextView mTip = null;
    private Context mContext;
    private Object mObj;
    private String mResult = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ((AutoMMI) getApplication()).recordResult(TAG, "", "0");
        setContentView(R.layout.tip);
        mTip = (TextView) findViewById(R.id.tip);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume(); 
        new Thread(new Runnable() {
            public void run() {               
                mUiHandler.sendEmptyMessage(EVENT_RESPONSE_CMD);
            }
        }).start();

    }

    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            ar = (AsyncResult) msg.obj;
            switch (msg.what) {
                case EVENT_RESPONSE_CMD: {
                    showResult();
                    break;
                }
            }
        }
    };

    private void showResult() {
        String result = "";
        String testResult = "0";
        TestResult tr = new TestResult();
        byte[] sn_buff = new byte[64];
        System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, 64);
        String tag = "P";
        testResult = "1";
        sn_buff = tr.getNewSN(TestResult.MMI_WCHAT_SOTER_TAG, tag, sn_buff);
        tr.writeToProductInfo(sn_buff);
        mResult += getString(R.string.write_wcattk_key);
        mResult += getString(R.string.success);
        mTip.setText(mResult);
        Log.e(TAG,"sn_buff = " + sn_buff);
        Log.i(TAG, "sn_buff[30]="+sn_buff[30]);
        ((AutoMMI) getApplication()).recordResult(TAG, mResult, testResult);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        finish();
    }
}
// Gionee zhangke 20160123 modify for CR01630432 end
