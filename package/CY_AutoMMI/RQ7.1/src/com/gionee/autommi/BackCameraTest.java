package com.gionee.autommi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
//Gionee zhangke 20160608 add for CR01715357 start
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.Intent;
import java.io.File;
//Gionee zhangke 20160608 add for CR01715357 end
import android.os.SystemClock;

public class BackCameraTest extends BaseActivity implements PictureCallback, AutoFocusCallback {
    public static final String TAG = "BackCameraTest";
    private static final int TAKE_PIC = 1;
    protected static final int AUTO_FOCUS = 0;
    private Camera bCamera;
    private CameraPreview mPreview;
    // Gionee zhangke 20160608 add for CR01715357 start
    private static final String BCAMERA_PICS_FOLDER_PATCH = "/data/misc/gionee/bcamera";
    protected String mPicPath = "/data/misc/gionee/b";
    private static final String TAKE_FLASHLIGHT_PICTURE_BROADCAST = "com.gionee.take.picture.flashlight.on";
    private static final String TAKE_NO_FLASHLIGHT_PICTURE_BROADCAST = "com.gionee.take.picture.flashlight.off";
    private int mCount = 0;
    private static final int MAX_COUNT = 10;
    // Gionee zhangke 20160608 add for CR01715357 end

    boolean AutoFocus = true;

    private MediaActionSound mCameraSound;
    ShutterCallback mShutterCallBack = new ShutterCallback() {
        public void onShutter() {
            mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
        }
    };

