package cy.com.android.mmitest.item;

import android.app.Activity;
import cy.com.android.mmitest.utils.DswLog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import android.os.SystemProperties;
import android.content.ActivityNotFoundException;

public class BackCameraTest2 extends Activity implements OnClickListener {
    String TAG = "BackCameraTest2";
    Button mRightBtn, mWrongBtn;
    private boolean mBackCamera = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开后摄像头2 @" + Integer.toHexString(hashCode()));
        TestUtils.setWindowFlags(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        Intent localIntent;
        localIntent = new Intent("cy.com.android.mmitest.item.BackCameraTest2");
        DswLog.e(TAG, "BackCameraTest sendintent cy.com.android.mmitest.item.BackCameraTest2");
        //Gionee zhangke 20160422 modify for CR01673305 start    
        try {
            startActivityForResult(localIntent, 0);
        } catch (ActivityNotFoundException e) {
            // TODO: handle exception
        }
        //Gionee zhangke 20160422 modify for CR01673305 end
        DswLog.e(TAG, "BackCameraTest  startActivityForResult");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出后摄像头2 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
        DswLog.e(TAG, "BackCameraTest onResume ");

    }

    @Override
    public void onPause() {
        super.onPause();
        DswLog.e(TAG, "BackCameraTest onPause ");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BackCameraTest2.this.setContentView(R.layout.common_textview);
        DswLog.e(TAG,"BackCameraTest onActivityResult" + "requestCode = " + requestCode + " resultCode = " + resultCode);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);

        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);

        //Gionee zhangke 20160811 add for CR01745293 start
        Button mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        //Gionee <GN_BSP_MMI> <chengq> <20170512> modify for ID 135779 begin
        if(data == null){

        } else {
            boolean isCapture = data.getBooleanExtra("capture", true);
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

