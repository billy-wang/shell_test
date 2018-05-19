package com.gionee.autommi;



import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
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

        Log.e(TAG, "onResume");
		closeFlash();
	}

       public void closeFlash() {
        Log.e(TAG, "turn off torch");
        if (null == mCamera) {
            try {
                Log.e(TAG, "null == mCamera");
                mCamera = Camera.open();     
                mParameters = mCamera.getParameters();
              	Log.e(TAG, "mParameters"+mParameters);
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
        Log.e(TAG, "onPause");
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        Log.e(TAG, "onStop");
		this.finish();
	}
}
