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
import android.os.Handler;
import android.os.Message;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class BaseActivity extends Activity {
    protected static final String PERSIST_RADIO_DISPATCH_ALL_KEY = "persist.radio.dispatchAllKey";
    protected static final String PERSIST_RUNTIME_AUTOMMI = "persist.runtime.autommi";
	private String EXTRA_VOL = "volume";
    static String TAG = "BaseActivity";
	protected AudioManager am;
	protected static int[] voiceStreamTypes = {AudioManager.STREAM_MUSIC, AudioManager.STREAM_VOICE_CALL}; 
//	protected final String green = "/sys/class/leds/green/brightness";
//	protected final String red = "/sys/class/leds/red/brightness";
//	protected final String blue = "/sys/class/leds/blue/brightness";
	protected final int  GREEN = Color.GREEN;
	protected final int  RED = Color.RED;
	protected final int  BLUE = Color.BLUE;
	private final int NOTIFICATION_ID = 99;
	Color color ;
	private NotificationManager nm;
	private Notification notification;
    private static final String NODE_TYPE_LED_RED_BRIGHTNESS = "NODE_TYPE_LED_RED_BRIGHTNESS";
    private static final String NODE_TYPE_LED_BLUE_BRIGHTNESS = "NODE_TYPE_LED_BLUE_BRIGHTNESS";
    private static final String NODE_TYPE_LED_GREEN_BRIGHTNESS = "NODE_TYPE_LED_GREEN_BRIGHTNESS";
    private int level;
	private int plugged;
    private Intent lastBatteryData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; 
        getWindow().setAttributes(lp);
        Log.e(TAG,"registerReceiver");
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 begin
        registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 end
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
        uiHandler.sendEmptyMessage(0);
        
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
		nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		//Gionee zhangke 20160128 modify for CR01632872 start
		notification = new Notification.Builder(this)
			.setSmallIcon(R.drawable.icon)
			.setContentTitle("LCD Test")
			.build();
		//Gionee zhangke 20160128 modify for CR01632872 end
        
		notification.ledOffMS = 0;
		notification.ledOnMS = 1;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        
		//closeAllLeds();
	}


	protected void closeAllLeds() {
		// TODO Auto-generated method stub
		  nm.cancel(NOTIFICATION_ID);
        TestUtils.writeNodeState(BaseActivity.this,NODE_TYPE_LED_RED_BRIGHTNESS,0);
        TestUtils.writeNodeState(BaseActivity.this,NODE_TYPE_LED_BLUE_BRIGHTNESS,0);
        TestUtils.writeNodeState(BaseActivity.this,NODE_TYPE_LED_GREEN_BRIGHTNESS,0);
		
	}
	protected void openRed() {
		TestUtils.writeNodeState(BaseActivity.this,NODE_TYPE_LED_RED_BRIGHTNESS,255);
	}
	
	protected void openBlue() {
		TestUtils.writeNodeState(BaseActivity.this,NODE_TYPE_LED_BLUE_BRIGHTNESS,255);
	}

	protected void openGreen() {
		TestUtils.writeNodeState(BaseActivity.this,NODE_TYPE_LED_GREEN_BRIGHTNESS,255);
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
    @Override
    protected void onStart(){
		super.onStart();
    }

    @Override
	protected void onStop(){
		super.onStop();
		Log.e(TAG,"onStop");
		//Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 begin
		closeAllLeds();
		showLed();
		try{
			if(mBroadcastReceiver != null){
				this.unregisterReceiver(mBroadcastReceiver);
			}
		} catch (IllegalArgumentException e){
			Log.d(TAG," e = " + e.getMessage());
			e.printStackTrace();
		}
        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170504> modify for ID 130787 end
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, " autommi onPause11");
    }
    @Override
    protected void onResume() {
        super.onResume();
	}
	protected void showLed(){
        Log.e(TAG,"plugged = " + plugged +" , level = " + level);
		if(plugged != 0){            
		    if(level == 100){
			    openGreen();
		    }else if (level < 15){
			    openRed();
		    }else {
			    openBlue();
		    }
        } 
    }
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    exBatInfo(lastBatteryData);
                    this.sendEmptyMessageDelayed(0, 500);
                    break;
            }
        }
    };

    private void exBatInfo(Intent intent) {
        // TODO Auto-generated method stub
        if (null == intent)
            return;
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            level = intent.getIntExtra("level", 0);
            plugged = intent.getIntExtra("plugged", 0);
        }
        //Log.e(TAG,"  level = " + level);
    }

    protected BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            lastBatteryData = intent;
            exBatInfo(intent);
        }

    };
}
