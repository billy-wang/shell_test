package com.gionee.autommi;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.Handler;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import android.widget.ImageView;
import android.app.Activity;
import android.os.Message;
import android.widget.TextView;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import com.fingerprints.service.IFingerprintService;
import com.fingerprints.service.IFingerprintSensorTest;
import com.fingerprints.service.IFingerprintSensorTestListener;
import com.fingerprints.service.FingerprintSensorTest;
import com.fingerprints.service.FingerprintSensorTest.FingerprintSensorTestListener;
import android.content.Context;
import java.lang.reflect.Constructor;
import android.hardware.fingerprint.IGnFingerprintServiceReceiver;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.os.IBinder;
import android.os.Binder;


public class FingerDeadPixelTest extends BaseActivity {

    static private final String TAG = "FingerDeadPixelTest";
    private TextView mItem;
    private TextView mResult;
    private IFingerprintSensorTest mService;
    FingerprintSensorTest mFingerprintSensorTest;
    // Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    // Gionee zhangke 20160428 modify for CR01687958 end
    private Class<?> clazz;
    private Object o;
    private Method testMethod;
    private Method stopMethod;
    private IBinder mToken = new Binder();
    private Class<?> servicemanager;
    private IFingerprintService service = null;
    private Object fingerprintmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        // Gionee zhangke 20151027 add for CR01565388 start
        mItem = (TextView) findViewById(R.id.item);
        mResult = (TextView) findViewById(R.id.result);
        mItem.setText(getString(R.string.result_deadpixel_test));
        //Gionee <GN_BSP_MMI> <lifeilong> <20170706> modify for ID 164299 begin
        try {
            Class clazz = Class.forName("android.hardware.fingerprint.FingerprintManager");
            Constructor[] constructors = clazz.getConstructors();
            Constructor c = null;
            for (int i = 0; i < constructors.length; i++) {
                Log.e(TAG,"" + constructors[i]);
                c = constructors[i];
            }
            c.setAccessible(true);
            o = c.newInstance(FingerDeadPixelTest.this,service);
            testMethod = clazz.getMethod("test",IBinder.class, int.class, IGnFingerprintServiceReceiver.class);
            stopMethod = clazz.getMethod("cancelTest",IBinder.class);
            fingerprintmanager = (Object)this.getSystemService("fingerprint");
        } catch (ClassNotFoundException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (InstantiationException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (IllegalAccessException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (InvocationTargetException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        }
        ((AutoMMI) getApplication()).recordResult(TAG, "", "0");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 begin
        try {
            if(testMethod != null) {
                testMethod.invoke(fingerprintmanager,mToken, 2, mGnFingerprintServiceReceiver);
            }
        } catch (IllegalAccessException e) {
             e.printStackTrace();
        } catch (InvocationTargetException e) {
             e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 begin
        try {
            if(testMethod != null) {
                stopMethod.invoke(fingerprintmanager,mToken);
            }
        } catch (IllegalAccessException e) {
             e.printStackTrace();
        } catch (InvocationTargetException e) {
             e.printStackTrace();
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 end
    }
    
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        finish();
    }

    //Gionee <GN_BSP_MMI> <lifeilong> <20170706> modify for ID 164299 begin
    private IGnFingerprintServiceReceiver mGnFingerprintServiceReceiver = new IGnFingerprintServiceReceiver.Stub(){
            public void onError(long deviceId, int errMsgId){
                Log.d(TAG,"onError	errMsgId="+errMsgId);
            }

            public void onTestCmd(long deviceId, int cmdId, int result){
                Log.d(TAG,"onTestCmd cmdId="+cmdId+" result="+result);
                if (result == 0) {
                    uiHandler.sendEmptyMessage(0);
                } else {
                    uiHandler.sendEmptyMessage(1);
                }
            }
    };

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mResult.setText(R.string.sensortest_deadpixel_test_success);
                    ((AutoMMI) getApplication()).recordResult(TAG, "", "1");
                    break;
                case 1:
                    mResult.setText(R.string.sensortest_deadpixel_test_fail);
                    ((AutoMMI) getApplication()).recordResult(TAG, "", "0");
                    break;
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170706> modify for ID 164299 end

}
