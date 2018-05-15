
package cy.com.android.mmitest;


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
import cy.com.android.mmitest.utils.DswLog;
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
import cy.com.android.mmitest.item.FeatureOption;
//Gionee zhangke 20151019 add for CR01571097 end
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import cy.com.android.mmitest.item.CheckEfuse;
import android.widget.Toast;
import cy.com.android.mmitest.utils.ProinfoUtil;
import cy.com.android.mmitest.item.DevicesInfo;
import android.os.Environment;
import android.content.Context;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;

public class CyMMITest extends BaseActivity implements OnItemClickListener {
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
    static String TAG = "CyMMITest";
    SharedPreferences.Editor mSNEditor;
    WindowManager.LayoutParams mWL;
    private PowerManager mPM;
    private int clickCount;
    private static final int EFUSE_WRITE_DLG = 0;  //写efuse
    private static final int EFUSE_BACKUP_DLG = 1; //已写efuse,恢复出厂设置
    private static final int EFUSE_ERROR_DLG = 2;  //写efuse报错
    private static final int EFUSE_NOFLAG_DLG = 3; //efuse 标志位错误
    private static final int NO_EFUSE_BACKUP_DLG = 4; //不支持efuse的恢复出厂设置
    private static final int CLEAR_DATA_DLG = 5;  //出厂设置清除数据
    private static final int RPMB_RESULT_DLG = 6; //RPMB写入结果
    private static final int RPMB_WRITE_DLG = 7; //RPMB写入结果
    private final int RPMB_SUCCESS_STATUS = 101;
    private final int RPMB_NOTKEY_STATUS = 102;
    private final int RPMB_CALLFAIL_STATUS = 103;
    private final int RPMB_MMI_FAIL_STATUS = 104;
    private Context mContext;
    private static final String NV_BACKUP_END = "android.intent.action.NV_BACKUP_END";
    private boolean buttonLight = false;
    private boolean buttonLightStatus = false;
    private int ringMode;
    int oldStream;
    private AudioManager am;
    //Gionee zhangke 20151130 add for CR01599820 start
    public final static String KILL_MMI_BROADCAST = "gn.kill.mmi";
    //Gionee zhangke 20151130 add for CR01599820 end

    //Gionee <GN_BSP_MMI> <chengq> <20170214> modify for ID 68645 begin
    private static int wavesState;
    //Gionee <GN_BSP_MMI> <chengq> <20170214> modify for ID 68645 end
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
    private static final int MMI_PASS = 0x50;
    private static final int MMI_FIAL = 0x46;
    private static final int MMI_WCHAT_SOTER_TAG = 30;
    private static final int MMI_IFAA_KEY_TAG = 29;
    private static final int MMI_ALL_TAG = 54;
    public static final int SN_LENGTH = 64;
    private byte[] mSnByteArray = new byte[SN_LENGTH];
    private int level;
    private int plugged;
    private String isEfused = "/proc/sbcflag";
    private String efuseEnd = "/proc/efuse_blower";
    private String isSupportEfuse = null;
    private String isEfusedEnd = null;
    //*********RPMB begin ************
    private String INIT_RPMBK_SUCCESS_PATH = "/data/thh/tee_00/init_rpmbk_SUCCESS";
    private String INIT_RPMBK_NOTKEY_FAILED_PATH = "/data/thh/tee_00/init_rpmbk_NOTKEY_FAILED";
    private String INIT_RPMBK_CALL_FAILED_PATH = "/data/thh/tee_00/init_rpmbk_CALL_FAILED";
    private boolean succFlag;
    private boolean notkeyFlag;
    private boolean callFlag;
    private int curRpmbFlag = -1;

