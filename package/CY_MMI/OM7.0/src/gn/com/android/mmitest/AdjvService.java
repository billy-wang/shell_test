package gn.com.android.mmitest;

import gn.com.android.mmitest.TestResult;

import gn.com.android.mmitest.TestUtils;

import java.io.IOException;

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
import android.app.Notification;
import android.app.PendingIntent;

import gn.com.android.mmitest.item.*;

public class AdjvService extends Service {

    private static final String TAG = "AdjvService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification status = new Notification(R.drawable.arrow, null,
                System.currentTimeMillis());
        status.flags |= Notification.FLAG_ONGOING_EVENT;
        //Gionee <GN_BSP_MMI> <chengq> <20170106> modify for ID 60451 begin
       /* status.setLatestEventInfo(this, "Scheduler Test running",
                "Scheduler Test running", null);*/
        //Gionee <GN_BSP_MMI> <chengq> <20170106> modify for ID 60451 end
        startForeground(1, status);
        DswLog.e(TAG, " start AdjvService");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
