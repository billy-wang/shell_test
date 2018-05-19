package com.gionee.autommi;

import android.app.Activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FrontFlashTest extends BaseActivity {

    static private final String TAG = "FrontFlashTest";
    String targeF;
    private Camera mCamera;
    Camera.Parameters mParameters = null;
    private static final int MESSAGE_REFRESH_BUTTON_STATUE = 0;
	private boolean mIsFlashOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.i(TAG, "onResume");
        turnOnFlashLight();

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.i(TAG, "onPause");
        turnOffFlashLight();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.d(TAG, "onStop");
        this.finish();
    }

    public boolean writeGestureNodeValue(String nodeType, int value) {
        Object pm = (Object) (getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
            method.invoke(pm, f.get(null), value);
            Log.i(TAG, "writeGestureNodeValue " + nodeType + " " + value);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e);
        }
        return false;
    }

    private void turnOnFlashLight() {
        Log.i(TAG, "turnOnFlashLight");
        mIsFlashOpened = writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 40);

        if (!mIsFlashOpened && null == mCamera) {
            try {
                Log.i(TAG, "open camera");
                mCamera = Camera.open(1);
                mParameters = mCamera.getParameters();
                String currFlashMode = mParameters.getFlashMode();
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                // Gionee zhangke 20160109 add for CR01619039 start
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                // Gionee zhangke 20160109 add for CR01619039 end
                mCamera.setParameters(mParameters);
                mCamera.startPreview();
                mIsFlashOpened = true;
            } catch (Exception e) {
                Log.e(TAG, "turnOnFlashLight Exception=" + e.getMessage());
            }
        }
    }

    private void turnOffFlashLight() {
        Log.e(TAG, "turnOffFlashLight");
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 0);

        if (mCamera != null) {
            Log.i(TAG, "close camera");
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }
}
