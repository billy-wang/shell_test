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
import android.content.Intent;

//Gionee zhangke 20151212 add for CR01608407 end

public class FrontFlash extends Activity implements OnClickListener {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "FrontFlash";
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
    
    private boolean frontFlashFlag = false;
    private Intent it;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TestUtils.checkToContinue(this);
        TestUtils.setWindowFlags(this);
        //Gionee zhangke 20151212 modify for CR01608407 start
        setContentView(R.layout.common_textview);
        it = this.getIntent();
        if(it != null){
            frontFlashFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"frontFlashFlag = " + frontFlashFlag);   

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        if(frontFlashFlag){
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }        
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
        
        @Override
        public void run() {
                // TODO Auto-generated method stub
				mIsTimeOver = true;
				if(mIsPass){
					mRightBtn.setEnabled(true);
				}

				mWrongBtn.setEnabled(true);
				mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(FrontFlash.this);
                mWrongBtn.setOnClickListener(FrontFlash.this);
                mRestartBtn.setOnClickListener(FrontFlash.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

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
         if(frontFlashFlag){
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
                if(frontFlashFlag){
                    TestUtils.asResult(TAG,"","1");
                }                
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(frontFlashFlag){
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
        mIsFlashOpened = writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 30);
        
        if (!mIsFlashOpened && null == mCamera) {
            try {
                Log.i(TAG, "open camera");
                mCamera = Camera.open(1);     
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
        
        mUiHandler.sendEmptyMessage(MESSAGE_REFRESH_BUTTON_STATUE);
    }

    private void turnOffFlashLight(){ 
        Log.e(TAG,"turnOffFlashLight");
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 0);
        
        if(mCamera!= null){
            Log.i(TAG, "close camera");
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
                if(mIsFlashOpened){
                    //Gionee zhangke 20160428 modify for CR01687958 start
                    mIsPass = true;
                    if(mIsTimeOver){
                        mRightBtn.setEnabled(true);
                    }
                    //Gionee zhangke 20160428 modify for CR01687958 end

                }
                break;
            default:
                break;
            }
        }
    };
    //Gionee zhangke 20151212 add for CR01608407 end
}
