package com.gionee.autommi;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;

public class DualBackCameraTest extends BaseActivity {
    public static final String TAG = "DualBackCameraTest";
    private static final String START_CAMERA_ACTIVITY_BROADCAST = "com.gionee.autommi.dualbcamera.start";
    private static final String END_CAMERA_ACTIVITY_BROADCAST = "com.gionee.autommi.dualbcamera.end";
    private static final String GET_CAPTURE_RESULT_DATA_BROADCAST = "com.gionee.autommi.dualbcamera.result";
    private static final String GET_RESULT_DATA = "getResultData";
    private boolean mIsPass = false;
    private int testResult20;
    private int testResult90;
    private int deviceId;
    private String CAMERA_ID = "cameraID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

        IntentFilter filter = new IntentFilter();
        filter.addAction(END_CAMERA_ACTIVITY_BROADCAST);
        filter.addAction(GET_CAPTURE_RESULT_DATA_BROADCAST);
        registerReceiver(mReceiver, filter);
		
        Intent it = this.getIntent();
        if(it != null){
            deviceId = it.getIntExtra("cameraID", -1);
        }
        Log.e(TAG, "CAMERA_ID = " + deviceId);

        Intent localIntent;
        localIntent = new Intent();
        localIntent.setAction(Intent.ACTION_MAIN);
		localIntent.setClassName("com.mediatek.stereocamera","com.mediatek.stereocamera.StereoCamera");
        localIntent.putExtra("cameraID",deviceId);
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Gionee zhangke 20160422 modify for CR01673305 start
        try {
            startActivityForResult(localIntent, 0);
        } catch (Exception e) {
            Log.e(TAG, "ActivityNotFoundException");
            Log.v(TAG, Log.getStackTraceString(e));
        }
        ((AutoMMI) getApplication()).recordResult(TAG, "", "0");

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
        Log.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    /*
    byte[16]:
	<Distance>|<Result of Distance>|<GEO level>|<black boundary>|<num>|<TOP>|<RIG>|<BOT>|<LEF>
	|<PHO Level>|<DIFF>|<R Score>|<G Score>|<B Score>|<CHA Check>|<CHA Score>

	*/
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mReceiver:action=" + intent.getAction());
            if (GET_CAPTURE_RESULT_DATA_BROADCAST.equals(intent.getAction())) {
                int[] data = intent.getIntArrayExtra(GET_RESULT_DATA);
                Log.i(TAG, "data.length="+data.length);
                //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170428> add for ID 127022 begin
                if (data != null ) {
                //Gionee <GN_BSP_AutoMMI> <lifeilong> <20170428> add for ID 127022 end
                    for (int i = 0; i < data.length; i++) {
                        Log.i(TAG, "data[" + i + "]=" + data[i]);
                    }
                    testResult(data);
                }
            } else if (END_CAMERA_ACTIVITY_BROADCAST.equals(intent.getAction())) {
                Log.e(TAG, "start finish");
                DualBackCameraTest.this.finish();
            }
        }
    };

    private void testResult(int[] data){
        StringBuilder content = new StringBuilder();
        if(data[0] == 20){
            if(data[2] != 2 && data[9] != 2 && data[14] == 0){
                testResult20 = 1;
            }else{
                testResult20 = 0;
            }
            data[1] = testResult20;
        }else if(data[0] == 90){
            if(data[2] != 2 && data[9] != 2 && data[14] == 0){
                testResult90 = 1;
            }else{
                testResult90 = 0;
            }
            data[1] = testResult90;
        }
        for(int i=0; i<data.length; i++){
            if(i == data.length - 1){
                content.append(data[i]);
            }else{
                content.append(data[i]+"|");
            }
        }
        Log.i(TAG, "testResult:content="+content);
        if(testResult20 == 1 && testResult90 == 1){
            writeProductInfoData(true);
            ((AutoMMI)getApplication()).recordResult(TAG, content.toString(), "1");
        }else{
            writeProductInfoData(false);
            ((AutoMMI)getApplication()).recordResult(TAG, content.toString(), "0");
        }
    }

    
    private void writeProductInfoData(boolean isTestSuccess){
		TestResult tr = new TestResult();
		byte[] sn_buff = new byte[64];
		System.arraycopy(tr.getProductInfo(), 0, sn_buff, 0, 64);
        String tag = "F";
        if(isTestSuccess){
            tag = "P";
        }
		sn_buff = tr.getNewSN(TestResult.MMI_DUAL_BACK_CAMERA_TAG, tag, sn_buff);

        tr.writeToProductInfo(sn_buff);
        Log.i(TAG, "sn_buff[28]="+sn_buff[28]);
    }

}
