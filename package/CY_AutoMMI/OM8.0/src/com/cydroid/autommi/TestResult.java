package com.cydroid.autommi;

import android.os.IBinder;
import android.os.ServiceManager;
import com.cydroid.autommi.NvRAMAgent;
import android.os.RemoteException;
import com.cydroid.util.DswLog;
import android.os.SystemProperties;
import vendor.mediatek.hardware.nvram.V1_0.INvram;
import java.util.ArrayList;
import com.android.internal.util.HexDump;


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
    public static final int SN_LENGTH = 64;
    private static volatile INvram mNvram = null;

/*    public byte[] getProductInfo() {
        IBinder binder = null;
        byte[] productInfoBuff = null;
        byte[] temp = new byte[64];

        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            DswLog.e(TAG, "getService	NvRAMAgent binder is null");
        }
        if (null != binder) {
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                DswLog.i(TAG, "getProductInfo NvRAMAgent read");
                productInfoBuff = agent.readFileByName(PRODUCT_INO_NAME);
            } catch (RemoteException ex) {
                DswLog.e(TAG, ex.toString());
            }
        }
        return productInfoBuff;
    }*/

    public byte[] getNewSN(int position, String value, byte[] sn) {
        sn[position] = value.getBytes()[0];
        return sn;
    }

  /*  public int writeToProductInfo(byte[] sn_buff) {
        IBinder binder = null;
        DswLog.e(TAG, "getService	NvRAMAgent binder ???");
        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            DswLog.e(TAG, "getService	NvRAMAgent binder is null");
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
                    DswLog.e(TAG, "writeToProductInfo NvRAMAgent write success");
                    for(int i=0; i<64; i++){
                        DswLog.i(TAG, "write_buff["+i+"]="+write_buff[i]);
                    }
                } else {
                    DswLog.e(TAG, "writeToProductInfo NvRAMAgent write failed");
                    return -1;
                }

            } catch (RemoteException ex) {
                DswLog.e(TAG, ex.toString());
                return -1;
            }
        }

        return 0;//success

    }*/

    //**************** Android O read & write INvramInfo begin ****************//
    public static byte[] readNvramInfo() {

        String readInfo = null;
        byte[] productInfoBuff = null;
        try {
            mNvram = INvram.getService();
            if (null == mNvram) {
                DswLog.e(TAG, "getPadioProxy: mnvram == null");
            }else{
                readInfo = mNvram.readFileByName(TestResult.PRODUCT_INO_NAME,TestResult.SN_LENGTH);
                productInfoBuff = HexDump.hexStringToByteArray(readInfo.substring(0, readInfo.length() - 1));
                DswLog.i(TAG, "getINvramInfo mNvram  read ="+readInfo);
            }
        } catch (RemoteException ex) {
            DswLog.e(TAG, ex.toString());
        }
        return productInfoBuff;
    }

    public static void writeToNvramInfo(byte[] sn_buff) {
        int wflag = 0;
        try {
            mNvram = INvram.getService();

            if (null == mNvram) {
                DswLog.e(TAG, "getPadioProxy: mnvram == null");
            }else{
                DswLog.i(TAG, "mNvram begin write");
                ArrayList<Byte> dataArray = new ArrayList<Byte>(TestResult.SN_LENGTH);
                for (int i = 0; i < TestResult.SN_LENGTH; i++) {
                    dataArray.add(i, new Byte(sn_buff[i]));
                }
                wflag = mNvram.writeFileByNamevec(TestResult.PRODUCT_INO_NAME,TestResult.SN_LENGTH, dataArray);
                String readInfo = mNvram.readFileByName(TestResult.PRODUCT_INO_NAME,TestResult.SN_LENGTH);
                DswLog.i(TAG, "writeFileByNamevec flag="+wflag);
                DswLog.i(TAG, "getINvramInfo mNvram write & read ="+readInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
            DswLog.e(TAG, "nvram write Error:"+ e.getMessage());
            return;
        }
    }

    //**************** Android O read & write INvramInfo end ****************//
}
