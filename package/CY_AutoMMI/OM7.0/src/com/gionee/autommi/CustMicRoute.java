package com.gionee.autommi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.media.AudioManager;
import com.gionee.util.DswLog;

public class CustMicRoute extends BaseActivity {
	private static final String EXTRA_STATE = "state";
	private static final String TAG = "CustMicRoute";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		Intent it = this.getIntent();
		String state = it.getStringExtra(EXTRA_STATE);
		if ("yes".equals(state)) {
			DswLog.d(TAG, "audio set AUTOMMI_HEADSET_NOMIC=1");
			am.setParameters("AUTOMMI_HEADSET_NOMIC=1");//模拟成插入耳机，这个耳机没有麦克输入，所有只能从主麦和副麦输入，从耳机输出
		}
		else if ("no".equals(state)) {
			DswLog.d(TAG, "audio set AUTOMMI_HEADSET_NOMIC=0");
			am.setParameters("AUTOMMI_HEADSET_NOMIC=0");
		}
		else {
			DswLog.d(TAG, "AutoMMI don't set AUTOMMI_HEADSET_NOMIC ");
		}
		this.finish();	
	}
}
