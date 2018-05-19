package com.cydroid.autommi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.widget.Toast;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
//import  com.mediatek.telephony.TelephonyManagerEx;
import android.telephony.TelephonyManager; 

public class SimCardTest extends BaseActivity {
	public static final String TAG = "SimCardTest";
//	private TelephonyManagerEx phoneManagerEx;
	private StorageManager storageManager;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		TelephonyManager telephonyManager =
				(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		StringBuilder builder = new StringBuilder();
		final int simSlotNum = telephonyManager.getPhoneCount();
		for(int i=0; i<simSlotNum;i++) {
			if(i != 0)
				builder.append("|");
			int simState = telephonyManager.getSimState(i);
			builder.append("Sim").append(i+1).append(":").append(simState);
		}
		String result = builder.toString();
		DswLog.e(TAG, result);
		((AutoMMI)getApplication()).recordResult(TAG, result, Integer.toString(simSlotNum));
		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.finish();
	}
}
