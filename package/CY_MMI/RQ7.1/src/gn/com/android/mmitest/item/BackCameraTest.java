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
import android.content.ActivityNotFoundException;
import android.os.PowerManager;
import android.content.Context;
import android.app.StatusBarManager;

public class BackCameraTest extends Activity implements OnClickListener {
    String TAG = "BackCameraTest";
    Button mRightBtn, mWrongBtn;
    private boolean mBackCamera = false;
    //Gionee zhangke 20161114 add for ID16169 begin
    PowerManager.WakeLock mWakeLock;
    //Gionee zhangke 20161114 add for ID16169 end
    private StatusBarManager sbm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        Log.e(TAG, "BackCameraTest oncreate");
        sbm = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.disable(StatusBarManager.DISABLE_RECENT); //mRightBtn.setVisibility(View.VISIBLE);        
        //Gionee zhangke 20161114 add for ID16169 begin
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GN_MMI");	
        mWakeLock.acquire();  
        //Gionee zhangke 20161114 add for ID16169 end

        Intent localIntent = new Intent("gn.com.android.mmitest.item.BackCameraTest");
        Log.e(TAG, "BackCameraTest sendintent gn.com.android.mmitest.item.BackCameraTest");
        try {

            startActivityForResult(localIntent, 1);
        } catch (ActivityNotFoundException e) {
            // TODO: handle exception
        }
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
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy -- > sbm.disable(StatusBarManager.DISABLE_NONE) ");
        sbm.disable(StatusBarManager.DISABLE_NONE);     
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        BackCameraTest.this.setContentView(R.layout.common_textview);
        Log.e(TAG, "BackCameraTest onActivityResult" + "requestCode = " + requestCode + " resultCode = " + resultCode + " data = " + data);
        mBackCamera = false;
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        Button mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170310> modify for ID 82143 begin
        if(data == null){
            mRightBtn.setEnabled(true);
            return;
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170310> modify for ID 82143 end
        boolean isCapture = data.getBooleanExtra("capture", true);
        if(isCapture){
            mRightBtn.setEnabled(true);
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.wrong_btn: {
                //Gionee zhangke 20161114 add for ID16169 begin
                if(mWakeLock != null){
                    mWakeLock.release();
                }
                //Gionee zhangke 20161114 add for ID16169 end

                TestUtils.wrongPress(TAG, this);
                break;
            }
            case R.id.right_btn: {
                //Gionee zhangke 20161114 add for ID16169 begin
                if(mWakeLock != null){
                    mWakeLock.release();
                }
                //Gionee zhangke 20161114 add for ID16169 end

                TestUtils.rightPress(TAG, this);
                break;
            }
            case R.id.restart_btn: {
                //Gionee zhangke 20161114 add for ID16169 begin
                if(mWakeLock != null){
                    mWakeLock.release();
                }
                //Gionee zhangke 20161114 add for ID16169 end

                TestUtils.restart(this, TAG);
                break;
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}

