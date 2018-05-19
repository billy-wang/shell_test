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

public class FingerDiscern extends BaseActivity {

	static private final String TAG = "Fpdiscern";
	//Gionee zhangke 20151027 add for CR01565388 start
    private TextView item;
    private TextView result;
	//Gionee zhangke 20151027 add for CR01565388 end
    private IFingerprintSensorTest mService;
    FingerprintSensorTest mFingerprintSensorTest;

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
		Log.e(TAG, "onResume");
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
