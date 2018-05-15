package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;

import android.app.Activity;
import gn.com.android.mmitest.utils.DswLog;
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

public class FrontCameraTest2 extends BaseActivity implements OnClickListener {
    String TAG = "FrontCameraTest2";
    Button mRightBtn, mWrongBtn;
    private boolean mFrontCamera = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开前摄像头2 @" + Integer.toHexString(hashCode()));
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        // Gionee xiaolin 21021203 modify for CR00738317 start
        TestUtils.setCurrentAciticityTitle(TAG,this);
        String ver = SystemProperties.get("ro.gn.gnromvernumber");
        Intent localIntent;
        localIntent = new Intent("gn.com.android.mmitest.item.FrontCameraTest2");
        DswLog.e(TAG,"FrontCameraTest sendintent gn.com.android.mmitest.item.FrontCameraTest2");

        //Gionee zhangke 20160108 modify for CR01637206 start
        localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //Gionee zhangke 20160422 modify for CR01673305 start
        try {
            startActivityForResult(localIntent, 0);
        } catch (ActivityNotFoundException ex) {

        }
        //Gionee zhangke 20160422 modify for CR01673305 end

        DswLog.e(TAG, "FrontCameraTest2  startActivityForResult");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出前摄像头2 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
        DswLog.e(TAG, "FrontCameraTest2 onResume ");

    }

    @Override
    public void onPause() {
        super.onPause();
        DswLog.e(TAG, "FrontCameraTest2 onPause ");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FrontCameraTest2.this.setContentView(R.layout.common_textview);
        DswLog.e(TAG, "FrontCameraTest2 onActivityResult" + "requestCode = " + requestCode + " resultCode = " + resultCode);
        mFrontCamera = false;
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        //Gionee zhangke 20160811 add for CR01745293 start
        Button mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        //Gionee <GN_BSP_MMI> <chengq> <20170512> modify for ID 135779 begin
        if (data == null) {

        } else {
            boolean isCapture = data.getBooleanExtra("capture", false);
            if (isCapture) {
                mRightBtn.setEnabled(true);
            }else {
                mRightBtn.setEnabled(false);

            }
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170512> modify for ID 135779 end
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.wrong_btn: {
                TestUtils.wrongPress(TAG, this);
                break;
            }
            case R.id.right_btn: {
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
}


