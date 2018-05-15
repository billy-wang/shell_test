package gn.com.android.mmitest.utils;

import java.util.Arrays;
import gn.com.android.mmitest.TestResult;
import gn.com.android.mmitest.NvRAMAgent;
import android.os.ServiceManager;
import android.os.IBinder;
import android.content.Intent;
import android.os.RemoteException;
import gn.com.android.mmitest.utils.DswLog;
import android.os.SystemProperties;

public class ProinfoUtil {

     private static final String TAG = ProinfoUtil.class.getName();

    public static boolean factoryTest_QC_BATTERY_SUPPORT() {
        DswLog.e(TAG, "factoryTest_QC_BATTERY_SUPPORT");
        byte[] productInfoBuff = getProductInfo();
        if (productInfoBuff != null && productInfoBuff.length > 498) {
            DswLog.d(TAG, "isFactoryResetBoot:productInfoBuff[498]=" + productInfoBuff[498]);
            if ('P' == productInfoBuff[TestResult.MMI_FACTORY_TEST_TAG])
                return false;
        }
        return true;
    }



    public static byte[] getProductInfo() {
        IBinder binder = null;
        byte[] productInfoBuff = null;
        DswLog.e(TAG, "getProductInfo");
        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            DswLog.e(TAG, "getService	NvRAMAgent binder is null");
        }
        if (null != binder) {
            ;
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                DswLog.d(TAG, "getProductInfo NvRAMAgent read");
                productInfoBuff = agent.readFileByName(TestResult.PRODUCT_INO_NAME);

            } catch (RemoteException ex) {
                DswLog.e(TAG, ex.toString());
            }
        }
        return productInfoBuff;
    }

    /**
     * MMI支持EFUSE写入
     * */
    public static boolean isWriteEfuse() {
        DswLog.i(TAG,"iswriteefuse="+SystemProperties.get("ro.mmi.write.efuse"));
        return SystemProperties.get("ro.mmi.write.efuse").equals("yes");
    }

    /**
     * 获取版本号
     */
    public static String getGnZnVersionNum() {
        return SystemProperties.get("ro.gn.gnznvernumber");
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

/*    public static boolean isEfuse2Write() {
        final PNAME mPname = getNameInPNAME();
        switch (mPname) {
            case SW17W08:
                DswLog.d(TAG, "isEfuse2Write true");
                return true;

            default:
                DswLog.d(TAG, "isEfuse2Write false");
                return false;
        }
    }*/

    public enum PNAME {
        /**
         * 一般项目
         */
        COMMON("common"),

        BFL7305A("BFL7305A"),
        BBL7307A("BBL7307A"),
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
        SW17W08("SW17W08");
        PNAME(String pName) {
        }
    }
}
