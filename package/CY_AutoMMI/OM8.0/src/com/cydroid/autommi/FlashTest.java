package com.cydroid.autommi;

import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.content.Context;

public class FlashTest extends BaseActivity {

	static private final String TAG = "FlashTest";

    private boolean mIsFlashOpened = false;
    private Context mContext;
    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean mFlashlightEnabled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        initdata(FlashTest.this);
	}

    private void initdata(Context context) {
        mContext = context.getApplicationContext();
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        tryInitCamera();
    }

    private void tryInitCamera() {
        try {
            mCameraId = getBackCameraId();
        } catch (CameraAccessException e) {
            DswLog.e(TAG, "Couldn't initialize. err="+e.getMessage());
        }

    }

    private void setFlashLight(boolean enabled) {
        DswLog.i(TAG, "setFlashLight: enable=" + enabled + ", current_state=" + mFlashlightEnabled);
        if (mCameraId == null) {
            tryInitCamera();
            if (mCameraId == null) {
                DswLog.e(TAG, "setFlashLight: camera unavailable or no camera facing back");
            }
        }
        if (mFlashlightEnabled != enabled) {
            mFlashlightEnabled = enabled;
            try {
                mCameraManager.setTorchMode(mCameraId, enabled);
            } catch (Exception e) {
                DswLog.e(TAG, "Couldn't set torch mode e=" + e.getMessage());
                mFlashlightEnabled = false;
            }
        }
    }


    private String getBackCameraId() throws CameraAccessException {
        String[] ids = mCameraManager.getCameraIdList();
        DswLog.i(TAG, "getBackCameraId length="+ids.length);
        for (String id : ids) {

            CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = cc.get(CameraCharacteristics.LENS_FACING);

            DswLog.d(TAG, "getBackCameraId: flashAvailable = " +
                    flashAvailable + ", lensFacing = " + lensFacing + "..." + CameraCharacteristics.LENS_FACING_BACK);

            if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();
        DswLog.e(TAG,"onResume");
        setFlashLight(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        DswLog.e(TAG,"onPause");
        setFlashLight(false);
    }
    @Override
		
    protected void onStop() {
        super.onStop();
        mCameraManager = null;
        DswLog.e(TAG, "onStop");
        this.finish();
    }
}
