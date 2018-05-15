package gn.com.android.mmitest.item;


import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Intent;
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
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
//Gionee zhangke 20151212 add for CR01608407 end
import android.content.Intent;

public class FlashLight extends Activity implements OnClickListener {
    Button mToneBt;
    //Gionee zhangke 20160921 modify for CR01763025 begin
    private Button mRightBtn, mWrongBtn, mRestartBtn, mToggleBtn;
    //Gionee zhangke 20160921 modify for CR01763025 end

    private static final String TAG = "FlashLight";
    private boolean isFlash = false;
    private static final String FLASHLIGHT_PATH= "/sys/class/flashlightdrv/kd_camera_flashlight/torch";
    //Gionee zhangke 20151212 add for CR01608407 start
    private Camera mCamera;
    Camera.Parameters mParameters = null;
    private boolean mIsFlashOpened = false;
    //Gionee zhangke 20151212 add for CR01608407 end
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 start
    private Object manager;
    private Method setTorch;
    private boolean backFlashFlag = false;
    private Intent it;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //stUtils.checkToContinue(this);
        TestUtils.setWindowFlags(this);
        //Gionee zhangke 20160921 modify for CR01763025 begin
        setContentView(R.layout.flashlight_test);
        it = this.getIntent();
        if(it != null){
            backFlashFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"backFlashFlag = " + backFlashFlag);       
        //Gionee zhangke 20160921 modify for CR01763025 end
        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setEnabled(false);
        if(backFlashFlag){
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }         
        mRightBtn.setVisibility(View.INVISIBLE);
        //Gionee zhangke 20160921 modify for CR01763025 begin
        mToggleBtn = (Button) findViewById(R.id.toggle_button);
        mToggleBtn.setOnClickListener(FlashLight.this);
        //Gionee zhangke 20160921 modify for CR01763025 end
        mRightBtn.setOnClickListener(FlashLight.this);
        mWrongBtn.setOnClickListener(FlashLight.this);
        mRestartBtn.setOnClickListener(FlashLight.this);
        
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.test_title);
        //Gionee zhangke 20151212 modify for CR01608407 end
        getCameraManager();     

    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170724> modify for ID 173903 begin
    private void getCameraManager(){
        try {
            Class clazz = Class.forName("android.hardware.camera2.CameraManager");
            manager = (Object)this.getSystemService("camera");
            Log.e(TAG," oncreate == camera == " + manager );            
            Log.e(TAG," oncreate == clazz == " + clazz );
            setTorch = clazz.getMethod("setTorchMode",String.class, boolean.class);
            Log.e(TAG," oncreate == setTorch == " + setTorch );
            } catch (ClassNotFoundException e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
            } catch (Exception e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
            }
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170724> modify for ID 173903 end

    @Override
    public void onResume() {
        super.onResume();
        turnOnFlashLight();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(backFlashFlag){
           this.finish();
           Log.d(TAG,"onStop as_record_finish_self");
        }          
    }


    @Override
    public void onPause() {
        super.onPause();
        //Gionee zhangke 20151212 add for CR01608407 start
        turnOffFlashLight();
        //Gionee zhangke 20151212 add for CR01608407 end
    }

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


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                if(backFlashFlag){
                    TestUtils.asResult(TAG,"","1");
                }                
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(backFlashFlag){
                    TestUtils.asResult(TAG,"","0");
                }                
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
            //Gionee zhangke 20160921 modify for CR01763025 begin
            case R.id.toggle_button: {
                mRightBtn.setEnabled(true);
                mRightBtn.setVisibility(View.VISIBLE);
                if(mIsFlashOpened){                    
                    turnOffFlashLight();
                    mToggleBtn.setText(getString(R.string.toggle_on));
                }else{
                    turnOnFlashLight();
                    mToggleBtn.setText(getString(R.string.toggle_off));
                }
                break;
            }

            //Gionee zhangke 20160921 modify for CR01763025 end
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    //Gionee zhangke 20151212 add for CR01608407 start
    private static final int MESSAGE_REFRESH_BUTTON_STATUE = 0;
    private void turnOnFlashLight(){
        Log.i(TAG,"turnOnFlashLight");
        mIsFlashOpened = writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 40);
        String currFlashMode = null;
        if (!mIsFlashOpened) {
            //Gionee <GN_BSP_MMI> <lifeilong> <20170724> modify for ID 173903 begin
            try {
                Log.d(TAG,"setTorch true ");
                setTorch.invoke(manager,"0",true);
            } catch (Exception e) {
                Log.d(TAG," e = " + e.getMessage());
                e.printStackTrace();
            } finally {
                mIsFlashOpened = true;
            }
            //Gionee <GN_BSP_MMI> <lifeilong> <20170724> modify for ID 173903 end
        }
    }

    private void turnOffFlashLight(){ 
        Log.e(TAG,"turnOffFlashLight");
        boolean mIsFlashClosed = writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 0);
        try {
            //Gionee <GN_BSP_MMI> <lifeilong> <20170724> modify for ID 173903 begin
            if(!mIsFlashClosed){
                Log.d(TAG,"setTorch false ");
                setTorch.invoke(manager,"0",false);
            }
            mIsFlashOpened = false;
        } catch (Exception e) {
            //Gionee <GN_BSP_MMI> <lifeilong> <20170726> modify for ID 174858 begin
            Log.d(TAG," e = " + e.getMessage());
            e.printStackTrace();
        } finally {
            mIsFlashOpened = false;
        }
            //Gionee <GN_BSP_MMI> <lifeilong> <20170726> modify for ID 174858 end
        //Gionee <GN_BSP_MMI> <lifeilong> <20170724> modify for ID 173903 end
    }
    //Gionee zhangke 20151212 add for CR01608407 end
}
