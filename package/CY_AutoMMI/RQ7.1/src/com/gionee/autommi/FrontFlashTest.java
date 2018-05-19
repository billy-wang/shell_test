package com.gionee.autommi;



import android.app.Activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
//Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 begin
import com.gionee.autommi.TestUtils;
//Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 begin
public class FrontFlashTest extends BaseActivity {

	static private final String TAG = "FrontFlashTest";
	String targeF;
    private Camera mCamera;
    Camera.Parameters mParameters = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);


	}
	
    
    public void openFlash() {
        //Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 begin
        boolean mIsFlashOpened = TestUtils.writeNodeState(FrontFlashTest.this,"NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 40);
        //Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 end
        if (!mIsFlashOpened && null == mCamera) {
            try {
            	Log.i(TAG,"openFlash");
                mCamera = Camera.open(1);     
                mParameters = mCamera.getParameters();
                String currFlashMode = mParameters.getFlashMode();
                if (currFlashMode == null
                        || (!currFlashMode.equals(Camera.Parameters.FLASH_MODE_TORCH))) {
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(mParameters);
                    mCamera.startPreview();
                } 
             }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
    	Log.i(TAG,"onResume");
		openFlash();

	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
    	Log.i(TAG,"onPause");
        //Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 begin
        TestUtils.writeNodeState(FrontFlashTest.this,"NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 0);
        //Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 end
	if(mCamera!= null){
	    Log.i(TAG,"onPausemCamera!= null");
	    		mCamera.setPreviewCallback(null);
	    		mCamera.stopPreview();
                mCamera.release();
                mCamera = null;}
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        Log.d("aaaa", "onStop");
		this.finish();
	}
}
