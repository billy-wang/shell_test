
package gn.com.android.mmitest;

import java.io.File;
import java.io.FileInputStream;
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
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import gn.com.android.mmitest.utils.DswLog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Message;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.os.AsyncResult;
import android.view.KeyEvent;
//Gionee xiaolin 20120824 add for CR00680743 start
import android.view.IWindowManager;
import android.os.ServiceManager;
import android.os.RemoteException;
//Gionee xiaolin 20120824 add for CR00680743 end
import gn.com.android.mmitest.item.NvRAMBackupAgent;

import android.os.SystemProperties;

import gn.com.android.mmitest.NvRAMAgent;

import java.util.Arrays;
//Gionee zhangke 20151013 add for CR01562456 start
import gn.com.android.mmitest.item.FeatureOption;
//Gionee zhangke 20151013 add for CR01562456 end

public class TestResult extends BaseActivity {
    private TextView mTitleTv, mContentTv, mSNTv;

    //    private SharedPreferences mResultSP = null;
    private HashMap<String, Integer> mResultSP;
    private HashMap<String, String> mSNResultSP;
    private ArrayList<String> mResultList;
    private Handler mUiHandler;
    Button mQuickBtn;
    private static final String TAG = "TestResult";
    private static final int EVENT_RESPONSE_SN_WRITE = 1, EVENT_RESPONSE_SN_READ = 2, EVENT_RESPONSE_AUTO_MODE_READ = 3;
    SharedPreferences.Editor mSNEditor;
    private int mCount;
    private boolean mSecWrite;
    private boolean mSecRead, mAuToSecRead;
    private String mSNToWrite;
    private Resources mRes;
    public static final String PRODUCT_INO_NAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";//see CFG_file_info_custom.h
    private Context mmiCtx = null;
    public static final int SN_LENGTH = 510;
  //  public static final int ALL_LENGTH = 1024;
    private byte[] mSnByteArray = new byte[SN_LENGTH];
    private static final int MMI_ALL_TAG = 54;
    private static final int MMI_GPS_TAG = 53;
    private static final int MMI_WIFI_TAG = 52;
    private static final int MMI_FM_TAG = 51;
    private static final int MMI_BT_TAG = 50;
    public static final int MMI_FACTORY_RESET_TAG = 48;
    private static final int MMI_3D_TOUCH_TAG = 32;
    private static final int MMI_LASER_TAG = 31;
    private static final int MMI_WCHAT_SOTER_TAG = 30;
    // Gionee zhangke 20160525 add for CR01706554 start
    private static final int MMI_IFAA_KEY_TAG = 29;
    // Gionee zhangke 20160525 add for CR01706554 end
    // Gionee zhangke 20160902 add for CR01756457 start
    public static final int MMI_DUAL_BACK_CAMERA_TAG = 28;
    public static final int MMI_BACK_FLASHLIGHT_CAL_TAG = 26;
    // Gionee zhangke 20160902 add for CR01756457 end
    public static final int MMI_FACTORY_TEST_TAG = 498;

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

        /*Gionee huangjianqiang 20160326 add for CR01660666 begin*/
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        String time = getString(R.string.test_time) + sdf.format(new Date());
        ((TextView) findViewById(R.id.test_time)).setText(time);

        /*Gionee huangjianqiang 20160326 add for CR01660666 end*/
        mQuickBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // Gionee xiaolin 20121012 add for CR00711174 start
                DswLog.e(TAG, "xuna add for test  = " + mSNResultSP.get("mIsAutoMode"));
                if ("true".equals(mSNResultSP.get("mIsAutoMode"))) {
                    //   if (TestUtils.mIsAutoMode) {
                    IBinder binder = null;
                    DswLog.e(TAG, "getService  NvRAMBackupAgent binder ???");
                    binder = ServiceManager.getService("NvRAMBackupAgent");
                    if (null == binder) {
                        DswLog.e(TAG, "getService  NvRAMBackupAgent binder is null");
                    }
                }
                // Gionee xiaolin 20121012 add for CR00711174 end

