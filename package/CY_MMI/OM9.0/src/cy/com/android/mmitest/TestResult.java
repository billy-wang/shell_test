
package cy.com.android.mmitest;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import cy.com.android.mmitest.utils.DswLog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;
import cy.com.android.mmitest.item.FeatureOption;
import cy.com.android.mmitest.utils.FlagNvramUtil;

public class TestResult extends BaseActivity {
    private TextView mTitleTv, mContentTv, mSNTv;

    private HashMap<String, Integer> mResultSP1;
    private HashMap<String, String> mSNResultSP;
    Button mQuickBtn;
    private static final String TAG = "TestResult";
    private static final int EVENT_RESPONSE_SN_WRITE = 1, EVENT_RESPONSE_SN_READ = 2, EVENT_RESPONSE_AUTO_MODE_READ = 3;
    SharedPreferences.Editor mSNEditor;
    private int mCount;
    private boolean mSecWrite;
    private boolean mSecRead, mAuToSecRead;
    private String mSNToWrite;
    private Resources mRes;
    public static String PRODUCT_INO_NAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";//see CFG_file_info_custom.h
    public static final int SN_LENGTH = 64;
  //  public static final int ALL_LENGTH = 1024;
    private byte[] mSnByteArray = new byte[SN_LENGTH];

    public static final int MMI_RPMB_TAG = 503;
    public static final int MMI_FACTORY_TEST_TAG = 498;
    private static final int MMI_ALL_TAG = 54;
    private static final int MMI_GPS_TAG = 53;
    private static final int MMI_WIFI_TAG = 52;
    private static final int MMI_FM_TAG = 51;
    private static final int MMI_BT_TAG = 50;
    public static final int MMI_FACTORY_RESET_TAG = 48;
    private static final int MMI_3D_TOUCH_TAG = 32;
    private static final int MMI_LASER_TAG = 31;
    private static final int MMI_WCHAT_SOTER_TAG = 30;
    private static final int MMI_IFAA_KEY_TAG = 29;

