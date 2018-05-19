package com.gionee.autommi;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import com.gionee.util.DswLog;
import android.os.Handler;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import android.widget.ImageView;
import android.app.Activity;
import android.os.Message;
import android.widget.TextView;
import com.fingerprints.service.IFingerprintService;
import com.fingerprints.service.IFingerprintSensorTest;
import com.fingerprints.service.IFingerprintSensorTestListener;
import com.fingerprints.service.FingerprintSensorTest;
import com.fingerprints.service.FingerprintSensorTest.FingerprintSensorTestListener;


public class FingerDeadPixelTest extends BaseActivity {

    static private final String TAG = "FingerDeadPixelTest";
    private TextView mItem;
    private TextView mResult;
    private IFingerprintSensorTest mService;
    FingerprintSensorTest mFingerprintSensorTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
        // Gionee zhangke 20151027 add for CR01565388 start
        mItem = (TextView) findViewById(R.id.item);
        mResult = (TextView) findViewById(R.id.result);
        mItem.setText(getString(R.string.result_deadpixel_test));
        // Gionee zhangke 20151027 add for CR01565388 end
        try{
            mFingerprintSensorTest = new FingerprintSensorTest();
        }catch(Exception e){
            DswLog.i(TAG, "mFingerprintSensorTest e="+e.getMessage());
        }
        ((AutoMMI) getApplication()).recordResult(TAG, "", "0");

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        DswLog.e(TAG, "onResume");
        try{
            DswLog.i(TAG, "onResume begin checkerboardTest");
            mFingerprintSensorTest.checkerboardTest(mFingerprintSensorTestListener);
            DswLog.i(TAG, "onResume end checkerboardTest");
        }catch(Exception e){
            DswLog.e(TAG, "checkerboardTest Exception="+e.getMessage());
        }

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        DswLog.e(TAG, "onPause");
    }

    private FingerprintSensorTestListener mFingerprintSensorTestListener = new FingerprintSensorTestListener() {
    	
    	@Override
    	public void onSelfTestResult(boolean result) {
    		// TODO Auto-generated method stub
    		DswLog.i(TAG, "onSelfTestResult result="+result);
    	}
    	
    	@Override
    	public void onImagequalityTestResult(int result) {
    		// TODO Auto-generated method stub
    		DswLog.i(TAG, "onImagequalityTestResult result="+result);
    	}
    	
    	@Override
    	public void onCheckerboardTestResult(int result) {
    		// TODO Auto-generated method stub
    		DswLog.i(TAG, "onCheckerboardTestResult result="+result);
    		if(result == 0){
                mResult.setText(R.string.sensortest_deadpixel_test_success);
                ((AutoMMI) getApplication()).recordResult(TAG, "", "1");  
    		}else {
                mResult.setText(R.string.sensortest_deadpixel_test_fail);
				((AutoMMI) getApplication()).recordResult(TAG, "", "0");
    		}
    	}
    
    	@Override
    	public void onCaptureTestResult(int result) {
    		// TODO Auto-generated method stub
    		DswLog.i(TAG, "onCaptureTestResult result="+result);
    	}
    
    };

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        finish();
    }

}
