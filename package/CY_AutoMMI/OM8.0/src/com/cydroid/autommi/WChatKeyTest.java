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
import android.security.KeyStore;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


//Gionee zhangke 20160123 modify for CR01630432 start
public class WChatKeyTest extends BaseActivity {
    public static final String TAG = "WChatKeyTest";
    private static final int EVENT_RESPONSE_CMD = 1;
    private TextView mTip = null;
    private Context mContext;
    private Class<?> mKeyStore;
    private static final String CLASS_KEY_STORE = "android.security.KeyStore";
    private Object mObj;
    private String mResult = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.tip);
        mTip = (TextView) findViewById(R.id.tip);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume(); 
        new Thread(new Runnable() {
            public void run() {
                getWChatPubKey();
                mUiHandler.sendEmptyMessage(EVENT_RESPONSE_CMD);
            }
        }).start();

    }

    private void getWChatPubKey(){
        try{
            DswLog.d(TAG, "getWChatPubKey start");
            mKeyStore = (Class<?>) Class.forName(CLASS_KEY_STORE);
            Object obj = mKeyStore.newInstance();
    		Method getInstances = mKeyStore.getMethod("getInstance");
            Object keystore = getInstances.invoke(obj);
            DswLog.i(TAG, "generateAttkKeyPair start");
    		Method generateAttkKeyPair = mKeyStore.getMethod("generateAttkKeyPair");
            int generateAttkKeyPairResult = (int)generateAttkKeyPair.invoke(keystore);
            DswLog.i(TAG, "generateAttkKeyPair end result="+generateAttkKeyPairResult);
            DswLog.i(TAG, "verifyAttkKeyPair start");
            Method verifyAttkKeyPair = mKeyStore.getMethod("verifyAttkKeyPair");
            int verifyAttkKeyPairResult = (int)verifyAttkKeyPair.invoke(keystore);
            DswLog.i(TAG, "verifyAttkKeyPair end result="+verifyAttkKeyPairResult);
            DswLog.i(TAG, "exportAttkPublicKey start");
            Method exportAttkPublicKey = mKeyStore.getMethod("exportAttkPublicKey");
            byte[] exportAttkPublicKeyResult = (byte[])exportAttkPublicKey.invoke(keystore);
            
            
           // for(int i=0; i<exportAttkPublicKeyResult.length; i++){
                //DswLog.i(TAG ,"exportAttkPublicKey result[" + i+"]="+exportAttkPublicKeyResult[i]);
           // }
           if(exportAttkPublicKeyResult != null){
               mResult = new String(exportAttkPublicKeyResult);
           }
		   DswLog.i(TAG, "exportAttkPublicKey end result="+mResult);
        }catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
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
        
        mTip.setText(mResult);
        
		TestResult tr = new TestResult();
		byte[] sn_buff = new byte[64];
		System.arraycopy(tr.readNvramInfo(), 0, sn_buff, 0, 64);
        String tag = "F";
        if(mResult != null && !mResult.equals("")){
            tag = "P";
            testResult = "1";
        }
		sn_buff = tr.getNewSN(TestResult.MMI_WCHAT_SOTER_TAG, tag, sn_buff);

        tr.writeToNvramInfo(sn_buff);
        DswLog.i(TAG, "sn_buff[30]="+sn_buff[30]);
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
