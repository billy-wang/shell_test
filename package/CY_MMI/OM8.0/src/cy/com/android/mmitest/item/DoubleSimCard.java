package cy.com.android.mmitest.item;
import android.content.Context;
import android.os.Bundle;
import cy.com.android.mmitest.utils.DswLog;
import android.widget.Toast;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;
import android.telephony.TelephonyManager;
import java.util.List;
import cy.com.android.mmitest.utils.ProinfoUtil;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.utils.HelPerformUtil;
import cy.com.android.mmitest.bean.OnPerformListen;

public class DoubleSimCard extends BaseActivity implements OnClickListener,OnPerformListen{
	private StorageManager storageManager;
	private final static String TAG = "DoubleSimCard";
	private boolean isSimPass = false;
	private int count_storage = 0;
	private Button mRightBtn, mWrongBtn, mRestartBtn;
	private TextView tvTitle,tvContent;
	private final int delaytime = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TestUtils.checkToContinue(this);
		TestUtils.setCurrentAciticityTitle(TAG,this);
		setContentView(R.layout.common_textview);
		tvTitle = (TextView) findViewById(R.id.test_title);
		tvContent = (TextView) findViewById(R.id.test_content);
		mRightBtn = (Button) findViewById(R.id.right_btn);
		mWrongBtn = (Button) findViewById(R.id.wrong_btn);
		mRestartBtn = (Button) findViewById(R.id.restart_btn);
		tvTitle.setText(R.string.sim_doubletest_title);

		mRightBtn.setEnabled(false);
		mWrongBtn.setEnabled(false);
		mRestartBtn.setEnabled(false);

		mRightBtn.setOnClickListener(DoubleSimCard.this);
		mWrongBtn.setOnClickListener(DoubleSimCard.this);
		mRestartBtn.setOnClickListener(DoubleSimCard.this);
	}

	@Override
	protected void onStart() {
		DswLog.d(TAG,"onStart");
		super.onStart();

		uiHandler.sendMessageDelayed(uiHandler.obtainMessage(1), delaytime);

	}



	@Override
	protected void onStop() {
		DswLog.d(TAG,"onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		DswLog.d(TAG,"onDestroy");
		super.onDestroy();

		uiHandler.removeCallbacksAndMessages(null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.right_btn: {
				mRightBtn.setEnabled(false);
				mWrongBtn.setEnabled(false);
				mRestartBtn.setEnabled(false);
				TestUtils.rightPress(TAG, this);
				break;
			}

			case R.id.wrong_btn: {
				mRightBtn.setEnabled(false);
				mWrongBtn.setEnabled(false);
				mRestartBtn.setEnabled(false);
				TestUtils.wrongPress(TAG, this);
				break;
			}

			case R.id.restart_btn: {
				mRightBtn.setEnabled(false);
				mWrongBtn.setEnabled(false);
				mRestartBtn.setEnabled(false);
				TestUtils.restart(this, TAG);
				break;
			}
		}

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return true;
	}

	private void onSimCardTest() {
		DswLog.d(TAG,"DoubleSimCard");

		boolean sim1_status = false;
		boolean sim2_status = false;

		TelephonyManager telephonyManager =	(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		final int simSlotNum = telephonyManager.getPhoneCount();
		for(int i=0; i<simSlotNum; i++) {
			int simState = telephonyManager.getSimState(i);
			if (i == 0) {
				builder.append("SimCard1:");
				if (TelephonyManager.SIM_STATE_READY == simState) sim1_status = true;
				else sim1_status = false;
			}
			if (i == 1) {
				builder.append("SimCard2:");
				if (TelephonyManager.SIM_STATE_READY == simState) sim2_status = true;
				else sim2_status = false;
			}

			builder.append(toSimState(simState)).append("\n");
		}
		String result = builder.toString();
		tvContent.setText(result);
		DswLog.d(TAG, "SimCard result=" + result + " simSlotNum="+simSlotNum);
		isSimPass = sim1_status && sim2_status;
	}

	private String toSimState(int state) {
		StringBuffer sb = new StringBuffer();
		switch (state) {
			case TelephonyManager.SIM_STATE_ABSENT :sb.append(getString(R.string.sim_sdcard_sim_none));break;
			case TelephonyManager.SIM_STATE_UNKNOWN :sb.append(getString(R.string.sim_sdcard_sim_unknow));break;
			case TelephonyManager.SIM_STATE_NETWORK_LOCKED :sb.append(getString(R.string.sim_sdcard_sim_netpin));break;
			case TelephonyManager.SIM_STATE_PIN_REQUIRED :sb.append(getString(R.string.sim_sdcard_sim_pin));break;
			case TelephonyManager.SIM_STATE_PUK_REQUIRED :sb.append(getString(R.string.sim_sdcard_sim_puk));break;
			case TelephonyManager.SIM_STATE_READY :sb.append(getString(R.string.sim_sdcard_sim_well));break;
		}
		return sb.toString();
	}


	private Handler uiHandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case 1:
					DswLog.d(TAG, "start test");
					onSimCardTest();

					if(isSimPass){
						DswLog.d(TAG, "test OK");
						mWrongBtn.setEnabled(false);
						mRightBtn.setEnabled(true);

						if (TestUtils.mIsAutoMode) {
							HelPerformUtil.getInstance().performDelayed(DoubleSimCard.this, HelPerformUtil.delayTime);
						}
					}else {
						DswLog.d(TAG, "test Fail");
						mWrongBtn.setEnabled(true);
						mRightBtn.setEnabled(false);
					}
					mRestartBtn.setEnabled(true);
					break;
			}

			uiHandler.sendMessageDelayed(uiHandler.obtainMessage(1), delaytime);
		}
	};

	@Override
	public void OnButtonPerform() {
		HelPerformUtil.getInstance().unregisterPerformListen();
		DswLog.i(TAG, "OnButtonPerform");
		mRightBtn.performClick();
	}
}
