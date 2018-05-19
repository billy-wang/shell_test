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

public class BaseActivity extends Activity {
	protected static final String PERSIST_RADIO_DISPATCH_ALL_KEY = "persist.radio.dispatchAllKey";
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
        
		closeAllLeds();
	}
	
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
}
