package com.gionee.autommi;


import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import com.zyt.jni.common.ZytJniInterface;
import com.gionee.autommi.R;
import com.gionee.autommi.TestUtils;

import java.io.BufferedReader;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.os.SystemClock;


public class A3VersionTest extends Activity  {

    private TextView mCurrent;
    private TextView mResult;
    private TextView mVersion;
    private String cosCodeBinName = "cos_code.bin"; // 1060 1070 1202 2002
    private final String PASS = "pass";
    private final String FAILURE = "failure";
    private final String WAITTING = "waitting";
    private boolean clickFlag = true;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private static final String TAG = "A3VersionTest";
	private static final String  RESULT_PATH = "/data/misc/gionee/veba3/";//"/data/app/";
	private String mCurrentVersion;
	private String mVersionString;
	private String testResult;
	private String result;
	private String mUpdate_version;
	private String current_version;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.a3_test);
		testResult = "0";
		result = "";
        Intent intent = getIntent();
        if(intent != null){
            mCurrentVersion = intent.getStringExtra("A3Version");
        }
		Log.i(TAG, "onCreate mCurrentVersion =" + mCurrentVersion);
        mCurrent = (TextView) findViewById(R.id.current_version);
        mResult = (TextView) findViewById(R.id.update_test_result);
		mVersion = (TextView) findViewById(R.id.update_version);
        init();
    }

    private void appendText(TextView tv , String a) {
        String origin = tv.getText().toString();
        tv.setText(origin + a);
    }
	
    @Override
    public void onStart() {
        super.onStart();
        final String toast = getResources().getString(R.string.wait_message);
        //final File cosfile = new File(Environment.getExternalStorageDirectory(), cosCodeBinName);
        final File cosfile = new File(RESULT_PATH,cosCodeBinName);

		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
		
		new Thread(new Runnable() {
		
			@Override
			public void run() {
				SystemClock.sleep(1500);
				int ret3 = ZytJniInterface.updateAndTestDriver(A3VersionTest.this, cosfile);
				String version = ZytJniInterface.getChipVersion(A3VersionTest.this);
				//Gionee <GN_BSP_AutoMMI><lifeilong><20161212> modify for ID 44625 begin
				//mUpdate_version = version.substring(0,25);
				mUpdate_version = version.substring(10,14);
				//Gionee <GN_BSP_AutoMMI><lifeilong><20161212> modify for ID 44625 end
				Log.e(TAG,"mUpdate_version = " + mUpdate_version);
				Log.w(TAG, "update and test:" + ret3);
				Message msg = handler.obtainMessage();
				msg.what = UPDATE_TEST;
				msg.obj = ret3;
				handler.sendMessage(msg);
			}
		}).start();


    }


    private void init() {
        String version = ZytJniInterface.getChipVersion(this);
        if (version != null) {
			String versionString = new String(version);
			current_version = versionString.substring(0,25) + versionString.substring(32,versionString.length());
			Log.e(TAG,"current_version = " + mCurrentVersion );
            appendText(mCurrent,getResources().getString(R.string.current_version) + mCurrentVersion + "\n");
        } else {
            appendText(mCurrent,getResources().getString(R.string.current_version) + " : is null");
        }
    }

    private final int UPDATE_TEST = 3;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case UPDATE_TEST:
                int ret3 = (Integer) msg.obj;
                if (ret3 == ZytJniInterface.UPDATE_COS_COMPUTING) {
                    Log.w(TAG, "please waitting,background have crypt");
					testResult = "0";
                    appendText(mResult,getResources().getString(R.string.update_wait));
                } else if (ret3 == ZytJniInterface.CHIP_IS_ABNORMAL) {
                    Log.w(TAG, "update or test failure");
					testResult = "0";
                    appendText(mResult,getResources().getString(R.string.update_failed));
                } else {
                    if(mCurrentVersion.equals(mUpdate_version)){
						appendText(mVersion,getResources().getString(R.string.update_version) + mUpdate_version + "\n");
						Log.e(TAG,"mUpdate_version = " + mUpdate_version );
						testResult = "1";
						appendText(mResult,getResources().getString(R.string.update_succ));
					}else{
						testResult = "0";
						appendText(mResult,getResources().getString(R.string.update_failed));
					}
   
                }
                
				((AutoMMI) getApplication()).recordResult(TAG, result, testResult);
                break;
            default:
                break;
            }
        }
    	};

   
}
