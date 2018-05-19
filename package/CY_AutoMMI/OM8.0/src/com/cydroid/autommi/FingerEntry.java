package com.cydroid.autommi;

import android.app.Activity;
import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.os.Handler;
import android.app.Activity;
import android.widget.TextView;
import android.os.RemoteException;
import android.os.Message;
import android.hardware.fingerprint.ICyFingerprintServiceReceiver;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.util.Log;
import android.os.IBinder;
import android.os.Binder;

public class FingerEntry extends BaseActivity {

	static private final String TAG = "FingerEntry";
    private TextView item;
    private TextView resultTitle;
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
        item =  (TextView) findViewById(R.id.item);
        resultTitle =  (TextView) findViewById(R.id.result);
        item.setText(getString(R.string.result_fingerentry));

		startTest();
	}


	@Override
	protected void onResume() {
		super.onResume();
		super.onResume();
		DswLog.i(TAG, "onResume");
		try {
			if(testMethod != null) {
				DswLog.i(TAG, "MMI call FingerprintManager test(4) begin");
				testMethod.invoke(fingerprintmanager,mToken, 4, mICyFingerprintServiceReceiver);
				DswLog.i(TAG, "MMI call FingerprintManager test(4) end");
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DswLog.e(TAG, "onDestroy");

	}

	private Handler uiHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					resultTitle.setText(R.string.success);
					((AutoMMI) getApplication()).recordResult(TAG, "", "1");
					break;
				default:
					resultTitle.setText(R.string.fail);
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
			DswLog.e(TAG,"MMI FingerPrintsTest startTest Failed #1");
			DswLog.v(TAG, Log.getStackTraceString(e));
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			DswLog.e(TAG,"MMI FingerPrintsTest startTest Failed #2");
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
