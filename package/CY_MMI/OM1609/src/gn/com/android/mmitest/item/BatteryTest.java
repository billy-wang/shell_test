
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


public class BatteryTest extends BaseActivity implements OnClickListener {
    private static final int DEF_CHARGE_CURRENT_VALVE = 200;
    private boolean isOverDefChargeCurrentValve = false;
    private Resources mRs;

    private TextView mTitleTv, mContentTv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "BatteryTest";

    // Gionee zhangxiaowei 20130615 add for CR00825877 start
    private boolean stopQuery;
    private Intent lastBatteryData;
    // Gionee zhangxiaowei 20130615 add for CR00825877 end
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 end

    //Gionee zhangxiaowei 20130615 add for CR00825877  start
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (stopQuery)
                return;

            switch (msg.what) {
                case 0:
                    exBatInfo(lastBatteryData);
                    this.sendEmptyMessageDelayed(0, 1000);
                    break;
            }
        }
    };
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            lastBatteryData = intent;
            exBatInfo(intent);
        }

    };

    // Gionee zhangxiaowei 20130615 add for CR00825877  end


    //Gionee zhangxiaowei 20130615 add for CR00825877  start
    private void exBatInfo(Intent intent) {
        // TODO Auto-generated method stub
        if (null == intent)
            return;
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

            int status = intent.getIntExtra("status", 0);
            int health = intent.getIntExtra("health", 0);
            boolean present = intent.getBooleanExtra("present", false);
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 0);
            int icon_small = intent.getIntExtra("icon-small", 0);
            int plugged = intent.getIntExtra("plugged", 0);
            int voltage = intent.getIntExtra("voltage", 0);
            int temperature = intent.getIntExtra("temperature", 0);
            String technology = intent.getStringExtra("technology");

            String statusString = "";
            switch (status) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    statusString = mRs.getString(R.string.battery_status_unknow);
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    statusString = mRs.getString(R.string.battery_status_charging);
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    statusString = mRs.getString(R.string.battery_status_discharging);
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    statusString = mRs.getString(R.string.battery_status_nocharging);
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    statusString = mRs.getString(R.string.battery_status_full);
                    break;
            }

            String healthString = "";

            switch (health) {
                case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                    healthString = mRs.getString(R.string.battery_health_unknow);
                    break;
                case BatteryManager.BATTERY_HEALTH_GOOD:
                    healthString = mRs.getString(R.string.battery_health_good);
                    break;
                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    healthString = mRs.getString(R.string.battery_health_overheart);
                    break;
                case BatteryManager.BATTERY_HEALTH_DEAD:
                    healthString = mRs.getString(R.string.battery_health_dead);
                    break;
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    healthString = mRs.getString(R.string.battery_health_over_voltage);
                    break;
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    healthString = mRs.getString(R.string.battery_health_unspecified_failure);
                    break;
            }

            //Gionee tanbotao 20160910 add for CR01760130 start
            String chargeCurrent = getChargeCurrent();
            //Gionee tanbotao 20160924 modify for CR01762822 begin
            int intChargeCurrent = Math.abs(Integer.parseInt(chargeCurrent, 10));
            //Gionee tanbotao 20160924 modify for CR01762822 end
            if (intChargeCurrent >= DEF_CHARGE_CURRENT_VALVE) {
                isOverDefChargeCurrentValve = true;
                Log.d(TAG,"DEF_CHARGE_CURRENT_VALVE >= 200mA, intChargeCurrent= "+intChargeCurrent);
            }else {
                isOverDefChargeCurrentValve = false;
                Log.d(TAG,"DEF_CHARGE_CURRENT_VALVE < 200mA intChargeCurrent= "+intChargeCurrent);
            }
            //Gionee tanbotao 20160910 add for CR01760130 end

            if (0 == plugged) {
                mRightBtn.setEnabled(false);
            } else {
                //Gionee zhangke 20160428 modify for CR01687958 start
                mIsPass = true;
                if (mIsTimeOver) {
                    if (isOverDefChargeCurrentValve) {
                        Log.d(TAG,"");
                        mRightBtn.setEnabled(true);
                    }
                }
                //Gionee zhangke 20160428 modify for CR01687958 end
            }

            String acString = mRs.getString(R.string.no_battery_plugged);
            // Gionee xiaolin 20121106 add for CR00725238 start
            // Gionee xiaolin 20121106 add for CR00725238 end
            switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    acString = mRs.getString(R.string.battery_plugged_ac);
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    acString = mRs.getString(R.string.battery_plugged_usb);
                    break;
            }
            String content = "";
            String chargeVoltage = getChargeVoltage();