    List<String> project_item = null;
    //*********RPMB end ************
    private static String PATH_TYPE_EXTERNAL_SD;
    private PowerManager.WakeLock wakeLock;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end
    //Gionee zhangke 20151130 modify for CR01599820 start
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DswLog.e(TAG, "mReceiver:action=" + action);
            if (action.equals(NV_BACKUP_END)) {
                DswLog.e(TAG, "send android.intent.action.MASTER_CLEAR and restoreMMI");
                Intent intent1 = new Intent(Intent.ACTION_MASTER_CLEAR);
                intent1.putExtra("eraseInternalData", false);
                intent1.putExtra("cyReason","restoreMMI");
                intent1.setPackage("android");
                intent1.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                context.sendBroadcast(intent1);
                finish();
            } else if (action.equals(KILL_MMI_BROADCAST)) {
                releaseMmi(true);
            } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                exBatInfo(intent);
            }

        }
    };
    //Gionee zhangke 20151130 modify for CR01599820 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************START MMI TEST***************");
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
        //Gionee zhangke 20160105 add for CR01617603

        FeatureOption.initMmiXml();
        //Gionee zhangke 20160105 add for CR01617603 end
        ListView lv = (ListView) findViewById(R.id.main_listview);
        DswLog.e(TAG, " start mmitest");
        Button quitBtn = (Button) findViewById(R.id.quit_btn);
        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Gionee zhangke 20151130 modify for CR01599820 start
                DswLog.i(TAG, "quit button is clicked");
                releaseMmi(true);
            }
        });
        TestUtils.setAppContext(CyMMITest.this);
        //Gionee <GN_BSP_MMI> <chengq> <20170418> modify for ID 118082 begin
        TestUtils.initConfigPath();
        //Gionee <GN_BSP_MMI> <chengq> <20170418> modify for ID 118082 end
        mSNEditor = TestUtils.getSNSharedPreferencesEdit(this);
        //Gionee <xiaolin><2013-06-26> modify for CR00825575 start
        String[] it = this.getResources().getStringArray(R.array.test_project_item);
        project_item = new ArrayList(Arrays.<String>asList(it));
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
        if (SystemProperties.get("ro.gn.format.kptcFlag").equals("yes")) {
            List<String> t = new ArrayList(Arrays.<String>asList(it));
            t.remove(getResources().getStringArray(R.array.test_project_item)[3]);
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
        DswLog.e(TAG, "ringMode = " + ringMode);

        am.setRingerMode(AudioManager.RINGER_MODE_SILENT);//0 Ringer mode that will be silent and will not vibrate
        int ringMode1 = am.getRingerMode();
        oldStream = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        DswLog.e(TAG, "setaudio after setRingerMode = " + ringMode1);
        //Gionee <zhangxiaowei><2013-04-11> add for CR00796293  begin

	/* disable by Billy.Wang */
        //DswLog.e(TAG, "MMI set setParameters :SET_AURISYS_ON=0");
        //am.setParameters("SET_AURISYS_ON=0");

        setMtkLogSize();
        startMtkLog();

        mContext = this;
        //Gionee zhangke 20151130 add for CR01599820 start
        IntentFilter filter_1 = new IntentFilter();
        filter_1.addAction(NV_BACKUP_END);
        filter_1.addAction(KILL_MMI_BROADCAST);
        filter_1.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mReceiver, filter_1);
        //Gionee zhangke 20151130 add for CR01599820 end

        //Gionee <GN_BSP_MMI> <chengq> <20170428> modify for ID 126256 begin
        CyMMITestApplication.sound_effect = Settings.System.getInt(getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED, 0);
        Settings.System.putInt(getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED,0);
        //Gionee <GN_BSP_MMI> <chengq> <20170428> modify for ID 126256 end

        //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 begin
        if (WavesFXContract.GN_MAXXAUDIO_SUPPORT){
            CyMMITestApplication.maxAudio = wavesState = WavesFXContract.getWavesState(getApplicationContext());
            DswLog.d(TAG, "before setting: wavesState="+wavesState);
            if (wavesState != 0){
                WavesFXContract.setWavesState(getApplicationContext(),0);
            }
            DswLog.d(TAG, "after setting : wavesState="+WavesFXContract.getWavesState(getApplicationContext()));
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 end
		//Gionee <GN_BSP_MMI> <chengq> <20170224> add for ID 49681 begin
		CyMMITestApplication.rotation = Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0);
		Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,0);
		DswLog.e(TAG,"start and save CyMMITestApplication.rotation is "+ CyMMITestApplication.rotation);
		//Gionee <GN_BSP_MMI> <chengq> <20170224> add for ID 49681 end
        //Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 begin
        if ( SystemProperties.get("ro.gn.stereo.support").equals("yes")) {
            CyMMITestApplication.stereoEnable = SystemProperties.get("persist.sys.gn.stereo.enable");
            SystemProperties.set("persist.sys.gn.stereo.enable", "no");
            DswLog.d(TAG,"start mmi persist.sys.gn.stereo.enable="+SystemProperties.get("persist.sys.gn.stereo.enable"));
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 end
        TestUtils.checkTestItems();
        lockWakeup();
    }

    private void stopAdjvService() {
        Intent intent = new Intent(CyMMITest.this,
                cy.com.android.mmitest.AdjvService.class);
        stopService(intent);
    }

    private void startAdjvService() {
        Intent intent = new Intent(CyMMITest.this,
                cy.com.android.mmitest.AdjvService.class);
        startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        startAdjvService();
        ProinfoUtil.setDispatchAllKey();
        DswLog.e(TAG, " CyMMITest onResume");

        PATH_TYPE_EXTERNAL_SD = getDefaultExternalSdPath();
    }

    @Override
    public void onPause() {
        super.onPause();
        DswLog.e(TAG, " CyMMITest onPause11");

    }

    @Override
    public void onStop() {
        super.onStop();
        DswLog.e(TAG, " CyMMITest onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        DswLog.e(TAG, " CyMMITest onStart");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
    private String getFailedMessage(){
        DswLog.e(TAG,"getFailedMessage  start ");
        String result = null;

        if (level < 50){
            DswLog.e(TAG,"电池电量不足50%");
            result = getResources().getString(R.string.efuse_battery_lower);
            return result;
        }
        if (FeatureOption.GN_RW_GN_MMI_IFAA_KEY_SUPPORT){
            if (mSnByteArray[MMI_IFAA_KEY_TAG] != MMI_PASS){
                DswLog.e(TAG,"IFAA测试没有通过");
                result = getResources().getString(R.string.efuse_ifaa_failed);
                return result;
            }
        }
        if (mSnByteArray[MMI_ALL_TAG] != MMI_PASS){
            DswLog.e(TAG,"MMI全部测试没有通过");
            result = getResources().getString(R.string.efuse_mmi_failed);
            return result;
        }
        DswLog.e(TAG,"getFailedMessage result = " + result);
        return result;
    }

    protected Dialog onCreateDialog(int id) {

        Dialog mdialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setTitle(R.string.master_clear_title);
        switch (id){
            case EFUSE_WRITE_DLG:
                builder.setMessage(R.string.efuse_need_write).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        DswLog.e(TAG,"mSnByteArray[MMI_ALL_TAG] = " + mSnByteArray[MMI_ALL_TAG] );
                        if(level >= 50 && mSnByteArray[MMI_ALL_TAG] == MMI_PASS){
                            startActivityForResult(new Intent(CyMMITest.this,CheckEfuse.class),0);
                        }else {
                            showDialog(EFUSE_ERROR_DLG);
                        }
                    }
                }).setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mdialog = builder.create();
                break;
            case EFUSE_BACKUP_DLG:
                builder.setMessage(R.string.efuse_to_backup).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mFactoryResetTask.execute();
                    }
                }).setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mdialog = builder.create();
                break;
            case EFUSE_ERROR_DLG:
                builder.setMessage(getFailedMessage()).setNegativeButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mdialog = builder.create();
                break;

            case NO_EFUSE_BACKUP_DLG:
                builder.setMessage(R.string.master_clear_final_desc).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mFactoryResetTask.execute();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dialog.dismiss();}
                });
                mdialog = builder.create();
                break;
            case EFUSE_NOFLAG_DLG:
                builder.setMessage(R.string.efuse_file_notexists).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mdialog = builder.create();
                break;
            case CLEAR_DATA_DLG:
                ProgressDialog mpDialog = new ProgressDialog(CyMMITest.this);
                mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mpDialog.setTitle(getResources().getString(R.string.recovery_title));
                mpDialog.setMessage(getResources().getString(R.string.recovery_alter));
                mpDialog.setCancelable(false);
                return mpDialog;
            case RPMB_RESULT_DLG:
                String rpmb_msg = null;
                if (curRpmbFlag == RPMB_MMI_FAIL_STATUS) {
                    rpmb_msg = getResources().getString(R.string.rpmb_mmi_failed);
                }else if (curRpmbFlag == RPMB_CALLFAIL_STATUS) {
                    rpmb_msg = getResources().getString(R.string.rpmbkCall);
                }else if (curRpmbFlag == RPMB_NOTKEY_STATUS) {
                    rpmb_msg = getResources().getString(R.string.rpmbkNotKey);
                }else if (curRpmbFlag == RPMB_SUCCESS_STATUS) {
                    rpmb_msg = getResources().getString(R.string.rpmbkSuccess);
                }else {
                    rpmb_msg = getResources().getString(R.string.rpmbkFailed);
                }
                DswLog.i(TAG,"result curRpmbFlag="+curRpmbFlag);
                builder.setTitle(R.string.set_rpmb_key)
                       .setMessage(rpmb_msg).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                mdialog = builder.create();
                break;
            case RPMB_WRITE_DLG:
                DswLog.i(TAG,"write curRpmbFlag");
                builder.setTitle(R.string.set_rpmb_key)
                        .setMessage(getString(R.string.rpmb_key_note1)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRPMBKeyFile();
                        dialog.dismiss();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { dialog.dismiss();}
                });
                mdialog = builder.create();
                break;
        }
        return mdialog;

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String result = null;
        result = data.getStringExtra("result");
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case 1:
                    DswLog.e(TAG,"onActivityResult = 1  go on efuse " );
                    String firstResult = getEfusedResult(efuseEnd);
                    DswLog.e(TAG,"firstResult = " + firstResult);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String secReslut = getEfusedResult(efuseEnd);
                    DswLog.e(TAG,"secReslut = " + secReslut);
                    if("1".equals(secReslut)){
                        DswLog.e(TAG,"secReslut = " + secReslut + "//mFactoryResetTask.execute();");
                    mFactoryResetTask.execute();
                    }else {
                        Toast.makeText(CyMMITest.this, getString(R.string.efuse_write_faild),Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2:
                    DswLog.e(TAG,"onActivityResult = 2  pass efuse " + "//mFactoryResetTask.execute();");
                    mFactoryResetTask.execute();
                    break;
                case 3:
                    DswLog.e(TAG,"onActivityResult = 3 " ) ;
                    break;
                default:
                    break;
                }
      }
      //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end



    public void updateSettings() {
        buttonLight = isButtonLightOn();
        DswLog.e(TAG, "buttonLight is " + buttonLight);

    }

    public boolean isButtonLightOn() {
        boolean result2 = false;
        //Platform
        //result2 = AmigoSettings.getInt(this.getContentResolver(),
        //      AmigoSettings.Button_Light_State, 0) != 0;

        return result2;
    }

    private int chooseItem(String str) {
        int result = 2;
        if (str.equals(project_item.get(0))) {
            result = 0;
        }else if (str.equals(project_item.get(1))) {
            result = 1;
        }else if (str.equals(project_item.get(2))) {
            result = 2;
        }else if (str.equals(project_item.get(3))) {
            result = 3;
        }else if (str.equals(project_item.get(4))) {
            result = 4;
        }else if (str.equals(project_item.get(5))) {
            result = 5;
        }else if (str.equals(project_item.get(6))) {
            result = 6;
        }
        return result;
    }

    private boolean checkRpmbFlag(){
        try{
            int exeReturn = -1;
            Process process = Runtime.getRuntime().exec("init_thh initrpmbk_status");
            exeReturn = process.waitFor();
            DswLog.d(TAG,"checkRpmbFlag initrpmbk_status exeReturn = " + exeReturn);
        } catch (Exception e) {
            DswLog.e(TAG, "initrpmbk_status " + e.getMessage());
            e.printStackTrace();
        }

        File succ = new File(INIT_RPMBK_SUCCESS_PATH);
        File notkey = new File(INIT_RPMBK_NOTKEY_FAILED_PATH);
        File call = new File(INIT_RPMBK_CALL_FAILED_PATH);
        if (succ.exists()) {
            curRpmbFlag = ProinfoUtil.writeRpmbNvRam() ? RPMB_SUCCESS_STATUS : -1;
        } else if (notkey.exists()) {
            curRpmbFlag = RPMB_NOTKEY_STATUS;
        } else if (call.exists()) {
            curRpmbFlag = RPMB_CALLFAIL_STATUS;
        }
        DswLog.d(TAG, " rpmb flags ="+curRpmbFlag);
        return succ.exists();
    }



    private void setRPMBKeyFile(){
        //flash already write RPMG flag
        if (checkRpmbFlag()) {
            showDialog(RPMB_RESULT_DLG);
            return;
        }
        //autotest is OK
        mSnByteArray = ProinfoUtil.getProductInfo(SN_LENGTH);
        if (mSnByteArray[MMI_ALL_TAG] != MMI_PASS) {
            curRpmbFlag = RPMB_MMI_FAIL_STATUS;
            showDialog(RPMB_RESULT_DLG);
            return;
        }

        try{
            int exeReturn = -1;
            Process process = Runtime.getRuntime().exec("init_thh initrpmbk");
            exeReturn = process.waitFor();
            DswLog.d(TAG,"setRPMBKeyFile exeReturn = " + exeReturn);
        } catch (Exception e) {
            DswLog.e(TAG, " e =  " + e.getMessage());
            e.printStackTrace();
        }
        checkRpmbFlag();
        showDialog(RPMB_RESULT_DLG);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DswLog.e(TAG, "chenguang parent.getItemAtPosition(position).toString()="+parent.getItemAtPosition(position).toString());
        switch (chooseItem(parent.getItemAtPosition(position).toString())) {
            case 0: {
                try {
                    Settings.Secure.setLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER, true);
                    TestUtils.openBtAndWifi(CyMMITest.this);
                    TestUtils.mIsAutoMode = true;
                    TestUtils.mIsAutoMode_2 = false;
                    SystemProperties.set("persist.radio.setcolor", "true");
                    mSNEditor.clear();
                    mSNEditor.putBoolean("mIsAutoMode", true);
                    mSNEditor.commit();
                    DswLog.e(TAG, "start enter atuo mmi ");
                    startActivity(new Intent(this, Class.forName("cy.com.android.mmitest.item."
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
                TestUtils.openBtAndWifi(CyMMITest.this);
                TestUtils.mIsAutoMode = false;
                TestUtils.mIsAutoMode_2 = false;
                SystemProperties.set("persist.radio.setcolor", "false");
                mSNEditor.clear();
                mSNEditor.commit();
                DswLog.e(TAG, "start enter hardware mmi ");
                startActivity(new Intent(this, SingleTestGridView.class));
                break;
            }

            case 2: {
                TestUtils.mIsAutoMode = false;
                TestUtils.mIsAutoMode_2 = false;
                mSNEditor.clear();
                mSNEditor.commit();
                DswLog.e(TAG, "start1 enter  mmi TestResult");
                startActivity(new Intent(this, TestResult.class));
                //  finish();
                break;
            }
            case 3: {

                if (ProinfoUtil.isWriteEfuse()) {
                    mSnByteArray = ProinfoUtil.getProductInfo(SN_LENGTH);
                    isSupportEfuse = getEfusedResult(isEfused);
                    DswLog.e(TAG,"isSupportEfuse = " + isSupportEfuse);
                    if("0".equals(isSupportEfuse)){
                        showDialog(EFUSE_WRITE_DLG);
                    }else if("1".equals(isSupportEfuse)){
                        showDialog(EFUSE_BACKUP_DLG);
                    }
                }else {
                    showDialog(NO_EFUSE_BACKUP_DLG);
                }
                break;
            }
            case 4: {
                try {
                    TestUtils.openBtAndWifi(CyMMITest.this);
                    TestUtils.mIsAutoMode_2 = true;
                    TestUtils.mIsAutoMode = false;
                    SystemProperties.set("persist.radio.setcolor", "true");
                    mSNEditor.clear();
                    mSNEditor.putBoolean("mIsAutoMode", true);
                    mSNEditor.commit();
                    DswLog.e(TAG, "start enter atuommi2 ");
                    startActivity(new Intent(this, Class.forName("cy.com.android.mmitest.item."
                            + TestUtils.getAutoItemKeys_2(this).get(0))));
                    // 	finish();
                } catch (ClassNotFoundException e) {
                    TestUtils.mIsAutoMode_2 = false;
                }
                break;
            }
            //Gionee zhangke 20160310 add for CR01650400 start
            case 5: {
                if (ProinfoUtil.isWriteRpmbTag()) {
                    curRpmbFlag = RPMB_SUCCESS_STATUS;
                    showDialog(RPMB_RESULT_DLG);
                }else {
                    showDialog(RPMB_WRITE_DLG);
                }
                break;
            }
            case 6: {
                TestUtils.mIsAutoMode = false;
                TestUtils.mIsAutoMode_2 = false;
                mSNEditor.clear();
                mSNEditor.commit();
                DswLog.e(TAG, "start1 enter  mmi DevicesInfo");
                startActivity(new Intent(this, DevicesInfo.class));
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
    public static String[] keepArray;

    static {
        if (true == SystemProperties.get("ro.gn.oversea.product").equals("yes")) {
            //GIONEE lijinfang 2012-11-21 modify for CR00734894 start
            if (true == SystemProperties.get("ro.cy.custom").equals("AFRICA_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE lijinfang 2012-11-21 modify for CR00734894 end
                //GIONEE lijinfang 2013-02-27 modify for CR00774302 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("INDIA_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Amigo", "Apps"};
                //GIONEE lijinfang 2013-02-27 modify for CR00774302 end
                //GIONEE linggz 2013-06-29 modify for CR00831684 start
                //GIONEE yeduanwang 2013-10-24 modify for CR00933312 begin
                //GIONEE lijinfang 2014-07-19 modify for Nepal gionee start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("BENGALI_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
            } else if (true == SystemProperties.get("ro.cy.custom").equals("NEPAL_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE lijinfang 2014-07-19 modify for Nepal gionee end
            } else if (true == SystemProperties.get("ro.cy.custom").equals("VIETNAM_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
            } else if (true == SystemProperties.get("ro.cy.custom").equals("MYANMAR_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
            } else if (true == SystemProperties.get("ro.cy.custom").equals("HK_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE yeduanwang 2013-10-24 modify for CR00933312 end
            } else if (true == SystemProperties.get("ro.cy.custom").equals("TAIWAN_GPLUS")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE linggz 2013-06-29 modify for CR00831684 end
                //GIONEE lijinfang 2013-06-05 modify for CR00823172 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("PHILIPPINES_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE lijinfang 2013-06-05 modify for CR00823172 end
                // Gionee yubo 2014-05-09 modify for CR01244512 begin
            } else if (true == SystemProperties.get("ro.cy.custom").equals("RUSSIA_PRESTIGIO")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Books", "Music", "Pictures", "Ringtones", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Video"};
                // Gionee yubo 2014-05-09 modify for CR01244512 end
                //GIONEE: caixf 2014-03-07 add for CR01101248 begin
            } else if (true == SystemProperties.get("ro.cy.custom").equals("VISUALFAN")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Pictures"};
                //GIONEE: caixf 2014-03-07 add for CR01101248 end
                // Gionee luoguangming 2014.05.20 modify for CR01246320 begin
            } else if (true == SystemProperties.get("ro.cy.custom").equals("PORTUGAL_SDT")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "sdt"};
                // Gionee luoguangming 2014.05.20 modify for CR01246320 end
                //Gionee caiqiaoling 2014-05-22 added for CR01269287 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("INDONESIA_MAXTRON")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Music", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Videos"};
                //Gionee caiqiaoling 2014-05-22 added for CR01269287 end
                //Gionee liuxr 2014-04-12 added for CR01185432 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("PAKISTAN_QMOBILE")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Musics", "Music", "gn_resources", "QMobile_resources", "gntheme", "Theme", "ThemePark", "Changer", "Videos"};
                //Gionee liuxr 2014-04-12 added for CR01185432 end

                //Gionee lucy 2014-06-18 add for CR01296261 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("BANGLADESH_WALTON")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Document", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Videos"};
                //Gionee lucy 2014-06-18 add for CR01296261 end
                //Gionee guanxiaowen 2014-06-23 added for CR01307949 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("ZIMBABWE_GTEL")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Apps", "Videos", "Movies", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Wallpapers"};
                //Gionee guanxiaowen 2014-06-23 added for CR01307949 end
                //Gionee xuyongji 2014-07-12 added for CR01321309 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("SOUTH_AMERICA_BLU")) {
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

    public static void EraseSD() {
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

        if (PATH_TYPE_EXTERNAL_SD == null)
            return;
        SDPATH = PATH_TYPE_EXTERNAL_SD;
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
                DswLog.e(TAG, "dir :" + file.toString());
                File files[] = file.listFiles();
                if (files == null) {
                    DswLog.e(TAG, file + " listFiles()" + " return null");
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
            DswLog.e(TAG, "delete file is not exist");
        }
    }
    // Gionee xiaolin 20120619 modify for CR00625881 end

    /**
     * Use AsyncTask, other then Thread to handle erasing file action
     */
    AsyncTask<Void, Void, Void> mFactoryResetTask = new AsyncTask<Void, Void, Void>() {

        protected void onPreExecute() {
            showDialog(CLEAR_DATA_DLG);
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
            removeDialog(CLEAR_DATA_DLG);
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
        bundle.putInt("cmd_target", 23);
        starti.putExtras(bundle);
        sendBroadcast(starti);
        DswLog.e(TAG, "start mtk mmi logcat ");
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
        bundle.putString("cmd_name", "set_total_log_size_6000");
        bundle.putInt("cmd_target", 1);
        starti.putExtras(bundle);
        sendBroadcast(starti);
        DswLog.e(TAG, "setMtkLogSize  to 6000M");
    }

    /**
     * Start FactoryRest service
     */
    private void startNvService() {
        Intent intent = new Intent(CyMMITest.this,
                cy.com.android.mmitest.item.NvService.class);
        startService(intent);
    }

    /**
     * Quit MMITest
     */

    //Gionee zhangke 20151130 add for CR01599820 start
    private void releaseMmi(boolean exit) {
        ProinfoUtil.revertDispatchAllKey();
        //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 begin
        if (WavesFXContract.GN_MAXXAUDIO_SUPPORT){
            WavesFXContract.setWavesState(mContext,CyMMITestApplication.maxAudio);
            DswLog.d(TAG, "exit MMI wavesState="+WavesFXContract.getWavesState(getApplicationContext()));
        }
		//Gionee <GN_BSP_MMI> <chengq> <20170224> add for ID 49681 begin
        //Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 begin
        if ( SystemProperties.get("ro.gn.stereo.support").equals("yes")) {
            SystemProperties.set("persist.sys.gn.stereo.enable", CyMMITestApplication.stereoEnable);
            DswLog.d(TAG,"exit mmi persist.sys.gn.stereo.enable="+SystemProperties.get("persist.sys.gn.stereo.enable"));
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 end
	    Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION,CyMMITestApplication.rotation);
		DswLog.d(TAG,"exit and set CyMMITestApplication.rotation is "+ CyMMITestApplication.rotation);
		//Gionee <GN_BSP_MMI> <chengq> <20170224> add for ID 49681 end
        //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 end
        //Gionee zhangke 20151124 modify for CR01597486 end

        //Gionee <GN_BSP_MMI> <chengq> <20170429> modify for ID 128069 begin
        Settings.System.putInt(getContentResolver(),Settings.System.SOUND_EFFECTS_ENABLED,CyMMITestApplication.sound_effect);
        //Gionee <GN_BSP_MMI> <chengq> <20170429> modify for ID 128069 end
        releaseWakeup();
        stopAdjvService();

        CyMMITestApplication.languageState = -1;
        TestUtils.initInitData();
        
        DswLog.d(TAG, "****************EXIT MMI TEST***************\n\n\n");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DswLog.i(TAG, "releaseMmi: stopMtklog start");
                stopMtkLog();
                DswLog.i(TAG, "releaseMmi: stopMtklog end");
            }
        }).start();


        DswLog.e(TAG, "releaseMmi:buttonLightStatus =  " + buttonLightStatus);
        if (buttonLightStatus) {
            //Platform
            //AmigoSettings.putInt(getContentResolver(),
            //AmigoSettings.Button_Light_State, 1);

        }
        //Gionee zhangke 20160303 delete for CR01645416 start
        //TestUtils.closeBtAndWifi(CyMMITest.this);
        //Gionee zhangke 20160303 delete for CR01645416 end
        am.setRingerMode(ringMode);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, oldStream, 0);
        int ringMode2 = am.getRingerMode();
        DswLog.e(TAG, "releaseMmi: mmi test is fininsh setRingerMode = " + ringMode2);

	/* disable by Billy.Wang */
        //DswLog.e(TAG, "MMI set setParameters :SET_AURISYS_ON=1");
        //am.setParameters("SET_AURISYS_ON=1");

        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            DswLog.e(TAG, "releaseMmi:Exception=" + e.getMessage());
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
        DswLog.e(TAG, "sales:forSale:" + isForSale);

        Configuration config = getResources().getConfiguration();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        if (!isForSale) {
            config.locale = Locale.SIMPLIFIED_CHINESE;
            CyMMITestApplication.languageState = 1;
        } else {
            config.locale = Locale.ENGLISH;
            CyMMITestApplication.languageState = 2;
        }
        DswLog.e(TAG, "config.locale:" + config.locale);
        getResources().updateConfiguration(config, metrics);
    }
    /* Gionee huangjianqiang 20160125 add end */

    public String getEfusedResult(String filename) {
        DswLog.e(TAG,"getEfusedResult start === " + filename );
        String efusedResult = null;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            File currentFilePath = new File(filename);
            if (currentFilePath.exists()) {
                DswLog.e(TAG,"getEfusedResult exists" );
                fileInputStream = new FileInputStream(currentFilePath);
                inputStreamReader = new InputStreamReader(fileInputStream);
                br = new BufferedReader(inputStreamReader);
                String data = null;
                while ((data = br.readLine()) != null) {
                    DswLog.e(TAG,"getEfusedResult br.readLine()) != null" );
                    efusedResult = data;
                    DswLog.e(TAG,"getEfusedResult data = " + data );
                }
            } else {
                DswLog.e(TAG,"Efused filename="+filename + " is not exists." );
                showDialog(EFUSE_NOFLAG_DLG);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            DswLog.e(TAG,"get Efuse file IOE error" );
            e.printStackTrace();
        } finally {
            closeStreamQuiet(fileInputStream);
            closeReaderQuiet(inputStreamReader);
            closeReaderQuiet(br);
        }
        DswLog.e(TAG,"efusedResult = " + efusedResult);
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
        level = intent.getIntExtra("level", 0);
        plugged = intent.getIntExtra("plugged", 0);
    }

    /**
     * @return String
     */
    public String getDefaultExternalSdPath() {
        DswLog.i(TAG + "/Utils", "-->getDefaultExternalSdPath()");
        String externalPath = null;
        StorageManager storageManager =
                (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = storageManager.getVolumeList();
        for (StorageVolume volume : volumes) {
            String volumePathStr = volume.getPath();
            if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(volume.getState())) {
                DswLog.i(TAG + "/Utils", volumePathStr + " is mounted!");
                VolumeInfo volumeInfo = storageManager.findVolumeById(volume.getId());
                if (isUSBOTG(volumeInfo)) {
                    continue;
                }
                if (volume.isEmulated()) {
                    String viId = volumeInfo.getId();
                    DswLog.i(TAG + "/Utils", "Is emulated and volumeInfo.getId() : " + viId);
                    // If external sd card, the viId will be like
                    // "emulated:179,130"
                    if (!viId.equalsIgnoreCase("emulated")) {
                        externalPath = volumePathStr;
                        break;
                    }
                } else {
                    DiskInfo diskInfo = volumeInfo.getDisk();
                    if (diskInfo == null) {
                        continue;
                    }
                    String diId = diskInfo.getId();
                    String emmcSupport =  SystemProperties.get("ro.mtk_emmc_support", "");
                    DswLog.i(TAG + "/Utils", "Is not emulated and diskInfo.getId() : " + diId);
                    // If is emmcSupport and is internal sd card, the diId will be like "disk:179,0"
                    // if is not emmcSupport and is internal sd card, the diId will be like "disk:7,1"
                    if ((emmcSupport.equals("1") && !diId.equalsIgnoreCase("disk:179,0"))
                            || (!emmcSupport.equals("1") && !diId.equalsIgnoreCase("disk:7,1"))) {
                        externalPath = volumePathStr;
                        break;
                    }
                }
            } else {
                DswLog.i(TAG + "/Utils", volumePathStr + " is not mounted!");
            }
        }
        DswLog.i(TAG + "/Utils", "<--getDefaultExternalSdPath() = " + externalPath);
        return externalPath;
    }

    /**
     * check if volume is USB OTG.
     * @return boolean
     */
    private boolean isUSBOTG(VolumeInfo volumeInfo) {
        DiskInfo diskInfo = volumeInfo.getDisk();
        if (diskInfo == null) {
            return false;
        }

        String diskID = diskInfo.getId();
        if (diskID != null) {
            // for usb otg, the disk id same as disk:8:x
            String[] idSplit = diskID.split(":");
            if (idSplit != null && idSplit.length == 2) {
                if (idSplit[1].startsWith("8,")) {
                    DswLog.i(TAG, "this is a usb otg");
                    return true;
                }
            }
        }
        return false;
    }
    public void lockWakeup() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire();
    }

    public  void releaseWakeup() {
        try {
            wakeLock.release();
        }catch (Exception e) {
            DswLog.d(TAG, e.getMessage());
        }
    }
}
