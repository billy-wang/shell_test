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
import java.io.IOException;

//Gionee zhangke 20151212 add for CR01608407 end

public class FlashLight extends Activity implements OnClickListener {
    Button mToneBt;
    //Gionee zhangke 20160921 modify for CR01763025 begin
    private Button mRightBtn, mWrongBtn, mRestartBtn, mToggleBtn;
    //Gionee zhangke 20160921 modify for CR01763025 end
    private static final String TAG = "FlashLight";
    private boolean isFlash = false;

    //Gionee zhangke 20151212 add for CR01608407 start
    private Camera mCamera;
    Camera.Parameters mParameters = null;
    private boolean mIsFlashOpened = false;
    //Gionee zhangke 20151212 add for CR01608407 end
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 start

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        //Gionee zhangke 20160921 modify for CR01763025 begin
        setContentView(R.layout.flashlight_test);
        //Gionee zhangke 20160921 modify for CR01763025 end

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setEnabled(false);
        mRightBtn.setOnClickListener(FlashLight.this);
        mWrongBtn.setOnClickListener(FlashLight.this);
        mRestartBtn.setOnClickListener(FlashLight.this);
        //Gionee zhangke 20160428 modify for CR01687958 end
        //Gionee zhangke 20160921 modify for CR01763025 begin
        mToggleBtn = (Button) findViewById(R.id.toggle_button);
        mToggleBtn.setOnClickListener(FlashLight.this);
        //Gionee zhangke 20160921 modify for CR01763025 end

        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.test_title);
        turnOnFlashLight();
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
            //Gionee zhangke 20160921 modify for CR01763025 begin
            case R.id.toggle_button: {
                mRightBtn.setEnabled(true);
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
        
        if (!mIsFlashOpened && null == mCamera) {
            try {
                Log.i(TAG, "open camera");
                
                mCamera = Camera.open();     
                mParameters = mCamera.getParameters();
                String currFlashMode = mParameters.getFlashMode();
				mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
				//Gionee zhangke 20160109 add for CR01619039 start
				mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
				//Gionee zhangke 20160109 add for CR01619039 end
				mCamera.setParameters(mParameters);
				mCamera.startPreview();
				mIsFlashOpened = true;
                
            }catch (Exception e) {
                Log.e(TAG,"turnOnFlashLight Exception="+e.getMessage());
            }
        }
    }

    private void turnOffFlashLight(){ 
        Log.e(TAG,"turnOffFlashLight");
        mIsFlashOpened = false;
        
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 0);
        if(mCamera!= null){
            Log.i(TAG, "close camera");
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    //Gionee zhangke 20151212 add for CR01608407 end

	private void setNodeData(String node, String data) {
        Log.i(TAG, "setNodeData node="+node+";data="+data);
        File file = new File(node);
        if(file.exists()){
            try{
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(data.getBytes());
                fos.close();
                Log.i(TAG, "setNodeData mIsFlashOpened = true");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
	}
    

}
