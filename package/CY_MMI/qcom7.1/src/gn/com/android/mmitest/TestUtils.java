package gn.com.android.mmitest;

import gn.com.android.mmitest.item.BluetoothTest;
import gn.com.android.mmitest.item.GPSTest;
import gn.com.android.mmitest.item.WIFITest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.lang.Thread;
//Gionee <GN_BSP_MMI><lifeilong><20161202> modify for ID37777 begin
import java.lang.reflect.Field;
import java.lang.reflect.Method;
//Gionee <GN_BSP_MMI><lifeilong><20161202> modify for ID37777 end

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.os.SystemProperties;
import gn.com.android.mmitest.item.FeatureOption;
//Gionee zhangke 20160628 modify for CR01724239 start
import android.view.WindowManager;
import android.view.View;
import android.view.Window;
import android.app.StatusBarManager;
//Gionee zhangke 20160628 modify for CR01724239 end
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;
import com.android.internal.util.XmlUtils;



public class TestUtils {
	public static WakeLock mWakeLock;

	private static SharedPreferences mSharedPreferences;
	private static SharedPreferences mSNSharedPreferences;

	private static ArrayList<String> mItems;

	private static ArrayList<String> mItemKeys;

	private static ArrayList<String> mAutoItemKeys;

	private static ArrayList<String> mAutoItems;

	private static ArrayList<String> mKeyItems;
	private static ArrayList<String> mKeyItemKeys;
	public static boolean mIsAutoMode;

	public static Context mAppContext;

	public static int WRITE_TO_SN_COUNT = 4;
	private static SharedPreferences.Editor mEditor;
	public static int VOL_MINUS = 0;
	public static int VOL_MINUS_INCALL = 3;

	public static SharedPreferences.Editor mSNEditor;
    public static final int BUTTON_ENABLED_DELAY_TIME = 2000;
    private static final String AMIGOSETTING_DB_CONFIG_FILE  = "/system/etc/gnmmiConfig.xml";
	//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID37777 begin
	public static final String CHARGING_SWITCH_ON = "NODE_TYPE_BATTERY_MMI_STATUS";
	//Gionee <GN_BSP_MMI><lifeilong><20161203> modify for ID37777 end
    private static final String TAG = "TestUtils";

	public static void setAppContext(Activity activity) {
		mAppContext = activity.getApplicationContext();
	}

	public static Map<String, String> factoryFlag = new HashMap<String, String>();
	static {
		factoryFlag.put(WIFITest.FACTORY_WIFI, "15");
		factoryFlag.put(BluetoothTest.FACTORY_BT, "18");
		factoryFlag.put(GPSTest.FACTORY_GPS, "14");
		//factoryFlag.put(WChatKeyTest.FACTORY_WC,"32");
	}

