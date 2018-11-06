package cy.com.android.mmitest.item;

import cy.com.android.mmitest.TestResult;
import cy.com.android.mmitest.utils.FlagNvramUtil;
import cy.com.android.mmitest.TestUtils;

import java.io.IOException;
import java.util.Arrays;

import android.os.SystemProperties;

import android.os.AsyncResult;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import cy.com.android.mmitest.utils.DswLog;
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
import cy.com.android.mmitest.item.FeatureOption;
import cy.com.android.mmitest.NvRAMAgent;
import android.widget.Toast;
import cy.com.android.mmitest.CyMMITest;
import cy.com.android.mmitest.R;
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
                    sendBroadcast(new Intent(CyMMITest.KILL_MMI_BROADCAST));
                    stopSelf();
                    break;
                case MSG_EXE_FACTORY_FAIL:
                    sendBroadcast(new Intent(CyMMITest.KILL_MMI_BROADCAST));
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
        final int action = intent.getIntExtra("setFactoryFlag", -1);
        DswLog.e(TAG, "ServiceonStart aciton="+action);
        switch (action) {
            case FlagNvramUtil.WRITE_FACTORY_FLAG:
                factoryWriteFlag();
                break;
            case FlagNvramUtil.CLEAR_FACTORY_FLAG:
                factoryClearFlag();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DswLog.e(TAG, "ServiconDestroy");
    }

    private void factoryWriteFlag() {
        if (FeatureOption.BACKUP_TO_PRODUCTINFO) {
            if (updateSN()) {
                Toast.makeText(getApplicationContext(), getString(R.string.res_exefactory_success),Toast.LENGTH_SHORT).show();
                CyMMITest.EraseSD();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_EXE_FACTORY_SUCESS),2500);
            }else {
                Toast.makeText(getApplicationContext(), getString(R.string.res_exefactory_fail),Toast.LENGTH_SHORT).show();
                DswLog.e(TAG, "写入MMI标志位失败，发送退出MMI广播");
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_EXE_FACTORY_FAIL),2500);
            }
        } else {
            Toast.makeText(this, "Please turn on MTK_PRODUCT_INFO_SUPPORT", Toast.LENGTH_LONG).show();
            DswLog.e(TAG, "FeatureOption.BACKUP_TO_PRODUCTINFO == false");
        }
    }

    private void factoryClearFlag() {
        byte[] sn_buff = FlagNvramUtil.readINvramInfo(TestResult.MMI_FACTORY_RESET_TAG + 1);
        sn_buff = FlagNvramUtil.getNewSN(TestResult.MMI_FACTORY_RESET_TAG, "0", sn_buff);
        FlagNvramUtil.writeToNvramInfo(sn_buff,TestResult.MMI_FACTORY_RESET_TAG + 1);

    }

    private void sendIntentLocked() {
        final Intent intent = new Intent(NV_BACKUP_END);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        DswLog.e(TAG, "send android.intent.action.NV_BACKUP_END");
        sendBroadcast(intent);
    }

    private boolean updateSN() {
        byte[] sn_buff = new byte[TestResult.SN_LENGTH];
        try {
            System.arraycopy(FlagNvramUtil.readINvramInfo(), 0, sn_buff, 0, TestResult.SN_LENGTH);
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
        sn_buff = FlagNvramUtil.getNewSN(TestResult.MMI_FACTORY_RESET_TAG, "P", sn_buff);
        FlagNvramUtil.writeToNvramInfo(sn_buff);
        return isFactoryResetBoot();
    }

    private boolean isFactoryResetBoot() {
        byte[] productInfoBuff = FlagNvramUtil.readINvramInfo();
        if (productInfoBuff != null && productInfoBuff.length > 48) {
            DswLog.i(TAG, "isFactoryResetBoot:productInfoBuff[48]=" + productInfoBuff[48]);
            return 'P' == productInfoBuff[48];
        }
        return false;
    }
    //Gionee zhangke 20151013 add for CR01562456 end

}
