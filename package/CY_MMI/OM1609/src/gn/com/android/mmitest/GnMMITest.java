
package gn.com.android.mmitest;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
/* Gionee huangjianqiang 20160125 add begin */
import java.util.Locale;
/* Gionee huangjianqiang 20160125 add end */
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
import android.os.Environment;
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
/* Gionee huangjianqiang 20160125 add begin */
import android.content.res.Configuration;
import android.util.DisplayMetrics;
/* Gionee huangjianqiang 20160125 add end */

//Platform
//import amigo.provider.AmigoSettings;
//import amigo.app.AmigoProgressDialog;

import android.app.Dialog;

import java.util.ArrayList;

import android.location.LocationManager;
//Gionee zhangke 20151019 add for CR01571097 start
import gn.com.android.mmitest.item.FeatureOption;
//Gionee zhangke 20151019 add for CR01571097 end

public class GnMMITest extends BaseActivity implements OnItemClickListener {
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
    int oldStream;
    private AudioManager am;
    //Gionee zhangke 20151130 add for CR01599820 start
    private final static String KILL_MMI_BROADCAST = "gn.kill.mmi";
    //Gionee zhangke 20151130 add for CR01599820 end

    /* Gionee huangjianqiang 20160125 add begin */
    private Locale oldLocale = null;
    /* Gionee huangjianqiang 20160125 add end */
    //Gionee <GN_BSP_MMI> <chengq> <20170214> modify for ID 68645 begin
    private static int wavesState;
    //Gionee <GN_BSP_MMI> <chengq> <20170214> modify for ID 68645 end
    //Gionee zhangke 20151130 modify for CR01599820 start
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mReceiver:action=" + intent.getAction());
            if (NV_BACKUP_END.equals(intent.getAction())) {
                Log.e(TAG, "send android.intent.action.MASTER_CLEAR");
                Intent intent1 = new Intent(Intent.ACTION_MASTER_CLEAR);
                intent1.putExtra("eraseInternalData", false);
                context.sendBroadcast(intent1);
                stopAdjvService();
                finish();
            } else if (KILL_MMI_BROADCAST.equals(intent.getAction())) {
                releaseMmi(true);
            }
        }
    };
    //Gionee zhangke 20151130 modify for CR01599820 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Gionee huangjianqiang 20160125 add begin */
        ConfigLanguage(getIntent());
        /* Gionee huangjianqiang 20160125 add end */
        // Gionee xiaolin 20120921 add for CR00693542 start
        TestUtils.sContinue = true;
        // Gionee xiaolin 20120921 add for CR00693542 end
        setContentView(R.layout.main_list);
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

        ListView lv = (ListView) findViewById(R.id.main_listview);
        Log.e(TAG, " start mmitest");
        Button quitBtn = (Button) findViewById(R.id.quit_btn);
        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Gionee zhangke 20151130 modify for CR01599820 start
                Log.i(TAG, "quit button is clicked");
                releaseMmi(true);
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
            t.remove(getResources().getStringArray(R.array.test_project_item)[5]);
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
        oldStream = am.getStreamVolume(AudioManager.STREAM_MUSIC);
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

        //Gionee <GN_BSP_MMI> <chengq> <20170428> modify for ID 126256 begin
        GnMMITestApplication.sound_effect = Settings.System.getInt(getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED, 0);
        Settings.System.putInt(getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED,0);
        //Gionee <GN_BSP_MMI> <chengq> <20170428> modify for ID 126256 end

        //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 begin
        if (WavesFXContract.GN_MAXXAUDIO_SUPPORT){
            GnMMITestApplication.maxAudio = wavesState = WavesFXContract.getWavesState(getApplicationContext());
            Log.d(TAG, "before setting: wavesState="+wavesState);
            if (wavesState != 0){
                WavesFXContract.setWavesState(getApplicationContext(),0);
            }
            Log.d(TAG, "after setting : wavesState="+WavesFXContract.getWavesState(getApplicationContext()));
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 end

    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(GnMMITest.this,
                gn.com.android.mmitest.AdjvService.class);
        startService(intent);
        //Gionee zhangke 20160304 add for CR01646450 start
        if (!GnMMITestApplication.isGoodix) {
            Intent fingerIntent = new Intent("com.fingerprints.service.FingerprintService");
            ComponentName component = new ComponentName("com.fingerprints.serviceext", "com.fingerprints.service.FingerprintService");
            fingerIntent.setComponent(component);
            startService(fingerIntent);
        }

        //Gionee zhangke 20160304 add for CR01646450 end
        SystemProperties.set("persist.radio.dispatchAllKey", "true");
        Log.e(TAG, " GnMMITest onResume");
        Log.e(TAG, " persist.radio.dispatchAllKey = " + SystemProperties.get("persist.radio.dispatchAllKey", "false"));
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
    protected void onStart() {
        super.onStart();
        Log.e(TAG, " GnMMITest onStart");
        TestUtils.checkTestItems();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAdjvService();
        Log.e(TAG, " GnMMITest onDestroy");

    }

    protected Dialog onCreateDialog(int id) {
        ProgressDialog mpDialog = new ProgressDialog(GnMMITest.this);
        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mpDialog.setTitle(getResources().getString(R.string.recovery_title));
        mpDialog.setMessage(getResources().getString(R.string.recovery_alter));
        mpDialog.setCancelable(false);

        return mpDialog;
    }

    public void updateSettings() {
        buttonLight = isButtonLightOn();
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
                    SystemProperties.set("persist.radio.setcolor", "true");
                    mSNEditor.clear();
                    mSNEditor.putBoolean("mIsAutoMode", true);
                    mSNEditor.commit();
                    Log.e(TAG, "start enter atuo mmi ");
                    startActivity(new Intent(this, Class.forName("gn.com.android.mmitest.item."
                            + TestUtils.getAutoItemKeys(this).get(0))));
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
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
                SystemProperties.set("persist.radio.setcolor", "false");
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
                if (null == mBuilder) {
                    mBuilder = new Builder(this);
                    mBuilder.setTitle(R.string.master_clear_title);
                    mBuilder.setMessage(R.string.master_clear_final_desc);
                    mBuilder.setPositiveButton(android.R.string.ok, new OnClickListener() {

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
                    mBuilder.setNegativeButton(android.R.string.cancel, null);
                }
                mBuilder.show();
                break;
            }
            case 4: {
                try {
                    TestUtils.openBtAndWifi(GnMMITest.this);
                    TestUtils.mIsAutoMode_2 = true;
                    TestUtils.mIsAutoMode = false;
                    SystemProperties.set("persist.radio.setcolor", "true");
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
                String rpmbflag = SystemProperties.get("persist.sys.rpmbflag", "0");
                if (rpmbflag.equals("1")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GnMMITest.this);
                    builder.setTitle(R.string.set_rpmb_key)
                            .setMessage(getString(R.string.rpmb_key_has_writed))
                            .setNegativeButton(android.R.string.cancel, null);
                    Dialog dialog = builder.create();
                    dialog.show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GnMMITest.this);
                    builder.setTitle(R.string.set_rpmb_key)
                            .setMessage(getString(R.string.rpmb_key_note))
                            .setPositiveButton(android.R.string.ok, new OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO Auto-generated method stub
                                    Log.i(TAG, "write rpmb key");
                                    /*Gionee huangjianqiang 20160518 add for CR01691496 begin*/
                                    releaseMmi(false);
                                    /*Gionee huangjianqiang 20160518 add for CR01691496 end*/
                                    SystemProperties.set("persist.sys.rpmbflag", "1");
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
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE lijinfang 2012-11-21 modify for CR00734894 end
                //GIONEE lijinfang 2013-02-27 modify for CR00774302 start
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("INDIA_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Amigo", "Apps"};
                //GIONEE lijinfang 2013-02-27 modify for CR00774302 end
                //GIONEE linggz 2013-06-29 modify for CR00831684 start
                //GIONEE yeduanwang 2013-10-24 modify for CR00933312 begin
                //GIONEE lijinfang 2014-07-19 modify for Nepal gionee start
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("BENGALI_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("NEPAL_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE lijinfang 2014-07-19 modify for Nepal gionee end
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("VIETNAM_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("MYANMAR_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("HK_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE yeduanwang 2013-10-24 modify for CR00933312 end
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("TAIWAN_GPLUS")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE linggz 2013-06-29 modify for CR00831684 end
                //GIONEE lijinfang 2013-06-05 modify for CR00823172 start
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("PHILIPPINES_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE lijinfang 2013-06-05 modify for CR00823172 end
                // Gionee yubo 2014-05-09 modify for CR01244512 begin   
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_PRESTIGIO")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Books", "Music", "Pictures", "Ringtones", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Video"};
                // Gionee yubo 2014-05-09 modify for CR01244512 end  
                //GIONEE: caixf 2014-03-07 add for CR01101248 begin
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("VISUALFAN")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Pictures"};
                //GIONEE: caixf 2014-03-07 add for CR01101248 end
                // Gionee luoguangming 2014.05.20 modify for CR01246320 begin
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("PORTUGAL_SDT")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "sdt"};
                // Gionee luoguangming 2014.05.20 modify for CR01246320 end 
                //Gionee caiqiaoling 2014-05-22 added for CR01269287 start
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("INDONESIA_MAXTRON")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Music", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Videos"};
                //Gionee caiqiaoling 2014-05-22 added for CR01269287 end
                //Gionee liuxr 2014-04-12 added for CR01185432 start
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("PAKISTAN_QMOBILE")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Musics", "Music", "gn_resources", "QMobile_resources", "gntheme", "Theme", "ThemePark", "Changer", "Videos"};
                //Gionee liuxr 2014-04-12 added for CR01185432 end

                //Gionee lucy 2014-06-18 add for CR01296261 start
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("BANGLADESH_WALTON")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Document", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Videos"};
                //Gionee lucy 2014-06-18 add for CR01296261 end
                //Gionee guanxiaowen 2014-06-23 added for CR01307949 start
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("ZIMBABWE_GTEL")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Apps", "Videos", "Movies", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Wallpapers"};
                //Gionee guanxiaowen 2014-06-23 added for CR01307949 end
                //Gionee xuyongji 2014-07-12 added for CR01321309 start
            } else if (true == SystemProperties.get("ro.gn.oversea.custom").equals("SOUTH_AMERICA_BLU")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Wallpaper"};
                //Gionee xuyongji 2014-07-12 added for CR01321309 end
            } else {
                keepArray = new String[]{"mapbar", "music", "gn_resources", "video", "gntheme", "Theme", "ThemePark", ".gn_apps.zip", "pctool", "Music", "Changer"};
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
        /*Gionee huangjianqiang 20160326 add for CR01623736 beign*/
        SDPATH = "/mnt/m_external_sd";
        sd = new File(SDPATH);
        if (sd.canWrite())
            dFile(new File(SDPATH));
        /*Gionee huangjianqiang 20160326 add for CR01623736 beign*/
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
            showDialog(WAIT_DLG);
            stopMtkLog();
            // A safe time to wait MTKLOG stopping
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
            }
        }

        ;

        @Override
        protected Void doInBackground(Void... params) {
            EraseSD();

            // Wait a seconds to make sure the file has been deleted
            try {
                Thread.sleep(3500);
            } catch (Exception e) {
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            removeDialog(WAIT_DLG);
            startNvService();
        }

        ;
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

    //Gionee zhangke 20151130 add for CR01599820 start
    private void releaseMmi(boolean exit) {
        //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 begin
        if (WavesFXContract.GN_MAXXAUDIO_SUPPORT){
            WavesFXContract.setWavesState(mContext,GnMMITestApplication.maxAudio);
            Log.d(TAG, "exit MMI wavesState="+WavesFXContract.getWavesState(getApplicationContext()));
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 end
        SystemProperties.set("persist.radio.dispatchAllKey", "false");
        Log.e(TAG, "releaseMmi: persist.radio.dispatchAllKey = " + SystemProperties.get("persist.radio.dispatchAllKey", "false"));
        //Gionee zhangke 20151124 modify for CR01597486 end

        //Gionee <GN_BSP_MMI> <chengq> <20170429> modify for ID 128069 begin
        Settings.System.putInt(getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED,GnMMITestApplication.sound_effect);
        //Gionee <GN_BSP_MMI> <chengq> <20170429> modify for ID 128069 end
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "releaseMmi: stopMtklog start");
                stopMtkLog();
                Log.i(TAG, "releaseMmi: stopMtklog end");
            }
        }).start();


        Log.e(TAG, "releaseMmi:buttonLightStatus =  " + buttonLightStatus);
        if (buttonLightStatus) {
            //Platform
            //AmigoSettings.putInt(getContentResolver(),
            //AmigoSettings.Button_Light_State, 1);

        }
        //Gionee zhangke 20160303 delete for CR01645416 start
        //TestUtils.closeBtAndWifi(GnMMITest.this);
        //Gionee zhangke 20160303 delete for CR01645416 end
        am.setRingerMode(ringMode);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, oldStream, 0);
        int ringMode2 = am.getRingerMode();
        Log.e(TAG, "releaseMmi: mmi test is fininsh setRingerMode = " + ringMode2);
        stopAdjvService();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e(TAG, "releaseMmi:Exception=" + e.getMessage());
        }
        //AmigoSettings.putInt(getContentResolver(), "control_center_switch", 1);

        //Gionee <GN_BSP_MMI> <chengq> <20170425> modify for ID 122934 begin
        if (exit) {
            finish();
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170425> modify for ID 122934 end

    }
    //Gionee zhangke 20151130 add for CR01599820 end

    /* Gionee huangjianqiang 20160125 add begin */
    private void ConfigLanguage(Intent intent) {
        if (intent == null) {
            return;
        }
        boolean isForSale = intent.getBooleanExtra("forSale", false);
        TestUtils.mIsForSale = isForSale;
        Log.e(TAG, "sales:forSale:" + isForSale);

        oldLocale = Locale.ENGLISH;
        Configuration config = getResources().getConfiguration();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (!isForSale) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
        } else {
            config.locale = oldLocale;
        }
        Log.e(TAG, "config.locale:" + config.locale);
        getResources().updateConfiguration(config, metrics);
    }
    /* Gionee huangjianqiang 20160125 add end */


}
