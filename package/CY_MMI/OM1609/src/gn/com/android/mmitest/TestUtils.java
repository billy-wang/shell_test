
package gn.com.android.mmitest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Xml;

import com.android.internal.util.XmlUtils;
//Gionee zhangke 20151014 add for CR01567923 start
import gn.com.android.mmitest.item.FeatureOption;
//Gionee zhangke 20151014 add for CR01567923 end
//Gionee zhangke 20151215 add for CR01609753 start
import android.view.WindowManager;
import android.view.View;
import android.view.Window;
//Gionee zhangke 20151215 add for CR01609753 end
//Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 111730 begin
import java.lang.reflect.Field;
import java.lang.reflect.Method;
//Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 111730 end
import gn.com.android.mmitest.utils.ProinfoUtil;
import java.io.BufferedReader;

public class TestUtils {
    public static WakeLock mWakeLock;

    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences mSNSharedPreferences;

    private static ArrayList<String> mItems;

    private static ArrayList<String> mItemKeys;

    private static ArrayList<String> mAutoItemKeys;

    private static ArrayList<String> mAutoItems;

    private static ArrayList<String> mKeyItems;
    private static Map<Integer, Character> mReadyToWrite = new HashMap<Integer, Character>();
    private static ArrayList<String> mKeyItemKeys;
    public static boolean mIsAutoMode;

    /* Gionee huangjianqiang 20160125 add begin */
    public static boolean mIsForSale;
    //Gionee <Oversea_Bug> <tanbotao> <20161205> for CR01772026 beign
    public static final boolean mIsGnIndia = SystemProperties.get("ro.gn.oversea.custom").equals("INDIA_GIONEE")||SystemProperties.get("persist.sys.area.setting").equals("India");
    //Gionee <Oversea_Bug> <tanbotao> <20161205> for CR01772026 end
    /* Gionee huangjianqiang 20160125 add end */
    /*Gionee huangjianqiang 20160616 add for CR01715226 begin*/
    public static final boolean mIsSKFlag = SystemProperties.get("ro.gn.oversea.custom").equals("ARGENTINA_SOLNIK");
    /*Gionee huangjianqiang 20160616 add for CR01715226 end*/
    /*Gionee huangjianqiang 20160729 add for CR01739425 begin*/
    public static final boolean IS_BLU = "yes".equals(SystemProperties.get("gn.mmi.blu.fm", "no"));
    /*Gionee huangjianqiang 20160729 add for CR01739425 end*/


    public static Context mAppContext;
    private static final String AMIGOSETTING_DB_CONFIG_FILE = "/system/etc/gnmmiConfig.xml";
    private static String value = null;

    public static int WRITE_TO_SN_COUNT = 4;
    private static SharedPreferences.Editor mEditor;
    public static int VOL_MINUS = 0;
    private static AudioManager mAM;
    static String TAG1 = "GnMMITest--TestUtils";
    //Gionee zhangke 20160428 modify for CR01687958 start
    public static final int BUTTON_ENABLED_DELAY_TIME = 1500;
    //Gionee zhangke 20160428 modify for CR01687958 end

    // Gionee zhangxiaowei 20130517 add for CR00812238 start
    public static int VOL_MINUS_INCALL = 3;
    // Gionee zhangxiaowei 20130517 add for CR00812238 start

    // Gionee xiaolin 20120806 add for CR00664416 start 
    public static boolean isBatteryTestRestart = true;
    // Gionee xiaolin 20120806 add for CR00664416 end 

    // Gionee xiaolin 20120802 add for CR00662674 start 
    public static HashMap<String, Integer> autoTestResult = new HashMap<String, Integer>();
    // Gionee xiaolin 20120802 add for CR00662674 end

    // Gionee xiaolin 20120921 add for CR00693542 start
    static boolean sContinue = false;

    public static void checkToContinue(Activity act) {
        if (!sContinue) {
            act.finish();
        }
    }
    // Gionee xiaolin 20120921 add for CR00693542 end

    public static SharedPreferences.Editor mSNEditor;

    public static void setAppContext(Activity activity) {
        mAppContext = activity.getApplicationContext();
    }

