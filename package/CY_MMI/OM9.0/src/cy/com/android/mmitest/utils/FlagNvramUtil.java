package cy.com.android.mmitest.utils;

import java.util.Arrays;
import cy.com.android.mmitest.item.FeatureOption;
import vendor.mediatek.hardware.nvram.V1_0.INvram;
import java.util.ArrayList;
import cy.com.android.mmitest.TestResult;
import com.android.internal.util.HexDump;
import android.os.RemoteException;

public class FlagNvramUtil {
    public  static final int WRITE_FACTORY_FLAG = 0x01;
    public  static final int CLEAR_FACTORY_FLAG = 0x02;
    private final static String TAG = "FlagNvramUtil";
    private static volatile INvram mNvram = null;

    //**************** Android O read & write INvramInfo begin ****************//
    public static byte[] readINvramInfo() {
        String readInfo = null;
        byte[] productInfoBuff = null;
        try {
            mNvram = INvram.getService();
            if (null == mNvram) {
                DswLog.e(TAG, "getPadioProxy: mnvram == null");
            }else{
                DswLog.e(TAG, "readINvramInfo: mNvram is OK");
                readInfo = mNvram.readFileByName(ProinfoUtil.getProinfoPath(),TestResult.SN_LENGTH);
                productInfoBuff = HexDump.hexStringToByteArray(readInfo.substring(0, readInfo.length() - 1));
                DswLog.i(TAG, "getINvramInfo mNvram  read ="+readInfo);
            }
        } catch (RemoteException ex) {
            DswLog.e(TAG, ex.toString());
        }
        for (int i = 0; i < TestResult.SN_LENGTH; i++) {
            DswLog.i(TAG, "getProductInfo[" + i + "]=" + productInfoBuff[i]);
        }

        return productInfoBuff;
    }

    public static byte[] readINvramInfo(int snLength) {

        String readInfo = null;
        byte[] productInfoBuff = null;
        try {
            INvram mNvram = INvram.getService();
            if (null == mNvram) {
                DswLog.e(TAG, "getPadioProxy: mnvram == null");
            }else{
                readInfo = mNvram.readFileByName(ProinfoUtil.getProinfoPath(),snLength);
                productInfoBuff = HexDump.hexStringToByteArray(readInfo.substring(0, readInfo.length() - 1));
            }
        } catch (RemoteException ex) {
            DswLog.e(TAG, ex.toString());
        }

        for (int i = 0; i < snLength; i++) {
            DswLog.i(TAG, "getProductInfo[" + i + "]=" + productInfoBuff[i]);
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
                String buff = null;
                buff = mNvram.readFileByName(ProinfoUtil.getProinfoPath(),TestResult.SN_LENGTH);
                ArrayList<Byte> dataArray = new ArrayList<Byte>(TestResult.SN_LENGTH);
                for (int i = 0; i < TestResult.SN_LENGTH; i++) {
                    dataArray.add(i, new Byte(sn_buff[i]));
                }
                wflag = mNvram.writeFileByNamevec(ProinfoUtil.getProinfoPath(),TestResult.SN_LENGTH, dataArray);
                String readInfo = mNvram.readFileByName(ProinfoUtil.getProinfoPath(),TestResult.SN_LENGTH);
                DswLog.i(TAG, "writeFileByNamevec flag="+wflag);
                DswLog.i(TAG, "getINvramInfo mNvram write & read ="+readInfo);
            }
            DswLog.e(TAG, "nvram writeToNvramInfo flag="+wflag);
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.e(TAG, "nvram write Error:"+ e.getMessage() + ":" + e.getCause());
            return;
        }
    }

    public static void writeToNvramInfo(byte[] sn_buff, int snLength) {
        int wflag = 0;
        try {
            INvram mNvram = INvram.getService();

            if (null == mNvram) {
                DswLog.e(TAG, "ProinfoUtil getPadioProxy: mnvram == null");
            }else{
                DswLog.i(TAG, "ProinfoUtil mNvram begin write");
                String buff = null;
                buff = mNvram.readFileByName(ProinfoUtil.getProinfoPath(),snLength);
                ArrayList<Byte> dataArray = new ArrayList<Byte>(snLength);
                for (int i = 0; i < snLength; i++) {
                    dataArray.add(i, new Byte(sn_buff[i]));
                }
                wflag = mNvram.writeFileByNamevec(ProinfoUtil.getProinfoPath(),snLength, dataArray);
                String readInfo = mNvram.readFileByName(ProinfoUtil.getProinfoPath(),snLength);
                DswLog.i(TAG, "ProinfoUtil writeFileByNamevec flag="+wflag);
            }
            DswLog.e(TAG, "ProinfoUtil nvram writeToNvramInfo flag="+wflag);
        } catch (Exception e) {
            e.printStackTrace();
            DswLog.e(TAG, "ProinfoUtil nvram write Error:"+ e.getMessage() + ":" + e.getCause());
            return;
        }
    }
    //**************** Android O read & write INvramInfo end ****************//

    public static byte[] getNewSN(int position, String value, byte[] sn) {
        sn[position] = value.getBytes()[0];
        return sn;
    }
}