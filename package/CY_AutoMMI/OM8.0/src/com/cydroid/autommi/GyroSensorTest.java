package com.cydroid.autommi;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.BroadcastReceiver;
import com.cydroid.util.DswLog;

public class GyroSensorTest extends BaseActivity implements SensorEventListener{
   
	
	private TextView tip;
	private SensorManager sensorManager;
	private Sensor sensor;
	public static String TAG = "GyroTest";
    private String res;
    // Gionee zhangke 20160816 add for CR01747494 start
    private static final String GET_SENSOR_DATA = "com.cydroid.autommi.get.sensor.data";
    private String mGetDataValue = "";
	private int count = 0;
    // Gionee zhangke 20160816 add for CR01747494 end

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tip);
		tip = (TextView) this.findViewById(R.id.tip);
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (null == sensor) {
			this.finish();
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        // Gionee zhangke 20160816 add for CR01747494 start
        IntentFilter filter = new IntentFilter();
        filter.addAction(GET_SENSOR_DATA);
        registerReceiver(mReceiver, filter);
        // Gionee zhangke 20160816 add for CR01747494 end

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		sensorManager.unregisterListener(this);
        // Gionee zhangke 20160816 add for CR01747494 start
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            DswLog.e(TAG, "unregisterReceiver:Exception=" + e.getMessage());
        }
        // Gionee zhangke 20160816 add for CR01747494 end

        this.finish();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
        float x = event.values[0]; 
        float y = event.values[1];
        float z = event.values[2]; 
		String t = "X : " + x + "\n"
				    + "Y : " + y + "\n"
				    + "Z : " + z + "\n";		
		tip.setText(t);
		res = "" + x + "|" + y + "|" + z;
		DswLog.d(TAG, "count=" +count + " value="+res);

		if (count == 3) {
			Toast.makeText(this, res, Toast.LENGTH_LONG).show();
		    ((AutoMMI)getApplication()).recordResult(TAG, res, "1");
        }
        // Gionee zhangke 20160816 add for CR01747494 start
        mGetDataValue = event.values[0]+"|"+event.values[1]+"|"+event.values[2];
        // Gionee zhangke 20160816 add for CR01747494 end
		count++;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

    // Gionee zhangke 20160816 add for CR01747494 start
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DswLog.e(TAG, "mReceiver:action=" + intent.getAction()+";mGetDataValue="+mGetDataValue);
            if (GET_SENSOR_DATA.equals(intent.getAction())) {
                Toast.makeText(context, mGetDataValue, Toast.LENGTH_LONG).show();
                ((AutoMMI) getApplication()).recordResult(TAG, mGetDataValue, "1");
            }
        }
    };
    // Gionee zhangke 20160816 add for CR01747494 end

}
