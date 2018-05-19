package com.cydroid.autommi;



import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.widget.Toast;

public class FlashCloseTest extends BaseActivity {

	static private final String TAG = "FlashCloseTest";
	String targeF;
    private Camera mCamera;
    Camera.Parameters mParameters = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

        DswLog.e(TAG, "onResume");
		closeFlash();
	}

       public void closeFlash() {
        DswLog.e(TAG, "turn off torch");
        if (null == mCamera) {
            try {
                DswLog.e(TAG, "null == mCamera");
                mCamera = Camera.open();     
                mParameters = mCamera.getParameters();
              	DswLog.e(TAG, "mParameters"+mParameters);
            	mParameters = mCamera.getParameters();
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mParameters);
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
             }catch (Exception e) {
                e.printStackTrace();
            }
        }

       }

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        DswLog.e(TAG, "onPause");
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        DswLog.e(TAG, "onStop");
		this.finish();
	}
}
