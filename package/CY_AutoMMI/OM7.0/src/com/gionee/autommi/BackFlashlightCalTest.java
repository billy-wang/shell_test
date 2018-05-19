package com.gionee.autommi;

import android.os.Bundle;
import com.gionee.util.DswLog;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;

public class BackFlashlightCalTest extends BaseActivity {
    public static final String TAG = "BackFlashlightCalTest";
    private static final String START_CAMERA_ACTIVITY_BROADCAST = "autommi.engineermode.camera.start";
    private static final String END_CAMERA_ACTIVITY_BROADCAST = "autommi.engineermode.camera.end";
    private static final String GET_FLASHLIGHT_CAL_RESULT_BROADCAST = "autommi.back.flashlight.cal.result";
    private static final String GET_RESULT_DATA = "getResultData";
    private boolean mIsPass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        DswLog.e(TAG, "onCreate");

        IntentFilter filter = new IntentFilter();
        filter.addAction(END_CAMERA_ACTIVITY_BROADCAST);
        filter.addAction(GET_FLASHLIGHT_CAL_RESULT_BROADCAST);
        registerReceiver(mReceiver, filter);

        Intent localIntent;
        localIntent = new Intent(START_CAMERA_ACTIVITY_BROADCAST);
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Gionee zhangke 20160422 modify for CR01673305 start
        try {
            startActivityForResult(localIntent, 0);
        } catch (Exception e) {
            DswLog.e(TAG, "ActivityNotFoundException");
        }
        ((AutoMMI) getApplication()).recordResult(TAG, "", "-1");

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        DswLog.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        DswLog.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DswLog.e(TAG, "mReceiver:action=" + intent.getAction());
            if (GET_FLASHLIGHT_CAL_RESULT_BROADCAST.equals(intent.getAction())) {
                int data = intent.getIntExtra(GET_RESULT_DATA, 0);
                DswLog.e(TAG, "testdata="+data);
                testResult(data);
            } else if (END_CAMERA_ACTIVITY_BROADCAST.equals(intent.getAction())) {
                DswLog.e(TAG, "start finish");
                BackFlashlightCalTest.this.finish();
            }
        }
    };

    private void testResult(int data){
		TestResult tr = new TestResult();
		byte[] sn_buff = new byte[64];
		System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, 64);
        String tag = "F";
        if(data == 1){
            tag = "P";
        }
		sn_buff = tr.getNewSN(TestResult.MMI_BACK_FLASHLIGHT_CAL_TAG, tag, sn_buff);

        tr.writeToProductInfo(sn_buff);
        DswLog.i(TAG, "sn_buff[26]="+sn_buff[26]);

        ((AutoMMI)getApplication()).recordResult(TAG, "", data+"");
        
    }

}
