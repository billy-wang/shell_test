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
//Gionee zhangke 20161114 add for ID16169 begin
import android.os.PowerManager;
import android.content.Context;
//Gionee zhangke 20161114 add for ID16169 end
import android.app.StatusBarManager;

public class FrontCameraTest extends Activity implements OnClickListener {
    String TAG =  "FrontCameraTest";
    Button mRightBtn, mWrongBtn;
      private  boolean mFrontCamera = false;
    //Gionee zhangke 20161114 add for ID16169 begin
    PowerManager.WakeLock mWakeLock;
    //Gionee zhangke 20161114 add for ID16169 end
    private StatusBarManager sbm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        Log.e(TAG,"FrontCameraTest oncreate");
        sbm = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
        sbm.disable(StatusBarManager.DISABLE_RECENT); //mRightBtn.setVisibility(View.VISIBLE);
        //Gionee zhangke 20161114 add for ID16169 begin
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
        mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "GN_MMI");	
        mWakeLock.acquire();  
        //Gionee zhangke 20161114 add for ID16169 end

        Intent localIntent = new Intent("gn.com.android.mmitest.item.FrontCameraTest");
        Log.e(TAG,"FrontCameraTest sendintent gn.com.android.mmitest.item.FrontCameraTest");
        try {
        startActivityForResult(localIntent, 1);
        }
        catch (ActivityNotFoundException ex ) {

        }
        Log.e(TAG,"FrontCameraTest  startActivityForResult");
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"FrontCameraTest onResume ");
       
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG,"FrontCameraTest onPause ");
 
        }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy -- > sbm.disable(StatusBarManager.DISABLE_NONE) ");
        sbm.disable(StatusBarManager.DISABLE_NONE);     
    }    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FrontCameraTest.this.setContentView(R.layout.common_textview);
        Log.e(TAG,"FrontCameraTest onActivityResult" + "requestCode = " + requestCode + " resultCode = " + resultCode);
        mFrontCamera =false;
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        Button mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        if(data == null){
            mRightBtn.setEnabled(true);
            return;
        }
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

