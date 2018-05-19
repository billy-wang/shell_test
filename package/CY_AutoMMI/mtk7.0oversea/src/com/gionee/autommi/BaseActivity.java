package com.gionee.autommi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.media.AudioManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.SystemProperties;
import android.hardware.Camera;
import android.content.Intent;
import android.util.Log;

//Gionee <Oveasea_Bug> <tanbotao> <20161026> for CR01768343 beign
import android.view.OrientationEventListener;
//Gionee <Oveasea_Bug> <taofp> <20161026> for CR01768343 end
public class BaseActivity extends Activity {
	protected static final String PERSIST_RADIO_DISPATCH_ALL_KEY = "persist.radio.dispatchAllKey";
	private String EXTRA_VOL = "volume";
    static String TAG = "BaseActivity";
	public static int wavesState;
	protected AudioManager am;
	protected static int[] voiceStreamTypes = {AudioManager.STREAM_MUSIC, AudioManager.STREAM_VOICE_CALL}; 
//	protected final String green = "/sys/class/leds/green/brightness";
//	protected final String red = "/sys/class/leds/red/brightness";
//	protected final String blue = "/sys/class/leds/blue/brightness";
	protected final int  GREEN = Color.GREEN;
	protected final int  RED = Color.RED;
	protected final int  BLUE = Color.BLUE;
    //Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 begin
	protected final int  BLACK = Color.BLACK;
    //Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 end
	private final int NOTIFICATION_ID = 99;
	Color color ;
	private NotificationManager nm;
	private Notification notification;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; 
        getWindow().setAttributes(lp);
        
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        String vv = this.getIntent().getStringExtra(EXTRA_VOL);
        if(!"true".equals(SystemProperties.get(PERSIST_RADIO_DISPATCH_ALL_KEY))) {
        	SystemProperties.set(PERSIST_RADIO_DISPATCH_ALL_KEY, "true");
        }

        //Gionee zhangke 20160226 delete for CR01632945 start
        /*
        Intent starti = new Intent();
        starti.setAction("com.mediatek.mtklogger.ADB_CMD");
        Bundle bundle = new Bundle();
        bundle.putString("cmd_name", "start");
        bundle.putInt("cmd_target", 7);
        starti.putExtras(bundle);
        sendBroadcast(starti);
        Log.e(TAG, "start mtk mmi logcat ");
        */
        //Gionee zhangke 20160226 delete for CR01632945 end

        int p;
        if(null != vv) {
        	p = Integer.parseInt(vv);
        } else {
        	p = 70;
        }
		for (int StreamType : voiceStreamTypes) {
			int maxvolume = am.getStreamMaxVolume(StreamType);
			int volume = (int) ((maxvolume * p) / 100.0);
		    am.setStreamVolume(StreamType, volume, 0);
		}
		//Gionee <Oveasea_Bug> <taofp> <20161026> for CR01768343 begin
		mOrientationListener = new MyOrientationEventListener(this);
//Gionee <Oveasea_Bug> <taofp> <20161026> for CR01768343 end
	}
    //Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 begin
	protected void showNotification() {
		nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		//Gionee zhangke 20160128 modify for CR01632872 start
		//Gionee <GN_BSP_MMI> <chengq> <20170113> modify for ID 62317 begin
		notification = new Notification.Builder(this)
			.setSmallIcon(R.drawable.icon)
			.setContentTitle("LCD Test")
			.setVibrate(new long[] { 0 })
			.build();
		//Gionee <GN_BSP_MMI> <chengq> <20170113> modify for ID 62317 end
		//Gionee zhangke 20160128 modify for CR01632872 end
        
		notification.ledOffMS = 0;
		notification.ledOnMS = 1;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
	}
	//Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 end
	protected void closeAllLeds() {
		// TODO Auto-generated method stub
		  nm.cancel(NOTIFICATION_ID);
		
	}
	protected void openLed(int color) {
		notification.ledARGB = color;
		nm.notify(NOTIFICATION_ID, notification);
	}
	
	protected void openLed(String color) {
		enableLed(color, true);
	}

	protected void closeLed(String color) {
		enableLed(color, false);
	}

	protected void enableLed(String color, boolean on) {
		try {
			OutputStream os = new FileOutputStream(color);
			String cmd = "0";
			if (on) {
				cmd = "1";
			}
			os.write(cmd.getBytes());
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	protected void maxCameraPicutreSize(Camera camera) {
	    Camera.Parameters p = camera.getParameters();
	    List<Camera.Size> ss = p.getSupportedPictureSizes();
	    Camera.Size ms = ss.get(0);
	    for ( Camera.Size s : ss ) {
	    	if(s.height * s.width > ms.height * ms.width) {
	    		ms = s;
	    	}
	    }
	    p.setPictureSize(ms.width, ms.height);
	    camera.setParameters(p);
	}
//-------------------------------------
	protected int mOrientation = 0;
	private MyOrientationEventListener mOrientationListener;
	private class MyOrientationEventListener extends OrientationEventListener {
		public MyOrientationEventListener(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {
			if (orientation == ORIENTATION_UNKNOWN) return;
			//Gionee <GN_BSP_MMI> <chengq> <20170211> modify for ID 68553 begin
			try {
				android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
				android.hardware.Camera.getCameraInfo(bf, info);
				orientation = (orientation + 45) / 90 * 90;
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					mOrientation = (info.orientation - orientation + 360) % 360;
				} else {  // back-facing camera
					mOrientation = (info.orientation + orientation) % 360;
				}
			}catch (RuntimeException e) {
				e.printStackTrace();
			}
			//Gionee <GN_BSP_MMI> <chengq> <20170211> modify for ID 68553 end
		}
	}

	private int bf=0;
	protected void setBFCamera(int c){
		bf=c;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//Gionee <Oveasea_Bug> <taofp> <20161026> for CR01768343 begin
		mOrientationListener.enable();
		//Gionee <Oveasea_Bug> <taofp> <20161026> for CR01768343 end
		closeMaxAudio();
		//Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 begin
		TestUtils.startAudioSetter();
		//Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 end
	}

	@Override
	protected void onPause() {
		revertMaxAudio();
		super.onPause();
		//Gionee <Oveasea_Bug> <taofp> <20161026> for CR01768343 begin
		mOrientationListener.disable();
		//Gionee <Oveasea_Bug> <taofp> <20161026> for CR01768343 end
		//Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 begin
		TestUtils.stopAudioSettter();
		//Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 end

	}
	//--------------------------------------------------
	//Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 begin
	public void closeMaxAudio () {
		if (WavesFXContract.GN_MAXXAUDIO_SUPPORT){
			wavesState = WavesFXContract.getWavesState(getApplicationContext());
			Log.d(TAG, "before setting: wavesState="+wavesState);
			if (wavesState != 0){
				WavesFXContract.setWavesState(getApplicationContext(),0);
			}
			Log.d(TAG, "after setting : wavesState="+WavesFXContract.getWavesState(getApplicationContext()));
		}else {
			Log.d(TAG, "devices not support maxaudio");
		}
	}

	public void revertMaxAudio() {
		if (WavesFXContract.GN_MAXXAUDIO_SUPPORT){
			WavesFXContract.setWavesState(this,wavesState);
			Log.d(TAG, "revert the maxaudio wavesState="+WavesFXContract.getWavesState(getApplicationContext()));
		}
	}

	//Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 end
}
