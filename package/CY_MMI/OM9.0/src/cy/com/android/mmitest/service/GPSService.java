package cy.com.android.mmitest.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;

import cy.com.android.mmitest.R;
import cy.com.android.mmitest.utils.DswLog;
import cy.com.android.mmitest.utils.Singleton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by qiang on 6/8/18.
 */
public class GPSService extends Service {

    private static final String TAG = "GPSService";
    private static final int MESSAGE_SHOW_TIME = 1;
    private int gps_status = -1;

    private Context mContext;

    private final String TAG_GIM = "MMITEST_GIM_GPS";
    private String strGPSInfo;
    private LocationManager locationManager;
    private GpsStatus gpsStatus = null;
    private Intent sIntent = new Intent();

    private int mTimeCount = 0;
    private boolean mLocaleSuccess = false;
    private boolean isGPSStart = false;
    private boolean isLocationChange = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //handler intent
        if (intent != null) {
            gps_status = intent.getIntExtra("gps_staus", -1);
            DswLog.i(TAG,"onStartCommand gps_status="+gps_status);

            mContext = GPSService.this;
            if (gps_status == 1) {
                if (Singleton.getInstance().isGServiceBusy) {
                    //do nothing
                    DswLog.i(TAG, "gps_status do nothing");
                }else {
                    Singleton.getInstance().isGServiceBusy = true;
                    startGPSTest();
                }

            }else if (gps_status == 2) {
                Singleton.getInstance().isGServiceBusy = false;
                stopSelf();
            }
        }
        //start GPS
        return START_STICKY;
    }

    private synchronized void stopLocation() {
        DswLog.i(TAG,"stopLocation");
        if (!isGPSStart)
            return;

        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
            locationManager.removeGpsStatusListener(statusListener);
            locationManager.removeUpdates(mLocationListener);
            DswLog.i(TAG,"remove listenner");
        } catch (Exception e) {
            DswLog.i(TAG, "Exception e=" + e.getMessage());
        }
        isGPSStart = false;
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DswLog.i(TAG, "Service onDestroy");
        stopLocation();

    }

    private void startGPSTest() {
        DswLog.i(TAG, "Service startGPSTest");
        initGPS();

        getLocation();

        mHandler.sendEmptyMessage(MESSAGE_SHOW_TIME);
    }

    private void getLocation() {
        DswLog.i(TAG,"getLocation");

        SystemClock.sleep(100);
        if (!Settings.Secure.isLocationProviderEnabled(mContext.getContentResolver(),
                LocationManager.GPS_PROVIDER)) {
            Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(),
                    LocationManager.GPS_PROVIDER, true);
        } else {
        }

        SystemClock.sleep(100);
        gpsStatus = locationManager.getGpsStatus(null);//add by cz 20140524

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                mLocationListener);
        locationManager.addGpsStatusListener(statusListener);
        isGPSStart = true;
    }

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
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            DswLog.e(TAG, "onLocationChanged location=" + location);
            if (null != location) {
                String latitude = Integer.toString((int) location.getLatitude());
                String info = String.format(Locale.ENGLISH, getString(R.string.info_text),
                        location.getLongitude(), location.getLatitude(),
                        location.getAltitude());

                if (latitude.equals("")) {
                } else if (mLocaleSuccess) {
                    isLocationChange = true;
                    sIntent.putExtra("isPass", isLocationChange);

                    sIntent.putExtra("sLongitude", location.getLongitude());
                    sIntent.putExtra("sLatitude", location.getLatitude());
                    sIntent.putExtra("sAltitude", location.getAltitude());

                    sIntent.putExtra("gps_vlatitude",info);
                    DswLog.i(TAG, "isPass="+true + " vlatitude="+info);
                }
            }
        }
    };
    private void writeGPSInfo2Txt(){

        Iterator<GpsSatellite> itrator = gpsStatus.getSatellites().iterator();
        ArrayList<GpsSatellite> satelliteList = new ArrayList<GpsSatellite>();
        int itmp = 0;
        StringBuilder sb = new StringBuilder();
        StringBuilder sub = new StringBuilder();
        sb.append(getString(R.string.satellite_count));

        int maxSatellites = gpsStatus.getMaxSatellites();
        while (itrator.hasNext() && itmp <= maxSatellites) {

            GpsSatellite satellite = itrator.next();
            satelliteList.add(satellite);
            sb.append(getString(R.string.satellite_snc, itmp++))
                    .append(satellite.getSnr()).append("\n");
            sub.append(satellite.getSnr()).append(",").append(itmp).append(";");
            if (itmp > 2)
                mLocaleSuccess = true;
        }

        sIntent.putExtra("gps_satellites", sb.toString());
        sIntent.putExtra("gps_satel_msg", sub.toString());
        sIntent.putExtra("gps_timelong", mTimeCount);
        sIntent.putExtra("gps_sate_number", itmp);

    }

    // GPS
    private GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            if(locationManager!=null){
                locationManager.getGpsStatus(gpsStatus);
            }
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                case GpsStatus.GPS_EVENT_STARTED:
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    if (isGPSStart)
                        writeGPSInfo2Txt();
                    break;

                case GpsStatus.GPS_EVENT_STOPPED:
                    // Event sent when the GPS system has stopped.//
                    // setText_Info("GPS_EVENT_STOPPED");
                    break;
                default:
                    break;
            }
        }
    };


    private void initGPS() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_TIME:
                  //  sIntent.putExtra("gps_timelong", mTimeCount);
                    //every 1500 stautus update
                  //  DswLog.i(TAG, "OnGPSListenner="+Singleton.getInstance().getOnGPSListenner());
                    if (Singleton.getInstance().getOnGPSListenner() != null) {
                        Singleton.getInstance().getOnGPSListenner().onTestGPS(sIntent);
                    }

                    if (!isLocationChange)
                        mTimeCount = mTimeCount + 1;

                    mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_TIME, 1000);
                 //   DswLog.i(TAG, "handler mTimeCount="+mTimeCount);
                    break;
            }
        }
    };

}
