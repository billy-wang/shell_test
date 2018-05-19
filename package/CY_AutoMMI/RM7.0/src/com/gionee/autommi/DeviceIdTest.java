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

public class DeviceIdTest extends BaseActivity {
    public static final String TAG = "DeviceIdTest";
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
                getDeviceId();
                mUiHandler.sendEmptyMessage(EVENT_RESPONSE_CMD);
            }
        }).start();

    }

    private void getDeviceId(){
        try{
            Log.d(TAG, "getWChatPubKey start");
            mKeyStore = (Class<?>) Class.forName(CLASS_KEY_STORE);
            Object obj = mKeyStore.newInstance();
            Method getInstances = mKeyStore.getMethod("getInstance");
            Object keystore = getInstances.invoke(obj);
            Log.i(TAG, "getDeviceId start");
            Method getDeviceId = mKeyStore.getMethod("getDeviceId");
            byte[] getDeviceIdResult = (byte[])getDeviceId.invoke(keystore);
            if(getDeviceIdResult != null){
                for(int i=0; i<getDeviceIdResult.length; i++){
                    Log.i(TAG ,"getDeviceIdResult result[" + i+"]="+getDeviceIdResult[i]);
                }
                mResult = bytes2HexString(getDeviceIdResult);
            }
            
            Log.i(TAG, "mResult="+mResult);
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
        String testResult = "1";
        if(mResult == null || mResult.equals("00000000000000000000000000000000")){
            testResult = "0";
        }
        mTip.setText(mResult);

        ((AutoMMI) getApplication()).recordResult(TAG, mResult, testResult);

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        finish();
    }
	
    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[ i ] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex;
        }
        return ret;
    }
}