    public static void acquireWakeLock(Activity activity) {
        if (mWakeLock == null || false == mWakeLock.isHeld()) {
            PowerManager powerManager = (PowerManager) (activity.getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE));
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Single Test");
        }
        if (false == mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    public static void openOrcloseHifi(Activity activity, boolean hifi) {
        mAM = (AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        if (hifi) {
            mAM.setParameters("HIFI_SWITCH=1");
        } else {
            mAM.setParameters("HIFI_SWITCH=0");
        }
    }

    public static int getHifiState(Activity activity) {
        mAM = (AudioManager) activity.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        String hifiString = mAM.getParameters("HIFI_SWITCH");
        int hifiValue = 0;
        if (hifiString == null || hifiString.length() == 0) {
            hifiValue = 0;
        } else {
            String[] contents = hifiString.split("=");
            hifiValue = Integer.valueOf(contents[1]);

        }
        return hifiValue;
    }

    public static void releaseWakeLock() {
        if (null != mWakeLock && true == mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        if (null == mSharedPreferences) {
		//Gionee <Oversea_Bug> <tanbotao> <20161209> for 43139 begin
            mSharedPreferences = context.getSharedPreferences("gn_mmi_test",
                    Context.MODE_PRIVATE);
		//Gionee <Oversea_Bug> <tanbotao> <20161209> for 32854 end
        }
        return mSharedPreferences;
    }

    public static SharedPreferences getSNSharedPreferences(Context context) {
        if (null == mSNSharedPreferences) {
		//Gionee <Oversea_Bug> <tanbotao> <20161209> for 43139 begin
            mSNSharedPreferences = context.getSharedPreferences("gn_mmi_sn",
                    Context.MODE_PRIVATE);
		//Gionee <Oversea_Bug> <tanbotao> <20161209> for 32854 end
        }
        return mSNSharedPreferences;
    }

    public static SharedPreferences.Editor getSharedPreferencesEdit(Context context) {
        if (null == mEditor) {
            mSharedPreferences = getSharedPreferences(context);
            mEditor = mSharedPreferences.edit();
        }
        return mEditor;
    }

    public static SharedPreferences.Editor getSNSharedPreferencesEdit(Context context) {
        if (null == mSNEditor) {
            mSNSharedPreferences = getSNSharedPreferences(context);
            mSNEditor = mSNSharedPreferences.edit();
        }
        return mSNEditor;
    }

    public static ArrayList<String> getItemKeys(Context context) {
        if (null == mItemKeys) {
            mItemKeys = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.single_test_keys)));
        }
        return mItemKeys;
    }