    public static final int MMI_DUAL_BACK_CAMERA_TAG = 28;
    public static final int MMI_BACK_FLASHLIGHT_CAL_TAG = 26;
    private static final int MMI_PASS = 0x50;
    private static final int MMI_FIAL = 0x46;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.test_result);
        setContentView(R.layout.test_result);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mQuickBtn = (Button) findViewById(R.id.quit_btn);
        mQuickBtn.setEnabled(true);
        mSNTv = (TextView) findViewById(R.id.snlog);
        mRes = this.getResources();
        mTitleTv.setText(R.string.wait);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        String time = getString(R.string.test_time) + sdf.format(new Date());
        ((TextView) findViewById(R.id.test_time)).setText(time);

        mQuickBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DswLog.e(TAG, "TestResult activity is finish");
                Intent it = new Intent(TestResult.this, CyMMITest.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                TestResult.this.startActivity(it);
                TestResult.this.finish();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onStart() {
        super.onStart();

        DswLog.d(TAG, "TestResult onStart TestUtils.mIsAutoMode = " + TestUtils.mIsAutoMode);

        mCount = 0;

        if (TestUtils.autoTestResult.isEmpty()) {
            DswLog.d(TAG, "autoTestResult is empty");
            mResultSP1 = getTestResult("gn_mmi_test1.xml");
        } else {
            DswLog.d(TAG, "autoTestResult is use data");
            mResultSP1 = TestUtils.autoTestResult;
        }

        mSNResultSP = getSNResult("gn_mmi_sn.xml");


        StringBuilder sb = new StringBuilder();

        int value = 1;

        //自动测试1
        sb.append(this.getResources().getString(R.string.result_from_test) + "\n");
        if (null != mResultSP1) {
            for (String key : mResultSP1.keySet()) {
                value = mResultSP1.get(key);
                if (0 == value) {
                    mCount++;
                    sb.append(mCount + ":   " + key + "\n");
                }
            }
        }

        mTitleTv.setText(getResources().getString(R.string.test_result_count, mCount));
        mContentTv.setText(sb.toString());
        updateSN();

    }

    private HashMap<String, String> getSNResult(String spName) {
        HashMap<String, String> result = new HashMap<String, String>();
        String path = "/data/data/cy.com.android.mmitest/shared_prefs/";
        File f;
        Scanner sc;
        //DswLog.e(TAG, "start getSNResult");
        try {
            f = new File(path + spName);
            if (f.exists()) {
                //DswLog.e(TAG, f + " exists !!!");
            }
            if (f.canRead()) {
                //DswLog.e(TAG, f + " is readable !!!");
            }
            sc = new Scanner(f);
        } catch (FileNotFoundException ex) {
            return null;
        }
        while (sc.hasNextLine()) {
            String curline = sc.nextLine();
            //DswLog.e(TAG, "curline " + curline);
            Map<String, String> ls = processLine_2(curline);
            if (null != ls) {
                result.putAll(ls);
            }
        }

        sc.close();
        return result;
    }

    private Map<String, String> processLine_2(String line) {
        int keyStart = line.indexOf('"', 0);
        if (-1 == keyStart) {
            DswLog.e(TAG, line + " format is wrong!");
            return null;
        }
        int keyStop = line.indexOf('"', keyStart + 1);
        String key = line.substring(keyStart + 1, keyStop);
        String value = null;
        if (2 == key.length()) {
            int vi = line.indexOf('>', keyStop);
            value = line.substring(vi + 1, vi + 2);
        } else {
            int valStart = line.indexOf('"', keyStop + 1);
            int valStop = line.indexOf('"', valStart + 1);
            value = line.substring(valStart + 1, valStop);
        }
        HashMap<String, String> temp = new HashMap<String, String>();
        temp.put(key, value);
        return temp;

    }

    private HashMap<String, Integer> getTestResult(String spName) {
        // TODO Auto-generated method stub
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        String path = "/data/data/cy.com.android.mmitest/shared_prefs/";
        File f;
        Scanner sc;
        //DswLog.e(TAG, "start!!!");
        try {
            f = new File(path + spName);
            if (f.exists()) {
                DswLog.e(TAG, f + " exists !!!");
            }
            if (f.canRead()) {
                DswLog.e(TAG, f + " is readable !!!");
            }
            sc = new Scanner(f);
        } catch (FileNotFoundException ex) {
            return null;
        }
        //DswLog.e(TAG, "here!!!");
        //DswLog.e(TAG, "sc.hasNextLine() " + sc.hasNextLine());
        while (sc.hasNextLine()) {
            String curline = sc.nextLine();
            //DswLog.e(TAG, "curline " + curline);
            Map<String, Integer> ls = processLine(curline);
            if (null != ls) {
                result.putAll(ls);
            }
        }
        //Gionee zhangke 20151008 add for CR01562456 start
        sc.close();
        //Gionee zhangke 20151008 add for CR01562456 end

        return result;
    }

    private Map<String, Integer> processLine(String line) {
        int keyStart = line.indexOf('"', 0);
        if (-1 == keyStart) {
            DswLog.e(TAG, line + " format is wrong!");
            return null;
        }
        int keyStop = line.indexOf('"', keyStart + 1);
        String key = line.substring(keyStart + 1, keyStop);

        int valStart = line.indexOf('"', keyStop + 1);
        int valStop = line.indexOf('"', valStart + 1);
        String value = line.substring(valStart + 1, valStop);
        DswLog.e(TAG, "keyStart : " + keyStart + " keyStop : " + keyStop);
        HashMap<String, Integer> temp = new HashMap<String, Integer>();
        DswLog.e(TAG, "keys : " + key + " value " + Integer.valueOf(value));
        temp.put(key, Integer.valueOf(value));
        return temp;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private void updateSN() {

        mSnByteArray = FlagNvramUtil.readINvramInfo();

        if ("true".equals(mSNResultSP.get("mIsAutoMode"))) {
            DswLog.d(TAG,"mIsAutoMode is true");
            mSnByteArray = getNewSnNumber(mSnByteArray);
            FlagNvramUtil.writeToNvramInfo(mSnByteArray);

            mSnByteArray = FlagNvramUtil.readINvramInfo();
            checkFlagWriteSuccess(mSnByteArray);
        }

        setSnTextView(mSnByteArray);
        mQuickBtn.setEnabled(true);
    }

    private byte[] getNewSnNumber(byte[] snNumber) {
        String[] labels = {"54", "53", "52", "51", "45"};
        for (String l : labels) {
            String t = mSNResultSP.get(l);
            if (t != null) {
                snNumber = FlagNvramUtil.getNewSN(Integer.parseInt(l) - 1, t, snNumber);
            }
        }

        if (FeatureOption.GN_RW_GN_MMI_FORCE_TOUCH_SUPPORT) {
            String t = mSNResultSP.get("33");
            if (t != null) {
                snNumber = FlagNvramUtil.getNewSN(MMI_3D_TOUCH_TAG, t, snNumber);
            }
        }

        if (FeatureOption.GN_RW_GN_MMI_LASER_SUPPORT) {
            String t = mSNResultSP.get("32");
            if (t != null) {
                snNumber = FlagNvramUtil.getNewSN(MMI_LASER_TAG, t, snNumber);
            }

        }
        if(FeatureOption.GN_RW_GN_MMI_BACK_FLASHLIGHT_CAL_SUPPORT){
            String t = mSNResultSP.get("27");
            if (t != null) {
                snNumber = FlagNvramUtil.getNewSN(MMI_BACK_FLASHLIGHT_CAL_TAG, t, snNumber);
            }
		
        }

        int failNvCount = 0;
        if (FeatureOption.GN_RW_GN_MMI_SENSOR_GPS_SUPPORT && snNumber[MMI_GPS_TAG] != MMI_PASS) {
            failNvCount++;
        } else if (FeatureOption.GN_RW_GN_MMI_WIFI_SUPPORT && snNumber[MMI_WIFI_TAG] != MMI_PASS) {
            failNvCount++;
        } else if (FeatureOption.GN_RW_GN_MMI_FM_SUPPORT && snNumber[MMI_FM_TAG] != MMI_PASS) {
            failNvCount++;
        } else if (snNumber[MMI_BT_TAG] != MMI_PASS) {
            failNvCount++;
        } else if (FeatureOption.GN_RW_GN_MMI_FORCE_TOUCH_SUPPORT && (snNumber[MMI_3D_TOUCH_TAG] != MMI_PASS)) {
            failNvCount++;
        } else if (FeatureOption.GN_RW_GN_MMI_LASER_SUPPORT && (snNumber[MMI_LASER_TAG] != MMI_PASS)) {
            failNvCount++;
        }else if(FeatureOption.GN_RW_GN_MMI_BACK_FLASHLIGHT_CAL_SUPPORT && (snNumber[MMI_BACK_FLASHLIGHT_CAL_TAG] != MMI_PASS)){
            failNvCount++;
        }

        DswLog.i(TAG,"getNewSnNumber:snNumber[MMI_GPS_TAG]="+snNumber[MMI_GPS_TAG]+";snNumber[MMI_WIFI_TAG]="
			+snNumber[MMI_WIFI_TAG]+";snNumber[MMI_BT_TAG]="+snNumber[MMI_BT_TAG]+";snNumber[MMI_3D_TOUCH_TAG]="+snNumber[MMI_3D_TOUCH_TAG]
			+";snNumber[MMI_LASER_TAG]="+snNumber[MMI_LASER_TAG]+";snNumber[MMI_WCHAT_SOTER_TAG]="+snNumber[MMI_WCHAT_SOTER_TAG]
            +";snNumber[MMI_BACK_FLASHLIGHT_CAL_TAG]="+snNumber[MMI_BACK_FLASHLIGHT_CAL_TAG]);
        DswLog.i(TAG,"getNewSnNumber:mCount="+mCount+";failNvCount="+failNvCount);

        if (mCount == 0 && failNvCount == 0) {
            snNumber = FlagNvramUtil.getNewSN(MMI_ALL_TAG, "P", snNumber);
        } else {
            snNumber = FlagNvramUtil.getNewSN(MMI_ALL_TAG, "F", snNumber);
        }

        return snNumber;
    }

    private void checkFlagWriteSuccess(byte[] mSnByteArray) {
        if (mCount == 0 && 'P' == mSnByteArray[MMI_ALL_TAG]) {
            DswLog.i(TAG, "flag P writed succeed");
        }else if (mCount != 0 && 'F' == mSnByteArray[MMI_ALL_TAG]) {
            DswLog.i(TAG, "flag F writed succeed");
        }else {
            DswLog.i(TAG, "flag writed Failed");
        }
    }

    private void setSnTextView(byte[] snNumber) {
        if (snNumber[MMI_ALL_TAG] == ' ' || snNumber[MMI_ALL_TAG] == '0' || snNumber[MMI_ALL_TAG] == 0) {
            mTitleTv.setText(R.string.no_mmitest);
        } else if (snNumber[MMI_ALL_TAG] == MMI_PASS) {
            mTitleTv.setText(R.string.mmitest_success);
        } else {
            mTitleTv.setText(R.string.mmitest_fail);
            StringBuilder sb = new StringBuilder();
            if (snNumber[MMI_BT_TAG] == MMI_PASS) {
                sb.append(mRes.getString(R.string.bluetooth) + ": " + mRes.getString(R.string.right) + "\n");
            } else {
                sb.append(mRes.getString(R.string.bluetooth) + ": " + mRes.getString(R.string.wrong) + "\n");
            }

            if (FeatureOption.GN_RW_GN_MMI_FM_SUPPORT) {
                if (snNumber[MMI_FM_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.fm) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.fm) + ": " + mRes.getString(R.string.wrong) + "\n");
                }
            }
            /*Gionee huangjianqiang 20160612 modify for CR01716527 begin*/
            if(FeatureOption.GN_RW_GN_MMI_WIFI_SUPPORT || FeatureOption.GN_RW_GN_MMI_WIFI5G_SUPPORT ) {
                if (snNumber[MMI_WIFI_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.wifi) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.wifi) + ": " + mRes.getString(R.string.wrong) + "\n");
                }
            }
            /*Gionee huangjianqiang 20160612 modify for CR01716527 end*/

            if (FeatureOption.GN_RW_GN_MMI_SENSOR_GPS_SUPPORT) {
                if (snNumber[MMI_GPS_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.gps) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.gps) + ": " + mRes.getString(R.string.wrong) + "\n");
                }
            }

            if (FeatureOption.GN_RW_GN_MMI_FORCE_TOUCH_SUPPORT) {
                if (snNumber[MMI_3D_TOUCH_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.force_touch) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.force_touch) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }

            if (FeatureOption.GN_RW_GN_MMI_LASER_SUPPORT) {
                if (snNumber[MMI_LASER_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.laser_cal) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.laser_cal) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }

            if (FeatureOption.GN_RW_GN_MMI_WCHAT_SOTER_SUPPORT) {
                if (snNumber[MMI_WCHAT_SOTER_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.wchat_soter) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.wchat_soter) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }
            if (FeatureOption.GN_RW_GN_MMI_IFAA_KEY_SUPPORT) {
                if (snNumber[MMI_IFAA_KEY_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.ifaa_key) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.ifaa_key) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }

            if (FeatureOption.GN_RW_GN_MMI_DUAL_BACK_CAMERA_SUPPORT) {
                if (snNumber[MMI_DUAL_BACK_CAMERA_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.dual_back_camera_test) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.dual_back_camera_test) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }
            if (FeatureOption.GN_RW_GN_MMI_BACK_FLASHLIGHT_CAL_SUPPORT) {
                if (snNumber[MMI_BACK_FLASHLIGHT_CAL_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.back_flashlight_cal_test) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.back_flashlight_cal_test) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }

            mSNTv.setText(sb.toString());
        }
    }

}