	public static void acquireWakeLock(Activity activity) {
		if (mWakeLock == null || false == mWakeLock.isHeld()) {
			PowerManager powerManager = (PowerManager) (activity
					.getApplicationContext()
					.getSystemService(Context.POWER_SERVICE));
			mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,
					"My Single Test");
		}
		if (false == mWakeLock.isHeld()) {
			mWakeLock.acquire();
		}
	}

	public static void releaseWakeLock() {
		if (null != mWakeLock && true == mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	public static SharedPreferences getSharedPreferences(Context context) {
		if (null == mSharedPreferences) {
			mSharedPreferences = context.getSharedPreferences("gn_mmi_test",
					Context.MODE_WORLD_WRITEABLE);
		}
		return mSharedPreferences;
	}

	public static SharedPreferences getSNSharedPreferences(Context context) {
		if (null == mSNSharedPreferences) {
			mSNSharedPreferences = context.getSharedPreferences("gn_mmi_sn",
					Context.MODE_WORLD_WRITEABLE);
		}
		return mSNSharedPreferences;
	}

	public static SharedPreferences.Editor getSharedPreferencesEdit(
			Context context) {
		if (null == mEditor) {
			mSharedPreferences = getSharedPreferences(context);
			mEditor = mSharedPreferences.edit();
		}
		return mEditor;
	}

	public static SharedPreferences.Editor getSNSharedPreferencesEdit(
			Context context) {
		if (null == mSNEditor) {
			mSNSharedPreferences = getSNSharedPreferences(context);
			mSNEditor = mSNSharedPreferences.edit();
		}
		return mSNEditor;
	}

	public static ArrayList<String> getItemKeys(Context context) {
		if (null == mItemKeys) {
			mItemKeys = new ArrayList(Arrays.asList(context.getResources()
					.getStringArray(R.array.single_test_keys)));
		}
		return mItemKeys;
	}

	public static ArrayList<String> getItems(Context context) {
		if (null == mItems) {
			mItems = new ArrayList(Arrays.asList(context.getResources()
					.getStringArray(R.array.single_test_items)));
		}
		return mItems;
	}

	public static ArrayList<String> getAutoItemKeys(Context context) {
		if (null == mAutoItemKeys) {
			mAutoItemKeys = new ArrayList(Arrays.asList(context.getResources()
					.getStringArray(R.array.auto_test_keys)));
		}
		return mAutoItemKeys;
	}

	public static ArrayList<String> getAutoItems(Context context) {
		if (null == mAutoItems) {
			mAutoItems = new ArrayList(Arrays.asList(context.getResources()
					.getStringArray(R.array.auto_test_items)));
		}
		return mAutoItems;
	}

	public static ArrayList<String> getKeyItems(Context context) {
		if (null == mKeyItems)
			configKeyTestArrays(context);

		return mKeyItems;
	}

	public static ArrayList<String> getKeyItemKeys(Context context) {
		if (null == mKeyItemKeys)
			configKeyTestArrays(context);

		return mKeyItemKeys;
	}

	// Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin
	public static void rightPress(String TAG, Activity activity) {
		activity.finish();
		if (mIsAutoMode_2) {

			processButtonPress_2(TAG, activity, true);
		}
		if (mIsAutoMode_3) {
			processButtonPress_3(TAG, activity, true);
		} else {
			processButtonPress(TAG, activity);
		}
	}

	public static void wrongPress(String TAG, Activity activity) {
		activity.finish();
		if (mIsAutoMode_2) {
			processButtonPress_2(TAG, activity, false);
		}
		if (mIsAutoMode_3) {
			processButtonPress_3(TAG, activity, false);
		} else {
			processButtonPress1(TAG, activity);
		}
	}

	// Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end

	public static void processButtonPress(String TAG, Activity activity) {
		activity.finish();
		if (true == mIsAutoMode) {
			mAutoItemKeys = getAutoItemKeys(mAppContext);
			int index = mAutoItemKeys.indexOf(TAG);
			if (null == mEditor) {
				getSharedPreferencesEdit(mAppContext);
			}
			if (index == 0) {
				mEditor.clear();
			}
			mEditor.putInt(TestUtils.getAutoItems(mAppContext).get(index), 1);
			mEditor.commit();
			Log.e("mmi_TestUtils", TestUtils.getAutoItems(activity).get(index)
					+ ":");
			if (index < mAutoItemKeys.size() - 1) {
				try {
					Intent it = new Intent().setClass(
							mAppContext,
							Class.forName("gn.com.android.mmitest.item."
									+ mAutoItemKeys.get(index + 1)));
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mAppContext.startActivity(it);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				Intent it = new Intent(mAppContext, TestResult.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mAppContext.startActivity(it);
			}
		}
	}

	public static void processButtonPress1(String TAG, Activity activity) {
		activity.finish();
		if (true == mIsAutoMode) {
			mAutoItemKeys = getAutoItemKeys(mAppContext);
			int index = mAutoItemKeys.indexOf(TAG);
			if (null == mEditor) {
				getSharedPreferencesEdit(mAppContext);
			}
			if (index == 0) {
				mEditor.clear();
			}
			mEditor.putInt(TestUtils.getAutoItems(mAppContext).get(index), 0);
			mEditor.commit();
			if (index < mAutoItemKeys.size() - 1) {
				try {
					Intent it = new Intent().setClass(
							mAppContext,
							Class.forName("gn.com.android.mmitest.item."
									+ mAutoItemKeys.get(index + 1)));
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					mAppContext.startActivity(it);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				Intent it = new Intent(mAppContext, TestResult.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				mAppContext.startActivity(it);
			}
		}
	}

	public static void restart(Activity activity, String TAG) {
		try {
			activity.finish();
			Intent it = new Intent(mAppContext,
					Class.forName("gn.com.android.mmitest.item." + TAG));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mAppContext.startActivity(it);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void delayRestart(Activity activity, String TAG,
			int DELAY_TIME) {
		try {
			activity.finish();
			try {
				Thread.sleep(DELAY_TIME);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Intent it = new Intent(mAppContext,
					Class.forName("gn.com.android.mmitest.item." + TAG));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mAppContext.startActivity(it);
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
		if (null != bAdapter && false == bAdapter.isEnabled()) {
			bAdapter.enable();
		}

		WifiManager wifiMgr = (WifiManager) activity
				.getSystemService(Context.WIFI_SERVICE);
		if (null != wifiMgr && false == wifiMgr.isWifiEnabled()) {
			wifiMgr.setWifiEnabled(true);
		}
	}

	private static ArrayList<String> mSingleItemKeys;
	private static ArrayList<String> mSingleItems;

	public static void configKeyTestArrays(Context context) {
		mKeyItems = new ArrayList<String>(Arrays.asList(context.getResources()
				.getStringArray(R.array.key_test_items)));
		mKeyItemKeys = new ArrayList<String>(Arrays.asList(context
				.getResources().getStringArray(R.array.key_test_keys)));
		Map<String, String> valueKeyMap = new HashMap<String, String>();
		Map<String, String> propDefMap = new HashMap<String, String>();
		Map<String, Integer> propToResMap = new HashMap<String, Integer>();

		if (mKeyItems.size() == mKeyItemKeys.size()) {
			int size = mKeyItemKeys.size();
			for (int i = 0; i < size; i++) {
				valueKeyMap.put(mKeyItems.get(i), mKeyItemKeys.get(i));
			}
		} else {
			Log.e("TestUtils", "wrong!");
			return;
		}

        //Gionee zhangke 20160628 modify for CR01724239 start 
        if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_MENU_SUPPORT){
            String value = context.getResources().getString(R.string.menu_key);
            removeKeyItem(value, valueKeyMap);
        }
        if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_APP_SUPPORT){
            String value = context.getResources().getString(R.string.app_key);
            removeKeyItem(value, valueKeyMap);
        }
        if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_SEARCH_SUPPORT){
            String value = context.getResources().getString(R.string.search_key);
            removeKeyItem(value, valueKeyMap);
        }
        if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_CAMERA_SUPPORT){
            String value = context.getResources().getString(R.string.camera_key);
            removeKeyItem(value, valueKeyMap);
        }
        /*
        if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_FOCUS_SUPPORT){
            String value = context.getResources().getString(R.string.focus_key);
            removeKeyItem(value, valueKeyMap);
        }*/
        if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_HALL_SUPPORT){ 
            String hall_o_value = context.getResources().getString(R.string.hall_o_key);
            String hall_c_value = context.getResources().getString(R.string.hall_c_key);
            removeKeyItem(hall_o_value, valueKeyMap);
            removeKeyItem(hall_c_value, valueKeyMap);
        }
        if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_BACK_SUPPORT){
            String value = context.getResources().getString(R.string.back_key);
            removeKeyItem(value, valueKeyMap);
        }
        if(!FeatureOption.GN_RW_GN_MMI_KEYTEST_HOME_SUPPORT){
            String value = context.getResources().getString(R.string.home_key);
            removeKeyItem(value, valueKeyMap);
        }
        //Gionee zhangke 20160628 modify for CR01724239 end 

	}

	private static void removeKeyItem(String value,
			Map<String, String> valueToKey) {
		mKeyItems.remove(value);
		mKeyItemKeys.remove(valueToKey.get(value));
	}

	public static void configTestItemArrays(Context context) {

		mAutoItemKeys = new ArrayList<String>(Arrays.asList(context
				.getResources().getStringArray(R.array.auto_test_keys)));
		mAutoItems = new ArrayList<String>(Arrays.asList(context.getResources()
				.getStringArray(R.array.auto_test_items)));
		mSingleItemKeys = new ArrayList<String>(Arrays.asList(context
				.getResources().getStringArray(R.array.single_test_keys)));
		mSingleItems = new ArrayList<String>(Arrays.asList(context
				.getResources().getStringArray(R.array.single_test_items)));
        //Gionee zhangke 20160628 add for CR01724239 start
        mAutoItemKeys_2 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_keys_2)));
        mAutoItems_2 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_items_2)));
        //Gionee zhangke 20160628 add for CR01724239 end

		Map<String, String> featureItem = new HashMap<String, String>();
		String[] featuresToCheck = new String[] {
				PackageManager.FEATURE_CAMERA_FRONT,
				PackageManager.FEATURE_SENSOR_ACCELEROMETER,
				PackageManager.FEATURE_SENSOR_COMPASS,
				PackageManager.FEATURE_SENSOR_LIGHT,
				PackageManager.FEATURE_SENSOR_GYROSCOPE };

		featureItem.put(PackageManager.FEATURE_CAMERA_FRONT, context
				.getResources().getString(R.string.front_camera));
		featureItem.put(PackageManager.FEATURE_SENSOR_ACCELEROMETER, context
				.getResources().getString(R.string.acceleration));
		featureItem.put(PackageManager.FEATURE_SENSOR_COMPASS, context
				.getResources().getString(R.string.magnetic_field));
		featureItem.put(PackageManager.FEATURE_SENSOR_LIGHT, context
				.getResources().getString(R.string.light_proximity));
		featureItem.put(PackageManager.FEATURE_SENSOR_GYROSCOPE, context
				.getResources().getString(R.string.gyroscope));

		 //sw version
		 if (!FeatureOption.GN_RW_GN_MMI_SW_VERSION_SUPPORT) {
			 String item  = context.getResources().getString(R.string.sw_version);
			 removeTestItem(item);
		 }
		 //screen brightness
		 if (!FeatureOption.GN_RW_GN_MMI_SCREEN_BRIGHTNESS_SUPPORT) {
			 String item  = context.getResources().getString(R.string.screen_brightness);
			 removeTestItem(item);
		 }
		 //lcd
		 if (!FeatureOption.GN_RW_GN_MMI_LCD_SUPPORT) {
			 String item  = context.getResources().getString(R.string.color);
			 removeTestItem(item);
		 }
		 //keys
		 if (!FeatureOption.GN_RW_GN_MMI_KEYTEST_SUPPORT) {
			 String item  = context.getResources().getString(R.string.keys);
			 removeTestItem(item);
		 }
		 //receiver
		 if (!FeatureOption.GN_RW_GN_MMI_RECEIVER_SUPPORT) {
			 String item  = context.getResources().getString(R.string.receiver);
			 removeTestItem(item);
		 }
		 //receiver2
		 if (!FeatureOption.GN_RW_GN_MMI_RECEIVER2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.receiver2);
			 removeTestItem(item);
		 }
		 //tone
		 if (!FeatureOption.GN_RW_GN_MMI_TONE_SUPPORT) {
			 String item  = context.getResources().getString(R.string.tone);
			 removeTestItem(item);
		 }
		 //tone2
		 if (!FeatureOption.GN_RW_GN_MMI_DUALTONE_SUPPORT) {
			 String item  = context.getResources().getString(R.string.tone2);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_VIBRATE_SUPPORT) {
			 String item  = context.getResources().getString(R.string.vibrate);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_MIC_SUPPORT) {
			 String item  = context.getResources().getString(R.string.phone_loopback);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_MIC2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.phone_loopback2);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_EARPHONE_SUPPORT) {
			 String item  = context.getResources().getString(R.string.earphone_loopback);
			 removeTestItem(item);
		 }
		 //hifi
		 if (!FeatureOption.GN_RW_GN_MMI_HIFI_SUPPORT) {
			 String item  = context.getResources().getString(R.string.hifi);
			 removeTestItem(item);
		 }
		 //FM
		 if (!FeatureOption.GN_RW_GN_MMI_FM_SUPPORT) {
			 String item  = context.getResources().getString(R.string.fm);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_TOUCH_PRIV_PAD_SUPPORT) {
			 String item  = context.getResources().getString(R.string.touch_priv_pad);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_TOUCH_PAD_SUPPORT) {
			 String item  = context.getResources().getString(R.string.touch_pad);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_CAMERA_BACK_SUPPORT) {
			 String item  = context.getResources().getString(R.string.back_camera);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_CAMERA_BACK2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.back_camera2);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_CAMERA_FRONT_SUPPORT) {
			 String item  = context.getResources().getString(R.string.front_camera);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_LIGHT_SUPPORT) {
			 String item  = context.getResources().getString(R.string.light_proximity);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_ACC_SUPPORT) {
			 String item  = context.getResources().getString(R.string.acceleration);
			 removeTestItem(item);
		 }		 
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_COMPASS_SUPPORT) {
			 String item  = context.getResources().getString(R.string.magnetic_field);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_GYRO_SUPPORT) {
			 String item  = context.getResources().getString(R.string.gyroscope);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_BLUETOOTH_SUPPORT) {
			 String item  = context.getResources().getString(R.string.bluetooth);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_WIFI_SUPPORT) {
			 String item  = context.getResources().getString(R.string.wifi);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_WIFI5G_SUPPORT) {
			 String item  = context.getResources().getString(R.string.wifi5g);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_NFC_SUPPORT) {
			 String item  = context.getResources().getString(R.string.nfc);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_NFC2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.nfc2);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_IRTEST_SUPPORT) {
			 String item  = context.getResources().getString(R.string.irtest);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_OTG_SUPPORT) {
			 String item  = context.getResources().getString(R.string.otg);
			 removeTestItem(item);
		 }		 
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_GPS_SUPPORT) {
			 String item  = context.getResources().getString(R.string.gps);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FLASH_SUPPORT) {
			 String item  = context.getResources().getString(R.string.flash);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FLASH2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.flash2);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FLASHLIGHT_SUPPORT) {
			 String item  = context.getResources().getString(R.string.FlashLight);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS_SUPPORT) {
			 String item  = context.getResources().getString(R.string.fingerprints);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.fingerprints2);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_BATTERY_SUPPORT) {
			 String item  = context.getResources().getString(R.string.battery);
			 removeTestItem(item);
		 }
		 //Gionee <GN_MMI><lifeilong><20161104> add for 75 begin
                 //Gionee <GN_BSP_MMI><lifeilong><20170112> modify for ID 62756 begin
		 if (!FeatureOption.GN_RW_GN_MMI_QC_BATTERY_SUPPORT) {
                 //Gionee <GN_BSP_MMI><lifeilong><20170112> modify for ID 62756 end
			 String item  = context.getResources().getString(R.string.bigbattery);
			 removeTestItem(item);
		 }
		 //Gionee <GN_MMI><lifeilong><20161104> add for 75 end
		 //Gionee <GN_MMI><lifeilong><20161104> add for 33053 begin
		 /*if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS3_SUPPORT) {
			 String item  = context.getResources().getString(R.string.fingerprints3);
			 removeTestItem(item);
		 }*/
		 //Gionee <GN_MMI><lifeilong><20161104> add for 33053 end
		 if (!FeatureOption.GN_RW_GN_MMI_PRESSURE_SUPPORT) {
			 String item  = context.getResources().getString(R.string.pressure);
			 removeTestItem(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_A3_VEB_SUPPORT) {
			 String item  = context.getResources().getString(R.string.a3_test);
			 removeTestItem(item);
		 }

		 
		 /*
		 if (!FeatureOption.GN_RW_GN_MMI_QC_BATTERY_SUPPORT) {
			 String item  = context.getResources().getString(R.string.battery);
			 removeTestItem(item);
		 }*/
		
		 if (!FeatureOption.GN_RW_GN_MMI_SETCOLOR_SUPPORT) {
			 String item  = context.getResources().getString(R.string.SetColor);
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
		 
		 //Gionee zhangke 20151014 add for CR01567923 start
		 if (!FeatureOption.GN_RW_GN_MMI_BRIGHTNESS_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.screen_brightness);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_COLOR_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.color);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_KEYS_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.keys);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_RECEIVER_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.receiver);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_RECEIVER2_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.receiver2);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_TONE_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.tone);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_DUALTONE_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.tone2);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_VIBRATE_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.vibrate);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_MIC_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.phone_loopback);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_MIC2_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.phone_loopback2);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_EARPHONE_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.earphone_loopback);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_HIFI_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.hifi);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FM_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.fm);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_TOUCH_PRIV_PAD_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.touch_priv_pad);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_TOUCH_PAD_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.touch_pad);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_CAMERA_BACK_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.back_camera);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_CAMERA_BACK2_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.back_camera2);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_CAMERA_FRONT_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.front_camera);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_LIGHT_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.light_proximity);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_ACC_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.acceleration);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_COMPASS_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.magnetic_field);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_GYRO_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.gyroscope);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_BLUETOOTH_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.bluetooth);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_WIFI_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.wifi);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_WIFI5G_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.wifi5g);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_NFC_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.nfc);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_NFC2_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.nfc2);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_IRTEST_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.irtest);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_OTG_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.otg);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_SENSOR_GPS_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.gps);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FLASH_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.flash);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FLASH2_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.flash2);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FLASHLIGHT_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.FlashLight);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.fingerprints);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_FINGERPRINTS2_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.fingerprints2);
			 removeAutoTest2Item(item);
		 }
		 if (!FeatureOption.GN_RW_GN_MMI_BATTERY_AUTOTEST2_SUPPORT) {
			 String item  = context.getResources().getString(R.string.battery);
			 removeAutoTest2Item(item);
		 }
		 //Gionee zhangke 20151128 add for CR01591381 start
		 if (!FeatureOption.GN_RW_GN_MMI_FORCE_TOUCH_AUTOTEST2_SUPPORT) {
			 String item = context.getResources().getString(R.string.force_touch);
			 removeAutoTest2Item(item);
		 }
		 //Gionee zhangke 20151128 add for CR01591381 end
		
		 //Gionee zhangke 20151014 add for CR01567923 end

	}

	static private void removeTestItem(String item) {
		int index = mAutoItems.indexOf(item);
		if (-1 != index) {
			mAutoItems.remove(index);
			mAutoItemKeys.remove(index);
		}

		index = mSingleItems.indexOf(item);
		if (-1 != index) {
			mSingleItems.remove(index);
			mSingleItemKeys.remove(index);
		}
	}

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
			configTestItemArrays(context);
		return mSingleItems.toArray(new String[0]);
	}

	public static String[] getSingleTestKeys(Context context) {
		if (null == mSingleItemKeys)
			configTestItemArrays(context);
		return mSingleItemKeys.toArray(new String[0]);
	}

	// Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin
	public static boolean mIsAutoMode_2 = false;

	private static void processButtonPress_2(String TAG, Activity activity,
			boolean success) {

		int index = getAutoItemKeys_2(activity).indexOf(TAG);
		Log.e("mmi_TestUtils", mAutoItemKeys_2.toString());
		int result = success ? 1 : 0;
		Log.e("mmi_TestUtils", "result" + result);
		Log.e("mmi_TestUtils", "index" + index);
		if (null == mEditor) {
			getSharedPreferencesEdit(activity);
		}
		if (index == 0) {
			mEditor.clear();
		}
		mEditor.putInt(TestUtils.getAutoItems_2(activity).get(index), result);
		mEditor.commit();

		Log.e("mmi_TestUtils", TestUtils.getAutoItems_2(activity).get(index)
				+ ":" + result);
		if (index < mAutoItemKeys_2.size() - 1) {
			try {
				Intent it = new Intent().setClass(
						activity,
						Class.forName("gn.com.android.mmitest.item."
								+ mAutoItemKeys_2.get(index + 1)));
				activity.startActivity(it);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			Intent it = new Intent(activity, TestResult.class);
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(it);
		}

	}

	private static ArrayList<String> mAutoItemKeys_2;
	private static ArrayList<String> mAutoItems_2;

	public static ArrayList<String> getAutoItemKeys_2(Context context) {
		if (null == mAutoItemKeys_2) {
			mAutoItemKeys_2 = new ArrayList<String>(Arrays.asList(context
					.getResources().getStringArray(R.array.auto_test_keys_2)));
		}
		return mAutoItemKeys_2;
	}

	public static ArrayList<String> getAutoItems_2(Context context) {
		if (null == mAutoItems_2) {
			mAutoItems_2 = new ArrayList<String>(Arrays.asList(context
					.getResources().getStringArray(R.array.auto_test_items_2)));
		}
		return mAutoItems_2;
	}

	// Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end
	// Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin
	public static boolean mIsAutoMode_3 = false;

	private static void processButtonPress_3(String TAG, Activity activity,
			boolean success) {

		int index = getAutoItemKeys_3(activity).indexOf(TAG);
		Log.e("mmi_TestUtils", mAutoItemKeys_3.toString());
		int result = success ? 1 : 0;
		Log.e("mmi_TestUtils", "result" + result);
		Log.e("mmi_TestUtils", "index" + index);
		if (null == mEditor) {
			getSharedPreferencesEdit(activity);
		}
		if (index == 0) {
			mEditor.clear();
		}
		mEditor.putInt(TestUtils.getAutoItems_3(activity).get(index), result);
		mEditor.commit();

		Log.e("mmi_TestUtils", TestUtils.getAutoItems_3(activity).get(index)
				+ ":" + result);
		if (index < mAutoItemKeys_3.size() - 1) {
			try {
				Intent it = new Intent().setClass(
						activity,
						Class.forName("gn.com.android.mmitest.item."
								+ mAutoItemKeys_3.get(index + 1)));
				activity.startActivity(it);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			Intent it = new Intent(activity, TestResult.class);
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			activity.startActivity(it);
		}

	}

	private static ArrayList<String> mAutoItemKeys_3;
	private static ArrayList<String> mAutoItems_3;

	public static ArrayList<String> getAutoItemKeys_3(Context context) {
		if (null == mAutoItemKeys_3) {
			mAutoItemKeys_3 = new ArrayList<String>(Arrays.asList(context
					.getResources().getStringArray(R.array.auto_test_keys_3)));
		}
		return mAutoItemKeys_3;
	}

	public static ArrayList<String> getAutoItems_3(Context context) {
		if (null == mAutoItems_3) {
			mAutoItems_3 = new ArrayList<String>(Arrays.asList(context
					.getResources().getStringArray(R.array.auto_test_items_3)));
		}
		return mAutoItems_3;
	}
	// Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end
	
    //Gionee zhangke 20160628 modify for CR01724239 start
    public static void setWindowFlags(Activity activity){
    	//Gionee <GN_BSP_GNMMI><lifeilong><20161122> modify for ID26961 27985 28542 29201 26054 begin
		//StatusBarManager mStatusBarManager = (StatusBarManager) activity.getSystemService("statusbar");
		//mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
		//Gionee <GN_BSP_GNMMI><lifeilong><20161122> modify for ID26961 27985 28542 29201 26054 end

        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
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
    //Gionee zhangke 20160628 modify for CR01724239 end

    public  static String  setStreamVoice( String a) {
        FileReader dbReader;
        String value = "";
        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);
        
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
        	Log.e(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
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
                    	 Log.e(TAG, "name=" + name1);
                    	 Log.e(TAG, "value=" + value);
                    	 dbReader.close();
                    	 break;
					}
                }
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Exception  config parser " + e);
            } catch (IOException e) {
                Log.e(TAG, "Exception in font config parser " + e);
            } 
        	return value;
    }


//Gionee <GN_BSP_MMI><lifeilong><20161202> modify for ID37777 begin

public static boolean writeNodeState(Context context, String nodeType, int value) {
	Object pm = (Object) (context.getSystemService("amigoserver"));
	try {
		Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
		Method method = cls.getMethod("SetNodeState", int.class, int.class);
		Field f = cls.getField(nodeType);
		method.invoke(pm, f.get(null), value);
		Log.i(TAG,"writeGestureNodeValue "+nodeType+" "+f.get(null)+":"+value);
		return true;
	} catch (Exception e) {
		Log.e(TAG, "Exception :" + e);
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
		Log.i(TAG,"getNodeValue "+nodeType+" "+f.get(null)+":"+value);
		return value;
	} catch (Exception e) {
		Log.e(TAG, "Exception :" + e);
	}
	return -1;
}

//Gionee <GN_BSP_MMI><lifeilong><20161202> modify for ID37777 end



}
