package com.gionee.autommi;

import android.os.IBinder;
import android.os.ServiceManager;
import com.gionee.autommi.NvRAMAgent;
import android.os.RemoteException;
import android.util.Log;
import android.os.SystemProperties;

public class TestResult{
    public static final String PRODUCT_INO_NAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
    public static final String TAG = "TestResult";
	public static final int MMI_LASER_TAG = 31;
	public static final int MMI_WCHAT_SOTER_TAG = 30;
    //Gionee zhangke 20160525 add for CR01706554 start
    public static final int MMI_IFAA_KEY_TAG = 29;
    //Gionee zhangke 20160525 add for CR01706554 end
    public static final int MMI_DUAL_BACK_CAMERA_TAG = 28;
    public static final int MMI_BACK_FLASHLIGHT_CAL_TAG = 26;

    public byte[] getProductInfo() {
        IBinder binder = null;
        byte[] productInfoBuff = null;
        byte[] temp = new byte[64];

        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            Log.e(TAG, "getService	NvRAMAgent binder is null");
        }
        if (null != binder) {
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                Log.i(TAG, "getProductInfo NvRAMAgent read");
                productInfoBuff = agent.readFileByName(PRODUCT_INO_NAME);
            } catch (RemoteException ex) {
                Log.e(TAG, ex.toString());
            }
        }
        return productInfoBuff;
    }

    public byte[] getNewSN(int position, String value, byte[] sn) {
        sn[position] = value.getBytes()[0];
        return sn;
    }

    public int writeToProductInfo(byte[] sn_buff) {
        IBinder binder = null;
        Log.e(TAG, "getService	NvRAMAgent binder ???");
        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            Log.e(TAG, "getService	NvRAMAgent binder is null");
            return -1;
        }
        if (null != binder) {
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                byte[] write_buff = agent.readFileByName(PRODUCT_INO_NAME);
                // int flag = agent.writeFile(AP_CFG_REEB_PRODUCT_INFO_LID, write_buff);
                System.arraycopy(sn_buff, 0, write_buff, 0, 64);
           
                int flag = agent.writeFileByName(PRODUCT_INO_NAME, write_buff);
                if (flag > 0) {
                    Log.e(TAG, "writeToProductInfo NvRAMAgent write success");
                    for(int i=0; i<64; i++){
                        Log.i(TAG, "write_buff["+i+"]="+write_buff[i]);
                    }
                } else {
                    Log.e(TAG, "writeToProductInfo NvRAMAgent write failed");
                    return -1;
                }

            } catch (RemoteException ex) {
                Log.e(TAG, ex.toString());
                return -1;
            }
        }

        return 0;//success

    }
}
