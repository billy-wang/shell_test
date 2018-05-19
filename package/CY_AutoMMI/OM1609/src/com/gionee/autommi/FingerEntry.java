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
import android.widget.TextView;
import com.fingerprints.service.IFingerprintService;
import com.fingerprints.service.IFingerprintSensorTest;
import com.fingerprints.service.IFingerprintSensorTestListener;
import com.fingerprints.service.FingerprintSensorTest;
import com.fingerprints.service.FingerprintSensorTest.FingerprintSensorTestListener;

public class FingerEntry extends BaseActivity {

	static private final String TAG = "FingerEntry";
    private TextView item;
    private TextView result;
    private IFingerprintSensorTest mService;
    FingerprintSensorTest mFingerprintSensorTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//((AutoMMI) getApplication()).recordResult(TAG, "", "0");
		setContentView(R.layout.result);
        item =  (TextView) findViewById(R.id.item);
        result =  (TextView) findViewById(R.id.result);
        item.setText(getString(R.string.result_fingerentry));
        try{
            mFingerprintSensorTest = new FingerprintSensorTest();
        }catch(Exception e){
            Log.i(TAG, "mFingerprintSensorTest e="+e.getMessage());
        }
	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        try{
            Log.i(TAG, "onResume begin captureImage");
            mFingerprintSensorTest.captureImage(true,mFingerprintSensorTestListener);
            Log.i(TAG, "onResume end captureImage");
        }catch(Exception e){
            Log.e(TAG, "captureImage Exception="+e.getMessage());
        }

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.e(TAG, "onPause");
		mFingerprintSensorTest.cancel();
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.e(TAG, "onStop");
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.e(TAG, "onDestroy");
		//System.exit(0);
	}

    private FingerprintSensorTestListener mFingerprintSensorTestListener = new FingerprintSensorTestListener() {
    	
    	@Override
    	public void onSelfTestResult(boolean result) {
    		// TODO Auto-generated method stub
    		Log.i(TAG, "onSelfTestResult result="+result);
    	}
    	
    	@Override
    	public void onImagequalityTestResult(int result) {
    		// TODO Auto-generated method stub
    		Log.i(TAG, "onImagequalityTestResult result="+result);
    	}
    	
    	@Override
    	public void onCheckerboardTestResult(int result) {
    		// TODO Auto-generated method stub
    		Log.i(TAG, "onCheckerboardTestResult result="+result);
    	}
    
    	@Override
    	public void onCaptureTestResult(int captureResult) {
    		// TODO Auto-generated method stub
    		Log.i(TAG, "onCaptureTestResult captureResult="+captureResult);
    		if(captureResult == 0){
				result.setText(R.string.success);
				((AutoMMI) getApplication()).recordResult(TAG, "", "1");
    		}else{
				result.setText(R.string.fail);
				((AutoMMI) getApplication()).recordResult(TAG, "", "0");
    		}
    	}
    
    };

}
