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
//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 begin
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 end







//Gionee zhangke 20160123 modify for CR01630432 start
public class WChatKeyTest extends BaseActivity implements QcRilHookCallback {
	//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 begin
	public static final String FACTORY_WC = "WChatKeyTest";
	//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 end
    public static final String TAG = "WChatKeyTest";
    private static final int EVENT_RESPONSE_CMD = 1;
    private TextView mTip = null;
    private Context mContext;
    private Class<?> mKeyStore;
    private static final String CLASS_KEY_STORE = "android.security.KeyStore";
    private Object mObj;
    private String mResult = "";
	//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 begin
	private SharedPreferences mResultSP;
    private SharedPreferences mSNResultSP;
    SharedPreferences.Editor mSNEditor;
    private int mCount;
    private QcNvItems nvItems = null;
	//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 end


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
            Log.d(TAG, "getWChatPubKey start");
            mKeyStore = (Class<?>) Class.forName(CLASS_KEY_STORE);
            Object obj = mKeyStore.newInstance();
    		Method getInstances = mKeyStore.getMethod("getInstance");
            Object keystore = getInstances.invoke(obj);
            Log.i(TAG, "generateAttkKeyPair start");
    		Method generateAttkKeyPair = mKeyStore.getMethod("generateAttkKeyPair");
            int generateAttkKeyPairResult = (int)generateAttkKeyPair.invoke(keystore);
            Log.i(TAG, "generateAttkKeyPair end result="+generateAttkKeyPairResult);
            Log.i(TAG, "verifyAttkKeyPair start");
            Method verifyAttkKeyPair = mKeyStore.getMethod("verifyAttkKeyPair");
            int verifyAttkKeyPairResult = (int)verifyAttkKeyPair.invoke(keystore);
            Log.i(TAG, "verifyAttkKeyPair end result="+verifyAttkKeyPairResult);
            Log.i(TAG, "exportAttkPublicKey start");
            Method exportAttkPublicKey = mKeyStore.getMethod("exportAttkPublicKey");
            byte[] exportAttkPublicKeyResult = (byte[])exportAttkPublicKey.invoke(keystore);
            
            
           // for(int i=0; i<exportAttkPublicKeyResult.length; i++){
                //Log.i(TAG ,"exportAttkPublicKey result[" + i+"]="+exportAttkPublicKeyResult[i]);
           // }
           if(exportAttkPublicKeyResult != null){
               mResult = new String(exportAttkPublicKeyResult).trim();
           }
		   Log.i(TAG, "exportAttkPublicKey end result=\n"+mResult);
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
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 begin
        nvItems = new QcNvItems(this, this);
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 end 
        mTip.setText(mResult);
/*

		TestResult tr = new TestResult();
		byte[] sn_buff = new byte[64];
		System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, 64);
        String tag = "F";
        if(mResult != null && !mResult.equals("")){
            tag = "P";
            testResult = "1";
        }
		sn_buff = tr.getNewSN(TestResult.MMI_WCHAT_SOTER_TAG, tag, sn_buff);

        tr.writeToProductInfo(sn_buff);
        Log.i(TAG, "sn_buff[30]="+sn_buff[30]);*/
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161124> modify for ID32386 begin
        if(!"".equals(mResult)){
			testResult = "1";
		}else{
			mTip.setText(getString(R.string.wchatfail));
		}
		//Gionee <GN_BSP_AutoMMI><lifeilong><20161124> modify for ID32386 end
		((AutoMMI) getApplication()).recordResult(TAG, mResult, testResult);
        

    }

	
	//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 begin
	public void onQcRilHookReady() {
		Log.e(TAG,"onQcRilHookReady  start ");
			mCount = 0;

			try {
				Context context = createPackageContext("gn.com.android.mmitest", Activity.CONTEXT_IGNORE_SECURITY);
				mResultSP = context.getSharedPreferences("gn_mmi_test", Context.MODE_WORLD_WRITEABLE);
				mSNResultSP = context.getSharedPreferences("gn_mmi_sn", Context.MODE_WORLD_WRITEABLE);
				//mResultSP = getSharedPreferences("gn_mmi_test", Context.MODE_WORLD_WRITEABLE);
				//mSNResultSP = getSharedPreferences("gn_mmi_sn", Context.MODE_WORLD_WRITEABLE);
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}

			mSNEditor = mSNResultSP.edit();
			// Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin
			
			/*******************/
			String factoryResult = "";
			try {
				String oFS = nvItems.getFactoryResult();
				Log.e(TAG, "oFS= " + oFS);
				String nFS = getNewFactorySet(oFS);
				Log.e(TAG, "nFS= " + nFS);
				nvItems.setFactoryResult(nFS + "0");
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
		if(!"".equals(mResult)){
			sb.setCharAt(32,'P');
		}else{
			sb.setCharAt(32,'F');
		}
        return sb.toString();
    }
	//Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 end



    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 begin
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 58824 begin
        if(mSNEditor != null){
            mSNEditor.clear();
            mSNEditor.commit();
        }
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 58824 end
        //Gionee <GN_BSP_AutoMMI><lifeilong><20161112> modify for 21007 end

        finish();
    }
}
// Gionee zhangke 20160123 modify for CR01630432 end
