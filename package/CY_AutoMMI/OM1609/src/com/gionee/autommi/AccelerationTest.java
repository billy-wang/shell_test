package com.gionee.autommi;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.util.Log;

public class AccelerationTest extends BaseActivity implements
		SensorEventListener {
	private SensorManager sensorManager;
	private Sensor sensor;
	private boolean flag;
	public static String TAG = "AccelerationTest";
    // Gionee zhangke 20160816 add for CR01747494 start
    private static final String GET_SENSOR_DATA = "com.gionee.autommi.get.sensor.data";
    private String mGetDataValue = "";
    // Gionee zhangke 20160816 add for CR01747494 end

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);	
		((AutoMMI)getApplication()).recordResult(TAG, "", "0");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        // Gionee zhangke 20160816 add for CR01747494 start
        IntentFilter filter = new IntentFilter();
        filter.addAction(GET_SENSOR_DATA);
        registerReceiver(mReceiver, filter);
        // Gionee zhangke 20160816 add for CR01747494 end

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sensorManager.unregisterListener(this);
        // Gionee zhangke 20160816 add for CR01747494 start
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e(TAG, "unregisterReceiver:Exception=" + e.getMessage());
        }
        // Gionee zhangke 20160816 add for CR01747494 end

		this.finish();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if(!flag) {
			flag = true;
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			String res = "" + x + "|" + y + "|" + z;
			((AutoMMI) getApplication()).recordResult(TAG, res, "1");
			Toast.makeText(this, res, Toast.LENGTH_LONG).show();
		}
        // Gionee zhangke 20160816 add for CR01747494 start
        mGetDataValue = event.values[0]+"|"+event.values[1]+"|"+event.values[2];
        // Gionee zhangke 20160816 add for CR01747494 end
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

    // Gionee zhangke 20160816 add for CR01747494 start
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mReceiver:action=" + intent.getAction()+";mGetDataValue="+mGetDataValue);
            if (GET_SENSOR_DATA.equals(intent.getAction())) {
                Toast.makeText(context, mGetDataValue, Toast.LENGTH_LONG).show();
                ((AutoMMI) getApplication()).recordResult(TAG, mGetDataValue, "1");
            }
        }
    };
    // Gionee zhangke 20160816 add for CR01747494 end


}
