
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.TestResult;

import gn.com.android.mmitest.TestUtils;
import java.io.IOException;
import android.os.AsyncResult;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.view.View.OnClickListener;
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
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;

public class NvService extends Service implements QcRilHookCallback {

    String TAG = "NvService";

    private String oFS = null;
    private String nFS = null;
    private Context context;
    private QcNvItems nvItems = null;

    // final Context mContext;
    private static final String NV_BACKUP_END = "android.intent.action.NV_BACKUP_END";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        Log.e(TAG, "ServiceonCreate");
        nvItems = new QcNvItems(this, this);

    }

    public void onQcRilHookReady() {

        try {
            Log.e(TAG, "onQcRilHookReady");
            oFS = nvItems.getFactoryResult();
            nFS = TestUtils.getNewSN(25, 'P', oFS);
            Log.e(TAG, "oFS =  " + oFS + " : " + oFS.length());
            Log.e(TAG, "nFS =  " + nFS + " : " + nFS.length());

            if (!oFS.equals(nFS)) {
                Log.e(TAG, "  write factory22222");
                nvItems.setFactoryResult(nFS + "0");
            }

            sendIntentLocked();
        } catch (Exception e) {

            // TODO: handle exception
            e.printStackTrace();
            Log.e(TAG, "e : " + e.getMessage());
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        // TODO Auto-generated method stub
        super.onStart(intent, startId);
        sendIntentLocked();
        Log.e(TAG, "ServiceonStart");
    }

    private void sendIntentLocked() {

        final Intent intent = new Intent(NV_BACKUP_END);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        Log.e(TAG, "sendBroadcast android.intent.action.NV_BACKUP_EN ");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.e(TAG, "ServiconDestroy");
    }

}
