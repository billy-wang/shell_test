package com.gionee.autommi;



import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
//Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 begin
import com.gionee.autommi.TestUtils;
//Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 end
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

        Log.d(TAG, "onResume");
		closeFlash();
	}

       public void closeFlash() {
        //Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 begin
        boolean mIsFlashOpened = TestUtils.writeNodeState(FlashCloseTest.this,"NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_0", 0);
        //Gionee <GN_BSP_AUTOMMI> <lifeilong> <20170317> modify for ID 86797 end
        Log.d(TAG, "turn off torch");
        if (null == mCamera) {
            try {
                Log.d(TAG, "null == mCamera");
                mCamera = Camera.open();     
                mParameters = mCamera.getParameters();
              	Log.d(TAG, "mParameters"+mParameters);
            	mParameters = mCamera.getParameters();
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(mParameters);
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
        Log.d("aaaa", "onPause");
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        Log.d("aaaa", "onStop");
		this.finish();
	}
}
