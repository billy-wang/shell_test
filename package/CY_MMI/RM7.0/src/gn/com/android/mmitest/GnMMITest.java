
package gn.com.android.mmitest;


import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncResult;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.IWindowManager;
import android.view.Surface;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.view.KeyEvent;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
//Gionee zhangke 20160304 add for CR01646450 start
import android.content.ComponentName;
//Gionee zhangke 20160304 add for CR01646450 end

//Platform
//import amigo.provider.AmigoSettings;
//import amigo.app.AmigoProgressDialog;

import android.app.Dialog;

import java.util.ArrayList;

import android.location.LocationManager;
//Gionee zhangke 20151019 add for CR01571097 start
import gn.com.android.mmitest.item.FeatureOption;
//Gionee zhangke 20151019 add for CR01571097 end
//Gionee zhangke 20160418 add for CR01678675 start
import android.app.ActivityManager;
//Gionee zhangke 20160418 add for CR01678675 end
//Gionee zhangke 20160913 add for CR01760585 start
import gn.com.android.mmitest.item.GnReflectionMethods;
import android.content.ContentResolver;
//Gionee zhangke 20160913 add for CR01760585 end

//Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import gn.com.android.mmitest.item.CheckEfuse;
import android.widget.Toast;
//Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end
import android.app.StatusBarManager;
import java.io.FileOutputStream;
import android.nfc.NfcAdapter;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import java.util.Locale;

public class GnMMITest extends Activity implements OnItemClickListener {
    /**
     * Called when the activity is first created.
     */

