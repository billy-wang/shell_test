package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.TestUtils;

import java.util.Iterator;
import java.util.Locale;
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
import gn.com.android.mmitest.utils.DswLog;
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

public class GPSTest extends BaseActivity implements View.OnClickListener {
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
                    // show time count
                    TextView timerView = (TextView) findViewById(R.id.timer);
                    //Gionee zhangke 20151225 modify for CR01608849 start
                    //DswLog.i(TAG, "TimerCountTask mTimeCount1="+mTimeCount);
                    mTimeCount = mTimeCount + 1;
                    timerView.setText(mTimeCount + "");
                    //DswLog.i(TAG, "TimerCountTask mTimeCount2="+mTimeCount);
                    //Gionee zhangke 20151225 modify for CR01608849 end

                }
            });
        }
    }

    /**
     * start timer
     */
    private void startTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new TimerCountTask(), 0, TIME_LENGTH);
        }
    }

    /**
     * cancel timer
     */
    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
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
                    DswLog.e(TAG, "onGpsStatusChanged");
                    break;
                }
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS: {
                    DswLog.i(TAG, "GpsStatus.GPS_EVENT_SATELLITE_STATUS");
                    if (null != mLocationMgr) {
                        GpsStatus status = mLocationMgr.getGpsStatus(null);
                        Iterator<GpsSatellite> iterator = status.getSatellites().iterator();
                        int index = 1;
                        // get satellite count
                        while (iterator.hasNext()) {
                            GpsSatellite satellite = iterator.next();
                            sb.append(getString(R.string.satellite_snc, index++))
                                    .append(satellite.getSnr()).append("\n");
                        }
                        DswLog.e(TAG, sb.toString());
                        DswLog.i(TAG, "index=" + index);
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

    private boolean stopTimeNUm=false;
    /**
     * Used for receiving notifications from the LocationManager when the
     * location has changed.
     */
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            mContentTv.setText(R.string.scanning_gps);
            //Gionee zhangke 20151225 modify for CR01608849 start
            //startTimer();
            DswLog.i(TAG, "onProviderEnabled startTimer");
            mTimeCount = 0;
            mUiHandler.removeMessages(MESSAGE_SHOW_TIME);
            mUiHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_TIME, TIME_LENGTH);
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
            DswLog.e(TAG, "onLocationChanged location=" + location);
            if (null != location) {
                /*Gionee huangjianqiang 20160531 modify for CR01710422 begin*/
                String info = String.format(Locale.ENGLISH, getString(R.string.info_text),
                        location.getLongitude(), location.getLatitude(),
                        location.getAltitude());
                /*Gionee huangjianqiang 20160531 modify for CR01710422 end*/
                DswLog.d(TAG, "onLocationChanged: " + info);
                mLocationView.setText(info);
                DswLog.i(TAG, "mLocaleSuccess=" + mLocaleSuccess + ";info=" + info);
                //Gionee tanbotao 20160912 modify for CR01760120 begin
                String latitude = Integer.toString((int) location.getLatitude());
                if (latitude.equals("")) {
                } else if (true == mLocaleSuccess) {
                    stopTimeNUm=true;
                    mRightBtn.setEnabled(true);
                }
                //Gionee tanbotao 20160912 modify for CR01760120 end
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开GPS @" + Integer.toHexString(hashCode()));

        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.gps_test);

        //Gionee zhangke 20151225 modify for CR01608849 start
        mTimerView = (TextView) findViewById(R.id.timer);
        //Gionee zhangke 20151225 modify for CR01608849 end
        // Location results
        mLocationView = (TextView) findViewById(R.id.gps_info);
                /*Gionee huangjianqiang 20160531 modify for CR01710422 begin*/
        String info = String.format(Locale.ENGLISH, getString(R.string.info_text), 0f, 0f, 0f);
                /*Gionee huangjianqiang 20160531 modify for CR01710422 end*/
        mLocationView.setText(info);

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出GPS @" + Integer.toHexString(hashCode()));
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
        DswLog.i(TAG, "onResume");
        if (true == mLocationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mContentTv.setText(R.string.scanning_gps);
            //Gionee zhangke 20151225 modify for CR01608849 start
            //startTimer();
            mUiHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_TIME, TIME_LENGTH);
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
        DswLog.i(TAG, "onPause");
        mUiHandler.removeMessages(MESSAGE_SHOW_TIME);
        mTimeCount = 0;
        stopTimeNUm=false;
        try {
            mLocationMgr.removeUpdates(mLocationListener);
            mLocationMgr.removeGpsStatusListener(mGpsListner);
        } catch (Exception e) {
            DswLog.i(TAG, "Exception e=" + e.getMessage());
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
                stopTimeNUm=false;
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
                    //DswLog.i(TAG, "mTimeCount="+mTimeCount);

                    //Gionee tanbotao 20160912 modify for CR01760120 begin
                    if (true == stopTimeNUm) {
                    } else {
                        mTimeCount = mTimeCount + 1;
                    }
                    //Gionee tanbotao 20160912 modify for CR01760120 end
                    mTimerView.setText(mTimeCount + "");
                    mUiHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_TIME, TIME_LENGTH);
                    break;
            }
        }
    };
    //Gionee zhangke 20151225 modify for CR01608849 end

}
