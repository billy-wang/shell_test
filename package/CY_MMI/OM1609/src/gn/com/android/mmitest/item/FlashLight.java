package gn.com.android.mmitest.item;


import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

//Gionee zhangke 20151212 add for CR01608407 start
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//Gionee zhangke 20151212 add for CR01608407 end

public class FlashLight extends BaseActivity implements OnClickListener {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn,mToggleBtn;
    private TextView mTitleTv;

    private static final String TAG = "FlashLight";
    private boolean isFlash = false;
    private static final String FLASHLIGHT_PATH = "/sys/class/flashlightdrv/kd_camera_flashlight/torch";

    //Gionee zhangke 20151212 add for CR01608407 start
    private Camera mCamera;
    Camera.Parameters mParameters = null;
    private boolean mIsFlashOpened = false;
    //Gionee zhangke 20151212 add for CR01608407 end
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 start
//    private FlashLightBroadcastReceiver mFLBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.checkToContinue(this);
        //Gionee <Oveasea_Bug> <tanbotao> <20161121> for CR01772500 begin
        setContentView(R.layout.flash_light);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setEnabled(false);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mToggleBtn = (Button) findViewById(R.id.toggle_button);
		mToggleBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn.setOnClickListener(this);
        mTitleTv = (TextView) findViewById(R.id.flash_test_title);
        mTitleTv.setText(R.string.test_title);

        try {
            //Gionee <GN_BSP_MMI> <chengq> <20170411> modify for ID 109364 begin
            mToggleBtn.setVisibility(View.GONE);
            //Gionee <GN_BSP_MMI> <chengq> <20170411> modify for ID 109364 end
            IntentFilter filter = new IntentFilter("com.gionee.flashlightClick.MMITEST");
            registerReceiver(flashLightBroadcastReceiver, filter);
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.gionee.flashlight");
            startActivity(intent);

        } catch (Exception e) {
            // TODO: handle exception
//            setContentView(R.layout.common_textview);
            Log.e(TAG, "Exception e-1");
            //Gionee <GN_BSP_MMI> <chengq> <20170411> modify for ID 109364 begin
            mToggleBtn.setVisibility(View.VISIBLE);
            //Gionee <GN_BSP_MMI> <chengq> <20170411> modify for ID 109364 end
            turnOnFlashLight();
//            writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 40);
            Log.e(TAG, "wrie NODE_TYPE_FLASHLIGHT_CAMERA_TORCH 401 ");
//            setContentView(R.layout.flash_light);
//            mRightBtn = (Button) findViewById(R.id.right_btn);
//            mRightBtn.setEnabled(false);
//            mRightBtn.setOnClickListener(this);
//            mWrongBtn = (Button) findViewById(R.id.wrong_btn);
//            mRestartBtn = (Button) findViewById(R.id.restart_btn);
//            mWrongBtn.setOnClickListener(this);
//            mRestartBtn.setOnClickListener(this);
//            mTitleTv = (TextView) findViewById(R.id.flash_test_title);
//            mTitleTv.setText(R.string.test_title);
            isFlash=true;
            //Gionee <Oveasea_Bug> <tanbotao> <20161121> for CR01772500 end
            Log.e(TAG, "Exception e-2");
        }
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        /*Gionee huangjianqiang 20160307 modify for CR01634928 begin*/
        //Gionee zhangke 20151212 add for CR01608407 start
        if (isFlash) {
            turnOffFlashLight();
        }
        //Gionee zhangke 20151212 add for CR01608407 end
        /*Gionee huangjianqiang 20160307 modify for CR01634928 end*/

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
    /* Gionee 20160907 tanbotao add for begin */
    private BroadcastReceiver flashLightBroadcastReceiver =new BroadcastReceiver (){
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e(TAG, "FlashLightBroadcastReceiver onReceive ");
            if (intent.getAction().equals("com.gionee.flashlightClick.MMITEST")) {
                boolean flashlight_clicked = intent.getBooleanExtra("flashlight_clicked", false);
                Log.e(TAG, "FlashLightBroadcastReceiver intent.getAction()+ flashlight_clicked ="+flashlight_clicked);

                mRightBtn.setEnabled(flashlight_clicked);
            }
            /*Gionee tanbotao 20160924 add for CR01761272 begin*/
            isFlash = true;
            /*Gionee tanbotao 20160924 add for CR01761272 begin*/
        }
    };

    /* Gionee 20160907 tanbotao add for end */


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
                Log.d(TAG, "zhangxiaowei -restart");
                TestUtils.restart(this, TAG);
                break;
            }
            //Gionee <BP_BSP_MMI> <chengq> <201704005> add for ID 103642 begin
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
            //Gionee <BP_BSP_MMI> <chengq> <201704005> add for ID 103642 end
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    //Gionee zhangke 20151212 add for CR01608407 start
    private static final int MESSAGE_REFRESH_BUTTON_STATUE = 0;

    private void turnOnFlashLight() {
        Log.i(TAG, "turnOnFlashLight");
        mIsFlashOpened = writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 40);

        if (!mIsFlashOpened && null == mCamera) {
            try {
                Log.i(TAG, "open camera");
                mCamera = Camera.open();
                mParameters = mCamera.getParameters();
                String currFlashMode = mParameters.getFlashMode();
                mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                //Gionee zhangke 20160109 add for CR01619039 start
                /*Gionee huangjianqiang 20160216 modify for CR01635509 begin*/
//				mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                /*Gionee huangjianqiang 20160216 modify for CR01635509 end*/
                //Gionee zhangke 20160109 add for CR01619039 end
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
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 0);
        mIsFlashOpened = false;
        if (mCamera != null) {
            Log.i(TAG, "close camera");
            /*Gionee huangjianqiang 20160216 add for CR01635509 begin*/
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
            /*Gionee huangjianqiang 20160216 add for CR01635509 end*/

            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    //Gionee zhangke 20151212 add for CR01608407 end

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flashLightBroadcastReceiver != null) {
            unregisterReceiver(flashLightBroadcastReceiver);
        }
    }
}