                DswLog.e(TAG, "TestResult activity is finish");
                SystemProperties.set("persist.radio.dispatchAllKey", "false");
                Intent it = new Intent(TestResult.this, GnMMITest.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                TestResult.this.startActivity(it);
                TestResult.this.finish();
            }
        });

        try {
            mmiCtx = createPackageContext("gn.com.android.mmitest", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onStart() {
        super.onStart();
        //Gionee xiaolin 20120824 add for CR00680743 start
        DswLog.e(TAG, "TestResult onStart TestUtils.mIsAutoMode = " + TestUtils.mIsAutoMode);
        try {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager
                    .getService(Context.WINDOW_SERVICE));
            wm.thawRotation();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        //Gionee xiaolin 20120824 add for CR00680743 end
        mCount = 0;
        // Gionee xiaolin 20120614 modify for CR00623722 start
        // Gionee xiaolin 20120802 add for CR00662674 start
        if (TestUtils.autoTestResult.isEmpty()) {
            mResultSP = getTestResult("gn_mmi_test.xml");
        } else {
            mResultSP = TestUtils.autoTestResult;
        }
        // Gionee xiaolin 20120802 add for CR00662674 end
        mSNResultSP = getSNResult("gn_mmi_sn.xml");
        // Gionee xiaolin 20120614 modify for CR00623722 end
        mResultList = new ArrayList(Arrays.asList(getResources().getStringArray(
                R.array.auto_test_items)));
        StringBuilder sb = new StringBuilder();
        int value = 1;
        sb.append(this.getResources().getString(R.string.result_from_test) + "\n");

        // Gionee xiaolin 20120614 modify for CR00623722 start
        if (null != mResultSP) {
            for (String key : mResultSP.keySet()) {
                value = mResultSP.get(key);
                if (0 == value) {
                    mCount++;
                    DswLog.e(TAG, "mCount111 = " + mCount);
                    sb.append(mCount + ":   " + key + "\n");
                }
            }
        }
        // Gionee xiaolin 20120614 modify for CR00623722 end
        mTitleTv.setText(getResources().getString(R.string.test_result_count, mCount));
        mContentTv.setText(sb.toString());
      /*  PhoneFactory.getDefaultPhone().getMobileRevisionAndImei(5,
                mUiHandler.obtainMessage(EVENT_RESPONSE_SN_READ));*/
        //Gionee zhangke 20151013 modify for CR01562456 start 
        DswLog.i("FeatureOption", "BACKUP_TO_PRODUCTINFO 3=" + FeatureOption.BACKUP_TO_PRODUCTINFO);
        if (FeatureOption.BACKUP_TO_PRODUCTINFO) {
            updateSN();
            DswLog.e(TAG, "TestResult please turn on MTK_PRODUCT_INFO_SUPPORT == false");
        } else {
            Toast.makeText(this, "please turn on MTK_PRODUCT_INFO_SUPPORT", Toast.LENGTH_LONG).show();
            DswLog.e(TAG, "TestResult please turn on MTK_PRODUCT_INFO_SUPPORT == true");
        }
        //Gionee zhangke 20151013 modify for CR01562456 end
        DswLog.e(TAG, "TestResult FeatureOption.BACKUP_TO_PRODUCTINFO");

    }

    // Gionee xiaolin 20120614 add for CR00623722 start
    private HashMap<String, String> getSNResult(String spName) {
        // TODO Auto-generated method stub
        HashMap<String, String> result = new HashMap<String, String>();
        String path = "/data/data/gn.com.android.mmitest/shared_prefs/";
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

        //Gionee zhangke 20151008 add for CR01562456 start
        sc.close();
        //Gionee zhangke 20151008 add for CR01562456 end
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
        String path = "/data/data/gn.com.android.mmitest/shared_prefs/";
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
        // TODO Auto-generated method stub
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
    // Gionee xiaolin 20120614 add for CR00623722 end

    public static byte[] getNewSN(int position, String value, byte[] sn) {
        sn[position] = value.getBytes()[0];
        return sn;
    }

    public static int writeToProductInfo(byte[] sn_buff) {
        IBinder binder = null;
        DswLog.e(TAG, "writeToProductInfo BACKUP_TO_PRODUCTINFO = " + FeatureOption.BACKUP_TO_PRODUCTINFO);
        DswLog.e(TAG, "getService	NvRAMAgent binder ???");
        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            DswLog.e(TAG, "getService	NvRAMAgent binder is null");
            return -1;
        }
        if (null != binder) {
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                byte[] write_buff = agent.readFileByName(PRODUCT_INO_NAME);
                // int flag = agent.writeFile(AP_CFG_REEB_PRODUCT_INFO_LID, write_buff);
                System.arraycopy(sn_buff, 0, write_buff, 0, SN_LENGTH);
                for (int i = 0; i < SN_LENGTH; i++) {
                    DswLog.i(TAG, "before write_buff[" + i + "]=" + write_buff[i]);
                }
                int flag = agent.writeFileByName(PRODUCT_INO_NAME, write_buff);
                if (flag > 0) {
                    DswLog.e(TAG, "writeToProductInfo NvRAMAgent write success");
                    write_buff = agent.readFileByName(PRODUCT_INO_NAME);
                    for (int i = 0; i < SN_LENGTH; i++) {
                        DswLog.i(TAG, "after write_buff[" + i + "]=" + write_buff[i]);
                    }

                } else {
                    DswLog.e(TAG, "writeToProductInfo NvRAMAgent write failed");
                    return -1;
                }

            } catch (RemoteException ex) {
                DswLog.e(TAG, ex.toString());
                return -1;
            }
        }

        return 0;//success

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    //Gionee zhangke 20151013 add for CR01562456 start 
    private void updateSN() {
        try {
            //mSnByteArray = getProductInfo();
            System.arraycopy(getProductInfo(), 0, mSnByteArray, 0, SN_LENGTH);
            String snNumber = new String(mSnByteArray);
            if (snNumber == null || snNumber.isEmpty()) {
                DswLog.v(TAG, "updateSN oldSn is null or empty!");
                Toast.makeText(this, "Error: SN is null", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            DswLog.e(TAG, "updateSN oldSn Exception:" + e.getMessage());
            Toast.makeText(this, "Error: SN is null", Toast.LENGTH_LONG).show();
            return;
        }
        for (int i = 0; i < SN_LENGTH; i++) {
            DswLog.i(TAG, "oldsn:mSnByteArray[" + i + "]=" + mSnByteArray[i]);
        }

        if ("true".equals(mSNResultSP.get("mIsAutoMode"))) {
            DswLog.i(TAG, "updateSN mcount2222 = " + mCount);

            mSnByteArray = getNewSnNumber(mSnByteArray);

            //Gionee zhangke 20151026 add for CR01574154 start
            writeToProductInfo(mSnByteArray);
            DswLog.i(TAG, "updateSN write sn = " + new String(mSnByteArray).trim());
            //Gionee zhangke 20151026 add for CR01574154 end
        }

        //Gionee zhangke 20151026 add for CR01574154 start
        setSnTextView(mSnByteArray);
        //Gionee zhangke 20151026 add for CR01574154 end

        mQuickBtn.setEnabled(true);

    }

    private boolean isFactoryResetBoot() {
        byte[] productInfoBuff = getProductInfo();
        if (productInfoBuff != null && productInfoBuff.length > 48) {
            DswLog.i(TAG, "isFactoryResetBoot:productInfoBuff[48]=" + productInfoBuff[48]);
            return 'P' == productInfoBuff[48];
        }
        return false;
    }

    private byte[] getProductInfo() {
        IBinder binder = null;
        byte[] productInfoBuff = null;

        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            DswLog.e(TAG, "getService	NvRAMAgent binder is null");
        }
        if (null != binder) {
            ;
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                DswLog.i(TAG, "getProductInfo NvRAMAgent read");
                productInfoBuff = agent.readFileByName(PRODUCT_INO_NAME);

            } catch (RemoteException ex) {
                DswLog.e(TAG, ex.toString());
            }
        }
        return productInfoBuff;
    }
    //Gionee zhangke 20151013 add for CR01562456 end 

    //Gionee zhangke 20151212 add for CR01608530 start
    private byte[] getNewSnNumber(byte[] snNumber) {
    /*GIONEE futao 20160315 modify for CR01653185  flag 45*/
        String[] labels = {"54", "53", "52", "51", "45"};
        for (String l : labels) {
            String t = mSNResultSP.get(l);
            if (t != null) {
                snNumber = getNewSN(Integer.parseInt(l) - 1, t, snNumber);
            }
        }

        //Gionee zhangke 20151201 add for start
        if (FeatureOption.GN_RW_GN_MMI_FORCE_TOUCH_SUPPORT) {
            String t = mSNResultSP.get("33");
            if (t != null) {
                snNumber = getNewSN(MMI_3D_TOUCH_TAG, t, snNumber);
            }
        }
        //Gionee zhangke 20151201 add for end

        //Gionee zhangke 20151209 add for CR01588796 start
        if (FeatureOption.GN_RW_GN_MMI_LASER_SUPPORT) {
            String t = mSNResultSP.get("32");
            if (t != null) {
                snNumber = getNewSN(MMI_LASER_TAG, t, snNumber);
            }

        }
        //Gionee zhangke 20160105 add for CR01618135 end
        //Gionee <HWFW_MMI> <zhangke><2016-09-24> add for CR01763471 begin
        if(FeatureOption.GN_RW_GN_MMI_BACK_FLASHLIGHT_CAL_SUPPORT){
            String t = mSNResultSP.get("27");
            if (t != null) {
                snNumber = getNewSN(MMI_BACK_FLASHLIGHT_CAL_TAG, t, snNumber);
            }
		
        }
        //Gionee <HWFW_MMI> <zhangke><2016-09-24> add for CR01763471 end
        int failNvCount = 0;
        /*Gionee huangjianqiang 201608025 modify for CR01752717 begin*/
        if (FeatureOption.GN_RW_GN_MMI_SENSOR_GPS_SUPPORT && snNumber[MMI_GPS_TAG] != MMI_PASS) {
            failNvCount++;
        } else if (FeatureOption.GN_RW_GN_MMI_WIFI_SUPPORT && snNumber[MMI_WIFI_TAG] != MMI_PASS) {
            failNvCount++;
            /*Gionee huangjianqiang 20160808 modify for CR01743052 begin*/
        } else if (FeatureOption.GN_RW_GN_MMI_FM_SUPPORT && snNumber[MMI_FM_TAG] != MMI_PASS) {
            /*Gionee huangjianqiang 20160808 modify for CR01743052 end*/
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
        /*Gionee huangjianqiang 201608025 modify for CR01752717 end*/
        //Gionee zhangke 20160309 delete for CR01650247 start
        /*
        else if(FeatureOption.GN_RW_GN_MMI_WCHAT_SOTER_SUPPORT && (snNumber[MMI_WCHAT_SOTER_TAG] != MMI_PASS)){
            failNvCount++;
        }*/
        //Gionee zhangke 20160309 delete for CR01650247 end
        DswLog.i(TAG,"getNewSnNumber:snNumber[MMI_GPS_TAG]="+snNumber[MMI_GPS_TAG]+";snNumber[MMI_WIFI_TAG]="
			+snNumber[MMI_WIFI_TAG]+";snNumber[MMI_BT_TAG]="+snNumber[MMI_BT_TAG]+";snNumber[MMI_3D_TOUCH_TAG]="+snNumber[MMI_3D_TOUCH_TAG]
			+";snNumber[MMI_LASER_TAG]="+snNumber[MMI_LASER_TAG]+";snNumber[MMI_WCHAT_SOTER_TAG]="+snNumber[MMI_WCHAT_SOTER_TAG]
            +";snNumber[MMI_BACK_FLASHLIGHT_CAL_TAG]="+snNumber[MMI_BACK_FLASHLIGHT_CAL_TAG]);
        DswLog.i(TAG,"getNewSnNumber:mCount="+mCount+";failNvCount="+failNvCount);
        if (mCount == 0 && failNvCount == 0) {
            snNumber = getNewSN(MMI_ALL_TAG, "P", snNumber);
        } else {
            snNumber = getNewSN(MMI_ALL_TAG, "F", snNumber);
        }

        //Gionee zhangke 20160105 add for CR01618135 end
        return snNumber;
        //Gionee zhangke 20151209 add for CR01588796 end

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

            // Gionee zhangke 20151019 modify for CR01571097 start
            if (FeatureOption.GN_RW_GN_MMI_FM_SUPPORT) {
                if (snNumber[MMI_FM_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.fm) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.fm) + ": " + mRes.getString(R.string.wrong) + "\n");
                }
            }
            // Gionee zhangke 20151019 modify for CR01571097 end
            /*Gionee huangjianqiang 20160612 modify for CR01716527 begin*/
            if(FeatureOption.GN_RW_GN_MMI_WIFI_SUPPORT || FeatureOption.GN_RW_GN_MMI_WIFI5G_SUPPORT ) {
                if (snNumber[MMI_WIFI_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.wifi) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.wifi) + ": " + mRes.getString(R.string.wrong) + "\n");
                }
            }
            /*Gionee huangjianqiang 20160612 modify for CR01716527 end*/


            // Gionee zhangke 20151019 modify for CR01571097 start
            if (FeatureOption.GN_RW_GN_MMI_SENSOR_GPS_SUPPORT) {
                if (snNumber[MMI_GPS_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.gps) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.gps) + ": " + mRes.getString(R.string.wrong) + "\n");
                }
            }
            // Gionee zhangke 20151019 modify for CR01571097 end

            // Gionee zhangke 20151201 add for start
            if (FeatureOption.GN_RW_GN_MMI_FORCE_TOUCH_SUPPORT) {
                if (snNumber[MMI_3D_TOUCH_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.force_touch) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.force_touch) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }
            // Gionee zhangke 20151201 add for end
            // Gionee zhangke 20151201 add for start
            if (FeatureOption.GN_RW_GN_MMI_LASER_SUPPORT) {
                if (snNumber[MMI_LASER_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.laser_cal) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.laser_cal) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }
            // Gionee zhangke 20151201 add for end
            // Gione zhangke 20160202 add for CR01634616 start
            if (FeatureOption.GN_RW_GN_MMI_WCHAT_SOTER_SUPPORT) {
                if (snNumber[MMI_WCHAT_SOTER_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.wchat_soter) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.wchat_soter) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }
            // Gionee zhangke 20160525 add for CR01706554 start
            if (FeatureOption.GN_RW_GN_MMI_IFAA_KEY_SUPPORT) {
                if (snNumber[MMI_IFAA_KEY_TAG] == MMI_PASS) {
                    sb.append(mRes.getString(R.string.ifaa_key) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    sb.append(mRes.getString(R.string.ifaa_key) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }
            // Gionee zhangke 20160525 add for CR01706554 end
            // Gionee zhangke 20160902 add for CR01706554 start
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

            // Gionee zhangke 20160902 add for CR01706554 end

            mSNTv.setText(sb.toString());
        }
    }

    //Gionee zhangke 20151212 add for CR01608530 end


}
