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

public class FrontCameraTest extends Activity implements OnClickListener {
    String TAG = "FrontCameraTest";
    Button mRightBtn, mWrongBtn, mRestartBtn;
    private StatusBarManager sbm;
    private boolean frontCameraFlag = false;
    private Intent it;
    private String killKey = "gn.com.android.mmitest.killcam";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "FrontCameraTest oncreate");
        //TestUtils.checkToContinue(this);
        TestUtils.setWindowFlags(this);
        sbm = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.disable(StatusBarManager.DISABLE_RECENT);//mRightBtn.setVisibility(View.VISIBLE);
        FrontCameraTest.this.setContentView(R.layout.common_textview);
        it = this.getIntent();
        if(it != null){
            frontCameraFlag=  it.getBooleanExtra("as", false);
            IntentFilter filter = new IntentFilter();
            filter.addAction(killKey);
            registerReceiver(mReceiver, filter);             
        }
        Log.d(TAG,"frontCameraFlag = " + frontCameraFlag);          
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setVisibility(View.INVISIBLE);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        //Gionee zhangke 20160811 add for CR01745293 start
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        if(frontCameraFlag){
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }        
        mRestartBtn.setOnClickListener(this);        
        Intent localIntent;
        localIntent = new Intent("gn.com.android.mmitest.item.FrontCameraTest");
        Log.e(TAG,"FrontCameraTest sendintent gn.com.android.mmitest.item.FrontCameraTest");
        //Gionee zhangke 20160422 modify for CR01673305 start
        try {
            startActivityForResult(localIntent, 0);
        } catch (ActivityNotFoundException ex) {

        }
        //Gionee zhangke 20160422 modify for CR01673305 end

        Log.e(TAG, "FrontCameraTest  startActivityForResult");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "FrontCameraTest onResume ");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "FrontCameraTest onPause ");

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
        Log.e(TAG, "FrontCameraTest onActivityResult" + "requestCode = " + requestCode + " resultCode = " + resultCode);
        if(data == null){
            mRightBtn.setEnabled(true);
            mRightBtn.setVisibility(View.VISIBLE);
            return;
        }
        boolean isCapture = data.getBooleanExtra("capture", true);
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
                if(frontCameraFlag){
                    TestUtils.asResult(TAG,"","0");
                }                
                TestUtils.wrongPress(TAG, this);
                break;
            }
            case R.id.right_btn: {
                if(frontCameraFlag){
                    TestUtils.asResult(TAG,"","1");
                }                
                TestUtils.rightPress(TAG, this);
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
                FrontCameraTest.this.finish();
            }
        }
    };
}


