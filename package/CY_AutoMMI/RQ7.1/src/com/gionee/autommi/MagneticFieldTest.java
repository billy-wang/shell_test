package com.gionee.autommi;
import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;

import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

public class MagneticFieldTest extends BaseActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor mFSensor;
    public static final String TAG = "MagneticFieldTest";
    private boolean flag = false;
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
            mFSensor = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
            ((AutoMMI)getApplication()).recordResult(TAG, "", "0");
        }

        @Override
        protected void onResume() {
            // TODO Auto-generated method stub
            super.onResume();
            sensorManager.registerListener(this, mFSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170628> modify for ID 163337 begin
        @Override
        protected void onPause() {
            // TODO Auto-generated method stub
            super.onPause();
            Log.e(TAG,"  onPause");
            //this.finish();
        }

        @Override
        protected void onStop() {
            // TODO Auto-generated method stub
            super.onStop();
            Log.e(TAG,"  onStop");
            //this.finish();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            Log.e(TAG,"  onDestroy ");
            sensorManager.unregisterListener(this);
        }    
         //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170628> modify for ID 163337 end
         
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if (!flag) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                if(!isZero(x) || !isZero(y) || !isZero(z)){
                    flag = true;
                    String res = "" + x + "|" + y + "|" + z;
                    ((AutoMMI) getApplication()).recordResult(TAG, res, "1");
                    Toast.makeText(this, res, Toast.LENGTH_LONG).show();
                    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170705> modify for ID 165822 begin
                    sensorManager.unregisterListener(this);
                    //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170705> modify for ID 165822 end
                }
            }
        }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

	}

	private boolean isZero(float f){
            return (f<0.000001&&f>-0.000001);
	}
}
