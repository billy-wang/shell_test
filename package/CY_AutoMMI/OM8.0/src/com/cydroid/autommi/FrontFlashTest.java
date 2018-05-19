package com.cydroid.autommi;

import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.content.Context;
import com.cydroid.autommi.TestUtils;
import android.os.SystemProperties;

public class FrontFlashTest extends BaseActivity {

    static private final String TAG = "FrontFlashTest";

    private boolean mIsFlashOpened = false;
    private Context mContext;
    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean mFlashlightEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initdata(FrontFlashTest.this);
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
                    flashAvailable + ", lensFacing = " + lensFacing + "..." + CameraCharacteristics.LENS_FACING_FRONT);

            if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                return id;
            }
        }
        return null;
    }

    private String getZnVersionNum() {
        return SystemProperties.get("ro.cy.znvernumber");
    }

    private void switchFlashStyle(boolean yes) {
        String rom = getZnVersionNum().split("_")[0].substring(0, 7);
        DswLog.d(TAG,"#project rom="+rom);
        switch (rom) {
            case "CSW1703":
                TestUtils.writeNodeState(mContext, "NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", yes ? 40:0);
                break;
            default:
                setFlashLight(yes);
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        DswLog.e(TAG,"onResume");
        switchFlashStyle(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DswLog.e(TAG,"onPause");
        switchFlashStyle(false);
    }
    @Override

    protected void onStop() {
        super.onStop();
        mCameraManager = null;
        DswLog.e(TAG, "onStop");
        this.finish();
    }
}