    // Gionee zhangke 20160608 add for CR01715357 start
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mReceiver:action=" + intent.getAction() + ";mCount="+mCount);
            if (mCount > MAX_COUNT) {
                return;
            }
            if (TAKE_FLASHLIGHT_PICTURE_BROADCAST.equals(intent.getAction())) {
                Camera.Parameters p = bCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                bCamera.setParameters(p);
                //SystemClock.sleep(100);
                //Gionee <GN_BSP_AutoMMI><lifeilong><20161205> modidy for ID 16085 begin
                mHandler.sendEmptyMessageDelayed(TAKE_PIC,200);
                //Gionee <GN_BSP_AutoMMI><lifeilong><20161205> modidy for ID 16085 end
            } else if (TAKE_NO_FLASHLIGHT_PICTURE_BROADCAST.equals(intent.getAction())) {
                Camera.Parameters p = bCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                bCamera.setParameters(p);
                //SystemClock.sleep(100);
                //Gionee <GN_BSP_AutoMMI><lifeilong><20161205> modidy for ID 16085 begin
                mHandler.sendEmptyMessageDelayed(TAKE_PIC,200);
                //Gionee <GN_BSP_AutoMMI><lifeilong><20161205> modidy for ID 16085 end
            }
        }
    };
    // Gionee zhangke 20160608 add for CR01715357 end

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case AUTO_FOCUS:
                Log.e(TAG, "---AUTO_FOCUS---");
                bCamera.autoFocus(BackCameraTest.this);
                break;
            case TAKE_PIC:
                Log.e(TAG, "---TAKE_PIC---");
                bCamera.takePicture(mShutterCallBack, null, null, BackCameraTest.this);
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.camera);
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setVisibility(View.GONE);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get an image from the camera
                bCamera.takePicture(null, null, null, BackCameraTest.this);
            }
        });
		Log.e(TAG,"onStart---open(0)");
        bCamera = Camera.open(0);
        maxCameraPicutreSize(bCamera);
        setCameraDisplayOrientation(this, 0, bCamera);
        mPreview = new CameraPreview(this, bCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        mCameraSound = new MediaActionSound();
        mCameraSound.load(MediaActionSound.SHUTTER_CLICK);
        ((AutoMMI) getApplication()).recordResult(TAG, "", "0");

        // Gionee zhangke 20160608 add for CR01715357 start
        IntentFilter filter = new IntentFilter();
        filter.addAction(TAKE_FLASHLIGHT_PICTURE_BROADCAST);
        filter.addAction(TAKE_NO_FLASHLIGHT_PICTURE_BROADCAST);
        registerReceiver(mReceiver, filter);
		Log.e(TAG,"onStart---end");
        //initFolder(BCAMERA_PICS_FOLDER_PATCH);
        // Gionee zhangke 20160608 add for CR01715357 end

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        mHandler.sendEmptyMessageDelayed(AUTO_FOCUS, 2000);
    }

    // \u6d93\u20ac\u7039\u6c33\ue6e6\u9366\u256bnPause\u95b2\u5a43\u6581
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.e(TAG, "---bCamera.release---");
        // Gionee zhangke 20160607 add for CR01713308 start
        mHandler.removeMessages(AUTO_FOCUS);
        mHandler.removeMessages(TAKE_PIC);
        // Gionee zhangke 20160607 add for CR01713308 end
        bCamera.setPreviewCallback(null);
        bCamera.release();
        // Gionee zhangke 20160607 add for CR01713308 start
        //deleteFolder(BCAMERA_PICS_FOLDER_PATCH);
        deleteFiles();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e(TAG, "unregisterReceiver:Exception=" + e.getMessage());
        }
        // Gionee zhangke 20160607 add for CR01713308 end

		this.finish();
    }

    // Gionee zhangke 20160607 add for CR01713308 start
    private void initFolder(String folderPath){
        File folder = new File(folderPath);
        if(!folder.exists()){
            folder.mkdir();
        }
		try {
			Runtime.getRuntime().exec("chmod 777 " + folderPath);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

    }
    private void initFile(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            try{
                file.createNewFile();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
		try {
			Runtime.getRuntime().exec("chmod 777 " + file);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
    }

    private void deleteFiles(){
        for(int i=0; i < MAX_COUNT; i++) {
            String picPath = mPicPath + i + ".jpg";
            File file = new File(picPath);
            if(file.exists()){
                file.delete();
            }
        }
    }
	
    private void deleteFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists()) {
            if (folder.isFile()) {
                folder.delete();
                return;
            } else if (folder.isDirectory()) {
                Log.e(TAG, "dir :" + folder.toString());
                File files[] = folder.listFiles();
                if (files == null) {
                    Log.e(TAG, folder + " listFiles()" + " return null");
                    return;
                }
                for (File file:files) {
                    file.delete();
                }
            }
        } else {
            Log.e(TAG, "delete file is not exist");
        }

    }
    // Gionee zhangke 20160607 add for CR01713308 end

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        // TODO Auto-generated method stub
        try {
            // Gionee zhangke 20160607 add for CR01713308 start
            String picPath = mPicPath + mCount + ".jpg";
            Log.e(TAG, "---onPictureTaken---,picPath="+picPath);
            initFile(picPath); 
            // Gionee zhangke 20160607 add for CR01713308 end
           
            OutputStream os = new FileOutputStream(picPath);
            os.write(data);
            os.close();
            ((AutoMMI) getApplication()).recordResult(TAG, "", "1");
            // Gionee zhangke 20160607 add for CR01713308 start
            Toast.makeText(this, picPath, Toast.LENGTH_SHORT).show();
            mCount++;
            bCamera.startPreview();
            // Gionee zhangke 20160607 add for CR01713308 end
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
        case Surface.ROTATION_0:
            degrees = 0;
            break;
        case Surface.ROTATION_90:
            degrees = 90;
            break;
        case Surface.ROTATION_180:
            degrees = 180;
            break;
        case Surface.ROTATION_270:
            degrees = 270;
            break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        // TODO Auto-generated method stub
        if (AutoFocus) {

            if (success) {
                //Gionee zhangke 20160608 delete for CR01715357 start
                //mHandler.sendEmptyMessage(TAKE_PIC);
                //Gionee zhangke 20160608 delete for CR01715357 end
                Log.e(TAG, "auto_focus success11!");
            } else {
                //Gionee zhangke 20160608 delete for CR01715357 start
                //mHandler.sendEmptyMessage(TAKE_PIC);
                //Gionee zhangke 20160608 delete for CR01715357 end
                Log.e(TAG, "fail to auto_focus11");
            }
            AutoFocus = false;
        }
    }

}
