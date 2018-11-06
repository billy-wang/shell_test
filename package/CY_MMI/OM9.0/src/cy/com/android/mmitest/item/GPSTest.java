package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.provider.Settings;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import cy.com.android.mmitest.R;
//Gionee zhangke 20151225 modify for CR01608849 start
import android.os.Handler;
import android.os.Message;
import cy.com.android.mmitest.bean.OnGPSListenner;
import cy.com.android.mmitest.utils.Singleton;
import java.util.Locale;
import android.content.Intent;
import cy.com.android.mmitest.utils.HelPerformUtil;
import cy.com.android.mmitest.bean.OnPerformListen;

public class GPSTest extends BaseActivity implements View.OnClickListener ,OnGPSListenner,OnPerformListen {
    private static final String TAG = "GPSTest";


    private TextView mContentTv, mLocationView,satelliteView;
    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private TextView mTimerView,mTimerScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开GPS @" + Integer.toHexString(hashCode()));

        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.gps_test);

        mTimerScan = (TextView) findViewById(R.id.gps_scan_time);
        mTimerView = (TextView) findViewById(R.id.timer);
        // Location results
        mLocationView = (TextView) findViewById(R.id.gps_info);
                /*Gionee huangjianqiang 20160531 modify for CR01710422 begin*/
        String info = String.format(Locale.ENGLISH, getString(R.string.info_text), 0f, 0f, 0f);
                /*Gionee huangjianqiang 20160531 modify for CR01710422 end*/
        mLocationView.setText(info);
        satelliteView = (TextView) findViewById(R.id.satellites_info);
        satelliteView.setText(getString(R.string.satellite_count));
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

        mContentTv = (TextView) findViewById(R.id.gps_state);

        Singleton.getInstance().setOnGPSListenner(this);

        restartGPSService(1);
    }

    @Override
    public void onTestGPS(Intent intent) {

        //intent msg
        DswLog.i(TAG, "onTestGPS");
        String satellites = intent.getStringExtra("gps_satellites");
        String satelMessage = intent.getStringExtra("gps_satel_msg");
        String latitude = intent.getStringExtra("gps_vlatitude");
        int numSate = intent.getIntExtra("gps_sate_number",0);
        int curSecond = intent.getIntExtra("gps_timelong",0);
        boolean isPass = intent.getBooleanExtra("isPass",false);

        if (latitude != null && latitude.length() > 1) {
            String info = String.format(Locale.ENGLISH, getString(R.string.info_text),
                    intent.getDoubleExtra("sLongitude", 0.000), intent.getDoubleExtra("sLatitude", 0.000),
                    intent.getDoubleExtra("sAltitude", 0.000));
            mLocationView.setText(info);
        }

        if (isPass) {
            restartGPSService(2);
            mRightBtn.setEnabled(true);

            if (TestUtils.mIsAutoMode) {
                HelPerformUtil.getInstance().performDelayed(GPSTest.this, 600);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.satellite_count));
        try {
            if (numSate > 0) {
                String[] subs = satelMessage.split(";");
                for (int i = 0; i < subs.length; i++) {
                    int limit = subs[i].indexOf(",");
                    sb.append(getString(R.string.satellite_snc, subs[i].substring(limit+1)))
                            .append(subs[i].subSequence(0,limit)).append("\n");
                }
            }
        }catch (Exception e) {
            DswLog.i(TAG, "error="+e.getMessage());
        }

        if (curSecond > 119) {
            mTimerScan.setText(R.string.gps_scan_timeout);
            mTimerView.setText("120");
            restartGPSService(2);
        }else {
            satelliteView.setText(sb.toString());
            mTimerView.setText(curSecond + "");
        }

    }


    private void restartGPSService(int number) {
        Intent cIntent = new Intent(this.getApplicationContext(),
                cy.com.android.mmitest.service.GPSService.class);
        cIntent.putExtra("gps_staus",number);
        startService(cIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出GPS @" + Integer.toHexString(hashCode()));
    }


    @Override
    protected void onStart() {
        super.onStart();
        DswLog.i(TAG, "GPSTest onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        DswLog.i(TAG, "GPSTest onResume setOnGPSListenner");
        Singleton.getInstance().setOnGPSListenner(this);
        mContentTv.setText(R.string.scanning_gps);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DswLog.i(TAG, "onPause unsetOnGPSListenner");
        Singleton.getInstance().setOnGPSListenner(null);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    @Override
    public void onClick(View v) {
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
                restartGPSService(2);
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
                restartGPSService(2);
                break;
            }

            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);

                if (TestUtils.mAppContext == null) {
                    TestUtils.setAppContext(this);
                }
                restartGPSService(2);
                TestUtils.restart(this, TAG);
                break;
            }
        }
    }


    @Override
    public void OnButtonPerform() {
        HelPerformUtil.getInstance().unregisterPerformListen();
        DswLog.i(TAG, "OnButtonPerform");
        mRightBtn.performClick();
    }
}
