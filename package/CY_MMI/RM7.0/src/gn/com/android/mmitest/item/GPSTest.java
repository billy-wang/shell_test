package gn.com.android.mmitest.item;

import gn.com.android.mmitest.TestUtils;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import gn.com.android.mmitest.R;
//Gionee zhangke 20151225 modify for CR01608849 start
import android.os.Handler;
import android.os.Message;
//Gionee zhangke 20151225 modify for CR01608849 end

public class GPSTest extends Activity implements View.OnClickListener {
	private static final String TAG = "GPSTest";
	private static final int TIME_LENGTH = 1000;

	private TextView mContentTv, mLocationView;
	private Button mRightBtn, mWrongBtn, mRestartBtn;
	private boolean mLocaleSuccess;

        LocationManager mLocationMgr;
        LocationListener mListener;

        //Gionee zhangke 20151225 modify for CR01608849 start
        private TextView mTimerView; 
        private static final int MESSAGE_SHOW_TIME = 0;
        //Gionee zhangke 20151225 modify for CR01608849 end

	// To update GPS update times
        Timer mTimer;
	private int mTimeCount = 0;

	private class TimerCountTask extends TimerTask {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        TextView timerView = (TextView) findViewById(R.id.timer);
                        mTimeCount = mTimeCount + 1;
                        timerView.setText(mTimeCount + "");
                        Log.e(TAG," mTimeCount  = " + mTimeCount);
                    }
                });
            }
            }

	private void startTimer() {
            if (mTimer == null) {
                mTimer = new Timer();
                mTimer.schedule(new TimerCountTask(), 0, TIME_LENGTH);
            }
	}
	private void cancelTimer() {
            if (mTimer != null) {
                mTimer.cancel();
                mTimeCount = 0;
                mTimer = null;
            }
	}

	// To receive notifications when GPS status has changed
        GpsStatus.Listener mGpsListner = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                    TextView satelliteView = (TextView) findViewById(R.id.satellites_info);
                    StringBuilder sb = new StringBuilder();
                    sb.append(getString(R.string.satellite_count));
                        switch (event) {
                            case GpsStatus.GPS_EVENT_STARTED: {
                                Log.e(TAG, "onGpsStatusChanged");
                                break;
                            }
                            case GpsStatus.GPS_EVENT_SATELLITE_STATUS: {
                                Log.i(TAG, "GpsStatus.GPS_EVENT_SATELLITE_STATUS");
                                if (null != mLocationMgr) {
                                    GpsStatus status = mLocationMgr.getGpsStatus(null);
                                    Iterator<GpsSatellite> iterator = status.getSatellites().iterator();
                                    int index = 1;
                                    // get satellite count
                                    while (iterator.hasNext()) {
                                        GpsSatellite satellite = iterator.next();
                                        sb.append(getString(R.string.satellite_snc, index++));
                                        //Gionee <GN_BSP_MMI> <lifeilong> <20170830> modify for ID 200567 begin
                                        sb.append(satellite.getSnr());//.append(satellite.getPrn()).append("\n");
                                        int prn = (int)satellite.getPrn();
                                        String gpsType = new String();
                                        if(prn >= 1 && prn <= 32){
                                            gpsType = getString(R.string.satellite_gps);
                                        } else if (prn >= 65 && prn <= 88){
                                            gpsType = getString(R.string.satellite_glonass);
                                        } else if (prn >= 193 && prn <= 197){
                                            gpsType = getString(R.string.satellite_qzss);
                                        } else if (prn >= 201 && prn <= 230){
                                            gpsType = getString(R.string.satellite_beidou);
                                        }
                                        Log.d(TAG," gpsType = " + gpsType);
                                        if(gpsType != null && gpsType.length() != 0 && !("".equals(gpsType))){
                                            sb.append(gpsType);
                                        }
                                        sb.append("\n");
                                        //Gionee <GN_BSP_MMI> <lifeilong> <20170830> modify for ID 200567 end
                                    }
                                    Log.e(TAG, sb.toString());
                                    Log.i(TAG,"index="+index);
                                    if (index > 2) {
                                        mLocaleSuccess = true;
                                    }
                                }
                                break;
                            }
                        }
                        satelliteView.setText(sb.toString());
                }
	};

        /**
        * Used for receiving notifications from the LocationManager when the
        * location has changed.
        */
            LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                mContentTv.setText(R.string.scanning_gps);
                //Gionee zhangke 20151225 modify for CR01608849 start
                //startTimer();
                Log.i(TAG, "onProviderEnabled startTimer");
                //mTimeCount = 0;
                cancelTimer();
                startTimer();
                //mUiHandler.removeMessages(MESSAGE_SHOW_TIME);
                //mUiHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_TIME, TIME_LENGTH);
                //Gionee zhangke 20151225 modify for CR01608849 end
            }

            @Override
            public void onProviderDisabled(String provider) {
                Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                LocationManager.GPS_PROVIDER, true);
            }

            @Override
            public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
                Log.e(TAG, "onLocationChanged location="+location);
                if (null != location) {
                    String info = String.format(getString(R.string.info_text),
                    location.getLongitude(), location.getLatitude(),
                    location.getAltitude());
                    mLocationView.setText(info);
                    Log.i(TAG, "mLocaleSuccess="+mLocaleSuccess+";info="+info);
                        if (true == mLocaleSuccess) {
                            mRightBtn.setEnabled(true);
                            mRightBtn.setVisibility(View.VISIBLE);
                            //Gionee zhangke 20160812 add for CR01745737 start
                            //mUiHandler.removeMessages(MESSAGE_SHOW_TIME);
                            cancelTimer();
                            //mTimeCount = 0;
                            //Gionee zhangke 20160812 add for CR01745737 end
                        }
                    }
                }
            };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            TestUtils.checkToContinue(this);
            TestUtils.setWindowFlags(this);
            setContentView(R.layout.gps_test);

            //Gionee zhangke 20151225 modify for CR01608849 start
            mTimerView = (TextView) findViewById(R.id.timer);
            //Gionee zhangke 20151225 modify for CR01608849 end
            // Location results
            mLocationView = (TextView) findViewById(R.id.gps_info);
            String info = String.format(getString(R.string.info_text), 0f, 0f, 0f);
            mLocationView.setText(info);

            //Gionee zhangke 20160428 modify for CR01687958 start
            mRightBtn = (Button) findViewById(R.id.right_btn);
            mRightBtn.setVisibility(View.INVISIBLE);
            mWrongBtn = (Button) findViewById(R.id.wrong_btn);
            mRestartBtn = (Button) findViewById(R.id.restart_btn);
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mWrongBtn.setEnabled(true);
                    mRestartBtn.setEnabled(true);

                    mRightBtn.setOnClickListener(GPSTest.this);
                    mWrongBtn.setOnClickListener(GPSTest.this);
                    mRestartBtn.setOnClickListener(GPSTest.this);
                }
            }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
            //Gionee zhangke 20160428 modify for CR01687958 end
            mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mContentTv = (TextView) findViewById(R.id.gps_state);
            Log.i(TAG, "onCreate");

	}


    //Gionee zhangke 20160428 modify for CR01679700 start
	@Override
        protected void onStart() {
            super.onStart();
        }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.i(TAG, "onResume");
        if (true == mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mContentTv.setText(R.string.scanning_gps);
            //Gionee <GN_BSP_MMI> <lifeilong> <20170522> modify for ID 146272 begin
            startTimer();
            //mUiHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_TIME, TIME_LENGTH);
            //Gionee zhangke 20151225 modify for CR01608849 end
        } else {
            mContentTv.setText(R.string.opening_gps);
        }
        
        //Gionee zhangke 20160428 modify for CR01679700 start
        mLocationMgr.addGpsStatusListener(mGpsListner);
        mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
        mLocationListener);
        //Gionee zhangke 20160428 modify for CR01679700 end

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.i(TAG, "onPause");
        //mUiHandler.removeMessages(MESSAGE_SHOW_TIME);
        cancelTimer();
        //mTimeCount = 0;
        try{
            mLocationMgr.removeUpdates(mLocationListener);
            mLocationMgr.removeGpsStatusListener(mGpsListner);
        }catch(Exception e){
            Log.i(TAG, "Exception e="+e.getMessage());
        }
    }
    
        @Override
        protected void onStop() {
            super.onStop();
        }
    //Gionee zhangke 20160428 modify for CR01679700 end

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            return true;
        }

        @Override
            public void onClick(View v) {
            // TODO Auto-generated method stub
                switch (v.getId()) {

                    case R.id.right_btn: {
                        mRightBtn.setEnabled(false);
                        mWrongBtn.setEnabled(false);
                        mRestartBtn.setEnabled(false);
                        if (TestUtils.mIsAutoMode) {
                            SharedPreferences.Editor editor = TestUtils
                            .getSNSharedPreferencesEdit(this);
                            editor.putString("54", "P");
                            editor.commit();
                        }
                        TestUtils.rightPress(TAG, this);
                        break;
                    }

                    case R.id.wrong_btn: {
                        mRightBtn.setEnabled(false);
                        mWrongBtn.setEnabled(false);
                        mRestartBtn.setEnabled(false);
                        if (TestUtils.mIsAutoMode) {
                            SharedPreferences.Editor editor = TestUtils
                            .getSNSharedPreferencesEdit(this);
                            editor.putString("54", "F");
                            editor.commit();
                        }
                        TestUtils.wrongPress(TAG, this);
                        break;
                    }

                    case R.id.restart_btn: {
                        mRightBtn.setEnabled(false);
                        mWrongBtn.setEnabled(false);
                        mRestartBtn.setEnabled(false);
                        if (TestUtils.mAppContext == null) {
                            TestUtils.setAppContext(this);
                        }
                        TestUtils.restart(this, TAG);
                        break;
                    }
                }
            }


        //Gionee zhangke 20151225 modify for CR01608849 start
        private Handler mUiHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MESSAGE_SHOW_TIME:
                    //Log.i(TAG, "mTimeCount="+mTimeCount);
                    mTimeCount = mTimeCount + 1;
                    mTimerView.setText(mTimeCount + "");
                    mUiHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_TIME, TIME_LENGTH);
                    break;
                }
            }
        };
	//Gionee zhangke 20151225 modify for CR01608849 end

}
