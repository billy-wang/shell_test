package cy.com.android.mmitest.item;


import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

public class FrontFlashTest extends BaseActivity implements OnClickListener {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn,mToggleBtn;

    private static final String TAG = "FrontFlashTest";
    private static final String FLASHLIGHT_PATH = "/sys/class/flashlightdrv/kd_camera_flashlight/torch";
    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean mFlashlightEnabled = false;
    private boolean mIsFlashOpened = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开前闪光灯 @" + Integer.toHexString(hashCode()));

        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.flash_light);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mRightBtn.setEnabled(false);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mToggleBtn = (Button) findViewById(R.id.toggle_button);
        mToggleBtn.setOnClickListener(this);
        TextView titleTv = (TextView) findViewById(R.id.flash_test_title);
        titleTv.setText(R.string.test_title);

        initdata(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出前闪光灯 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
        turnOnFlashLight();
    }

    @Override
    public void onPause() {
        super.onPause();
        turnOffFlashLight();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }

            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
            case R.id.toggle_button: {
                if(mIsFlashOpened){
                    mRightBtn.setEnabled(true);
                    turnOffFlashLight();
                    mToggleBtn.setText(getString(R.string.toggle_on));
                }else{
                    turnOnFlashLight();
                    mToggleBtn.setText(getString(R.string.toggle_off));
                }
                break;
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private void turnOnFlashLight() {
        DswLog.i(TAG, "turnOnFlashLight");
        setFlashLight(true);
        mIsFlashOpened = true;

    }

    private void turnOffFlashLight() {
        DswLog.e(TAG, "turnOffFlashLight");
        setFlashLight(false);
        mIsFlashOpened = false;
    }

    private void initdata(Context context) {
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
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
                DswLog.d(TAG, "use CamreaID="+id);
                return id;
            }
        }
        return null;
    }
}
