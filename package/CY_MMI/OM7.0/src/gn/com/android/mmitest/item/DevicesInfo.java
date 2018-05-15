package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.utils.DswLog;
import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.TestUtils;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import android.text.TextUtils;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.os.SystemProperties;
import java.io.IOException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;



public class DevicesInfo extends BaseActivity implements OnClickListener {
    private static String FILE_NAME_XMISC_STATUS = "/sys/devices/virtual/misc/x_misc/x_misc/status";
    private static String FILE_NAME_LCD_NAME = "/sys/devices/platform/leds-mt65xx/leds/lcd-backlight/lcd_name";
    private static String FILE_NAME_GTL = "/sys/devices/platform/gn_device_check/name";

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private TextView mContentTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.chenyee_gtl_type);
        setContentView(R.layout.activity_devicesinfo);
        DswLog.d(TAG, "\n\n\n****************onCreate DevicesInfo @" + Integer.toHexString(hashCode()));

        initView();
        showGtlType();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    private void initView() {
        mContentTv = (TextView) findViewById(R.id.test_content);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);

        mRightBtn.setOnClickListener(this);
        mRightBtn.setText("OK");
        mWrongBtn.setVisibility(View.GONE);
        mRestartBtn.setVisibility(View.GONE);
    }

    private void showGtlType() {

        String gtlType = null;
        String lcdType = null, GType = null, TPType = null, cameraType = null, LPType = null;
        String main_cam = null, main2_cam = null, sub_cam = null, sub2_cam = null;
        String fingerType = null;
        String memType = null;
        String batteryType = null;
        boolean isFileExists = false;
        File gtlFilePath = null;
        BufferedReader br = null;
        gtlFilePath = new File(FILE_NAME_GTL);
        isFileExists = gtlFilePath.exists();

        try {
            if (isFileExists) {
                br = new BufferedReader(new FileReader(FILE_NAME_GTL));
                String data = null;
                while ((data = br.readLine()) != null) {
                    // lcdType = data;
                    String[] str = data.split(":");

                    if (str.length == 1) continue;

                    if (data.contains("LCD")) {
                        lcdType = str[str.length - 1];
                    } else if (data.contains("G-sensor")) {
                        GType = str[str.length - 1];
                    } else if (data.contains("TP")) {
                        TPType = str[str.length - 1];
                    } else if (data.contains("Camera")) {
                        cameraType = str[str.length - 1];
                    } else if (data.contains("L/P-sensor")) {
                        LPType = str[str.length - 1];
                    } else if (data.contains("Finger")) {
                        fingerType = str[str.length - 1];
                    } else if (data.contains("MEMORY")) {
                        memType = str[str.length - 1];
                    } else if (data.contains("BATTERY")) {
                        batteryType = str[str.length - 1];
                    } else if (data.contains("Main_CAM") || data.contains("Back camera")) {
                        main_cam = str[str.length - 1];
                    } else if (data.contains("Main2_CAM")) {
                        main2_cam = str[str.length - 1];
                    } else if (data.contains("SUB_CAM") || data.contains("Front camera")) {
                        sub_cam = str[str.length - 1];
                    } else if (data.contains("SUB2_CAM")) {
                        sub2_cam = str[str.length - 1];
                    }
                }
            } else {
                gtlFilePath = new File(FILE_NAME_LCD_NAME);
                if (gtlFilePath.exists()) {
                    br = new BufferedReader(new FileReader(FILE_NAME_LCD_NAME));
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        lcdType = data;
                    }
                }
            }
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            DswLog.e(TAG, "DevicesInfo IO Error"+ e.getMessage());
        } catch (Exception e) {
            DswLog.e(TAG, "DevicesInfo Error"+ e.getMessage());
        }


        if (lcdType == null || lcdType.equals("")) {
            lcdType = this.getResources().getString(R.string.isnull);
        }

        if (GType == null || GType.replaceAll(" ", "").equals("")) {
            SensorManager sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
            GType = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getName();
        }

        if (GType == null || GType.equals("")) {
            GType = this.getResources().getString(R.string.isnull);
        }

        if (LPType == null || LPType.replaceAll(" ", "").equals("")) {
            SensorManager sensorManager = (SensorManager) this.getSystemService(this.SENSOR_SERVICE);
            LPType = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).getName();
        }

        if (TPType == null || TPType.equals("")) {
            TPType = this.getResources().getString(R.string.isnull);
        }

        String gnType = this.getResources().getString(R.string.chenyee_type) + ": ";
        String typeTittle = "";
        if (isFileExists) {
            gtlType = "TP" + gnType + TPType + "\n" + "LCD" + gnType + lcdType + "\n" + "G-Sensor" + gnType + GType;

            if (!TextUtils.isEmpty(LPType)) {
                gtlType += ("\nL/PSensor" + gnType + LPType);
            }
            if (!TextUtils.isEmpty(cameraType)) {
                gtlType += ("\nCamera:" + cameraType);
            }

            if (!TextUtils.isEmpty(main_cam)) {
                gtlType += ("\nMain_CAM:" + main_cam);
            }
            if (!TextUtils.isEmpty(main2_cam)) {
                gtlType += ("\nMain2_CAM:" + main2_cam);
            }
            if (!TextUtils.isEmpty(sub_cam)) {
                gtlType += ("\nSUB_CAM:" + sub_cam);
            }
            if (!TextUtils.isEmpty(sub2_cam)) {
                gtlType += ("\nSUB2_CAM:" + sub2_cam);
            }
            if (!TextUtils.isEmpty(fingerType)) {
                gtlType += ("\nFingerType:" + fingerType);
            }
            if (!TextUtils.isEmpty(batteryType)) {
                gtlType += ("\nBatteryType:" + batteryType);
            }
            if (!TextUtils.isEmpty(memType)) {
                gtlType += ("\nMemoryType:" + memType);
            }
        } else {
            gtlType = lcdType;
        }
        String audioVerNo = SystemProperties.get("persist.gn.audio.param.verno");
        if (!TextUtils.isEmpty(audioVerNo)) {
            gtlType = gtlType + "\n\n" + "AUDIO Version Number:";
            gtlType = gtlType + "\n" + audioVerNo;
        }

        mContentTv.setText(gtlType);
        mRightBtn.setEnabled(true);
    }
}
