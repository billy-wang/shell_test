package gn.com.android.mmitest.utils;

import java.util.Arrays;
import gn.com.android.mmitest.TestResult;
import gn.com.android.mmitest.NvRAMAgent;
import android.os.ServiceManager;
import android.os.IBinder;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

public class ProinfoUtil {

     private static final String TAG = ProinfoUtil.class.getName();

    public static boolean factoryTest_QC_BATTERY_SUPPORT() {
        Log.e(TAG, "factoryTest_QC_BATTERY_SUPPORT");
        byte[] productInfoBuff = getProductInfo();
        if (productInfoBuff != null && productInfoBuff.length > 498) {
            Log.d(TAG, "isFactoryResetBoot:productInfoBuff[498]=" + productInfoBuff[498]);
            if ('P' == productInfoBuff[TestResult.MMI_FACTORY_TEST_TAG] || 'F' == productInfoBuff[TestResult.MMI_FACTORY_TEST_TAG])
                return false;
        }
        return true;
    }



    private static byte[] getProductInfo() {
        IBinder binder = null;
        byte[] productInfoBuff = null;
        Log.e(TAG, "getProductInfo");
        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            Log.e(TAG, "getService	NvRAMAgent binder is null");
        }
        if (null != binder) {
            ;
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                Log.d(TAG, "getProductInfo NvRAMAgent read");
                productInfoBuff = agent.readFileByName(TestResult.PRODUCT_INO_NAME);

            } catch (RemoteException ex) {
                Log.e(TAG, ex.toString());
            }
        }
        return productInfoBuff;
    }
}