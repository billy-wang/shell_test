package gn.com.android.mmitest.item;

import android.app.Activity;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.os.SystemProperties;
import android.content.ActivityNotFoundException;
import android.app.StatusBarManager;
import android.content.Context;
import gn.com.android.mmitest.TestUtils;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

public class BackCameraTest extends Activity implements OnClickListener {
    String TAG = "BackCameraTest";
    Button mRightBtn, mWrongBtn, mRestartBtn;
    private boolean mBackCamera = false;
    private StatusBarManager sbm;
    private boolean backCameraFlag = false;
    private Intent it;
    private String killKey = "gn.com.android.mmitest.killcam";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "BackCameraTest oncreate");
        //TestUtils.checkToContinue(this);
        TestUtils.setWindowFlags(this);
        BackCameraTest.this.setContentView(R.layout.common_textview);
        sbm = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.disable(StatusBarManager.DISABLE_RECENT); //mRightBtn.setVisibility(View.VISIBLE);
        it = this.getIntent();
        if(it != null){
            backCameraFlag=  it.getBooleanExtra("as", false);
            IntentFilter filter = new IntentFilter();
            filter.addAction(killKey);
            registerReceiver(mReceiver, filter);
        }
        Log.d(TAG,"backCameraFlag = " + backCameraFlag);        
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setVisibility(View.INVISIBLE);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        if(backCameraFlag){
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }
        mRestartBtn.setOnClickListener(this);        
        String ver = SystemProperties.get("ro.gn.gnromvernumber");
        Intent localIntent;
        localIntent = new Intent("gn.com.android.mmitest.item.BackCameraTest");
        Log.e(TAG, "BackCameraTest sendintent gn.com.android.mmitest.item.BackCameraTest"); 
        //Gionee zhangke 20160422 modify for CR01673305 start    
        try {
            startActivityForResult(localIntent, 0);
        } catch (ActivityNotFoundException e) {
            // TODO: handle exception
        }
        //Gionee zhangke 20160422 modify for CR01673305 end
        Log.e(TAG, "BackCameraTest  startActivityForResult");       
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "BackCameraTest onResume ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "BackCameraTest onPause ");

    }
    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "BackCameraTest onStop ");

    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy -- > sbm.disable(StatusBarManager.DISABLE_NONE) ");
        sbm.disable(StatusBarManager.DISABLE_NONE);
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.e(TAG, "unregisterReceiver:Exception=" + e.getMessage());
        }        
    }    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG,"BackCameraTest onActivityResult" + "requestCode = " + requestCode + " resultCode = " + resultCode);
        if(data == null){
            mRightBtn.setEnabled(true);
            mRightBtn.setVisibility(View.VISIBLE);
            return;
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170410> modify for ID 105202 begin
        //boolean isCapture = data.getBooleanExtra("capture", true);
        boolean isCapture = data.getBooleanExtra("capture", false);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170410> modify for ID 105202 end

        if(isCapture){
            mRightBtn.setEnabled(true);
            mRightBtn.setVisibility(View.VISIBLE);
        }
        //Gionee zhangke 20160811 add for CR01745293 end


    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.wrong_btn: {
                if(backCameraFlag){
                    TestUtils.asResult(TAG,"","0");
                }                
                TestUtils.wrongPress(TAG, this);
                break;
            }
            case R.id.right_btn: {
                if(backCameraFlag){
                    TestUtils.asResult(TAG,"","1");
                }                
                TestUtils.rightPress(TAG, this);
                Log.d(TAG,"onClick  RightButton");
                break;
            }
            //Gionee zhangke 20160811 add for CR01745293 start
            case R.id.restart_btn: {
                TestUtils.restart(this, TAG);
                break;
            }
            //Gionee zhangke 20160811 add for CR01745293 end
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mReceiver:action=" + intent.getAction());
            if (killKey.equals(intent.getAction())) {
                BackCameraTest.this.finish();
            }
        }
    };
}

