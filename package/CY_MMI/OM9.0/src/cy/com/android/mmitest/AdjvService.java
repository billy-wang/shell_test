package cy.com.android.mmitest;

import cy.com.android.mmitest.TestResult;

import cy.com.android.mmitest.TestUtils;

import java.io.IOException;

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
import android.app.Notification;
import android.app.PendingIntent;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import cy.com.android.mmitest.item.*;

public class AdjvService extends Service {

    private static final String TAG = "AdjvService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NotificationChannel channel = new NotificationChannel("id","name", NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        /*Notification status = new Notification(R.drawable.arrow, null,
                System.currentTimeMillis());
        status.flags |= Notification.FLAG_ONGOING_EVENT;*/
        //Gionee <GN_BSP_MMI> <chengq> <20170106> modify for ID 60451 begin
       /* status.setLatestEventInfo(this, "Scheduler Test running",
                "Scheduler Test running", null);*/
        //Gionee <GN_BSP_MMI> <chengq> <20170106> modify for ID 60451 end


        Notification notification = new Notification.Builder(this,"id").build();

        startForeground(1, notification);
        DswLog.e(TAG, "start AdjvService notification");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onDestroy() {
        DswLog.e(TAG, "stop AdjvService");
        super.onDestroy();
    }

}
