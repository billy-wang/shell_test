package com.gionee.autommi;



import android.app.Activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
//Gionee zhangke 20160324 add for CR01655707 start
import java.lang.reflect.Field;
import java.lang.reflect.Method;
//Gionee zhangke 20160324 add for CR01655707 end

public class FlashTest extends BaseActivity {

	static private final String TAG = "FlashTest";
	String targeF;
    private Camera mCamera;
    Camera.Parameters mParameters = null;
    //Gionee zhangke 20160324 add for CR01655707 start
    private boolean mIsFlashOpened = false;
    //Gionee zhangke 20160324 add for CR01655707 end 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);


	}

    //Gionee zhangke 20160324 add for CR01655707 start
    public boolean writeGestureNodeValue(String nodeType, int value) {
        Object pm = (Object) (getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
            method.invoke(pm, f.get(null), value);
			Log.i(TAG,"writeGestureNodeValue "+nodeType+" "+value);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e);
        }
        return false;
    }
    //Gionee zhangke 20160324 add for CR01655707 end

   	public void openFlash() {
        //Gionee zhangke 20160324 add for CR01655707 start
        mIsFlashOpened = writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 40);
        
        if (!mIsFlashOpened && null == mCamera) {
            try {
            	Log.e(TAG,"openFlash");
                mCamera = Camera.open();     
                mParameters = mCamera.getParameters();
                String currFlashMode = mParameters.getFlashMode();
                if (currFlashMode == null
                        || (!currFlashMode.equals(Camera.Parameters.FLASH_MODE_TORCH))) {
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                    mCamera.setParameters(mParameters);
                    mCamera.startPreview();
                } 
             }catch (Exception e) {
            	 Log.e(TAG,"openFlash is exception");
                e.printStackTrace();
            }
        }
        //Gionee zhangke 20160324 add for CR01655707 end
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.e(TAG,"onResume");
        openFlash();
    
    }
	
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.e(TAG,"onPause");
        //Gionee zhangke 20160324 add for CR01655707 start
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 0);
        //Gionee zhangke 20160324 add for CR01655707 end
        if(mCamera!= null){
            Log.e(TAG,"onPausemCamera!= null");
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    @Override
		
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.e(TAG, "onStop");
        this.finish();
    }
}
