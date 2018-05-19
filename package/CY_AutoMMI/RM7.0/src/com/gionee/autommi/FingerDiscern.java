package com.gionee.autommi;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import android.widget.ImageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
//Gionee zhangke 20151027 add for CR01565388 start
import android.widget.TextView;
//Gionee zhangke 20151027 add for CR01565388 end
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
import android.util.Log;

public class FingerDiscern extends BaseActivity {

	static private final String TAG = "Fpdiscern";
	//Gionee zhangke 20151027 add for CR01565388 start
    private TextView item;
    private TextView result;
	//Gionee zhangke 20151027 add for CR01565388 end
    private IFingerprintSensorTest mService;
    FingerprintSensorTest mFingerprintSensorTest;
    private Class<?> clazz;
    private Object o;
    private Method testMethod;
    private Method stopMethod;
    private IBinder mToken = new Binder();
    private Class<?> servicemanager;
    private IFingerprintService service = null;
    private Object fingerprintmanager;
    private Handler handler;
    private static final int TEST_CMD_DEADPIXEL = 2;
    private static final int TEST_CMD_CAPTURE = 4;    
    private static final int TEST_CMD_TEST = 30;
    private static final int TEST_TIMER_OUT = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            setContentView(R.layout.result);
            //Gionee zhangke 20151027 add for CR01565388 start
            item =  (TextView) findViewById(R.id.item);
            result =  (TextView) findViewById(R.id.result);
            item.setText(getString(R.string.result_fingediscern));
            //Gionee zhangke 20151027 add for CR01565388 end
            try {
                Class clazz = Class.forName("android.hardware.fingerprint.FingerprintManager");
                fingerprintmanager = (Object)this.getSystemService("fingerprint");
                Log.e(TAG," oncreate == fingerprintmanager == " + fingerprintmanager );            
                Log.e(TAG," oncreate == clazz == " + clazz );
                testMethod = clazz.getMethod("test",IBinder.class, int.class, IGnFingerprintServiceReceiver.class);
                Log.e(TAG," oncreate == testMethod == " + testMethod );
                stopMethod = clazz.getMethod("cancelTest",IBinder.class);
                Log.e(TAG," oncreate == stopMethod == " + stopMethod );
                handler = new Handler();
                handler.postDelayed(mRunnable, TEST_TIMER_OUT);
            } catch (ClassNotFoundException e) {
                    Log.v(TAG, Log.getStackTraceString(e));
                    e.printStackTrace();
            } catch (NoSuchMethodException e) {
                    Log.v(TAG, Log.getStackTraceString(e));
                    e.printStackTrace();
            }
            ((AutoMMI) getApplication()).recordResult(TAG, "", "0");
	}
    
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.i(TAG, "hand enable button  time out !");
            uiHandler.sendEmptyMessage(6);
            result.setText(R.string.fingerprint_capture_image_timeout);
        }
    };    



	@Override
	protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
            try {
                Log.i(TAG, "onResume begin captureImage");            
                if(testMethod != null) {
                    testMethod.invoke(fingerprintmanager,mToken, 4, mGnFingerprintServiceReceiver);
                    Log.i(TAG, "onResume end captureImage");                
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
            try {
                if(testMethod != null) {
                    stopMethod.invoke(fingerprintmanager,mToken);
                }
            } catch (IllegalAccessException e) { 
                 e.printStackTrace();
            } catch (InvocationTargetException e) {  
                 e.printStackTrace();
            }
            handler.removeCallbacks(mRunnable);
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170708> modify for ID 164298 begin
            finish();
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170708> modify for ID 164298 end
	}

   private IGnFingerprintServiceReceiver mGnFingerprintServiceReceiver = new IGnFingerprintServiceReceiver.Stub(){
        public void onError(long deviceId, int errMsgId){
            Log.d(TAG,"onError  errMsgId="+errMsgId);
        } 

        public void onTestCmd(long deviceId, int cmdId, int result){
            Log.d(TAG,"onTestCmd cmdId="+cmdId+" result="+result);
            if (result == 0) {
                uiHandler.sendEmptyMessage(0);
            } else if (result == -1) {
                uiHandler.sendEmptyMessage(1);
            } else if (result == -4) {
                uiHandler.sendEmptyMessage(4);
            } else if (result == -3) {
                uiHandler.sendEmptyMessage(3);
            } else if (result == -2) {
                uiHandler.sendEmptyMessage(2);
            } else {
                uiHandler.sendEmptyMessage(5);
            }
            handler.removeCallbacks(mRunnable);
        }
    };


    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    result.setText(R.string.success);
                    ((AutoMMI) getApplication()).recordResult(TAG, "", "1");
                    break;
                case 1:
                    result.setText(R.string.fingerprint_capture_image_noise);
                    break;
                case 2:
                    result.setText(R.string.fingerprints_capture_not_enough);
                    break;
                case 3:
                    result.setText(R.string.fingerprint_capture_image_timeout);
                    break;
                case 4:
                    result.setText(R.string.fingerprint_capture_image_fail);
                    break;
                case 5:
                    result.setText(R.string.fail);
                    break;
                case 6:
                    try {
                        if(testMethod != null) {
                            stopMethod.invoke(fingerprintmanager,mToken);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {   
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };


}
