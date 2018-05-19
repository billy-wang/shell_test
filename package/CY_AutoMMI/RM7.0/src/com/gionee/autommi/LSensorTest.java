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
import android.util.Log;
//Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 end


public class LSensorTest extends BaseActivity implements SensorEventListener {
    public static final String TAG = "LSensorTest";
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
    //Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tip);
        tip = (TextView) this.findViewById(R.id.tip); 
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        lSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        showNotification(BLACK, 1, 0);
        ((AutoMMI)getApplication()).recordResult(TAG, "", "0");
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


        @Override
        protected void onStart() {
            // TODO Auto-generated method stub
            super.onStart();
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170526> modify for ID 149372 begin
            sensorManager.registerListener(this, lSensor, SensorManager.SENSOR_DELAY_FASTEST);
            //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170526> modify for ID 149372 end
        }

	@Override
	protected void onStop() {
            // TODO Auto-generated method stub
            super.onStop();
            sensorManager.unregisterListener(this);
            //Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 begin
            showNotification1();
            //Gionee <GN_AutoMMI><lifeilong><20161102> add for 17105 end
            this.finish();
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
        if (15 < value) {
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
