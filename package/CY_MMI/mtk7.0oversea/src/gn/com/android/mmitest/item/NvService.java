package gn.com.android.mmitest.item;

import gn.com.android.mmitest.TestResult;

import gn.com.android.mmitest.TestUtils;

import java.io.IOException;
import java.util.Arrays;

import android.os.SystemProperties;

import android.os.AsyncResult;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.os.ServiceManager;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.content.Context;
import android.os.HandlerThread;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Toast;
//Gionee zhangke 20151013 add for CR01562456 start
import gn.com.android.mmitest.item.FeatureOption;
import gn.com.android.mmitest.NvRAMAgent;
//Gionee zhangke 20151013 add for CR01562456 end

public class NvService extends Service {
    String TAG = "NvService";
    private static final int EVENT_RESPONSE_SN_WRITE = 11,
            EVENT_RESPONSE_SN_READ = 22;
    private String newSN = null;
    private String oldSn = null;
    // final Context mContext;
    private static final String NV_BACKUP_END = "android.intent.action.NV_BACKUP_END";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "ServiceonCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e(TAG, "ServiceonStart");

        //Gionee zhangke 20151013 modify for CR01562456 start 
        if (FeatureOption.BACKUP_TO_PRODUCTINFO) {
            if (updateSN()) {
                sendIntentLocked();
                Log.e(TAG, "FeatureOption.BACKUP_TO_PRODUCTINFO == true");
            }
        } else {
            //Gionee zhangke 20151026 add for CR01574155 start
//            Toast.makeText(this, "test ", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Please turn on MTK_PRODUCT_INFO_SUPPORT", Toast.LENGTH_LONG).show();
            Log.e(TAG, "FeatureOption.BACKUP_TO_PRODUCTINFO == false");
            //Gionee zhangke 20151026 add for CR01574155 end
        }
        //Gionee zhangke 20151013 modify for CR01562456 end
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "ServiconDestroy");
    }

    private void sendIntentLocked() {
        final Intent intent = new Intent(NV_BACKUP_END);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        Log.e(TAG, "send android.intent.action.NV_BACKUP_END");
        sendBroadcast(intent);
    }

    //Gionee zhangke 20151013 add for CR01562456 start 
    private String getSnNumber() {
        String snNumber = "";
        //Gionee zhangke 20151201 modify for CR01601602 start
        byte[] buff = getProductInfo();
        if (buff != null && buff.length != 0) {
            if (buff.length > 64) {
                snNumber = new String(getProductInfo(), 0, 64);
            } else {
                snNumber = new String(getProductInfo());
            }
            Log.i(TAG, "snNumber get from productinfo");
        } else {
            snNumber = SystemProperties.get("gsm.serial");
            Log.i(TAG, "snNumber get from gsm.serial");
        }
        //Gionee zhangke 20151201 modify for CR01601602 end

        return snNumber;
    }

    private boolean updateSN() {
        byte[] sn_buff = new byte[TestResult.SN_LENGTH];
        try {
            System.arraycopy(getProductInfo(), 0, sn_buff, 0, TestResult.SN_LENGTH);
            String oldSn = new String(sn_buff);
            if (oldSn == null || "".equals(oldSn)) {
                Log.i(TAG, "updateSN oldSn =" + oldSn);
                //Gionee zhangke 20151026 add for CR01574155 start
                Toast.makeText(this, "Error: SN is null", Toast.LENGTH_LONG).show();
                //Gionee zhangke 20151026 add for CR01574155 end
                return false;
            }
        } catch (Exception e) {
            Log.i(TAG, "updateSN Exception =" + e.getMessage());
            //Gionee zhangke 20151026 add for CR01574155 start
            Toast.makeText(this, "Error: SN is null", Toast.LENGTH_LONG).show();
            //Gionee zhangke 20151026 add for CR01574155 end
            return false;

        }
        sn_buff = TestResult.getNewSN(TestResult.MMI_FACTORY_RESET_TAG, "P", sn_buff);

        Log.d(TAG, "updateSN: sn="+ Arrays.toString(sn_buff));
        Log.d(TAG, "updateSN: sn="+ sn_buff.toString());
        TestResult.writeToProductInfo(sn_buff);

        return true;
    }

    private boolean isFactoryResetBoot() {
        byte[] productInfoBuff = getProductInfo();
        if (productInfoBuff != null && productInfoBuff.length > 48) {
            Log.i(TAG, "isFactoryResetBoot:productInfoBuff[48]=" + productInfoBuff[48]);
            return 'P' == productInfoBuff[48];
        }
        return false;
    }

    private byte[] getProductInfo() {
        IBinder binder = null;
        byte[] productInfoBuff = null;
        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            Log.e(TAG, "getService	NvRAMAgent binder is null");
        }
        if (null != binder) {
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                Log.i(TAG, "getProductInfo NvRAMAgent read");
                productInfoBuff = agent.readFileByName(TestResult.PRODUCT_INO_NAME);

            } catch (RemoteException ex) {
                Log.e(TAG, ex.toString());
            }
        }
        return productInfoBuff;
    }
    //Gionee zhangke 20151013 add for CR01562456 end

}
