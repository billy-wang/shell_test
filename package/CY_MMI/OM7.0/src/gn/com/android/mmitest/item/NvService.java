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
import gn.com.android.mmitest.utils.DswLog;
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
import android.widget.Toast;
import gn.com.android.mmitest.GnMMITest;
import gn.com.android.mmitest.R;
import android.os.Handler;
//Gionee zhangke 20151013 add for CR01562456 end

public class NvService extends Service {
    String TAG = "NvService";
    private static final int EVENT_RESPONSE_SN_WRITE = 11,
            EVENT_RESPONSE_SN_READ = 22;
    private String newSN = null;
    private String oldSn = null;
    // final Context mContext;
    private static final String NV_BACKUP_END = "android.intent.action.NV_BACKUP_END";
    private static final int MSG_EXE_FACTORY_SUCESS = 0x01;
    private static final int MSG_EXE_FACTORY_FAIL = 0x02;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_EXE_FACTORY_SUCESS:
                    sendIntentLocked();
                    sendBroadcast(new Intent(GnMMITest.KILL_MMI_BROADCAST));
                    stopSelf();
                    break;
                case MSG_EXE_FACTORY_FAIL:
                    sendBroadcast(new Intent(GnMMITest.KILL_MMI_BROADCAST));
                    stopSelf();
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DswLog.e(TAG, "ServiceonCreate");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        DswLog.e(TAG, "ServiceonStart");

        //Gionee zhangke 20151013 modify for CR01562456 start 
        if (FeatureOption.BACKUP_TO_PRODUCTINFO) {
            if (updateSN()) {
                Toast.makeText(getApplicationContext(), getString(R.string.res_exefactory_success),Toast.LENGTH_SHORT).show();
                GnMMITest.EraseSD();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_EXE_FACTORY_SUCESS),2500);
            }else {
                Toast.makeText(getApplicationContext(), getString(R.string.res_exefactory_fail),Toast.LENGTH_SHORT).show();
                DswLog.e(TAG, "写入MMI标志位失败，发送退出MMI广播");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_EXE_FACTORY_FAIL),2500);
            }
        } else {
            //Gionee zhangke 20151026 add for CR01574155 start
//            Toast.makeText(this, "test ", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Please turn on MTK_PRODUCT_INFO_SUPPORT", Toast.LENGTH_LONG).show();
            DswLog.e(TAG, "FeatureOption.BACKUP_TO_PRODUCTINFO == false");
            //Gionee zhangke 20151026 add for CR01574155 end
        }
        //Gionee zhangke 20151013 modify for CR01562456 end
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DswLog.e(TAG, "ServiconDestroy");
    }

    private void sendIntentLocked() {
        final Intent intent = new Intent(NV_BACKUP_END);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        DswLog.e(TAG, "send android.intent.action.NV_BACKUP_END");
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
            DswLog.i(TAG, "snNumber get from productinfo");
        } else {
            snNumber = SystemProperties.get("gsm.serial");
            DswLog.i(TAG, "snNumber get from gsm.serial");
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
                DswLog.i(TAG, "### updateSN oldSn =" + oldSn);
                //Gionee zhangke 20151026 add for CR01574155 start
                Toast.makeText(this, "Error: SN is null", Toast.LENGTH_LONG).show();
                //Gionee zhangke 20151026 add for CR01574155 end
                return false;
            }
        } catch (Exception e) {
            DswLog.i(TAG, "### updateSN Exception =" + e.getMessage());
            //Gionee zhangke 20151026 add for CR01574155 start
            Toast.makeText(this, "Error: SN is null", Toast.LENGTH_LONG).show();
            //Gionee zhangke 20151026 add for CR01574155 end
            return false;

        }
        sn_buff = TestResult.getNewSN(TestResult.MMI_FACTORY_RESET_TAG, "P", sn_buff);
        TestResult.writeToProductInfo(sn_buff);

        return isFactoryResetBoot();
    }

    private boolean isFactoryResetBoot() {
        byte[] productInfoBuff = getProductInfo();
        if (productInfoBuff != null && productInfoBuff.length > 48) {
            DswLog.i(TAG, "isFactoryResetBoot:productInfoBuff[48]=" + productInfoBuff[48]);
            return 'P' == productInfoBuff[48];
        }
        return false;
    }

    private byte[] getProductInfo() {
        IBinder binder = null;
        byte[] productInfoBuff = null;
        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            DswLog.e(TAG, "getService	NvRAMAgent binder is null");
        }
        if (null != binder) {
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                DswLog.i(TAG, "getProductInfo NvRAMAgent read");
                productInfoBuff = agent.readFileByName(TestResult.PRODUCT_INO_NAME);

            } catch (RemoteException ex) {
                DswLog.e(TAG, ex.toString());
            }
        }
        return productInfoBuff;
    }
    //Gionee zhangke 20151013 add for CR01562456 end

}
