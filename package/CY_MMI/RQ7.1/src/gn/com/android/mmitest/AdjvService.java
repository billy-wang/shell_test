package gn.com.android.mmitest;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AdjvService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Notification status = new Notification(R.drawable.arrow, null,
				System.currentTimeMillis());
		status.flags |= Notification.FLAG_ONGOING_EVENT;
		status.setLatestEventInfo(this, "Scheduler Test running",
				"Scheduler Test running", null);
		startForeground(1, status);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
