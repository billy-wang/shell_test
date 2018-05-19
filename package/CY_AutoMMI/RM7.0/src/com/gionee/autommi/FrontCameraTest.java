package com.gionee.autommi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
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
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.Display;
import android.util.DisplayMetrics;


public class FrontCameraTest extends BaseActivity implements PictureCallback {
    public static final String TAG = "FrontCameraTest";
    private static final int TAKE_PIC = 0;
    private Camera fCamera;
    private CameraPreview mPreview;
    // Gionee zhangke 20160608 add for CR01715357 start
    private static final String FCAMERA_PICS_FOLDER_PATCH = "/data/misc/gionee/fcamera";
    protected String mPicPath = "/data/misc/gionee/f";
    private static final String TAKE_FLASHLIGHT_PICTURE_BROADCAST = "com.gionee.take.picture.flashlight.on";
    private static final String TAKE_NO_FLASHLIGHT_PICTURE_BROADCAST = "com.gionee.take.picture.flashlight.off";
    private int mCount = 0;
    private static final int MAX_COUNT = 10;
    // Gionee zhangke 20160608 add for CR01715357 end
    private FrameLayout preview;
    boolean AutoFocus = true;
    private int screenWidth;
    private int screenHeight;
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
                Camera.Parameters p = fCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                fCamera.setParameters(p);
                mHandler.sendEmptyMessage(TAKE_PIC);
            } else if (TAKE_NO_FLASHLIGHT_PICTURE_BROADCAST.equals(intent.getAction())) {
                Camera.Parameters p = fCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                fCamera.setParameters(p);
                mHandler.sendEmptyMessage(TAKE_PIC);
            }
        }
    };
    // Gionee zhangke 20160608 add for CR01715357 end

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TAKE_PIC:
				Log.e(TAG, "---takePicture begin ---");
				fCamera.takePicture(mShutterCallBack, null, null, FrontCameraTest.this);
				Log.e(TAG, "---takePicture end ---");
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.camera);
		Button captureButton = (Button) findViewById(R.id.button_capture);// 拍照按钮
		captureButton.setVisibility(View.GONE);
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        String size = getSurfaceViewSize(screenWidth,screenHeight);
		captureButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get an image from the camera
				fCamera.takePicture(null, null, null, FrontCameraTest.this);// 拍照,JPG图像数据
			}
		});
		//fCamera = Camera.open(1);
        fCamera = Camera.openLegacy(1, Camera.CAMERA_HAL_API_VERSION_1_0);
			
        
		maxCameraPicutreSize(fCamera);

		Parameters params = fCamera.getParameters();
		try {
			Method m = params.getClass().getMethod("setCameraMode",
					new Class[] { int.class });
			m.invoke(params, 1);
		} catch (NoSuchMethodException e) {
			Log.e(TAG, "open 3 failed error=" + e.getMessage());
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			Log.e(TAG, "open 4 failed error=" + e.getMessage());
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			Log.e(TAG, "open 4 failed error=" + e.getMessage());
			e.printStackTrace();
		}

		// params.setCameraMode(1);
		fCamera.setParameters(params);
		setCameraDisplayOrientation(this, 0, fCamera);
		mPreview = new CameraPreview(this, fCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		mCameraSound = new MediaActionSound();
		mCameraSound.load(MediaActionSound.SHUTTER_CLICK);
		((AutoMMI) getApplication()).recordResult(TAG, "", "0");
                setSurfaceViewSize(size,preview);
		Log.e(TAG, "-------onCreate---------");
        // Gionee zhangke 20160608 add for CR01715357 start
        IntentFilter filter = new IntentFilter();
        filter.addAction(TAKE_FLASHLIGHT_PICTURE_BROADCAST);
        filter.addAction(TAKE_NO_FLASHLIGHT_PICTURE_BROADCAST);
        registerReceiver(mReceiver, filter);
        //initFolder(FCAMERA_PICS_FOLDER_PATCH);
        // Gionee zhangke 20160608 add for CR01715357 end

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//mHandler.sendEmptyMessageDelayed(TAKE_PIC, 2000);
	}

	// 一定要在onPause释放
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.e(TAG, "---fCamera.release begin---");
		//Gionee zhangke 20160607 add for CR01713308 start
		mHandler.removeMessages(TAKE_PIC);
		//Gionee zhangke 20160607 add for CR01713308 end

		fCamera.setPreviewCallback(null);
		fCamera.release();
		Log.e(TAG, "---fCamera.release end---");
        // Gionee zhangke 20160607 add for CR01713308 start
        deleteFiles();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e(TAG, "unregisterReceiver:Exception=" + e.getMessage());
        }
        // Gionee zhangke 20160607 add for CR01713308 end
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170417> modify for ID 116486 begin
        GnReflectionMethods gnMethod = new GnReflectionMethods(
                        "android.hardware.Camera",
                        "setProperty", new Class[]{String.class,String.class}, new String[]{"debug.camera.open", "-1"});

        gnMethod.getInvokeResult1(this);

        this.finish();
    }


    public String getSurfaceViewSize(int width, int height) {
        if (equalRate(width, height, 1.33f)) {
            return "4:3";
        } else {
            return "16:9";
        }
    }

    public boolean equalRate(int width, int height, float rate) {
        float r = (float)width /(float) height;
        if (Math.abs(r - rate) <= 0.2) {
            return true;
        } else {
            return false;
        }
    }

    private void setSurfaceViewSize(String surfaceSize,FrameLayout preview) {
        ViewGroup.LayoutParams params = preview.getLayoutParams();
        Log.d(TAG,"surfaceSize == " + surfaceSize);
        params.height = 4 * screenWidth / 3;
        preview.setLayoutParams(params);
    }

    // Gionee zhangke 20160607 add for CR01713308 start
    private void initFolder(String folderPath){
        File folder = new File(folderPath);
        if(!folder.exists()){
            folder.mkdir();
        }
		try {
			Runtime.getRuntime().exec("chmod 666 " + folderPath);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

    }
    private void deleteFolder(String folderPath) {
        File folder = new File(folderPath);
        if(folder.exists()){
            folder.delete();
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

    // Gionee zhangke 20160607 add for CR01713308 end


	// 当执行mCamera.takePicture(null, null, jpegCallback);语句之后，
	// 应该要进入onPictureTaken函数完成图片的保存工作
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		try {
            // Gionee zhangke 20160607 add for CR01713308 start
            String picPath = mPicPath + mCount + ".jpg";
            Log.e(TAG, "---onPictureTaken---,picPath="+picPath);
            initFile(picPath);
            // Gionee zhangke 20160607 add for CR01713308 end

			Log.e(TAG, "---onPictureTaken--- data="+data.length);
			OutputStream os = new FileOutputStream(picPath);
			os.write(data);
			os.close();

            // Gionee zhangke 20160607 add for CR01713308 start
            Toast.makeText(this, picPath, Toast.LENGTH_SHORT).show();
            mCount++;
            fCamera.startPreview();
            // Gionee zhangke 20160607 add for CR01713308 end
			((AutoMMI) getApplication()).recordResult(TAG, "", "1");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setCameraDisplayOrientation(Activity activity,
			int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
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
		camera.setDisplayOrientation(result);// 照片显示的角度
	}

}
