package com.cydroid.autommi;

import android.app.Activity;
import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.app.Activity;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.hardware.fingerprint.ICyFingerprintServiceReceiver;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.util.Log;
import android.os.IBinder;
import android.os.Binder;


public class FingerDeadPixelTest extends BaseActivity {

    static private final String TAG = "FingerDeadPixelTest";
    private TextView mItem;
    private TextView mResult;
    private IBinder mToken = new Binder();
    private Object fingerprintmanager;
    private boolean fingerFlag = false;
    private Class<?> clazz;
    private Method testMethod;
    private Method stopMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AutoMMI) getApplication()).recordResult(TAG, "", "0");
        setContentView(R.layout.result);
        mItem = (TextView) findViewById(R.id.item);
        mResult = (TextView) findViewById(R.id.result);
        mItem.setText(getString(R.string.result_deadpixel_test));

        startTest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DswLog.e(TAG, "onResume");
        try {
            if(testMethod != null) {
                DswLog.i(TAG, "MMI call FingerprintManager test(2) begin");
                testMethod.invoke(fingerprintmanager,mToken, 2, mICyFingerprintServiceReceiver);
                DswLog.i(TAG, "MMI call FingerprintManager test(2) end");
            }
        } catch (IllegalAccessException e) {
            DswLog.i(TAG, "MMI call FingerprintManager Fail #1");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            DswLog.i(TAG, "MMI call FingerprintManager Fail #2");
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        DswLog.e(TAG, "onPause");
    }



    @Override
    protected void onStop() {
        super.onStop();
        DswLog.e(TAG, "onStop");
        finish();
    }

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mResult.setText(R.string.sensortest_deadpixel_test_success);
                    ((AutoMMI) getApplication()).recordResult(TAG, "", "1");
                    break;
                default:
                    mResult.setText(R.string.sensortest_deadpixel_test_fail);
                    ((AutoMMI) getApplication()).recordResult(TAG, "", "0");
                    break;
            }
        }
    };


    private void startTest() {
        try {
            Class clazz = Class.forName("android.hardware.fingerprint.FingerprintManager");
            fingerprintmanager = (Object)this.getSystemService("fingerprint");
            DswLog.e(TAG,"fingerprintmanager=" + fingerprintmanager );
            DswLog.e(TAG,"clazz" + clazz );
            testMethod = clazz.getMethod("test",IBinder.class, int.class, ICyFingerprintServiceReceiver.class);
            DswLog.e(TAG,"testMethod=" + testMethod );
            stopMethod = clazz.getMethod("cancelTest",IBinder.class);
            DswLog.e(TAG,"stopMethod=" + stopMethod );
        } catch (ClassNotFoundException e) {
            DswLog.e(TAG,"MMI FingerPrintsTest2 startTest Failed #1");
            DswLog.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            DswLog.e(TAG,"MMI FingerPrintsTest2 startTest Failed #2");
            DswLog.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    private ICyFingerprintServiceReceiver mICyFingerprintServiceReceiver = new ICyFingerprintServiceReceiver.Stub(){
        public void onError(long deviceId, int errMsgId, int vendorCode){
            DswLog.d(TAG,"onError deviceId=" + deviceId + " errMsgId="+errMsgId + " vendorCode="+vendorCode);
        }

        public void onTestCmd(long deviceId, int cmdId, int result){
            DswLog.d(TAG,"onTestCmd cmdId="+cmdId+" result="+result);
            uiHandler.sendEmptyMessage(result);

            try {
                if(testMethod != null) {
                    DswLog.i(TAG, "MMI call FingerprintManager cancelTest() begin");
                    stopMethod.invoke(fingerprintmanager,mToken);
                    DswLog.i(TAG, "MMI call FingerprintManager cancelTest() end");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    };
}
