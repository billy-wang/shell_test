package com.cydroid.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import com.cydroid.util.DswLog;
import vendor.mediatek.hardware.nvram.V1_0.INvram;
import com.android.internal.util.HexDump;
import java.util.ArrayList;
import com.cydroid.util.DswLog;
import android.os.RemoteException;


//import com.android.internal.os.storage.ExternalStorageFormatter;

/**
* @Description: 系统公共接口工具类
 */
public class SystemUtil {
    public static final String TAG = "SystemUtil";

    public  static void ledChgCancel(){
        revertDispatchAllKey();
    }

    public static void ledChgOpen() {
        revertDispatchAllKey();
    }
    
    public static String getChargerVoltage() {
        return "cat /sys/class/power_supply/battery/ChargerVoltage";
    }
    
    public static String getChargerCurrent() {
        return "cat /sys/class/power_supply/battery/BatteryPresentCurrent";
    }
    
    /**
     * 获取版本号
     */
    public static String getGnZnVersionNum() {
        return SystemProperties.get("ro.cy.znvernumber");
    }

    /**
     * 获取类型
     */
    public static String getBuildType() {
        return SystemProperties.get("ro.build.type");
    }

    /**
	 * 
	 */
    public static String getGsmSERial() {
        return SystemProperties.get("gsm.serial");
    }

    /**
     * 屏蔽HOME键FLAG
     */
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000; //WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED;

    /**
     * 系统收音机
     */
    public static final int STREAM_FM = AudioManager.STREAM_MUSIC;

    /*************** auto test ******************/
    /**
     * 获取拨号的Intent action
     */
    public static final String INTENT_ACTION_CALL_PRIVILEGED = Intent.ACTION_CALL_PRIVILEGED;



    /**
     * 获取SD卡的信息
     */
    public String getSDCardInfo(Context context) {
        String info = "";

        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = mStorageManager.getVolumeList();
        for (int i = 0; i < volumes.length; i++) {
            String path = volumes[i].getPath();
            String state = mStorageManager.getVolumeState(path);
            if (path.contains("sdcard")) {
                StatFs stat = new StatFs(path);
                int allVol = (int) (((long) stat.getBlockCount() * stat.getBlockSize()) / (1024 * 1024));
                int avaiableVol = (int) (((long) stat.getAvailableBlocks() * stat.getBlockSize()) / (1024 * 1024));
                info += "/storage/sdcard0:mounted:" + allVol + ":" + avaiableVol + "|";
            }
        }
        info = info.substring(0, info.length() - 1);
        
        return info;
    }

    public static boolean getCameraVersion() {
        final PNAME mPname = getNameInPNAME();
        switch (mPname) {
            case SW17W08:
            case SWW1617:
                DswLog.d("AutoMMICamerVersion", "getCameraVersion true");
                return true;

            default:
                DswLog.d("AutoMMICamerVersion", "getCameraVersion false");
                return false;
        }
    }
    
    /**
     * 获取内部存储的信息
     */
    public String getInternalInfo(Context context) {
        String info = "";

        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = mStorageManager.getVolumeList();
        for (int i = 0; i < volumes.length; i++) {
            String path = volumes[i].getPath();
            String state = mStorageManager.getVolumeState(path);
            if (path.contains("emulated")) {
                StatFs stat = new StatFs(path);
                int allVol = (int) (((long) stat.getBlockCount() * stat.getBlockSize()) / (1024 * 1024));
                int avaiableVol = (int) (((long) stat.getAvailableBlocks() * stat.getBlockSize()) / (1024 * 1024));
                info += "/storage/sdcard1:mounted:"+ allVol + ":" + avaiableVol + "|";
            }
        }
        info = info.substring(0, info.length() - 1);
        return info;
    }
    
    /**
     * 是否是工程模式版本
     */
    public static boolean isEngProject() {
        return SystemProperties.get("ro.build.type").equals("eng");
    }