    private AlertDialog.Builder mBuilder;
    public static final int EVENT_RESPONSE_SN_WRITE = 1;
    public static final int EVENT_SET_SN = 2;
    private Phone mPhone = null;
    private static TelephonyManager mTeleMgr;
    public static Handler mSetSNHandler;
    public HandlerThread mSetSNHandlerThread;
    static String TAG = "GnMMITest";
    SharedPreferences.Editor mSNEditor;
    WindowManager.LayoutParams mWL;
    private PowerManager mPM;
    private int clickCount;
    private static final int WAIT_DLG = 1;
    private Context mContext;
    private static final String NV_BACKUP_END = "android.intent.action.NV_BACKUP_END";
    private boolean buttonLight = false;
    private boolean buttonLightStatus = false;
    private int ringMode;
    private AudioManager am;
    //Gionee zhangke 20151130 add for CR01599820 start
    private final static String KILL_MMI_BROADCAST = "gn.kill.mmi";
    //Gionee zhangke 20151130 add for CR01599820 end
    //Gionee zhangke 20160913 add for CR01760585 start
    private final static String AMIGO_SETTING_CC_SWITCH = "control_center_switch";
    private final static int LOCK_CONTROL_CENTER = 0;
    private final static int UNLOCK_CONTOL_CENTER = 1;
    //Gionee zhangke 20160913 add for CR01760585 end
    
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
    private static final int MMI_PASS = 0x50;
    private static final int MMI_FIAL = 0x46;
    private static final int MMI_WCHAT_SOTER_TAG = 30;
    private static final int MMI_IFAA_KEY_TAG = 29;
    private static final int MMI_ALL_TAG = 54;
    private static final int MMI_WIFI_TAG = 52;
    public static final int SN_LENGTH = 64;
    private Intent lastBatteryData;
    private byte[] mSnByteArray = new byte[SN_LENGTH];
    private int level;
    private int plugged;
    private String isEfused = "/proc/sbcflag";
    private String efuseEnd = "/proc/efuse_blower";
    private String isSupportEfuse = null;
    private String isEfusedEnd = null;
    private boolean supportEfuse = false;
    private boolean alreadyEfuse = false;
    private TestResult testResult;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 begin
    private String INIT_RPMBK_SUCCESS_PATH = "/data/thh/tee_00/init_rpmbk_SUCCESS";
    private String INIT_RPMBK_NOTKEY__FAILED_PATH = "/data/thh/tee_00/init_rpmbk_NOTKEY_FAILED";
    private String INIT_RPMBK_CALL_FAILED_PATH = "/data/thh/tee_00/init_rpmbk_CALL_FAILED";
    //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 end
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end
    //Gionee zhangke 20151130 modify for CR01599820 start
    private byte[] result;
    private TestResult mTestResult;
    public static final int RPMB_LENGTH = 505;
    private static final int RPMB_TAG = 503;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170808> modify for ID 183052 begin
    private byte[] mRpmbByteArray = new byte[TestResult.SN_LENGTH];
    //Gionee <GN_BSP_MMI> <lifeilong> <20170808> modify for ID 183052 end
    private boolean succFlag;
    private boolean notkeyFlag;
    private boolean callFlag;
    private String rpmbKey = "/data/misc/gionee/init_rpmb.sh";///data/misc/gionee  ///system/etc/init_rpmb
    private File rpmb_record = new File(rpmbKey);
    public static final String SUSPEND_BUTTON = "suspend_button";

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mReceiver:action="+intent.getAction());
            if (NV_BACKUP_END.equals(intent.getAction())) {
                Log.e(TAG, "send android.intent.action.MASTER_CLEAR");
                Intent intent1 = new Intent(Intent.ACTION_MASTER_CLEAR);
                intent1.putExtra("eraseInternalData", false);
                context.sendBroadcast(intent1);
                stopAdjvService();
                finish();
            }else if (KILL_MMI_BROADCAST.equals(intent.getAction())) {
                releaseMmi();
                System.exit(0);
            }
        }
    };
    //Gionee zhangke 20151130 modify for CR01599820 end

    WindowManager.LayoutParams mlp;
    private boolean mIsScreenBright = false;
    private boolean mIsScreenBrightStatus = false;
    private ContentResolver resolver;

    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 begin
    public static boolean mNfcOn = false;
    public static boolean mNfcOff = false;
    public static boolean mNfcTurn = false;
    private NfcAdapter mNfcAdapter;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 end
    private Locale oldLocale = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.sContinue = true;
        setContentView(R.layout.main_list);
        ConfigLanguage(getIntent());
        //Gionee <GN_BSP_MMI> <lifeilong> <20170623> modify for ID 161269 begin
        resolver = getContentResolver();
        mTestResult = new TestResult();
        registerReceiver(mNfcReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        try{
            //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 begin
            if(FeatureOption.GN_RW_GN_MMI_RPMB_FLAG_SUPPORT){
                //checkRpmbFlag();
            }
            //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 begin
            GnReflectionMethods gnMethod = new GnReflectionMethods(
                "amigo.provider.AmigoSettings",
                "putInt", new Class[]{ContentResolver.class, String.class,int.class},
                new Object[]{resolver, AMIGO_SETTING_CC_SWITCH, LOCK_CONTROL_CENTER});
                gnMethod.getInvokeResult1(this);

            //Gionee <GN_BSP_MMI> <lifeilong> <20170904> add for ID 203491 begin
            GnReflectionMethods buttonMethod = new GnReflectionMethods(
                "amigo.provider.AmigoSettings",
                "putInt", new Class[]{ContentResolver.class, String.class,int.class},
                new Object[]{resolver, SUSPEND_BUTTON, LOCK_CONTROL_CENTER});
                buttonMethod.getInvokeResult1(this);
                openDump();
            //Gionee <GN_BSP_MMI> <lifeilong> <20170904> add for ID 203491 end
            Log.i(TAG, "AmigoSettings putInt control_center_switch 0");
        }catch(Exception e){
            Log.e(TAG, "Exception = "+e.getMessage());
        }
         //Gionee <GN_BSP_MMI> <lifeilong> <20170623> modify for ID 161269 end
        updateSettings();
        if (buttonLight == true) {
            //Platform
            //AmigoSettings.putInt(this.getContentResolver(),
            //        AmigoSettings.Button_Light_State, 0);
            
            buttonLightStatus = true;
        }
        //Gionee zhangke 20160105 add for CR01617603 start
        FeatureOption.initMmiXml();
        //Gionee zhangke 20160105 add for CR01617603 end
        registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        testResult = new TestResult();
        
        ListView lv = (ListView) findViewById(R.id.main_listview);
        Log.e(TAG, " start mmitest");
        Button quitBtn = (Button) findViewById(R.id.quit_btn);
        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Gionee zhangke 20151130 modify for CR01599820 start
                Log.i(TAG, "quit button is clicked");
                releaseMmi();
                finish();
            }
        });
        TestUtils.setAppContext(GnMMITest.this);
        mSNEditor = TestUtils.getSNSharedPreferencesEdit(this);
        //Gionee <xiaolin><2013-06-26> modify for CR00825575 start
        String[] it = this.getResources().getStringArray(R.array.test_project_item);
        //Gionee zhangke 20151019 add for CR01571097 start
        if (!FeatureOption.GN_RW_GN_MMI_AUTOTEST2_SUPPORT) {
            List<String> t = new ArrayList(Arrays.<String>asList(it));
            t.remove(4);
            it = t.toArray(new String[1]);
        }
        //Gionee zhangke 20151019 add for CR01571097 end
        //Gionee zhangke 20160310 add for CR01650400 start
        if (!FeatureOption.GN_RW_GN_MMI_RPMB_SUPPORT) {
            List<String> t = new ArrayList(Arrays.<String>asList(it));
            t.remove(5);
            it = t.toArray(new String[1]);
        }
        //Gionee zhangke 20160310 add for CR01650400 end
        
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, it));
        //Gionee <xiaolin><2013-06-26> modify for CR00825575 end
        lv.setOnItemClickListener(this);
        TestUtils.configTestItemArrays(this);

        // Gionee xiaolin 20120625 add for CR00628165 start
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //   am.setParameters("srs_cfg:trumedia_enable=0");
        ringMode = am.getRingerMode();
        Log.e(TAG, "ringMode = " + ringMode);

        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);//0 Ringer mode that will be silent and will not vibrate
        int ringMode1 = am.getRingerMode();
        Log.e(TAG, "setaudio after setRingerMode = " + ringMode1);
        //Gionee <zhangxiaowei><2013-04-11> add for CR00796293  begin
        setMtkLogSize();
        startMtkLog();

        mContext = this;
        //Gionee zhangke 20151130 add for CR01599820 start
        IntentFilter filter_1 = new IntentFilter();
        filter_1.addAction(NV_BACKUP_END);
        filter_1.addAction(KILL_MMI_BROADCAST);
        registerReceiver(mReceiver, filter_1);
        //Gionee zhangke 20151130 add for CR01599820 end
        //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
        mHandler.sendEmptyMessage(10);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end
        TestUtils.acquireWakeLock(this);
        Log.e(TAG," == TestUtils.releaseWakeLock(this) == GnMMITest --> oncreate() ");
    }
    public boolean isRespirationLampNotificationOn() {
        boolean result = false;
        result = Settings.System.getInt(this.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, 0) != 0;
        return result;
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 begin
    private void checkRpmbFlag(){
        File succ = new File(INIT_RPMBK_SUCCESS_PATH);
        File notkey = new File(INIT_RPMBK_NOTKEY__FAILED_PATH);
        File call = new File(INIT_RPMBK_CALL_FAILED_PATH);
        succFlag = succ.exists();
        notkeyFlag = notkey.exists();
        callFlag = call.exists();
        String flags = "";
        if(succFlag){
            flags = "P";//80
            Log.d(TAG, " succ.exist() ");
        } else if (notkeyFlag) {
            flags = "N";
            Log.d(TAG, " notkey.exist() ");
        } else if (callFlag){
            flags = "C";
            Log.d(TAG, " call.exist() ");
        } else {
            flags = "0";//48
            Log.d(TAG, " no file exist() !! ");
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170808> modify for ID 183052 begin
        System.arraycopy(mTestResult.getProductInfo(), 0, mRpmbByteArray, 0, TestResult.SN_LENGTH);
        mRpmbByteArray = mTestResult.getNewSN(GnMMITest.RPMB_TAG,flags, mRpmbByteArray);
        Log.e(TAG," == file oncreate  [RPMB_TAG]2 == " + mRpmbByteArray[RPMB_TAG] );
        mTestResult.writeToProductInfo(mRpmbByteArray);
        System.arraycopy(mTestResult.getProductInfo(), 0, mRpmbByteArray, 0, TestResult.SN_LENGTH);
        Log.e(TAG," ==  file oncreate  [RPMB_TAG]3 == " + mRpmbByteArray[RPMB_TAG] );
        //Gionee <GN_BSP_MMI> <lifeilong> <20170808> modify for ID 183052 end
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 end

    //Gionee <GN_BSP_MMI> <lifeilong> <20170725> modify for ID 175443 begin
    private void setKeyFile(){
        try{
            int exeReturn = -1;
            Process process = Runtime.getRuntime().exec("init_thh initrpmbk");
            exeReturn = process.waitFor();
            Log.d(TAG,"exeReturn = " + exeReturn);
            //Gionee <GN_BSP_MMI> <lifeilong> <20170810> modify for ID 185110 begin
            checkRpmbFlag();
            showDialog(5);
            //Gionee <GN_BSP_MMI> <lifeilong> <20170810> modify for ID 185110 end
        } catch (Exception e) {
            Log.e(TAG, " e =  " + e.getMessage());
            e.printStackTrace();
        }
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170725> modify for ID 175443 end

    @Override
    public void onResume() {
        super.onResume();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170904> add for ID 203491 begin
        Intent stopIntent = new Intent("com.gionee.floatingtouch.action.STOP_SERVICE");
        stopIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        sendBroadcast(stopIntent);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170904> add for ID 203491 end
        //Gionee <GN_BSP_MMI> <lifeilong> <20170829> modify for ID 199526 begin
        TestUtils.setWindowFlags(this);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170829> modify for ID 199526 end
        Intent intent = new Intent(GnMMITest.this,
                gn.com.android.mmitest.AdjvService.class);
        startService(intent);
        //Gionee zhangke 20160304 add for CR01646450 start
        Intent fingerIntent = new Intent("com.fingerprints.service.FingerprintService");
        ComponentName component = new ComponentName("com.fingerprints.serviceext", "com.fingerprints.service.FingerprintService"); 
        fingerIntent.setComponent(component);
        startService(fingerIntent);
        //Gionee zhangke 20160304 add for CR01646450 end
        SystemProperties.set("persist.radio.dispatchAllKey", "true");
        Log.e(TAG, " GnMMITest onResume");
        Log.e(TAG, " persist.radio.dispatchAllKey = " + SystemProperties.get("persist.radio.dispatchAllKey", "false"));
        //Gionee zhangke 20160421 add for CR01671288 start
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        try{
            //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 begin
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (mNfcAdapter == null) {
                Log.i(TAG, "mNfcAdapter == null");
                return;
            } else {
                Log.i(TAG, "mNfcAdapter != null");
            }
            if (!mNfcAdapter.isEnabled()) {
                Log.i(TAG, "mNfcAdapter != isEnable");
                mNfcAdapter.enable();
            } else {
                Log.i(TAG, "mNfcAdapter = isEnable");
            }
            //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 end
            activityManager.forceStopPackage("com.android.camera");
            activityManager.forceStopPackage("jlzn.com.android.compass");
        }catch(Exception e){
            Log.i(TAG, "forceStopPackage Exception="+e.getMessage());
        }
        //Gionee zhangke 20160421 add for CR01671288 end

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, " GnMMITest onPause11");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, " GnMMITest onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mBroadcastReceiver);
        mHandler.removeMessages(10);
        stopAdjvService();
        Log.e(TAG, " GnMMITest onDestroy");

    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
    private String getFailedMessage(){
        Log.e(TAG,"getFailedMessage  start ");
        StringBuilder failMsg = new StringBuilder();
        if (level < 25){
            Log.e(TAG,"  \u7535\u6c60\u7535\u91cf\u4e0d\u8db325%  ");
            failMsg.append("  \u7535\u6c60\u7535\u91cf\u4e0d\u8db325%  ");
        }
        if (result[MMI_ALL_TAG] != MMI_PASS){
            Log.e(TAG,"  MMI\u5168\u90e8\u6d4b\u8bd5\u6ca1\u6709\u901a\u8fc7  ");
            failMsg.append("  MMI\u5168\u90e8\u6d4b\u8bd5\u6ca1\u6709\u901a\u8fc7  ");
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20171016> add for ID 238446 begin
        if(result[MMI_WIFI_TAG] != MMI_PASS){
            Log.e(TAG,"  WIFI\u6d4b\u8bd5\u6ca1\u6709\u901a\u8fc7  ");
            failMsg.append("  WIFI\u6d4b\u8bd5\u6ca1\u6709\u901a\u8fc7  ");
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20171016> add for ID 238446 end
        if (FeatureOption.GN_RW_GN_MMI_WCHAT_SOTER_SUPPORT){
            if (result[MMI_WCHAT_SOTER_TAG] != MMI_PASS){
                Log.e(TAG,"  \u5fae\u4fe1soter\u6d4b\u8bd5\u6ca1\u6709\u901a\u8fc7  ");
                failMsg.append("  \u5fae\u4fe1soter\u6d4b\u8bd5\u6ca1\u6709\u901a\u8fc7  ");
            }
        }
        if (FeatureOption.GN_RW_GN_MMI_IFAA_KEY_SUPPORT){
            if (result[MMI_IFAA_KEY_TAG] != MMI_PASS){
                Log.e(TAG,"  IFAA\u6d4b\u8bd5\u6ca1\u6709\u901a\u8fc7  ");
                failMsg.append("  IFAA\u6d4b\u8bd5\u6ca1\u6709\u901a\u8fc7  ");
            }
        }
        String result = new String(failMsg);
        Log.e(TAG,"getFailedMessage result = " + result);
        return result;

    };

    
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
              switch (msg.what) { 
              case 0:
                  break;
              case 1:
                  showDialog(1);
                  break;
              case 2:
                  removeDialog(1);
                  startActivityForResult(new Intent(GnMMITest.this,CheckEfuse.class),0);
                  break;
              case 3:
                  removeDialog(1);
                  showDialog(2);
                  break;
              case 10:
                  exBatInfo(lastBatteryData);
                  this.sendEmptyMessageDelayed(0, 2000);
                  break;
              }
        }
    };
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setTitle("\u6062\u590d\u51fa\u5382\u8bbe\u7f6e");
        switch (id){
            case 0:
                builder.setMessage("\u5c1a\u672a\u70e7\u5199efuse,\u662f\u5426\u8981\u70e7\u5199efuse ?").setPositiveButton("\u786e\u5b9a", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result = testResult.getProductInfo();
                        Log.e(TAG,"mSnByteArray[MMI_ALL_TAG] = " + result[MMI_ALL_TAG] );
                        if(level >= 25 && result[MMI_ALL_TAG] == MMI_PASS){
                            //Gionee <GN_BSP_MMI> <lifeilong> <20171016> add for ID 238446 begin
                            if(result[MMI_WIFI_TAG] == MMI_PASS){
                                //Gionee <GN_BSP_MMI> <lifeilong> <20170406> modify for ID 105838 begin
                                if(FeatureOption.GN_RW_GN_MMI_WCHAT_SOTER_SUPPORT){
                                    if(result[MMI_WCHAT_SOTER_TAG] == MMI_PASS && result[MMI_IFAA_KEY_TAG] == MMI_PASS){
                                        mHandler.sendEmptyMessage(2);
                                    }else {
                                        mHandler.sendEmptyMessage(3);
                                    }
                                }else {
                                    mHandler.sendEmptyMessage(2);
                                }
                                //Gionee <GN_BSP_MMI> <lifeilong> <20170406> modify for ID 105838 end
                            }else {
                                mHandler.sendEmptyMessage(3);
                            }
                            //Gionee <GN_BSP_MMI> <lifeilong> <20171016> add for ID 238446 end
                        }else {
                            mHandler.sendEmptyMessage(3);
                        }

                    }
                }).setNegativeButton("\u53d6\u6d88",null);
                dialog = builder.create();
                break;
            case 1:
                builder.setMessage("\u5df2\u7ecf\u70e7\u5199efuse,\u662f\u5426\u7ee7\u7eed\u6062\u590d\u51fa\u5382\u8bbe\u7f6e ?").setPositiveButton("\u786e\u5b9a", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFactoryResetTask.execute();
                    }
                }).setNegativeButton("\u53d6\u6d88",null);
                dialog = builder.create();
                break;
            case 2:
                String failedResult = getFailedMessage();
                builder.setMessage(failedResult + " , \u8bf7\u6ee1\u8db3\u6761\u4ef6\u4e4b\u540e\u518d\u8fdb\u884c\u70e7\u5199efuse ").setNegativeButton("\u786e\u8ba4",null);

                dialog = builder.create();
                break;

            case 3:
                builder.setMessage("\u662f\u5426\u6062\u590d\u51fa\u5382\u8bbe\u7f6e").setPositiveButton("\u786e\u8ba4", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFactoryResetTask.execute();
                    }
                }).setNegativeButton("\u53d6\u6d88", null);
                dialog = builder.create();
                break;
            //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 begin
            case 5:
                String str = new String();//getString(R.string.rpmb_key_has_writed) //set_rpmb_key
                Log.d(TAG, "succFlag   = " + succFlag + "  , notkeyFlag  =  " + notkeyFlag + "  ,  callFlag   = " + callFlag);
                if(succFlag){
                    str = getString(R.string.rpmbkSuccess);
                } else if (notkeyFlag) {
                    str = getString(R.string.rpmbkNotKey);
                } else if (callFlag){
                    str = getString(R.string.rpmbkCall);
                } else {
                    str = getString(R.string.rpmbkFailed);
                }
                builder.setTitle(getString(R.string.set_rpmb_key)).setMessage(str).setNegativeButton("\u786e\u8ba4", null);
                dialog = builder.create();
                break;
            //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 end
            case 6://\u8be5\u7248\u672c\u4e0d\u652f\u6301\u8be5\u529f\u80fd
                builder.setMessage("\u8be5\u7248\u672c\u4e0d\u652f\u6301\u8be5\u529f\u80fd").setNegativeButton("\u786e\u8ba4",null);
                dialog = builder.create();
                break;
            //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 begin
            case 4 :
                ProgressDialog mpDialog = new ProgressDialog(GnMMITest.this);
                mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mpDialog.setTitle("\u63d0\u793a");
                mpDialog.setMessage("\u6b63\u5728\u5220\u9664\u6587\u4ef6");
                mpDialog.setCancelable(false);
                return mpDialog;
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 begin
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
        getWindow().getDecorView().getSystemUiVisibility());
        //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 end
        return dialog;

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String result = null;
        result = data.getStringExtra("result");
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case 1:
                    Log.e(TAG,"onActivityResult = 1  go on efuse " );
                    String firstResult = getEfusedResult(efuseEnd);
                    Log.e(TAG,"firstResult = " + firstResult);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String secReslut = getEfusedResult(efuseEnd);
                    Log.e(TAG,"secReslut = " + secReslut);
                    if("1".equals(secReslut)){
                        Log.e(TAG,"secReslut = " + secReslut + "//mFactoryResetTask.execute();");
                    mFactoryResetTask.execute();
                    }else {
                        Toast.makeText(GnMMITest.this, getString(R.string.efuse_faild),Toast.LENGTH_SHORT).show();                    
                    }
                    break;
                case 2:
                    Log.e(TAG,"onActivityResult = 2  pass efuse " + "//mFactoryResetTask.execute();");
                    mFactoryResetTask.execute();
                    break;
                case 3:
                    Log.e(TAG,"onActivityResult = 3 " ) ;
                    break;
                default:
                    break;
                }
      }
      //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end


    public static String getNewSN(int position, Character value) {
        String sn = SystemProperties.get("gsm.serial");
        Log.e(TAG, "NewSN = " + sn);
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
            sb.setCharAt(position, value);
        }
        return sb.toString();
    }

    public void updateSettings() {
        buttonLight = isButtonLightOn();
        mIsScreenBright = isRespirationLampNotificationOn();
        Log.e(TAG, "buttonLight is " + buttonLight);

    }

    public boolean isButtonLightOn() {
        boolean result2 = false;
        //Platform
        //result2 = AmigoSettings.getInt(this.getContentResolver(),
          //      AmigoSettings.Button_Light_State, 0) != 0;
            
        return result2;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        switch (position) {
            case 0: {
                try {
                    Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, true);
                    TestUtils.openBtAndWifi(GnMMITest.this);
                    TestUtils.mIsAutoMode = true;
                    TestUtils.mIsAutoMode_2 = false;
                    mSNEditor.clear();
                    mSNEditor.putBoolean("mIsAutoMode", true);
                    mSNEditor.commit();
                    Log.e(TAG, "start enter atuo mmi ");
                    startActivity(new Intent(this, Class.forName("gn.com.android.mmitest.item."
                            + TestUtils.getAutoItemKeys(this).get(0))));
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.e(TAG,"ClassNotFoundException   ====>  TestUtils.mIsAutoMode = false");
                    TestUtils.mIsAutoMode = false;
                    e.printStackTrace();
                }
                break;
            }

            case 1: {
                Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, true);
                TestUtils.openBtAndWifi(GnMMITest.this);
                TestUtils.mIsAutoMode = false;
                TestUtils.mIsAutoMode_2 = false;
                mSNEditor.clear();
                mSNEditor.commit();
                Log.e(TAG, "start enter hardware mmi ");
                startActivity(new Intent(this, SingleTestGridView.class));
                break;
            }

            case 2: {
                TestUtils.mIsAutoMode = false;
                TestUtils.mIsAutoMode_2 = false;
                mSNEditor.clear();
                mSNEditor.commit();
                Log.e(TAG, "start1 enter  mmi TestResult");
                startActivity(new Intent(this, TestResult.class));
                //  finish();
                break;
            }

            case 3: {
                //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
                /*if (null == mBuilder) {
                    mBuilder = new Builder(this);
                    mBuilder.setTitle(R.string.master_clear_title);
                    mBuilder.setMessage(R.string.master_clear_final_desc);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170330> modify for ID 99593 begin
                    mBuilder.setPositiveButton("确定", new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            // Gionee xiaolin 20120619 modify for CR00625881 start
                            clickCount++;
                            if (clickCount > 1)
                                return;
                            mFactoryResetTask.execute();
                            // Gionee xiaolin 20120619 modify for CR00625881 end
                        }

                    });
                    mBuilder.setNegativeButton("取消", null);
                    //Gionee <GN_BSP_MMI> <lifeilong> <20170330> modify for ID 99593 end
                }
                mBuilder.show();*/
                Log.d(TAG,"  FeatureOption.GN_RW_GN_MMI_FACTORY_EFUSE_SUPPORT  = " + FeatureOption.GN_RW_GN_MMI_FACTORY_EFUSE_SUPPORT);
                Log.d(TAG,"  FeatureOption.GN_RW_GN_MMI_ZF_SUPPORT  = " + FeatureOption.GN_RW_GN_MMI_ZF_SUPPORT);
                if(FeatureOption.GN_RW_GN_MMI_ZF_SUPPORT){
                    showDialog(6);
                }else if(FeatureOption.GN_RW_GN_MMI_FACTORY_EFUSE_SUPPORT){ //GN_RW_GN_MMI_FACTORY_EFUSE_SUPPORT
                    showDialog(3);
                } else {
                    isSupportEfuse = getEfusedResult(isEfused);
                    Log.e(TAG,"isSupportEfuse = " + isSupportEfuse);
                    if("0".equals(isSupportEfuse)){
                       showDialog(0);
                    }else if("1".equals(isSupportEfuse)){
                       mHandler.sendEmptyMessage(1);
                    }else {
                       showDialog(3);
                    }
                }
                //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end
                break;
            }
            case 4: {
                try {
                    TestUtils.openBtAndWifi(GnMMITest.this);
                    TestUtils.mIsAutoMode_2 = true;
                    TestUtils.mIsAutoMode = false;
                    mSNEditor.clear();
                    mSNEditor.putBoolean("mIsAutoMode", true);
                    mSNEditor.commit();
                    Log.e(TAG, "start enter atuommi2 ");
                    startActivity(new Intent(this, Class.forName("gn.com.android.mmitest.item."
                            + TestUtils.getAutoItemKeys_2(this).get(0))));
                    // 	finish();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    TestUtils.mIsAutoMode_2 = false;
                }
                break;
            }
            //Gionee zhangke 20160310 add for CR01650400 start
            case 5: {
                //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 begin
                Log.d(TAG, " GN_RW_GN_MMI_RPMB_FLAG_SUPPORT ==   " + FeatureOption.GN_RW_GN_MMI_RPMB_FLAG_SUPPORT );
                if(FeatureOption.GN_RW_GN_MMI_RPMB_FLAG_SUPPORT){
                    try {
                        //Gionee <GN_BSP_MMI> <lifeilong> <20170810> modify for ID 185110 begin
                        File succ = new File(INIT_RPMBK_SUCCESS_PATH);
                        File notkey = new File(INIT_RPMBK_NOTKEY__FAILED_PATH);
                        File call = new File(INIT_RPMBK_CALL_FAILED_PATH);
                        boolean succFlag = succ.exists();
                        boolean notkeyFlag = notkey.exists();
                        boolean callFlag = call.exists();
                        if(succFlag){
                            checkRpmbFlag();
                            showDialog(5);
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(GnMMITest.this);
                            builder.setTitle(R.string.set_rpmb_key)
                                .setMessage("\u662f\u5426\u5199\u5165\u0052\u0070\u006d\u0062\u0020\u004b\u0065\u0079")
                                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    Log.i(TAG, "write rpmb key");
                                    setKeyFile();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null);
                            Dialog dialog = builder.create();
                            dialog.show();
                        }
                        Log.d(TAG, "succFlag || notkeyFlag ||callFlag   ===> true showDialog " );
                        //Gionee <GN_BSP_MMI> <lifeilong> <20170810> modify for ID 185110 end
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG,e.getMessage());
                        checkRpmbFlag();
                        showDialog(5);
                    } //Gionee <GN_BSP_MMI> <lifeilong> <20170725> modify for ID 175443 end
                //Gionee <GN_BSP_MMI> <lifeilong> <20170719> add for ID 171246 end
                } else {
                //Gionee <GN_BSP_MMI> <lifeilong> <20170808> modify for ID 183052 begin
                    System.arraycopy(mTestResult.getProductInfo(), 0, mRpmbByteArray, 0, TestResult.SN_LENGTH);
                    //snNumber[RPMB_TAG]
                    Log.e(TAG," == snNumber[RPMB_TAG]1 == " + mRpmbByteArray[RPMB_TAG] );
                    String rpmbflag = SystemProperties.get("persist.sys.rpmbflag", "0");
                    Log.e(TAG," == rpmbflag == " + rpmbflag );
                    if(rpmbflag.equals("1") || mRpmbByteArray[RPMB_TAG] == 80){
                        AlertDialog.Builder builder = new AlertDialog.Builder(GnMMITest.this);
                        builder.setTitle(R.string.set_rpmb_key)
                            .setMessage(getString(R.string.rpmb_key_has_writed))
                            .setNegativeButton(android.R.string.cancel, null);
                        Dialog dialog = builder.create();
                        dialog.show();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(GnMMITest.this);
                        builder.setTitle(R.string.set_rpmb_key)
                            .setMessage(getString(R.string.rpmb_key_note))
                            .setPositiveButton(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                Log.i(TAG, "write rpmb key");
                                Log.i(TAG, " GN_RW_GN_MMI_NEW_RPMB_SUPPORT " + FeatureOption.GN_RW_GN_MMI_NEW_RPMB_SUPPORT);
                                if (FeatureOption.GN_RW_GN_MMI_NEW_RPMB_SUPPORT == true) {
                                    mRpmbByteArray = mTestResult.getNewSN(GnMMITest.RPMB_TAG, "1", mRpmbByteArray); 
                                    Log.e(TAG," == snNumber[RPMB_TAG]2 == " + mRpmbByteArray[RPMB_TAG] );
                                    mTestResult.writeToProductInfo(mRpmbByteArray);
                                    //snNumber[RPMB_TAG]
                                    System.arraycopy(mTestResult.getProductInfo(), 0, mRpmbByteArray, 0, TestResult.SN_LENGTH);
                                    //Gionee <GN_BSP_MMI> <lifeilong> <20170808> modify for ID 183052 end
                                    Log.e(TAG," == snNumber[RPMB_TAG]3 == " + mRpmbByteArray[RPMB_TAG] );
                                } else {
                                    Log.e(TAG," == set flag == 1 ");
                                    SystemProperties.set("persist.sys.rpmbflag", "1");
                                }
                                releaseMmi();
                                Intent i = new Intent(Intent.ACTION_REBOOT);
                                i.putExtra("nowait", 1);
                                i.putExtra("interval", 1);
                                i.putExtra("window", 0);
                                mContext.sendBroadcast(i);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null);
                        Dialog dialog = builder.create();
                        dialog.show();
                    }
                }
                break;
            }
            //Gionee zhangke 20160310 add for CR01650400 end

        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    // Gionee xiaolin 20120619 modify for CR00625881 start
    private static String[] keepArray;

    static {
        if (true == SystemProperties.get("ro.gn.oversea.product").equals("yes")) {
            //GIONEE lijinfang 2012-11-21 modify for CR00734894 start
            if (true == SystemProperties.get("ro.gn.oversea.custom").equals("AFRICA_GIONEE")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Music", "APK", "Free games"};
                //GIONEE lijinfang 2012-11-21 modify for CR00734894 end
            } else {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool"};
            }
        } else {
            //keepArray = new String[]{"mapbar", "music","gn_resources", "video","gntheme", "ThemePark","主题","随变主题",".gn_apps.zip", "pctool", "音乐", "视频", "随变", "锁屏"};
            keepArray = new String[]{"Amigo", "mapbar", "music", "gn_resources", "video", "ThemePark", ".gn_apps.zip", "pctool", "音乐", "视频", "锁屏"};

        }
    }

    // Gionee liuying 20121025 modify for CR00718158 end
    // Gionee xuming 20121024 modify for CR00703768 end
    private static List<String> keepList = Arrays.asList(keepArray);
    private static String SDPATH = null;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            lastBatteryData = intent;
            exBatInfo(intent);
        }

    };

    private static void EraseSD() {
        // Gionee xiaolin 20120620 modify for CR00626921 start
        SDPATH = "/mnt/sdcard";
        File sd = new File(SDPATH);
        if (sd.canWrite())
            dFile(new File(SDPATH));

        //Gionee zhangke 20160326 modify for CR01661342 start
        SDPATH = "/mnt/m_external_sd";
        //Gionee zhangke 20160326 modify for CR01661342 end
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
                    Log.e(TAG, file + " listFiles()" + " return null");
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
            Log.e(TAG, "delete file is not exist");
        }
    }
    // Gionee xiaolin 20120619 modify for CR00625881 end

    /**
     * Use AsyncTask, other then Thread to handle erasing file action
     */
    AsyncTask<Void, Void, Void> mFactoryResetTask = new AsyncTask<Void, Void, Void>() {

        protected void onPreExecute() {
            //Gionee zhangke 20160426 modify for CR01684127 start
            showDialog(4);
            //Gionee zhangke 20160426 modify for CR01684127 end
        }

        @Override
        protected Void doInBackground(Void... params) {
            //Gionee zhangke 20160426 modify for CR01684127 start
            stopMtkLog();
            // A safe time to wait MTKLOG stopping
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
            }
            //Gionee zhangke 20160418 add for CR01678675 start
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            try{
                activityManager.forceStopPackage("com.mediatek.mtklogger");
            }catch(Exception e){
                Log.i(TAG, "mFactoryResetTask forceStopPackage Exception="+e.getMessage());
            }
            //Gionee zhangke 20160418 add for CR01678675 end
            //Gionee zhangke 20160426 modify for CR01684127 end
            EraseSD();

            // Wait a seconds to make sure the file has been deleted
            try {
                Thread.sleep(3500);
            } catch (Exception e) {
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            removeDialog(4);
            startNvService();
        }

    };

    /**
     * Start MTKLog
     */
    private void startMtkLog() {
        Intent starti = new Intent();
        starti.setAction("com.mediatek.mtklogger.ADB_CMD");
        Bundle bundle = new Bundle();
        bundle.putString("cmd_name", "start");
        bundle.putInt("cmd_target", 7);
        starti.putExtras(bundle);
        sendBroadcast(starti);
        Log.e(TAG, "start mtk mmi logcat ");
    }

    /**
     * Stop MTKLog
     */
    private void stopMtkLog() {
        Intent stopi = new Intent();
        stopi.setAction("com.mediatek.mtklogger.ADB_CMD");
        Bundle bundle = new Bundle();
        bundle.putString("cmd_name", "stop");
        //Gionee zhangke 20151030 modify for CR01577329 start
        bundle.putInt("cmd_target", 23);
        //Gionee zhangke 20151030 modify for CR01577329 end
        stopi.putExtras(bundle);
        sendBroadcast(stopi);
    }

    /**
     * GIONEE add for CR01548302
     * Enlarge MTKLOG total size
     */
    private void setMtkLogSize() {
        Intent starti = new Intent();
        starti.setAction("com.mediatek.mtklogger.ADB_CMD");
        Bundle bundle = new Bundle();
        bundle.putString("cmd_name", "set_total_log_size_3000");
        bundle.putInt("cmd_target", 1);
        starti.putExtras(bundle);
        sendBroadcast(starti);
        Log.e(TAG, "setMtkLogSize  to 3000M");
    }

    /**
     * Start FactoryRest service
     */
    private void startNvService() {
        Intent intent = new Intent(GnMMITest.this,
                gn.com.android.mmitest.item.NvService.class);
        startService(intent);
    }

    /**
     * Quit MMITest
     */
    private void stopAdjvService() {
        Intent intent1 = new Intent(GnMMITest.this,
                gn.com.android.mmitest.AdjvService.class);
        stopService(intent1);
    }


    private void openDump (){
        SystemProperties.set("persist.sys.log_open", "1");
        SystemProperties.set("persist.sys.fingerprint_dump", "1");
    }
    private void closeDump(){
        SystemProperties.set("persist.sys.log_open", "0");
        SystemProperties.set("persist.sys.fingerprint_dump", "0");
    }
    //Gionee zhangke 20151130 add for CR01599820 start
    private void releaseMmi(){
        
        SystemProperties.set("persist.radio.dispatchAllKey", "false");
        closeDump();
        Log.e(TAG, "releaseMmi: persist.radio.dispatchAllKey = " + SystemProperties.get("persist.radio.dispatchAllKey", "false"));
        //Gionee zhangke 20151124 modify for CR01597486 end 
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "releaseMmi: stopMtklog start");
                stopMtkLog();
                Log.i(TAG, "releaseMmi: stopMtklog end");
            }
        }).start();
        //Gionee <GN_BSP_MMI> <lifeilong> <2170710> modify for ID 168149 begin
        StatusBarManager sbm = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.disable(StatusBarManager.DISABLE_NONE);
        //Gionee <GN_BSP_MMI> <lifeilong> <2170710> modify for ID 168149 end
        
        //Gionee <GN_BSP_MMI> <lifeilong> <20170509> modify for ID 135347 begin
        /*if (mIsScreenBrightStatus) {
            Settings.System.putInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
        }*/
        //Gionee <GN_BSP_MMI> <lifeilong> <20170509> modify for ID 135347 end
        Log.e(TAG, "releaseMmi:buttonLightStatus =  " + buttonLightStatus);
        if (buttonLightStatus) {
            //Platform
            //AmigoSettings.putInt(getContentResolver(),
                //AmigoSettings.Button_Light_State, 1);				   

        }
        //Gionee zhangke 20160303 delete for CR01645416 start
        //TestUtils.closeBtAndWifi(GnMMITest.this);
        //Gionee zhangke 20160303 delete for CR01645416 end
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        int ringMode2 = am.getRingerMode();
        Log.e(TAG, "releaseMmi: mmi test is fininsh setRingerMode = " + ringMode2);
        stopAdjvService();
        try{
            unregisterReceiver(mNfcReceiver);
            unregisterReceiver(mReceiver);
        }catch(Exception e){
            Log.e(TAG, "releaseMmi:Exception="+e.getMessage());
        }
        //AmigoSettings.putInt(getContentResolver(), "control_center_switch", 1);
        //Gionee zhangke 20160913 add for CR01760585 start
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
        //Gionee zhangke 20160913 add for CR01760585 end
        //Gionee <GN_BSP_MMI> <lifeilong> <20170427> modify for ID 125854 begin
        TestUtils.releaseWakeLock();
        Log.e(TAG," == TestUtils.releaseWakeLock(this) == releaseMmi ");
        //Gionee <GN_BSP_MMI> <lifeilong> <20170427> modify for ID 125854 end

    }
    //Gionee zhangke 20151130 add for CR01599820 end

    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
    public String getEfusedResult(String filename) {
        Log.e(TAG,"getEfusedResult start === " + filename );
        String efusedResult = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            File currentFilePath = new File(filename);
            if (currentFilePath.exists()) {
                Log.e(TAG,"getEfusedResult exists" );
                fileInputStream = new FileInputStream(currentFilePath);
                inputStreamReader = new InputStreamReader(fileInputStream);
                br = new BufferedReader(inputStreamReader);
                String data = null;
                while ((data = br.readLine()) != null) {
                    Log.e(TAG,"getEfusedResult br.readLine()) != null" );
                    efusedResult = data;
                    Log.e(TAG,"getEfusedResult data = " + data );
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStreamQuiet(fileInputStream);
            closeReaderQuiet(inputStreamReader);
            closeReaderQuiet(br);
        }
        Log.e(TAG,"efusedResult = " + efusedResult);
        return efusedResult;
    }

    void closeStreamQuiet(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream = null;
            }
        }
    }

    void closeReaderQuiet(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reader = null;
            }
        }
    }



    private void exBatInfo(Intent intent) {
        // TODO Auto-generated method stub
        if (null == intent)
            return;
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            level = intent.getIntExtra("level", 0);
            plugged = intent.getIntExtra("plugged", 0);
        }
        //Log.e(TAG,"plugged = " + plugged + " , level = " + level);
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end


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




    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 begin
    BroadcastReceiver mNfcReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)){
                int intExtra = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, 0);
                Log.d(TAG,"intextra = " + intExtra);
                if(intExtra == 3){
                    mNfcOn = true;
                    mNfcOff = false;
                } else if (intExtra == 1){
                    mNfcOff = true;
                    mNfcOn = false;
                } else if (intExtra == 2){
                    mNfcTurn = true;
                    mNfcOff = false;
                    mNfcOn = false;
                }
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170902> modify for ID 203277 end

}