//            String chargeCurrent = getChargeCurrent();
            Log.i(TAG, "chargeVoltage = " + chargeVoltage);
            if (plugged != 0 && chargeVoltage != null) {
                content = mRs.getString(R.string.battery_health) + healthString + "\n"
                        + mRs.getString(R.string.battery_plugged) + acString + "\n"
                        + mRs.getString(R.string.charge_v) + chargeVoltage + " mV\n"
                        + mRs.getString(R.string.charge_i) + intChargeCurrent + " mA\n"
                        + mRs.getString(R.string.battery_v) + voltage + " mV\n"
                        + mRs.getString(R.string.battery_capacity) + scale + "\n"
                        + mRs.getString(R.string.battery_power) + level + "\n"
                        + mRs.getString(R.string.battery_technology) + technology;

            } else {
                content = mRs.getString(R.string.battery_health) + healthString + "\n"
                        + mRs.getString(R.string.battery_plugged) + acString + "\n"
                        + mRs.getString(R.string.battery_v) + voltage + " mV\n"
                        + mRs.getString(R.string.battery_capacity) + scale + "\n"
                        + mRs.getString(R.string.battery_power) + level + "\n"
                        + mRs.getString(R.string.battery_technology) + technology;
            }
            mTitleTv.setText(mRs.getString(R.string.battery_status) + statusString);
            mContentTv.setText(content);
            // Gionee xiaolin 20121106 delete for CR00725238 start
            // mRightBtn.setEnabled(true);
            // Gionee xiaolin 20121106 delete for CR00725238 end
        }
    }

    //Gionee zhangxiaowei 20130615 add for CR00825877  end
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        mRs = getResources();
        setContentView(R.layout.common_textview);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mContentTv = (TextView) findViewById(R.id.test_content);
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
                mIsTimeOver = true;
                if (mIsPass) {
                    if (isOverDefChargeCurrentValve){
                        mRightBtn.setEnabled(true);
                    }else {
                        mRightBtn.setEnabled(false);
                    }
                }

                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(BatteryTest.this);
                mWrongBtn.setOnClickListener(BatteryTest.this);
                mRestartBtn.setOnClickListener(BatteryTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //Gionee zhangxiaowei 20130615 add for CR00825877  start
        stopQuery = false;
        uiHandler.sendEmptyMessage(0);
        //Gionee zhangxiaowei 20130615 add for CR00825877  end
    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mBroadcastReceiver);
        // Gionee zhangxiaowei 20130615 add for CR00825877 start
        stopQuery = true;
        uiHandler.removeMessages(0);
        // Gionee zhangxiaowei 20130615 add for CR00825877 end
        // Gionee xiaolin 20120803 add for CR00657408 start 
        // Gionee xiaolin 20120806 modify for CR00664416 start
        if (TestUtils.mIsAutoMode && !TestUtils.isBatteryTestRestart) {
            // Gionee xiaolin 20120806 modify for CR00664416 end
            //Gionee zhangke 20151210 delete for CR01604187 start
            //Process.killProcess(Process.myPid());
            //Gionee zhangke 20151210 delete for CR01604187 end
        }
        // Gionee xiaolin 20120803 add for CR00657408 end
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                // Gionee xiaolin 20120806 add for CR00664416 start 
                TestUtils.isBatteryTestRestart = false;
                // Gionee xiaolin 20120806 add for CR00664416 end
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                // Gionee xiaolin 20120806 add for CR00664416 start 
                TestUtils.isBatteryTestRestart = false;
                // Gionee xiaolin 20120806 add for CR00664416 end
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }

            case R.id.restart_btn: {
                // Gionee xiaolin 20120806 add for CR00664416 start 
                TestUtils.isBatteryTestRestart = true;
                // Gionee xiaolin 20120806 add for CR00664416 end
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    public String getChargeVoltage() {
        String changeVoltage = null;
        String mFileName = "/sys/class/power_supply/battery/ChargerVoltage";
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File voltageFilePath = new File(mFileName);
                if (voltageFilePath.exists()) {
                    fileInputStream = new FileInputStream(voltageFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        changeVoltage = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return changeVoltage;
    }

    public String getChargeCurrent() {
        String chargeCurrent = null;
        String mFileName = "/sys/class/power_supply/battery/BatteryAverageCurrent";
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File currentFilePath = new File(mFileName);
                if (currentFilePath.exists()) {
                    fileInputStream = new FileInputStream(currentFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        chargeCurrent = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chargeCurrent;
    }

}
