package cy.com.android.mmitest.utils;

import cy.com.android.mmitest.TestResult;
import android.content.Intent;
import android.os.RemoteException;
import cy.com.android.mmitest.utils.DswLog;
import android.os.SystemProperties;
import vendor.mediatek.hardware.nvram.V1_0.INvram;
import com.android.internal.util.HexDump;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import cy.com.android.mmitest.utils.FlagNvramUtil;

public class ProinfoUtil {

    private static final String TAG = ProinfoUtil.class.getName();
    private static int curSnLength = 64;
    private static String proinfoPath = null;
    public static final int GOOGLE_KEY_SUCCESS_STATUS = 4;
    public static final int GOOGLE_KEY_FAIL_STATUS = 5;
    public static final int GOOGLE_KEY_NONE_STATUS = 6;


    public static boolean factoryTest_QC_BATTERY_SUPPORT() {
        DswLog.e(TAG, "factoryTest_QC_BATTERY_SUPPORT");
        curSnLength = 510;
        byte[] productInfoBuff = FlagNvramUtil.readINvramInfo(curSnLength);
        if (productInfoBuff != null && productInfoBuff.length > 498) {
            DswLog.d(TAG, "isFactoryResetBoot:productInfoBuff[498]=" + productInfoBuff[498]);
            if ('P' == productInfoBuff[TestResult.MMI_FACTORY_TEST_TAG])
                return false;
        }
        return true;
    }


    /**
     * MMI支持EFUSE写入
     * */
    public static boolean isWriteEfuse() {
        DswLog.i(TAG,"iswriteefuse="+SystemProperties.get("ro.cy.mmi.wefuse"));
        return SystemProperties.get("ro.cy.mmi.wefuse").equals("yes");
    }