    public static boolean chooseVersion(String version) {
        String str = getGnZnVersionNum();
        if (str != null && str.length() > 0) {
            String versionName = str.split("_")[0].substring(0, 7);
            if (versionName != null && versionName.length() > 0) {
                if (versionName.equals(version))
                    return true;
            }
        }
        return false;
    }
    //Gionee <GN_BSP_AutoMMI> <chengq> <20170506> modify for ID 125584 begin
    public static boolean chooseVFVersion() {
        String str = getGnZnVersionNum();
        if (str != null && str.length() > 0) {
            String subVersionName = str.split("_")[0];
            if (subVersionName != null && subVersionName.length() >= 9) {
                String versionName = subVersionName.substring(7, 9);
                if (versionName != null && versionName.length() > 0) {
                    try {
                        if (versionName.equals("VF") || versionName.equals("LT"))
                            return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            }
        }
        return false;
    }
    //Gionee <GN_BSP_AutoMMI> <chengq> <20170506> modify for ID 125584 end

    /**
	 * 获取项目名枚举项
	 * {@link PNAME#BFL7305A }
	 */
	public static PNAME getNameInPNAME() {
		PNAME key = PNAME.COMMON;
		String string = getGnZnVersionNum();
		if (null != string) {
			String versionName = string.split("_")[0].substring(0, 7);
			if (null != versionName && !"".equals(versionName)) {
				try {
				key = PNAME.valueOf(versionName);
				} catch (IllegalArgumentException e) {
				}
			}
		}
		return key;
	}
	
	/**
	 * 项目名枚举类
	 *
	 */
	public enum PNAME {
		/**
		 * 一般项目
		 */
		COMMON("common"),
		/**
		 * 按键机w900
		 */
		BFL7305A("BFL7305A"),
		/**
		 * 平板机7307
		 * @param pName
		 */
		BBL7307A("BBL7307A"),
		/**
		 * android5.0
		 * @param pName
		 */
		BBL7313A("BBL7313A"), 
		BFL7512B("BFL7512B"),
		BBL7515A("BBL7515A"), 
		BBL7516A("BBL7516A"),
		BBL7337A("BBL7337A"),
        BBL7371("BBL7371"),
        SWW1609("SWW1609"),
        BBL7551("BBL7551"),
        WBL7372("WBL7372"),
        SWW1617("SWW1617"),
        SWW1618("SWW1618"),
        SW17W05("SW17W05"),
        SWW1631("SWW1631"),
        SWW1627("SWW1627"),
        SWW1616("SWW1616"),
        SW17W16("SW17W16"),
        SW17W08("SW17W08");
        PNAME(String pName) {
		}
	}
	public static final String A2I_CHANGE_BIN = "a2i_change_bin";

	public static void changeA2ibin4UserVersion(ContentResolver mContentResolver) {
		Settings.System.putInt(mContentResolver, A2I_CHANGE_BIN, 0);
		switchA2imainClose(mContentResolver);
	}
	
	public static void changeA2ibin4MmiVersion(ContentResolver mContentResolver) {
		Settings.System.putInt(mContentResolver, A2I_CHANGE_BIN, 1);
		switchA2imainOpen(mContentResolver);
	}
	
	public static void switchA2imainOpen(ContentResolver mContentResolver){
		Settings.System.putInt(mContentResolver , "a2i_main_switch",1);
	}
	
	public static void switchA2imainClose(ContentResolver mContentResolver){
		Settings.System.putInt(mContentResolver , "a2i_main_switch",0);
	}

    public static void setDispatchAllKey() {
        String action = SystemProperties.get("ro.build.version.release");
        switch (action) {
            case "8.0.0":
                SystemProperties.set("persist.radio.dispatchAllKey", "true");
                DswLog.d("AutoMMI", " persist.radio.dispatchAllKey = " + SystemProperties.get("persist.radio.dispatchAllKey", "false"));
                break;
            default:
                SystemProperties.set("persist.sys.dispatchAllKey", "true");
                DswLog.e("AutoMMI", " persist.sys.dispatchAllKey = " + SystemProperties.get("persist.sys.dispatchAllKey", "false"));
                break;
        }
    }

    public static void revertDispatchAllKey() {
        String action = SystemProperties.get("ro.build.version.release");
        switch (action) {
            case "8.0.0":
                SystemProperties.set("persist.radio.dispatchAllKey", "false");
                DswLog.d("AutoMMI", " persist.radio.dispatchAllKey = " + SystemProperties.get("persist.radio.dispatchAllKey", "false"));
                break;
            default:
                SystemProperties.set("persist.sys.dispatchAllKey", "false");
                DswLog.e("AutoMMI", " persist.sys.dispatchAllKey = " + SystemProperties.get("persist.sys.dispatchAllKey", "false"));
                break;
        }
    }

    public static String getProinfoPath() {
        String proinfoPath = null;
        if (proinfoPath == null) {
            PNAME mPname = getNameInPNAME();
            switch (mPname) {
                case SWW1616:
                case SW17W16:
                    proinfoPath = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
                    break;
                default:
                    proinfoPath = "/vendor/nvdata/APCFG/APRDEB/PRODUCT_INFO";
                    break;
            }
        }
        return proinfoPath;
    }

    public static byte[] getNewSN(int position, String value, byte[] sn) {
        sn[position] = value.getBytes()[0];
        return sn;
    }

    //**************** Android O read & write INvramInfo begin ****************//
    public static byte[] readINvramInfo(int length) {
        INvram mNvram = null;
        String readInfo = null;
        byte[] productInfoBuff = null;
        try {
            mNvram = INvram.getService();
            if (null == mNvram) {
                DswLog.e(TAG, "getPadioProxy: mnvram == null");
            }else{
                DswLog.e(TAG, "readINvramInfo: mNvram is OK");
                readInfo = mNvram.readFileByName(getProinfoPath(),length);
                productInfoBuff = HexDump.hexStringToByteArray(readInfo.substring(0, readInfo.length() - 1));
                DswLog.i(TAG, "getINvramInfo mNvram  read ="+readInfo);
            }
        } catch (RemoteException ex) {
            DswLog.e(TAG, ex.toString());
        }
        return productInfoBuff;
    }

    public static void writeToNvramInfo(byte[] sn_buff, int length) {
        INvram mNvram = null;
        int wflag = 0;
        try {
            mNvram = INvram.getService();
            if (null == mNvram) {
                DswLog.e(TAG, "getPadioProxy: mnvram == null");
            }else{
                DswLog.i(TAG, "mNvram begin write");
                ArrayList<Byte> dataArray = new ArrayList<Byte>(length);
                for (int i = 0; i < length; i++) {
                    dataArray.add(i, new Byte(sn_buff[i]));
                }
                wflag = mNvram.writeFileByNamevec(getProinfoPath(),length, dataArray);
                DswLog.i(TAG, "writeFileByNamevec flag="+wflag);
            }
            DswLog.e(TAG, "nvram writeToNvramInfo flag="+wflag);
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.e(TAG, "nvram write Error:"+ e.getMessage() + ":" + e.getCause());
            return;
        }
    }
    //**************** Android O read & write INvramInfo end ****************//
}
