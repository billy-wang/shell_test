package com.gionee.autommi;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 begin
import android.app.Notification;
import android.app.NotificationManager;
import com.gionee.util.DswLog;
//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 end


public class LSensorTest extends BaseActivity implements SensorEventListener {
	public static final String TAG = "LSensorTest";
    private Sensor lSensor;
    private SensorManager sensorManager;
    private boolean pass;
    private boolean lightFlag;
    private boolean darkFlag;
    private TextView tip;

//Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 begin
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tip);
		tip = (TextView) this.findViewById(R.id.tip); 
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		lSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");

		showNotification();
		openLed(BLACK);
	}

	@Override
	protected void onPause() {
		closeAllLeds();
		try{
			Thread.sleep(50);
		}catch(Exception e){
			DswLog.d(TAG,"onStart Thread.sleep() Error");
		}
		super.onPause();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		sensorManager.registerListener(this, lSensor, SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		sensorManager.unregisterListener(this);
		this.finish();
	}
//Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 end
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
   
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		tip.setText("光感值 ： " + event.values[0]);
		int value = (int) event.values[0];
		if (5 < value) {
			darkFlag = true;
	    } else  {
	    	lightFlag = true;
	    } 
		if(darkFlag && lightFlag && !pass) {
			pass = true;
			((AutoMMI)getApplication()).recordResult(TAG, "", "1");
		}
	}
}
