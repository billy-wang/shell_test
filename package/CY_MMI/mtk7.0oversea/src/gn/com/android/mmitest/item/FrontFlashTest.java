package gn.com.android.mmitest.item;


import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
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

public class FrontFlashTest extends BaseActivity implements OnClickListener {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "FrontFlashTest";
    private boolean isFlash = false;
    private static final String FLASHLIGHT_PATH = "/sys/class/flashlightdrv/kd_camera_flashlight/torch";
    private final int FLASH_TORCH_1 = 1;
    private final int FLASH_TORCH = 2;
    private Camera mCamera;
    Camera.Parameters mParameters = null;
    private boolean mIsFlashOpened = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.checkToContinue(this);
        setContentView(R.layout.common_textview);
        isFlash = true;

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.test_title);
    }

    @Override
    public void onResume() {
        super.onResume();
        turnOnFlashLight();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onPause() {
        super.onPause();
        turnOffFlashLight();

    }

    public boolean writeGestureNodeValue(int value) {
        Object pm = getSystemService(Context.POWER_SERVICE);
        try {
            Class cls = Class.forName("android.os.PowerManager");
            Method method = cls.getMethod("setTorchBrightness", int.class);
            method.invoke(pm, value);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e);
        }
        return false;
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170221> modify for ID 69609 begin
    public boolean writeGestureNodeValue(String nodeType, int value) {
        Object pm = (Object) (getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
            method.invoke(pm, f.get(null), value);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e);
        }
        return false;
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170221> modify for ID 69609 end
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
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
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private static final int MESSAGE_REFRESH_BUTTON_STATUE = 0;

    private void turnOnFlashLight() {
        Log.i(TAG, "turnOnFlashLight");
        //Gionee <GN_BSP_MMI> <chengq> <20170225> add for ID 66991 begin
        if (FeatureOption.GN_RW_GN_MMI_FRONT_FLASH_SUPPORT_VALUE == FLASH_TORCH_1) {
            mIsFlashOpened = writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 40);
            Log.d(TAG,"turn on torch_1 40");
        }else if (FeatureOption.GN_RW_GN_MMI_FRONT_FLASH_SUPPORT_VALUE == FLASH_TORCH) {
            mIsFlashOpened = writeGestureNodeValue(30);
            Log.d(TAG,"turn on torch 30");
        }else {
            Log.i(TAG,"turn on the front flash have wrong torch value");
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170225> add for ID 66991 end
        if (!mIsFlashOpened && null == mCamera) {
            try {
                Log.i(TAG, "open camera");
                mCamera = Camera.open(1);
                mParameters = mCamera.getParameters();
                String currFlashMode = mParameters.getFlashMode();
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(mParameters);
                mCamera.startPreview();
                mIsFlashOpened = true;
            } catch (Exception e) {
                Log.e(TAG, "turnOnFlashLight Exception=" + e.getMessage());
            }
        }

        mUiHandler.sendEmptyMessage(MESSAGE_REFRESH_BUTTON_STATUE);
    }

    private void turnOffFlashLight() {
        Log.e(TAG, "turnOffFlashLight");
        //Gionee <GN_BSP_MMI> <chengq> <20170225> add for ID 66991 begin
        if (FeatureOption.GN_RW_GN_MMI_FRONT_FLASH_SUPPORT_VALUE == FLASH_TORCH_1) {
            writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 0);
            Log.d(TAG,"turn off torch_1 0");
        }else if (FeatureOption.GN_RW_GN_MMI_FRONT_FLASH_SUPPORT_VALUE == FLASH_TORCH) {
            mIsFlashOpened = writeGestureNodeValue(0);
            Log.d(TAG,"turn off torch 0");
        }else {
            Log.d(TAG,"turn off the front flash have wrong torch value");
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170225> add for ID 66991 end
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 0);
        if (mCamera != null) {
            Log.i(TAG, "close camera");
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH_BUTTON_STATUE:
                    if (mIsFlashOpened) {
                        mRightBtn.setEnabled(true);
                    }
                    mWrongBtn.setEnabled(true);
                    mRestartBtn.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };
}