    public static ArrayList<String> getItems(Context context) {
        if (null == mItems) {
            mItems = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.single_test_items)));
        }
        return mItems;
    }

    public static ArrayList<String> getAutoItemKeys(Context context) {
        if (null == mAutoItemKeys) {
            mAutoItemKeys = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_keys)));
        }
        return mAutoItemKeys;
    }

    public static ArrayList<String> getAutoItems(Context context) {
        if (null == mAutoItems) {
            mAutoItems = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_items)));
        }
        return mAutoItems;
    }

    public static ArrayList<String> getKeyItems(Context context) {
        // Gionee xiaolin 20120511 modify for CR00596984 start
        if (null == mKeyItems) {
            configKeyTestArrays(context);
        }
        // Gionee xiaolin 20120511 modify for CR00596984 end
        return mKeyItems;
    }

    public static ArrayList<String> getKeyItemKeys(Context context) {
        // Gionee xiaolin 20120511 modify for CR00596984 start
        if (null == mKeyItemKeys) {
            configKeyTestArrays(context);
        }
        // Gionee xiaolin 20120511 modify for CR00596984 end
        return mKeyItemKeys;
    }

    // Gionee xiaolin 20121023 modify for CR00717365 start
    public static void rightPress(String TAG, Activity activity) {
        activity.finish();
        int index = mAutoItemKeys.indexOf(TAG);
        Log.e(TAG1, "rightPress mIsAutoMode_2 =   " + mIsAutoMode_2);
        if (mIsAutoMode_2) {
            processButtonPress_2(TAG, activity, true);
        } else {
            processButtonPress(TAG, activity, true);
        }
    }

    public static void wrongPress(String TAG, Activity activity) {
        activity.finish();
        Log.e(TAG1, "wrongPress mIsAutoMode_2 =   " + mIsAutoMode_2);
        if (mIsAutoMode_2) {
            processButtonPress_2(TAG, activity, false);
        } else {
            processButtonPress(TAG, activity, false);
        }
    }

    private static void processButtonPress(String TAG, Activity activity, boolean success) {
        Log.e(TAG1, "processButtonPress mIsAutoMode =   " + mIsAutoMode);
        if (true == mIsAutoMode) {
            mAutoItemKeys = getAutoItemKeys(activity);
            int index = mAutoItemKeys.indexOf(TAG);
            Log.e(TAG, "processButtonPress  " + TAG);
            int result = success ? 1 : 0;
            Log.e(TAG, "processButtonPress result is  " + result);
            if (null == mEditor) {
                getSharedPreferencesEdit(activity);
            }
            if (index == 0) {
                mEditor.clear();
            }
            mEditor.putInt(TestUtils.getAutoItems(activity).get(index), result);
            mEditor.commit();
            // Gionee xiaolin 20120802 add for CR00662674 start
            autoTestResult.put(TestUtils.getAutoItems(activity).get(index), result);
            SharedPreferences sp = getSharedPreferences(activity);
            int i = sp.getInt(TestUtils.getAutoItems(activity).get(index), result);
            if (i != result) {
                Log.e(TAG, " processButtonPress : write result failed one time! try again.");
                mEditor.putInt(TestUtils.getAutoItems(activity).get(index), result);
                mEditor.commit();
            }
            // Gionee xiaolin 20120802 add for CR00662674 end

            Log.e(TAG1, TestUtils.getAutoItems(activity).get(index) + ":" + result);
            if (index < mAutoItemKeys.size() - 1) {
                try {
                    Intent it = new Intent().setClass(
                            activity,
                            Class.forName("gn.com.android.mmitest.item."
                                    + mAutoItemKeys.get(index + 1)));
                    //Gionee zhangke 20151202 add for CR01602890 start
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | it.FLAG_ACTIVITY_SINGLE_TOP
                            | it.FLAG_ACTIVITY_CLEAR_TOP);
                    //Gionee zhangke 20151202 add for CR01602890 end

                    activity.startActivity(it);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                Intent it = new Intent(activity, TestResult.class);
                //Gionee zhangke 20151205 add for CR01604187 start
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | it.FLAG_ACTIVITY_SINGLE_TOP
                        | it.FLAG_ACTIVITY_CLEAR_TOP);
                //Gionee zhangke 20151205 add for CR01604187 end
                activity.startActivity(it);
            }
        }
    }
    // Gionee xiaolin 20121023 modify for CR00717365 end

    public static void restart(Activity activity, String TAG) {
        try {
            activity.finish();
            Log.e(TAG1, "restart this activity");
            Intent it = new Intent(activity, Class
                    .forName("gn.com.android.mmitest.item." + TAG));
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(it);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static String getNewSN(int position, Character value, String sn) {
        StringBuffer sb = new StringBuffer(sn);
        int length = sn.length();
        if (length < position - 1) {
            for (int i = 0; i < position - sn.length(); i++) {
                sb.append(" ");
            }
            sb.append(value);
        } else if (length == position - 1) {
            sb.append(value);
        } else {
            sb.setCharAt(position - 1, value);
        }
        return sb.toString();
    }

    public static void openBtAndWifi(Activity activity) {
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null != bAdapter && false == bAdapter.isEnabled()) {// bAdapter.isEnabled蓝牙状态
            bAdapter.enable();
            Log.e(TAG1, "open bluetooth");
        }
   /*     try {
            Thread.sleep(500);
        }catch(Exception e) {
        	e.printStackTrace();
        }*/
        bAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_NONE);
        Log.e(TAG1, "setScanMode = BluetoothAdapter.SCAN_MODE_NONE");

        WifiManager wifiMgr = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (null != wifiMgr && false == wifiMgr.isWifiEnabled()) {
            wifiMgr.setWifiEnabled(true);
        }
    }

    public static void closeBtAndWifi(Activity activity) {
        BluetoothAdapter bAdapterexit = BluetoothAdapter.getDefaultAdapter();
        if (bAdapterexit != null && true == bAdapterexit.isEnabled()) {
            bAdapterexit.disable();
            Log.e("zhangxiaowei", "close bluetooth");
        }

    }

    private static ArrayList<String> mSingleItemKeys;
    private static ArrayList<String> mSingleItems;

    public static void configKeyTestArrays(Context context) {
        mKeyItems = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.key_test_items)));
        mKeyItemKeys = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.key_test_keys)));
        Map<String, String> valueKeyMap = new HashMap<String, String>();
        Map<String, String> propDefMap = new HashMap<String, String>();
        Map<String, Integer> propToResMap = new HashMap<String, Integer>();

        if (mKeyItems.size() == mKeyItemKeys.size()) {
            int size = mKeyItemKeys.size();
            for (int i = 0; i < size; i++) {
                valueKeyMap.put(mKeyItems.get(i), mKeyItemKeys.get(i));
            }
        } else {
            Log.e(TAG1, "wrong!");
            return;
        }

        //Gionee zhangke 20151019 modify for CR01571097 start 
        if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_MENU_SUPPORT) {
            String value = context.getResources().getString(R.string.menu_key);
            removeKeyItem(value, valueKeyMap);
        }
        if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_APP_SUPPORT) {
            String value = context.getResources().getString(R.string.app_key);
            removeKeyItem(value, valueKeyMap);
        }
        if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_SEARCH_SUPPORT) {
            String value = context.getResources().getString(R.string.search_key);
            removeKeyItem(value, valueKeyMap);
        }
        if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_CAMERA_SUPPORT) {
            String value = context.getResources().getString(R.string.camera_key);
            removeKeyItem(value, valueKeyMap);
        }
        if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_FOCUS_SUPPORT) {
            String value = context.getResources().getString(R.string.focus_key);
            removeKeyItem(value, valueKeyMap);
        }
        if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_HALL_SUPPORT) {
            String hall_o_value = context.getResources().getString(R.string.hall_o_key);
            String hall_c_value = context.getResources().getString(R.string.hall_c_key);
            removeKeyItem(hall_o_value, valueKeyMap);
            removeKeyItem(hall_c_value, valueKeyMap);
        }
        if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_BACK_SUPPORT) {
            String value = context.getResources().getString(R.string.back_key);
            removeKeyItem(value, valueKeyMap);
        }
        if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_HOME_SUPPORT) {
            String value = context.getResources().getString(R.string.home_key);
            removeKeyItem(value, valueKeyMap);
        }
        /*
        propDefMap.put("gn.mmi.keytest.menu", "yes");
        propDefMap.put("gn.mmi.keytest.app", "yes");
        propDefMap.put("gn.mmi.keytest.search", "yes");
        propDefMap.put("gn.mmi.keytest.camera", "yes");
        propDefMap.put("gn.mmi.keytest.focus", "yes");
        propDefMap.put("gn.mmi.keytest.hall", "yes"); 
        propDefMap.put("gn.mmi.keytest.back", "yes");
        propDefMap.put("gn.mmi.keytest.home", "yes");
        
        propToResMap.put("gn.mmi.keytest.menu", R.string.menu_key);
        propToResMap.put("gn.mmi.keytest.app", R.string.app_key);
        propToResMap.put("gn.mmi.keytest.search", R.string.search_key);
        propToResMap.put("gn.mmi.keytest.camera", R.string.camera_key);
        propToResMap.put("gn.mmi.keytest.focus", R.string.focus_key);
        propToResMap.put("gn.mmi.keytest.back", R.string.back_key);
        propToResMap.put("gn.mmi.keytest.home", R.string.home_key);
        
        
        for ( String prop : propDefMap.keySet()) {
            if (!"yes".equals(SystemProperties.get(prop, propDefMap.get(prop)))) {
                if ("gn.mmi.keytest.hall".equals(prop)) {
                  String hall_o_value = context.getResources().getString(R.string.hall_o_key);
                  String hall_c_value = context.getResources().getString(R.string.hall_c_key);
                  removeKeyItem(hall_o_value, valueKeyMap);
                  removeKeyItem(hall_c_value, valueKeyMap);
                } else {
                  String value = context.getResources().getString(propToResMap.get(prop));
                  removeKeyItem(value, valueKeyMap);
                }
            }
        }
        */
        //Gionee zhangke 20151019 modify for CR01571097 end 
    }

    private static void removeKeyItem(String value, Map<String, String> valueToKey) {
        mKeyItems.remove(value);
        mKeyItemKeys.remove(valueToKey.get(value));
    }

    // Gionee xiaolin 20120528 modify for CR00611372 start
    public static void configTestItemArrays(Context context) {

        mAutoItemKeys = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_keys)));
        mAutoItems = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_items)));
        mSingleItemKeys = new ArrayList<String>(Arrays.asList(context.getResources()
                .getStringArray(R.array.single_test_keys)));
        mSingleItems = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.single_test_items)));

        //Gionee zhangke 20151014 add for CR01567923 start
        mAutoItemKeys_2 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_keys_2)));
        mAutoItems_2 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_items_2)));
        //Gionee zhangke 20151014 add for CR01567923 end 

        //Gionee zhangke 20151019 modify for CR01571097 start 
        /*
        Map<String, String> featureItem = new HashMap<String, String>();
        String[] featuresToCheck = new String[] {
            PackageManager.FEATURE_CAMERA_FRONT,
            PackageManager.FEATURE_SENSOR_ACCELEROMETER,
            PackageManager.FEATURE_SENSOR_COMPASS,
            PackageManager.FEATURE_SENSOR_LIGHT,
            PackageManager.FEATURE_SENSOR_GYROSCOPE,
            PackageManager.FEATURE_LOCATION_GPS
        };
        
        featureItem.put(PackageManager.FEATURE_CAMERA_FRONT,
                context.getResources().getString(R.string.front_camera));
        featureItem.put(PackageManager.FEATURE_SENSOR_ACCELEROMETER,
                context.getResources().getString(R.string.acceleration));
        featureItem.put(PackageManager.FEATURE_SENSOR_COMPASS, 
                context.getResources().getString(R.string.magnetic_field));
        featureItem.put(PackageManager.FEATURE_SENSOR_LIGHT,
                context.getResources().getString(R.string.light_proximity));
        featureItem.put(PackageManager.FEATURE_SENSOR_GYROSCOPE, 
                context.getResources().getString(R.string.gyroscope));
        featureItem.put(PackageManager.FEATURE_LOCATION_GPS, 
                context.getResources().getString(R.string.gps));

        PackageManager pm = context.getPackageManager();
        for (String feature : featuresToCheck) {
            if (!hasHardWareFeature(feature)) {
                removeTestItem(featureItem.get(feature));
            }
        }    
        */
        //sw version
        if (!FeatureOption.GN_RW_GN_MMI_SW_VERSION_SUPPORT) {
            String item = context.getResources().getString(R.string.sw_version);
            removeTestItem(item);
        }
        //screen brightness
        if (!FeatureOption.GN_RW_GN_MMI_SCREEN_BRIGHTNESS_SUPPORT) {
            String item = context.getResources().getString(R.string.screen_brightness);
            removeTestItem(item);
        }
        //lcd
        if (!FeatureOption.GN_RW_GN_MMI_LCD_SUPPORT) {
            String item = context.getResources().getString(R.string.color);
            removeTestItem(item);
        }
        //keys
        if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_SUPPORT) {
            String item = context.getResources().getString(R.string.keys);
            removeTestItem(item);
        }
        //receiver
        if (!FeatureOption.GN_RW_GN_MMI_RECEIVER_SUPPORT) {
            String item = context.getResources().getString(R.string.receiver);
            removeTestItem(item);
        }
        //receiver2
        if (!FeatureOption.GN_RW_GN_MMI_RECEIVER2_SUPPORT) {
            String item = context.getResources().getString(R.string.receiver2);
            removeTestItem(item);
        }
        //tone
        if (!FeatureOption.GN_RW_GN_MMI_TONE_SUPPORT) {
            String item = context.getResources().getString(R.string.tone);
            removeTestItem(item);
        }
        //tone2
        if (!FeatureOption.GN_RW_GN_MMI_DUALTONE_SUPPORT) {
            String item = context.getResources().getString(R.string.tone2);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_VIBRATE_SUPPORT) {
            String item = context.getResources().getString(R.string.vibrate);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_MIC_SUPPORT) {
            String item = context.getResources().getString(R.string.phone_loopback);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_MIC2_SUPPORT) {
            String item = context.getResources().getString(R.string.phone_loopback2);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_EARPHONE_SUPPORT) {
            String item = context.getResources().getString(R.string.earphone_loopback);
            removeTestItem(item);
        }
        //hifi
        if (!FeatureOption.GN_RW_GN_MMI_HIFI_SUPPORT) {
            String item = context.getResources().getString(R.string.hifi);
            removeTestItem(item);
        }
        //FM
        if (!FeatureOption.GN_RW_GN_MMI_FM_SUPPORT) {
            String item = context.getResources().getString(R.string.fm);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_TOUCH_PRIV_PAD_SUPPORT) {
            String item = context.getResources().getString(R.string.touch_priv_pad);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_TOUCH_PAD_SUPPORT) {
            String item = context.getResources().getString(R.string.touch_pad);
            removeTestItem(item);
        }
		if (!FeatureOption.GN_RW_GN_MMI_BACK_FLASHLIGHT_CAL_SUPPORT) {
			String item  = context.getResources().getString(R.string.back_flashlight_cal_test);
			removeTestItem(item);
		}

        if (!FeatureOption.GN_RW_GN_MMI_CAMERA_BACK_SUPPORT) {
            String item = context.getResources().getString(R.string.back_camera);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_CAMERA_BACK2_SUPPORT) {
            String item  = context.getResources().getString(R.string.back_camera2);
            removeTestItem(item);
        }

        if (!FeatureOption.GN_RW_GN_MMI_CAMERA_FRONT_SUPPORT) {
            String item = context.getResources().getString(R.string.front_camera);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_LIGHT_SUPPORT) {
            String item = context.getResources().getString(R.string.light_proximity);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_ACC_SUPPORT) {
            String item = context.getResources().getString(R.string.acceleration);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_COMPASS_SUPPORT) {
            String item = context.getResources().getString(R.string.magnetic_field);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_GYRO_SUPPORT) {
            String item = context.getResources().getString(R.string.gyroscope);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_BLUETOOTH_SUPPORT) {
            String item = context.getResources().getString(R.string.bluetooth);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_WIFI_SUPPORT) {
            String item = context.getResources().getString(R.string.wifi);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_WIFI5G_SUPPORT) {
            String item = context.getResources().getString(R.string.wifi5g);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_NFC_SUPPORT) {
            String item = context.getResources().getString(R.string.nfc);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_NFC2_SUPPORT) {
            String item = context.getResources().getString(R.string.nfc2);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_IRTEST_SUPPORT) {
            String item = context.getResources().getString(R.string.irtest);
            removeTestItem(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170215> modify for ID 68092 begin
        if (!FeatureOption.GN_RW_GN_MMI_IRTEST_GOOGLE_SUPPORT) {
            String item = context.getResources().getString(R.string.irtest2);
            removeTestItem(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170215> modify for ID 68092 end
        if (!FeatureOption.GN_RW_GN_MMI_OTG_SUPPORT) {
            String item = context.getResources().getString(R.string.otg);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_GPS_SUPPORT) {
            String item = context.getResources().getString(R.string.gps);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_FLASH_SUPPORT) {
            String item = context.getResources().getString(R.string.flash);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_FLASH2_SUPPORT) {
            String item = context.getResources().getString(R.string.flash2);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_FLASHLIGHT_SUPPORT) {
            String item = context.getResources().getString(R.string.FlashLight);
            removeTestItem(item);
        }
        /*Gionee huangjianqiang 20160420 add for CR01681501 begin*/
        if (!FeatureOption.GN_RW_GN_MMI_FRONT_FLASH_SUPPORT) {
            String item = context.getResources().getString(R.string.FrontFlashTest);
            removeTestItem(item);
        }
        /*Gionee huangjianqiang 20160420 add for CR01681501 end*/
        if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS_SUPPORT) {
            String item = context.getResources().getString(R.string.fingerprints);
            removeTestItem(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS2_SUPPORT) {
            String item = context.getResources().getString(R.string.fingerprints2);
            removeTestItem(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170116> modify for ID 64213 begin
        if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS3_SUPPORT) {
            String item = context.getResources().getString(R.string.fingerprints3);
            removeSingleItem(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170116> modify for ID 64213 end
        if (!FeatureOption.GN_RW_GN_MMI_BATTERY_SUPPORT) {
            String item = context.getResources().getString(R.string.battery);
            removeTestItem(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170106> modify for ID 60451 begin
        if (!FeatureOption.GN_RW_GN_MMI_QC_BATTERY_SUPPORT) {
            String item  = context.getResources().getString(R.string.largebattery);
            removeTestItem(item);
        }
		//Gionee <GN_BSP_MMI> <chengq> <20170106> modify for ID 60451 end

        if (!FeatureOption.GN_RW_GN_MMI_SETCOLOR_SUPPORT) {
            String item = context.getResources().getString(R.string.SetColor);
            removeTestItem(item);
        }
        //GIONEE 2015-08-28 add for CR01543998 begin
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_TEMPERATURE_SUPPORT) {
            String item = context.getResources().getString(R.string.temperature);
            removeTestItem(item);
        }
        // GIONEE 2015-08-28 add for CR01543998 end
        //Gionee zhangke 20151128 add for CR01591381 start
        if (!FeatureOption.GN_RW_GN_MMI_FORCE_TOUCH_SUPPORT) {
            String item = context.getResources().getString(R.string.force_touch);
            removeTestItem(item);
        }
        //Gionee zhangke 20151128 add for CR01591381 end

        //Gionee zhangke 20151128 add for CR01588796 start
        if (!FeatureOption.GN_RW_GN_MMI_LASER_SUPPORT) {
            String item = context.getResources().getString(R.string.laser_cal);
            removeTestItem(item);
        }
        //Gionee zhangke 20151128 add for CR01588796 end

        //Gionee zhangke 20151019 modify for CR01571097 end
        //Gionee <GN_BSP_MMI> <chengq> <20170116> modify for ID 64213 begin
        if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS3_AUTOTEST1_SUPPORT) {
            String item = context.getResources().getString(R.string.fingerprints3);
            removeAutoTest1Item(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170116> modify for ID 64213 end
        //Gionee zhangke 20151014 add for CR01567923 start
        if (!FeatureOption.GN_RW_GN_MMI_BRIGHTNESS_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.screen_brightness);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_COLOR_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.color);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_KEYS_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.keys);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_RECEIVER_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.receiver);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_RECEIVER2_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.receiver2);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_TONE_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.tone);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_DUALTONE_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.tone2);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_VIBRATE_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.vibrate);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_MIC_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.phone_loopback);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_MIC2_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.phone_loopback2);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_EARPHONE_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.earphone_loopback);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_HIFI_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.hifi);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_FM_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.fm);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_TOUCH_PRIV_PAD_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.touch_priv_pad);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_TOUCH_PAD_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.touch_pad);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_CAMERA_BACK_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.back_camera);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_CAMERA_BACK2_AUTOTEST2_SUPPORT) {
            String item  = context.getResources().getString(R.string.back_camera2);
            removeAutoTest2Item(item);
        }

        if (!FeatureOption.GN_RW_GN_MMI_CAMERA_FRONT_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.front_camera);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_LIGHT_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.light_proximity);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_ACC_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.acceleration);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_COMPASS_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.magnetic_field);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_GYRO_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.gyroscope);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_BLUETOOTH_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.bluetooth);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_WIFI_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.wifi);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_WIFI5G_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.wifi5g);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_NFC_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.nfc);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_NFC2_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.nfc2);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_IRTEST_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.irtest);
            removeAutoTest2Item(item);
        }
		//Gionee <GN_BSP_MMI> <chengq> <20170215> modify for ID 68092 begin
        if (!FeatureOption.GN_RW_GN_MMI_IRTEST_GOOGLE_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.irtest2);
            removeAutoTest2Item(item);
        }	
		//Gionee <GN_BSP_MMI> <chengq> <20170215> modify for ID 68092 end
        if (!FeatureOption.GN_RW_GN_MMI_OTG_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.otg);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_SENSOR_GPS_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.gps);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_FLASH_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.flash);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_FLASH2_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.flash2);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_FLASHLIGHT_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.FlashLight);
            removeAutoTest2Item(item);
        }

        /*Gionee huangjianqiang 20160420 add for CR01681501 begin*/
        if (!FeatureOption.GN_RW_GN_MMI_FRONT_FLASH_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.FrontFlashTest);
            removeAutoTest2Item(item);
        }
        /*Gionee huangjianqiang 20160420 add for CR01681501 end*/
        if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.fingerprints);
            removeAutoTest2Item(item);
        }
        if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS2_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.fingerprints2);
            removeAutoTest2Item(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170109> add for ID 61637 begin
        if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS3_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.fingerprints3);
            removeAutoTest2Item(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170109> add for ID 61637 end
        if (!FeatureOption.GN_RW_GN_MMI_BATTERY_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.battery);
            removeAutoTest2Item(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170107> modify for ID 60451 begin
        if (!FeatureOption.GN_RW_GN_MMI_QC_BATTERY_AUTOTEST2_SUPPORT) {
            String item  = context.getResources().getString(R.string.largebattery);
            removeAutoTest2Item(item);
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170107> modify for ID 60451 end

        //Gionee zhangke 20151128 add for CR01591381 start
        if (!FeatureOption.GN_RW_GN_MMI_FORCE_TOUCH_AUTOTEST2_SUPPORT) {
            String item = context.getResources().getString(R.string.force_touch);
            removeAutoTest2Item(item);
        }
        //Gionee zhangke 20151128 add for CR01591381 end

        //Gionee zhangke 20151014 add for CR01567923 end
    }


    private static boolean hasHardWareFeature(String feature) {

        Map<String, String> featureToSysProp = new HashMap<String, String>();
        featureToSysProp.put(PackageManager.FEATURE_CAMERA_FRONT, "gn.mmi.camera.front");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_ACCELEROMETER, "gn.mmi.sensor.acc");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_COMPASS, "gn.mmi.sensor.compass");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_LIGHT, "gn.mmi.sensor.light");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_PROXIMITY, "gn.mmi.sensor.prox");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_GYROSCOPE, "gn.mmi.sensor.gyro");
        //Gionee zhangxiaowei 20130514 add for CR00811986 begin
        featureToSysProp.put(PackageManager.FEATURE_LOCATION_GPS, "gn.mmi.sensor.gps");// gps
        //Gionee zhangxiaowei 20130514 add for CR00811986 end
        if (PackageManager.FEATURE_SENSOR_GYROSCOPE.equals(feature)) {
            return "yes".equals(SystemProperties.get(featureToSysProp.get(feature), "yes"));
        }
        return "yes".equals(SystemProperties.get(featureToSysProp.get(feature), "yes"));
    }
    //  Gionee xiaolin 20120528 modify for CR00611372 end
    //Gionee <GN_BSP_MMI> <chengq> <20170116> modify for ID 64213 begin
    static private void removeTestItem(String item) {
        removeAutoTest1Item(item);
        removeSingleItem(item);
    }

    private static void removeSingleItem(String item) {
        int index = mSingleItems.indexOf(item);
        if (-1 != index) {
            mSingleItems.remove(index);
            mSingleItemKeys.remove(index);
        }
    }
    private static void removeAutoTest1Item(String item) {
        int index = mAutoItems.indexOf(item);
        if (-1 != index) {
            mAutoItems.remove(index);
            mAutoItemKeys.remove(index);
        }
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170116> modify for ID 64213 end
    //Gionee zhangke 20151014 add for CR01567923 start
    private static void removeAutoTest2Item(String item) {
        int index = mAutoItems_2.indexOf(item);
        if (-1 != index) {
            mAutoItems_2.remove(index);
            mAutoItemKeys_2.remove(index);
        }
    }
    //Gionee zhangke 20151014 add for CR01567923 end

    public static String[] getSingleTestItems(Context context) {
        if (null == mSingleItems)
            return null;
        return mSingleItems.toArray(new String[0]);
    }

    public static String[] getSingleTestKeys(Context context) {
        if (null == mSingleItemKeys)
            return null;
        return mSingleItemKeys.toArray(new String[0]);
    }

    // Gionee xiaolin 20121017 add for CR00715318 start
    //静音
 /*   private static String dAudioState = null;
    public static void muteAudio(Context cxt, boolean mute) {
        AudioProfileManager apm = (AudioProfileManager)cxt.getSystemService(Context.AUDIOPROFILE_SERVICE);
        if (null == dAudioState){
            dAudioState = apm.getActiveProfileKey();
        }
        if(mute)
            apm.setActiveProfile("mtk_audioprofile_silent");
        else
            apm.setActiveProfile(dAudioState);
    }*/
    // Gionee xiaolin 20121017 add for CR00715318 end

    public static boolean mIsAutoMode_2 = false;

    private static void processButtonPress_2(String TAG, Activity activity, boolean success) {
        Log.e(TAG1, "processButtonPress_2 ");
        int index = getAutoItemKeys_2(activity).indexOf(TAG);
        Log.e(TAG1, mAutoItemKeys_2.toString());
        int result = success ? 1 : 0;
        if (null == mEditor) {
            getSharedPreferencesEdit(activity);
        }
        //Gionee zhangke 20160105 delete for CR01618135 start
        /*
        if (index == 0) {
            mEditor.clear();
        }
        */
        //Gionee zhangke 20160105 delete for CR01618135 end
        mEditor.putInt(TestUtils.getAutoItems_2(activity).get(index), result);
        mEditor.commit();

        Log.e(TAG1, TestUtils.getAutoItems_2(activity).get(index) + ":" + result);
        if (index < mAutoItemKeys_2.size() - 1) {
            try {
                Intent it = new Intent().setClass(
                        activity,
                        Class.forName("gn.com.android.mmitest.item."
                                + mAutoItemKeys_2.get(index + 1)));
                //Gionee zhangke 20151202 add for CR01602890 start
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | it.FLAG_ACTIVITY_SINGLE_TOP
                        | it.FLAG_ACTIVITY_CLEAR_TOP);
                //Gionee zhangke 20151202 add for CR01602890 end

                activity.startActivity(it);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Intent it = new Intent(activity, TestResult.class);
            //Gionee zhangke 20151205 add for CR01604187 start
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | it.FLAG_ACTIVITY_SINGLE_TOP
                    | it.FLAG_ACTIVITY_CLEAR_TOP);
            //Gionee zhangke 20151205 add for CR01604187 end
            activity.startActivity(it);
        }

    }

    private static ArrayList<String> mAutoItemKeys_2;
    private static ArrayList<String> mAutoItems_2;

    public static ArrayList<String> getAutoItemKeys_2(Context context) {
        //Gionee zhangke 20151014 delete for CR01567923 start
        /*
        if (null == mAutoItemKeys_2) {
            mAutoItemKeys_2 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_keys_2)));
            //Configure second microphone test for AutoMMI test2
			if (!"yes".equals(SystemProperties.get("gn.mmi.mic2", "yes"))) {
				String item = "PhoneLoopbackTest2";
				if (mAutoItemKeys_2.contains(item)) {
					mAutoItemKeys_2.remove("PhoneLoopbackTest2");
				}
			}
        }
        */
        //Gionee zhangke 20151014 delete for CR01567923 end
        return mAutoItemKeys_2;
    }

    public static ArrayList<String> getAutoItems_2(Context context) {
        //Gionee zhangke 20151014 delete for CR01567923 start
        /*
        if (null == mAutoItems_2) {
            mAutoItems_2 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_items_2)));
			// Configure second microphone test for AutoMMI test2
			if (!"yes".equals(SystemProperties.get("gn.mmi.mic2", "yes"))) {
				String item = context.getString(R.string.phone_loopback2);
				if (mAutoItems_2.contains(item)) {
					mAutoItems_2.remove(item);
				}
			}
        }
        */
        //Gionee zhangke 20151014 delete for CR01567923 end

        return mAutoItems_2;
    }

    public static String setStreamVoice(String a) {
        FileReader dbReader;

        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);

        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            Log.e(TAG1, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
            return null;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(dbReader);
            XmlUtils.beginDocument(parser, "gnmmi");
            while (true) {
                XmlUtils.nextElement(parser);
                String name = parser.getName();
                if (!"gnmmi".equals(name)) {
                    return null;
                }

                String name1 = parser.getAttributeValue(null, "name");
                if (a.equals(name1)) {
                    value = parser.getAttributeValue(null, "value");
                    Log.e(TAG1, "name=" + name1);
                    Log.e(TAG1, "value=" + value);
                    dbReader.close();
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG1, "Exception  config parser " + e);
        } catch (IOException e) {
            Log.e(TAG1, "Exception in font config parser " + e);
        }
        return value;
    }

    //Gionee zhangke 20151215 add for CR01609753 start
    public static void setWindowFlags(Activity activity) {
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        //*Gionee huangjianqiang 20160310 add for CR01638778 begin*/
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        /*Gionee huangjianqiang 20160310 add for CR01638778 end*/
        window.setAttributes(lp);
        View view = window.getDecorView();
        int visFlags = View.STATUS_BAR_DISABLE_BACK
                | View.STATUS_BAR_DISABLE_HOME
                | View.STATUS_BAR_DISABLE_RECENT
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | 0x00004000;//View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED

        view.setSystemUiVisibility(visFlags);

    }
    //Gionee zhangke 20151215 add for CR01609753 end
    //Gionee <GN_BSP_MMI> <chengq> <20170116> modify for ID 64213 begin
    public static void checkTestItems() {
        if (mAutoItemKeys != null)
        for (String str : mAutoItemKeys) {
            Log.i(TAG1,"auto_test_1 item is "+ str +"\n");
        }
        if (mSingleItemKeys != null)
        for (String str : mSingleItemKeys) {
            Log.i(TAG1,"single_test item is "+ str +"\n");
        }
        if (mAutoItemKeys_2 != null)
        for (String str : mAutoItemKeys_2) {
            Log.i(TAG1,"auto_test_2 item is "+ str +"\n");
        }
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170116> modify for ID 64213 end
    //Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 111730 begin
    public static boolean writeNodeState(Context context, String nodeType, int value) {
        Object pm = (Object) (context.getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
            method.invoke(pm, f.get(null), value);
			Log.i(TAG1,"writeGestureNodeValue "+nodeType+" "+f.get(null)+":"+value);
            return true;
        } catch (Exception e) {
            Log.e(TAG1, "Exception :" + e);
        }
        return false;
    }

    public static int getNodeState(Context context, String nodeType) {
        Object pm = (Object) (context.getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("GetNodeState", int.class);
            Field f = cls.getField(nodeType);
            int value = (int)method.invoke(pm, f.get(null));
            Log.i(TAG1,"getNodeValue "+nodeType+" "+f.get(null)+":"+value);
            return value;
        } catch (Exception e) {
            Log.e(TAG1, "Exception :" + e);
        }
        return -1;
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 111730 end

    public static boolean getSbcFlag() {
        String mSbcFileName = "/proc/sbcflag";
        File mSbcFile = null;
        String mSbcStatua = null;
        try {
            mSbcFile = new File(mSbcFileName);
            if (mSbcFile.exists()) {
                BufferedReader bf = new BufferedReader(new FileReader(mSbcFile));
                mSbcStatua = bf.readLine();
                Log.i(TAG1, "#1 "+mSbcStatua);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG1, "#2 "+mSbcStatua);
            return false;
        }
        Log.i(TAG1, "#3 "+mSbcStatua);
        return mSbcFile != null && mSbcStatua.equals("1");
    }
}