    public static String switchModem2() {
        String modemFile = "sys/devices/platform/cy_down_sar/rf_band_seting";
        File mSbcFile = null;
        String mSbcStatua = null;
        try {
            mSbcFile = new File(modemFile);
            if (mSbcFile.exists()) {
                BufferedReader bf = new BufferedReader(new FileReader(mSbcFile));
                mSbcStatua = bf.readLine();
                DswLog.i(TAG, "#1 "+mSbcStatua);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.i(TAG, "#2 "+mSbcStatua);
        }
        DswLog.i(TAG, "#3 "+mSbcStatua);
        if (mSbcStatua == null ) {
            return "error";
        }

        if (mSbcStatua.equals("0")) {
            return "A";
        }else if (mSbcStatua.equals("1")) {
            return "B";
        }else if (mSbcStatua.equals("2")) {
            return "C";
        }else if (mSbcStatua.equals("3")) {
            return "D";
        }
        return "error";
    }

    public static String getNotStat(String file) {
        File mSbcFile = null;
        String mSbcStatua = null;
        try {
            mSbcFile = new File(file);
            if (mSbcFile.exists()) {
                BufferedReader bf = new BufferedReader(new FileReader(mSbcFile));
                mSbcStatua = bf.readLine();
                DswLog.i(TAG, "billy_rt5509.0_calibrated #1 "+mSbcStatua);
            }
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.i(TAG, "billy_rt5509.0_calibrated #2 "+mSbcStatua);
        }
        DswLog.i(TAG, "billy_rt5509.0_calibrated #3 "+mSbcStatua);
        return mSbcStatua;
    }

    public static String switchModem() {
        String str = SystemProperties.get("ro.boot.opt_md1_support");
        if (str.equals("9")) {
            //D
            return "D";
        }else if (str.equals("11")) {
            //C
            return "C";
        }else if (str.equals("14")) {
            //B
            return "B";
        }else if (str.equals("12")) {
            return "A";
        //Chenyee <CY_Sensor> <tanbotao> <20180704> modify for CSW1803A-633 begin
        } else if (str.equals("15")) {
            return "LT";
        }
        //Chenyee <CY_Sensor> <tanbotao> <20180704> modify for CSW1803A-633 end
        return "error";
    }

    public static boolean isCheckVersion(String pversion) {
        String string = getGnZnVersionNum();
        if (null != string) {
            String versionName = string.split("_")[0].substring(0, 7);
            DswLog.d(TAG,"versionName="+versionName);
            if (null != versionName && pversion.equals(versionName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCheckVersionSub(String pversion) {
        String string = getGnZnVersionNum();
        if (null != string && string.contains(pversion)) {
            return true;
        }
        return false;
    }

    /**
     * 获取版本号
     */
    public static String getGnZnVersionNum() {
        return SystemProperties.get("ro.cy.znvernumber");
    }

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

    public enum PNAME {
        /**
         * 一般项目
         */
        COMMON("common"),
        CSW1702("CSW1702"),
        CSW1703("CSW1703"),
        CSW1705("CSW1705"),
        SWW1616("SWW1616"),
        CSW1707("CSW1707"),
        CSW1802("CSW1802"),
        CSW1803("CSW1803"),
        SW17W16("SW17W16");
        PNAME(String pName) {
        }
    }

    public static boolean writeRpmbNvRam() {
        int curSnLength = 504;

        byte[] sn_buff = new byte[curSnLength];
        try {
            System.arraycopy(FlagNvramUtil.readINvramInfo(curSnLength), 0, sn_buff, 0, curSnLength);
            String oldSn = new String(sn_buff);
            if (oldSn == null || "".equals(oldSn)) {
                DswLog.i(TAG, "writeRpmbNvRamreadINvramInfo is null =" + oldSn);
                return false;
            }
        } catch (Exception e) {
            DswLog.i(TAG, "writeRpmbNvRam Exception =" + e.getMessage());
            return false;
        }
        //RKF:P  499~503
        sn_buff = FlagNvramUtil.getNewSN(503, "P", sn_buff);
        FlagNvramUtil.writeToNvramInfo(sn_buff,curSnLength);

        return isWriteRpmbTag();
    }

    public static boolean isWriteRpmbTag() {
        int snLength = 504;
        DswLog.e(TAG, "isWriteRpmbTag");
        byte[] productInfoBuff = FlagNvramUtil.readINvramInfo(snLength);
        if (productInfoBuff != null && productInfoBuff.length > TestResult.MMI_RPMB_TAG) {
            DswLog.d(TAG, "isWriteRpmbTag:productInfoBuff[TestResult.MMI_RPMB_TAG]=" + productInfoBuff[TestResult.MMI_RPMB_TAG]);
            if ('P' == productInfoBuff[TestResult.MMI_RPMB_TAG])
                return true;
        }
        return false;
    }

    public static int getGm3Value() {
        DswLog.e(TAG, "getGm3Value");
        int snLength = 24;
        String proinfoPath = "/vendor/nvdata/APCFG/APRDCL/FG";
        String readInfo = null;
        byte[] mSnByteArray = null;
        try {
            INvram mNvram = INvram.getService();
            if (null == mNvram) {
                DswLog.e(TAG, "getService: mnvram == null");
            }else{
                readInfo = mNvram.readFileByName(proinfoPath,snLength);
                mSnByteArray = HexDump.hexStringToByteArray(readInfo.substring(0, readInfo.length() - 1));
            }
        } catch (RemoteException ex) {
            DswLog.e(TAG, ex.toString());
        }

        if (mSnByteArray == null) {
            return -1;
        }

        for (int i = 20; i < snLength; i++) {
            DswLog.i(TAG, "mSnByteArray[" + i + "]=" + (mSnByteArray[i] & 0xff));
        }

        int value  = (mSnByteArray[20] & 0xff)  + (mSnByteArray[21] & 0xff) * (1 << 8) + (mSnByteArray[22] & 0xff) * (1 << 16) + (mSnByteArray[23] & 0xff) * (1 << 24);
        DswLog.e(TAG, "value="+value);
        return value;
    }

    public static boolean isGm3Pass() {
        DswLog.e(TAG, "isGm3Pass");

        int value = getGm3Value();

        if (value > 899 && value < 1099) {
            return true;
        }
        return false;
    }

    public static int checkGoogleKey() {
        int gKey_status = -1;
        try{
            int exeReturn = -1;
            Process process = Runtime.getRuntime().exec("init_thh initgooglekey_status");
            exeReturn = process.waitFor();
            DswLog.d(TAG,"checkGoogleKey exeReturn = " + exeReturn);

            String result = SystemProperties.get("soter.teei.attestkey.status");
            if (result == null || result.equals("NO")) {
                gKey_status = GOOGLE_KEY_NONE_STATUS;
                DswLog.d(TAG,"checkGoogleKey: google key status is none");
            }else if (result.equals("FAIL")) {
                gKey_status = GOOGLE_KEY_FAIL_STATUS;
                DswLog.d(TAG,"checkGoogleKey: google key status is failed");
            }else if (result.equals("OK")) {
                gKey_status = GOOGLE_KEY_SUCCESS_STATUS;
                DswLog.d(TAG,"checkGoogleKey: google key status is succeed");
            }
        } catch (Exception e) {
            DswLog.e(TAG, " e =  " + e.getMessage());
            e.printStackTrace();
        }
        return gKey_status;
    }

    public static boolean isSetAurisys() {
        PNAME mPname = getNameInPNAME();
        switch (mPname) {
            case CSW1707:
                DswLog.e(TAG, "isSetAurisys false");
                return false;
            default:
                DswLog.e(TAG, "isSetAurisys true");
                return true;
        }
    }

    public static boolean isTestDoubleSim() {
        PNAME mPname = getNameInPNAME();
        switch (mPname) {
            case CSW1803:
                if (isCheckVersionSub("CSW1803DB")) {
                    DswLog.e(TAG, "Test Single SimCard");
                    return false;
                }
            case CSW1702:
                DswLog.e(TAG, "Test Double SimCard");
                return true;
            default:
                DswLog.e(TAG, "Test Single SimCard");
                return false;
        }
    }

    public static String getProinfoPath() {
        String action = SystemProperties.get("ro.build.version.release");
        switch (action) {
            case "8.0.0":
                proinfoPath = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
                break;
            default:
                proinfoPath = "/vendor/nvdata/APCFG/APRDEB/PRODUCT_INFO";
                break;
        }
        return proinfoPath;
    }

    public static void setDispatchAllKey() {
        String action = SystemProperties.get("ro.build.version.release");
        switch (action) {
            case "8.0.0":
                SystemProperties.set("persist.radio.dispatchAllKey", "true");
                DswLog.d(TAG, " persist.radio.dispatchAllKey = " + SystemProperties.get("persist.radio.dispatchAllKey", "false"));
                break;
            default:
                SystemProperties.set("persist.sys.dispatchAllKey", "true");
                DswLog.e(TAG, " persist.sys.dispatchAllKey = " + SystemProperties.get("persist.sys.dispatchAllKey", "false"));
                break;
        }
    }

    public static void revertDispatchAllKey() {
        String action = SystemProperties.get("ro.build.version.release");
        switch (action) {
            case "8.0.0":
                SystemProperties.set("persist.radio.dispatchAllKey", "false");
                DswLog.d(TAG, " persist.radio.dispatchAllKey = " + SystemProperties.get("persist.radio.dispatchAllKey", "false"));
                break;
            default:
                SystemProperties.set("persist.sys.dispatchAllKey", "false");
                DswLog.e(TAG, " persist.sys.dispatchAllKey = " + SystemProperties.get("persist.sys.dispatchAllKey", "false"));
                break;
        }
    }
}
