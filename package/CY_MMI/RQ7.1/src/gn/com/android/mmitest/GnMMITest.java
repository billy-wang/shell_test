package gn.com.android.mmitest;

import java.io.IOException;

import android.content.BroadcastReceiver;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import amigo.app.AmigoProgressDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.location.LocationManager;
import android.view.KeyEvent;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.app.StatusBarManager;
import gn.com.android.mmitest.item.RpmbKeyTest;
import java.util.Locale;
import java.util.ArrayList;

import android.app.StatusBarManager;
//Gionee zhangke 20160627 add for CR01617603 start
import gn.com.android.mmitest.item.FeatureOption;
//Gionee zhangke 20160627 add for CR01617603 end
import android.app.ActivityManager;
import android.content.ComponentName;
//Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 begin
import gn.com.android.mmitest.item.GnReflectionMethods;
import android.content.ContentResolver;
//Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 end
//Gionee <GN_BSP_MMI><lifeilong><20161216> modify for ID 47100 begin
import android.media.AudioManager;
//Gionee <GN_BSP_MMI><lifeilong><20161216> modify for ID 47100 end
import android.content.res.Configuration;
import android.util.DisplayMetrics;
//Gionee <GN_BSP_MMI> <lifeilong> <20171024> modify for ID 245610 begin
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
//Gionee <GN_BSP_MMI> <lifeilong> <20171024> modify for ID 245610 end

    public class GnMMITest extends Activity implements OnItemClickListener {
        /** Called when the activity is first created. */

        private AlertDialog.Builder mBuilder;
        public static Handler mSetSNHandler;
        public HandlerThread mSetSNHandlerThread;
        static String TAG = "GnMMITest";
        SharedPreferences.Editor mSNEditor;
        WindowManager.LayoutParams mWL;
        private PowerManager mPM;
        private int clickCount;
        private StatusBarManager mStatusBarManager;
        private Context mContext;
        public static final int backMask = StatusBarManager.DISABLE_EXPAND;
        private static final String NV_BACKUP_END = "android.intent.action.NV_BACKUP_END";
        //Gionee <GN_BSP_MMI><lifeilong><20161202> modify for ID37777 begin
        private static final String CHARGING_SWITCH_ON = "NODE_TYPE_BATTERY_MMI_STATUS";
        public static final String NODE_TYPE_POWER_SUPPLY_BATTERY_MMI_STATUS = "/sys/devices/soc/qpnp-smbcharger-16/power_supply/battery/mmi_status";
        //Gionee <GN_BSP_MMI><lifeilong><20161202> modify for ID37777 end
        // Gionee zhangxiaowei 20131117 add for CR00952851 start
        private boolean acceleRometer = false;
        private boolean macceleRometerStatus = false;
        private boolean sound = false;
        private boolean soundStatus = false;
        // Gionee zhangxiaowei 20131117 add for CR00952851 end
        private static final int WAIT_DLG = 1;
        //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 begin
        private final static String AMIGO_SETTING_CC_SWITCH = "control_center_switch";
        private final static int LOCK_CONTROL_CENTER = 0;
        private final static int UNLOCK_CONTOL_CENTER = 1;
        //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 end
        //Gionee <GN_BSP_MMI><lifeilong><20161216> modify for ID 47100 begin
        private int ringMode;
        private AudioManager am;
        //Gionee <GN_BSP_MMI><lifeilong><20161216> modify for ID 47100 end
        private Locale oldLocale = null;
        //Gionee <GN_BSP_MMI> <lifeilong> <20171024> modify for ID 245610 begin
        private Object manager;
        private Constructor constructor;
        private Object iFAAManager;
        private byte[] getReadBytes;
        private boolean deleteIfaaKey = false;
        private Object a;// = getIfaaManager();
        private Method processMethod;// = getProcessMethod();
        //Gionee <GN_BSP_MMI> <lifeilong> <20171024> modify for ID 245610 end
        private Button quitBtn;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_list);
        //Gionee <GN_BSP_MMI> <lifeilong> <20171027> modify for ID 247559 begin
        ConfigLanguage(getIntent());
        a = getIfaaManager();
        processMethod = getProcessMethod();
        //Gionee <GN_BSP_MMI> <lifeilong> <20171027> modify for ID 247559 end
        Log.e(TAG, "GnMMITest onCreate1111");
        // Gionee zhangxiaowei 20131117 add for CR00952851 start
        updateSettings();
        if (acceleRometer == true) {
            Settings.System.putInt(this.getContentResolver(),
            Settings.System.ACCELEROMETER_ROTATION, 0);
            macceleRometerStatus = true;
        }
        if (sound == true) {
            Settings.System.putInt(this.getContentResolver(),
            Settings.System.SOUND_EFFECTS_ENABLED, 0);
            soundStatus = true;
        }
        // Gionee zhangxiaowei 20131117 add for CR01723140 end
        //Gionee zhangke 20160627 add for CR01617603 start
        FeatureOption.initMmiXml();
        //Gionee zhangke 20160627 add for CR01617603 end
        startQcomLog();
        ListView lv = (ListView) findViewById(R.id.main_listview);
        quitBtn = (Button) findViewById(R.id.quit_btn);
        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Gionee <GN_BSP_MMI><lifeilong><20161202> modify for ID37777 begin
                TestUtils.writeNodeState(GnMMITest.this,CHARGING_SWITCH_ON,0);
                Log.e(TAG,"GnMMI -- > CHARGING_SWITCH_ON -->  0 " );
                //Gionee <GN_BSP_MMI> <lifeilong> <20170427> modidy for ID 126356 begin
                TestUtils.writeNodeState(GnMMITest.this,NODE_TYPE_POWER_SUPPLY_BATTERY_MMI_STATUS,0);
                Log.e(TAG,"GnMMI -- > ODE_TYPE_POWER_SUPPLY_BATTERY_MMI_STATUS -->  0 " );
                //Gionee <GN_BSP_MMI> <lifeilong> <20170427> modidy for ID 126356 end
                //Gionee <GN_BSP_MMI><lifeilong><20161202> modify for ID37777 end
                //Gionee <GN_BSP_MMI><zhangke><20161106> modify for ID19681 begin
                releaseMmi();
                finish();
                //Gionee <GN_BSP_MMI><zhangke><20161106> modify for ID19681 end
            }
        });

        TestUtils.setAppContext(GnMMITest.this);
        mSNEditor = TestUtils.getSNSharedPreferencesEdit(this);

        // Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin

        String[] it = this.getResources().getStringArray(
        R.array.test_project_item);
        if (!FeatureOption.GN_RW_GN_MMI_AUTOTEST2_SUPPORT) {
            List<String> t = new ArrayList(Arrays.<String> asList(it));
            t.remove(4);
            it = t.toArray(new String[1]);
        }
        //Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 begin
        if (!FeatureOption.GN_RW_GN_MMI_RPMB_SUPPORT) {
            List<String> t = new ArrayList(Arrays.<String> asList(it));
            t.remove(5);
            it = t.toArray(new String[1]);
        }
        //Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 end
        //Gionee <GN_BSP_MMI><lifeilong><20161216> modify for ID 47100 begin
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ringMode = am.getRingerMode();
        Log.e(TAG, "ringMode = " + ringMode);
        mHandler.sendEmptyMessageDelayed(1,1500);
        //am.setRingerMode(AudioManager.RINGER_MODE_SILENT);//0 Ringer mode that will be silent and will not vibrate
        int ringMode1 = am.getRingerMode();
        //Gionee <GN_BSP_MMI><lifeilong><20161216> modify for ID 47100 end

        lv.setAdapter(new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, it));
        // Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end
        lv.setOnItemClickListener(this);
        TestUtils.configTestItemArrays(this);
        IntentFilter filter_1 = new IntentFilter();
        filter_1.addAction(NV_BACKUP_END);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e(TAG, "in onReceive 1 \n");
                if (NV_BACKUP_END.equals(intent.getAction())) {
                    Log.e(TAG, "send android.intent.action.MASTER_CLEAR");
                    Intent intent1 = new Intent(Intent.ACTION_MASTER_CLEAR);
                    intent1.putExtra("eraseInternalData", false);
                    context.sendBroadcast(intent1);
                }
            }
        }, filter_1);
        TestUtils.writeNodeState(GnMMITest.this,CHARGING_SWITCH_ON,1);
        Log.e(TAG,"GN MMI -- > CHARGING_SWITCH_ON -- > 1 " );
        //Gionee <GN_BSP_MMI> <lifeilong> <20170427> modidy for ID 126356 begin
        TestUtils.writeNodeState(GnMMITest.this,NODE_TYPE_POWER_SUPPLY_BATTERY_MMI_STATUS,1);
        Log.e(TAG,"GN MMI -- > NODE_TYPE_POWER_SUPPLY_BATTERY_MMI_STATUS -- > 1 " ); 
        //Gionee <GN_BSP_MMI> <lifeilong> <20170427> modidy for ID 126356 end
        ContentResolver resolver = getContentResolver();
        //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 begin
        try{
            GnReflectionMethods gnMethod = new GnReflectionMethods(
                "amigo.provider.AmigoSettings",
                "putInt", new Class[]{ContentResolver.class, String.class,int.class}, 
                new Object[]{resolver, AMIGO_SETTING_CC_SWITCH, LOCK_CONTROL_CENTER});
            gnMethod.getInvokeResult1(this);
            Log.i(TAG, "AmigoSettings putInt control_center_switch 0");
        }catch(Exception e){
            Log.e(TAG, "Exception = "+e.getMessage());
        }
        //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 end
    }

    @Override
    public void onResume() {
        super.onResume();
        TestUtils.setWindowFlags(this);
        Intent intent = new Intent(GnMMITest.this,
        gn.com.android.mmitest.AdjvService.class);
        quitBtn.setText(getResources().getString(R.string.quit));
        startService(intent);
        //Gionee zhangke 20160304 add for CR01646450 start
        Intent fingerIntent = new Intent("com.fingerprints.service.FingerprintService");
        ComponentName component = new ComponentName("com.fingerprints.serviceext", "com.fingerprints.service.FingerprintService"); 
        fingerIntent.setComponent(component);
        startService(fingerIntent);
        //Gionee zhangke 20160304 add for CR01646450 end

        SystemProperties.set("persist.radio.dispatchAllKey", "true");
        Log.e(TAG, " GnMMITest onResume");
        Log.e(TAG," persist.radio.dispatchAllKey = " + SystemProperties.get("persist.radio.dispatchAllKey","false"));
        //Gionee zhangke 20160421 add for CR01671288 start
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        try{
            activityManager.forceStopPackage("com.android.camera");
            activityManager.forceStopPackage("jlzn.com.android.compass");
            activityManager.forceStopPackage("com.caf.fmradio");
        }catch(Exception e){
            Log.i(TAG, "forceStopPackage Exception="+e.getMessage());
        }
        //Gionee zhangke 20160421 add for CR01671288 end
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, " GnMMITest onStop");
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setTitle(R.string.master_clear_title);
        switch (id){
            case 0 ://\u8be5\u7248\u672c\u4e0d\u652f\u6301\u8be5\u529f\u80fd
                builder.setMessage(R.string.not_support).setNegativeButton(R.string.factory_enter,null);
                dialog = builder.create();
                break;
            case 2:
                checkIfaaState();
                builder.setTitle(R.string.master_clear_title);
                builder.setMessage(R.string.master_clear_final_desc);
                builder.setPositiveButton(android.R.string.ok,
                    new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int which) {
                            // TODO Auto-generated method stub
                            //Gionee <GN_BSP_MMI> <lifeilong> <20171024> modify for ID 245610 begin
                            boolean removeResult = false;
                            if(SystemProperties.get("ro.gn.oversea.product").equals("yes")){
                                Log.d(TAG,"oversea !  skip remove IFAAkey  ===> reFactoryThread ");
                                removeResult = true;
                            } else {
                                if(deleteIfaaKey){
                                    removeResult = removeIfaaKey(GnMMITest.this);
                                }else {
                                    removeResult = true;
                                }
                            }
                            Log.d(TAG," removeResult = " + removeResult);
                            if(removeResult){
                                Log.e(TAG,"start EraseSD-->delete file");
                                showDialog(WAIT_DLG);
                                new reFactoryThread().start();
                                Log.e(TAG, "EraseSD end is ok");    
                            }else {
                                showDialog(3);
                            }
                            //Gionee <GN_BSP_MMI> <lifeilong> <20171024> modify for ID 245610 end
                        }
                }).setNegativeButton(android.R.string.cancel, null).setCancelable(false);
                dialog = builder.create();
                break;
            case 3:
                builder.setTitle(R.string.master_clear_title)
                    .setMessage(getString(R.string.ifaa_key_failed))
                    .setNegativeButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which){

                    }
                }).setCancelable(false);
                dialog = builder.create();
                break;
            case 1 :
                ProgressDialog mpDialog = new ProgressDialog(GnMMITest.this);
                mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mpDialog.setTitle("提示");
                mpDialog.setMessage("正在删除文件");
                return mpDialog ;
            }
            //Gionee <GN_BSP_MMI> <lifeilong> <20170909> modify for ID 208130 begin
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            dialog.getWindow().getDecorView().setSystemUiVisibility(
            getWindow().getDecorView().getSystemUiVisibility());
            //Gionee <GN_BSP_MMI> <lifeilong> <20170909> modify for ID 208130 end
            return dialog;
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20171013> modify for ID 224893 begin
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0 :
                    removeDialog(WAIT_DLG);
                    break;
                case 1 :
                    Log.d(TAG,"setRingerMode == > RINGER_MODE_SILENT ");
                    am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    break;
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20171013> modify for ID 224893 end
    // Gionee zhangxiaowei 20131117 add for CR00952851 start
    public void updateSettings() {
        acceleRometer = isacceleRometerOn();
        sound = isSoundOn();
    }

    public boolean isacceleRometerOn() {
        boolean result = false;
        result = Settings.System.getInt(this.getContentResolver(),
        Settings.System.ACCELEROMETER_ROTATION, 0) != 0;
        return result;
    }

    public boolean isSoundOn() {
        boolean result1 = false;
        result1 = Settings.System.getInt(this.getContentResolver(),
        Settings.System.SOUND_EFFECTS_ENABLED, 0) != 0;
        return result1;
    }

    // Gionee zhangxiaowei 20131117 add for CR00952851 end

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		switch (position) {
		case 0: {
			try {
				Settings.Secure.setLocationProviderEnabled(
						getContentResolver(), LocationManager.GPS_PROVIDER,
						true);
				TestUtils.openBtAndWifi(GnMMITest.this);
				TestUtils.mIsAutoMode = true;
				TestUtils.mIsAutoMode_2 = false;
				TestUtils.mIsAutoMode_3 = false;
				mSNEditor.clear();
				mSNEditor.putBoolean("mIsAutoMode", true);
				mSNEditor.commit();
				startActivity(new Intent(this,
						Class.forName("gn.com.android.mmitest.item."
								+ TestUtils.getAutoItemKeys(this).get(0))));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				TestUtils.mIsAutoMode = false;
				e.printStackTrace();
			}
			break;
		}

		case 1: {
			Settings.Secure.setLocationProviderEnabled(getContentResolver(),
					LocationManager.GPS_PROVIDER, true);
			TestUtils.openBtAndWifi(GnMMITest.this);
			TestUtils.mIsAutoMode = false;
			TestUtils.mIsAutoMode_2 = false;
			TestUtils.mIsAutoMode_3 = false;
			mSNEditor.clear();
			mSNEditor.commit();
			Log.e("lich", TAG + "145");
			startActivity(new Intent(this, SingleTestGridView.class));
			break;
		}

		case 2: {
			TestUtils.mIsAutoMode = false;
			TestUtils.mIsAutoMode_2 = false;
			TestUtils.mIsAutoMode_3 = false;
			mSNEditor.clear();
			mSNEditor.commit();
			Log.e("lich", TAG + "152");
			startActivity(new Intent(this, TestResult.class));
			break;
		}

		case 3: {
                        if(FeatureOption.GN_RW_GN_MMI_ZF_SUPPORT){
                            showDialog(0);
                            return;
                        } else  {
                            showDialog(2);
                        }
			break;
		}
		// Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin
		case 4: {
			try {
				TestUtils.openBtAndWifi(GnMMITest.this);
				TestUtils.mIsAutoMode_2 = true;
				TestUtils.mIsAutoMode_3 = false;
				TestUtils.mIsAutoMode = false;
				mSNEditor.clear();
				mSNEditor.putBoolean("mIsAutoMode", true);
				mSNEditor.commit();
				startActivity(new Intent(this,
						Class.forName("gn.com.android.mmitest.item."
								+ TestUtils.getAutoItemKeys_2(this).get(0))));
				finish();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				TestUtils.mIsAutoMode_2 = false;
			}
			break;
		}
		//Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 begin
		case 5: {
                        startActivity(new Intent(this,RpmbKeyTest.class));
			break;
		}
		//Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 end

		// Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return true;
	}

	private static String[] keepArray;
	static {
		if (true == SystemProperties.get("ro.gn.oversea.product").equals("yes")) {
			// GIONEE lijinfang 2012-11-21 modify for CR00734894 start
			if (true == SystemProperties.get("ro.gn.oversea.custom").equals(
					"AFRICA_GIONEE")) {
				keepArray = new String[] { "mapbar", ".gn_apps.zip", "pctool",
						"Music", "APK", "Free games" };
				// GIONEE lijinfang 2012-11-21 modify for CR00734894 end
			} else {
				keepArray = new String[] { "mapbar", ".gn_apps.zip", "pctool" };
			}
		} else {
			keepArray = new String[] { "mapbar", "music", "video", "截屏", "主题",
					".gn_apps.zip", "pctool", "音乐", "随变","gn_resources","Amigo"};//8609kk apk 删除视频
		}
	}

	private static List<String> keepList = Arrays.asList(keepArray);
	private static String SDPATH = null;

	private static void EraseSD() {
		// Gionee xiaolin 20120620 modify for CR00626921 start
		SDPATH = "/mnt/sdcard";
		File sd = new File(SDPATH);
		if (sd.canWrite())
			dFile(new File(SDPATH));

		SDPATH = "/mnt/sdcard2";
		sd = new File(SDPATH);
		if (sd.canWrite())
			dFile(new File(SDPATH));
		// Gionee xiaolin 20120620 modify for CR00626921 end
	}

	private static void dFile(File file) {
		for (String item : keepList) {
			if ((SDPATH + "/" + item).equalsIgnoreCase(file.toString()))
				return;
		}

		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
				return;
			} else if (file.isDirectory()) {
				Log.e(TAG, "dir :" + file.toString());
				File files[] = file.listFiles();
				if (files == null) {
					Log.i(TAG, file + " listFiles()" + " return null");
					return;
				}
				for (int i = 0; i < files.length; i++) {
					dFile(files[i]);
				}
			}

			if (!SDPATH.equals(file.toString())) {
				file.delete();
			}

		} else {
			Log.e(TAG, "要删除的文件不存在!");
		}
	}

    /**
     * Start MTKLog
     */
    private void startQcomLog() {
        SystemProperties.set("debug.sys.logger.running", "1");
    }
    
    /**
     * Stop MTKLog
     */
    private void stopQcomLog() {
        SystemProperties.set("debug.sys.logger.running", "0");
    }   


    private void ConfigLanguage(Intent intent) {
        if (intent == null) {
            return;
        }
        boolean isForSale = intent.getBooleanExtra("forSale", false);
        TestUtils.mIsForSale = isForSale;
        Log.e(TAG, "sales:forSale:" + isForSale);

        oldLocale = Locale.ENGLISH;//SIMPLIFIED_CHINESE ENGLISH
        Configuration config = getResources().getConfiguration();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (!isForSale) {
            config.locale = Locale.SIMPLIFIED_CHINESE;//
        } else {
            config.locale = oldLocale;
        }
        Log.e(TAG, "config.locale:" + config.locale);
        getResources().updateConfiguration(config, metrics);
    }

    //Gionee <GN_BSP_MMI> <lifeilong> <20171024> modify for ID 245610 begin
    private boolean removeIfaaKey(Context context){
        boolean removeFlag = false;
        try{
            byte[] removeBytes = new byte[4096];
            removeBytes[4] = 0x09;
            removeBytes[5] = 0x00;
            removeBytes[6] = 0x60;
            removeBytes[7] = 0x00;
            getReadBytes = (byte[]) processMethod.invoke(a,context,removeBytes);
            if(getReadBytes != null){
                Log.i(TAG, "readIfaaKey = "+getReadBytes.toString().trim());
                if(getReadBytes[0] == 0){
                    Log.d(TAG,"----------getReadBytes[0] == 0x00-");
                    removeFlag = true;
                }else{
                    removeFlag = false;
                }
            }else{
                Log.i(TAG, "getReadBytes is null");
                removeFlag = false;
            }
        } catch (IllegalAccessException e){
            Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (InvocationTargetException e){
            Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        }

        return removeFlag;
    }

    private void checkIfaaState(){
        byte[] readBytes = new byte[]{0,0,0,0, 0x07,0x00,0x60,0x00};
        try{
            Log.i(TAG,"readIfaaKey"); //(byte[]) processMethod.invoke(a,context,removeBytes);
            getReadBytes = (byte[]) processMethod.invoke(a,GnMMITest.this,readBytes);
            Log.i(TAG, "readIfaaKey ====getReadBytes[0] "+getReadBytes[0]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[1] "+getReadBytes[1]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[2] "+getReadBytes[2]);
            Log.i(TAG, "readIfaaKey ====getReadBytes[3] "+getReadBytes[3]);
            if(getReadBytes != null){
                Log.i(TAG, "readIfaaKey = "+getReadBytes.toString().trim());
                if(getReadBytes[0] == 0){
                    Log.d(TAG,"----------getReadBytes[0] == 0x00-");
                    deleteIfaaKey = true;
                }else{
                    deleteIfaaKey = false;
                }
            }else{
                Log.i(TAG, "getReadBytes is null");
                deleteIfaaKey = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private Object getIfaaManager(){
        try {
            iFAAManager = null ;
            Class clazz = Class.forName("org.ifaa.android.manager.IFAAManagerFactory");
            Log.e(TAG," oncreate == clazz == " + clazz );
            Constructor[] constructors = clazz.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                constructor = constructors[i];
            }
            manager = constructor.newInstance();
            Method[] methods = clazz.getMethods();
            Method method = null ;
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("getIFAAManager")){
                    method = methods[i];
                }
            }
            iFAAManager = method.invoke(manager,GnMMITest.this,0x01);
            Log.d(TAG,"manager = " + manager + "  , method =  "  + method + "  , iFAAManager = " + iFAAManager);
        } catch (ClassNotFoundException e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
        } catch (Exception e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
        }
        return iFAAManager;
    }

    private Method getProcessMethod(){
        Method method = null ;
        try {
            Class clazz = Class.forName("org.ifaa.android.manager.IFAAManager");
            Log.e(TAG," oncreate == clazz == " + clazz );
            Method[] methods = clazz.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Log.d(TAG," method = " + methods[i]);
                if(methods[i].getName().equals("processCmd")){
                    method = methods[i];
                }//processCmd
            }
        } catch (ClassNotFoundException e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
        } catch (Exception e) {
                Log.v(TAG, Log.getStackTraceString(e));
                e.printStackTrace();
        }
        return method;
    }

    //Gionee <GN_BSP_MMI> <lifeilong> <20171024> modify for ID 245610 end


    //Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 begin
    private void releaseMmi(){
		// Gionee zhangxiaowei 20131117 add for CR00952851 start
		if (macceleRometerStatus) {
			Settings.System.putInt(getContentResolver(),
					Settings.System.ACCELEROMETER_ROTATION, 1);
		
		}
		if (soundStatus) {
			Settings.System.putInt(getContentResolver(),
					Settings.System.SOUND_EFFECTS_ENABLED, 1);
		
		}
		// Gionee zhangxiaowei 20131117 add for CR00952851 end
		
		// mPM.dispatchAllKey(false);
		// MmiManager mMmiManager =
		// (MmiManager)GnMMITest.this.getSystemService("mmi");
		// mMmiManager.dispatchAllKey(false);
		SystemProperties.set("persist.radio.dispatchAllKey", "false");
		stopQcomLog();
		Log.e(TAG,
				" persist.radio.dispatchAllKey = "
						+ SystemProperties
								.get("persist.radio.dispatchAllKey",
										"false"));
		//Gionee <GN_BSP_GNMMI><lifeilong><20161121> modify for 27985 begin
		StatusBarManager mStatusBarManager = (StatusBarManager) GnMMITest.this.getSystemService("statusbar");
		mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
		//Gionee <GN_BSP_GNMMI><lifeilong><20161121> modify for 27985 end
		Log.e(TAG,"statusbar disable_none");
		Intent intent1 = new Intent(GnMMITest.this,
				gn.com.android.mmitest.AdjvService.class);
		stopService(intent1);
        //Gionee <GN_BSP_MMI><lifeilong><20161213> add for ID 45891 begin
        ContentResolver resolver = getContentResolver();
        try{
            GnReflectionMethods gnMethod = new GnReflectionMethods(
                "amigo.provider.AmigoSettings",
                "putInt", new Class[]{ContentResolver.class, String.class,int.class}, 
                new Object[]{resolver, AMIGO_SETTING_CC_SWITCH, UNLOCK_CONTOL_CENTER});
    
            gnMethod.getInvokeResult1(GnMMITest.this);
            Log.i(TAG, "AmigoSettings putInt control_center_switch 1");
        }catch(Exception e){
            Log.e(TAG, "Exception = "+e.getMessage());
        }
        //Gionee <GN_BSP_MMI><lifeilong><20161216> modify for ID 47100 begin
        am.setRingerMode(ringMode);
        int ringMode2 = am.getRingerMode();
        TestUtils.closeBtAndWifi(this);
        Log.e(TAG, "releaseMmi: mmi test is fininsh setRingerMode = " + ringMode2);
        //Gionee <GN_BSP_MMI><lifeilong><20161216> modify for ID 47100 end
    }

    //Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 end

    class reFactoryThread extends Thread {
         public void run() {
             TestUtils.writeNodeState(GnMMITest.this,CHARGING_SWITCH_ON,0);
             Log.e(TAG,"case 3 --> CHARGING_SWITCH_ON --> 0");
             stopQcomLog();
             ContentResolver resolver = getContentResolver();
             try{
                 GnReflectionMethods gnMethod = new GnReflectionMethods(
                 "amigo.provider.AmigoSettings",
                 "putInt", new Class[]{ContentResolver.class, String.class,int.class},
                 new Object[]{resolver, AMIGO_SETTING_CC_SWITCH, UNLOCK_CONTOL_CENTER});
                 gnMethod.getInvokeResult1(GnMMITest.this);
                 Log.i(TAG, "AmigoSettings putInt control_center_switch 1");
             }catch(Exception e){
                 Log.e(TAG, "Exception = "+e.getMessage());
             }
             EraseSD();
             Intent intent = new Intent(GnMMITest.this,gn.com.android.mmitest.item.NvService.class);
             startService(intent);
             mHandler.sendEmptyMessage(0);
             Log.e(TAG, "run end");
            }
         }
    }
