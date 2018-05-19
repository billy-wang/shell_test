package com.gionee.autommi.composition;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.widget.TextView;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.R;
import com.gionee.autommi.LSensorTest;
//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 begin
import android.app.Notification;
import android.app.NotificationManager;
import android.util.Log;
import android.app.Activity;
//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 end

public class LightReceiver extends BaseActivity implements SensorEventListener{
    private int duration = 2; // seconds
    private final int sampleRate = 16000;//8000
    private int numSamples ;
    private double[] sample ;
    private final double freqOfTone = 1000; // hz
    private byte[] generatedSnd ;
	private static final String DURA = "dura";
    Thread tester = new Thread() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			genTone();
			playSound();
		}	
    };
	private AudioTrack track;
	
    private Sensor lSensor;
    private SensorManager sensorManager;
    private boolean pass;
    private boolean lightFlag;
    private boolean darkFlag;
    private TextView tip;
	//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 begin
    private static final int BLACK = 0xFF000000; //black
    NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;
	private static final String TAG = "LightReceiver";
	//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 end

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tip);
		tip = (TextView) this.findViewById(R.id.tip); 
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		lSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		((AutoMMI)getApplication()).recordResult(LSensorTest.TAG, "", "0");
		//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 begin
        mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
		showNotification(BLACK, 1, 0);
		//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 end		
		Intent it = this.getIntent();
		duration = Integer.parseInt(it.getStringExtra(DURA)) * 2;
		compose();
	}
	//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 begin
	private void showNotification(int color, int on, int off) {
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
			Notification notice = new Notification.Builder(this)
					.setSmallIcon(R.drawable.icon)
					.setContentTitle("LCD Test")
					.build();
			notice.ledARGB = color;
			notice.ledOnMS = on;
			notice.ledOffMS = off;
			notice.flags |= Notification.FLAG_SHOW_LIGHTS;
			Log.d(TAG, "zhangxiaowei mmicolor" + Integer.toHexString(notice.ledARGB) + "on--" + notice.ledOnMS + "off--" + notice.ledOffMS);
			try {
				mNotificationManager.notify(NOTIFICATION_ID, notice);
			} catch (Exception e) {
				Log.e(TAG, "mNotificationManager.notify error=" + e.getMessage());
			}
		}
	
		private void showNotification1() {
			mNotificationManager.cancel(NOTIFICATION_ID);
		}
		//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 end


	
    private void compose() {
		// TODO Auto-generated method stub
    	numSamples = duration * sampleRate;
    	sample = new double[numSamples];
    	generatedSnd = new byte[2 * numSamples];		
	}


	void genTone() {
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    void playSound() {
    	track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, sampleRate,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        track.write(generatedSnd, 0, generatedSnd.length);
        track.play();
    }


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		track.stop();
		track.release();
		sensorManager.unregisterListener(this);
		//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 begin
		showNotification1();
		//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 end

		this.finish();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		tester.start();
		sensorManager.registerListener(this, lSensor, SensorManager.SENSOR_DELAY_UI);
	}
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
   
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		tip.setText("光感值 ： " + event.values[0]);
		int value = (int) event.values[0];
        //Gionee <GN_BSP_MMI> <lifeilong> <20170430> modify for ID 128115 begin
		if (13 < value) {
        //Gionee <GN_BSP_MMI> <lifeilong> <20170430> modify for ID 128115 begin
			darkFlag = true;
	    } else  {
	    	lightFlag = true;
	    } 
		if(darkFlag && lightFlag && !pass) {
			pass = true;
			((AutoMMI)getApplication()).recordResult(LSensorTest.TAG, "", "1");
		}
	}
}
