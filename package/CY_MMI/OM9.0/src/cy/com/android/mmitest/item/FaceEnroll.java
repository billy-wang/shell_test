package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import android.app.Activity;
import cy.com.android.mmitest.utils.DswLog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;
import android.content.ActivityNotFoundException;
import java.io.File;

public class FaceEnroll extends BaseActivity implements OnClickListener {
    String TAG = "FaceEnroll";
    Button mRightBtn, mWrongBtn;
    private boolean mFrontCamera = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************人脸识别 @" + Integer.toHexString(hashCode()));

        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);

        try {
            File file = new File("/mnt/sdcard/enroll.flag");
            file.createNewFile();
        } catch (Exception e) {
            DswLog.i(TAG,"Error can't create /mnt/sdcard/enroll.flag");
            e.printStackTrace();
        }

        Intent localIntent;
        localIntent = new Intent("cy.com.android.mmitest.item.FrontCameraTest2");
        DswLog.e(TAG,"FaceEnroll sendintent cy.com.android.mmitest.item.FrontCameraTest2");
        localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            startActivityForResult(localIntent, 0);
        } catch (ActivityNotFoundException ex) {

        }
        DswLog.e(TAG, "FaceEnroll  startActivityForResult");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出前摄像头2 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
        DswLog.e(TAG, "FaceEnroll onResume ");

    }

    @Override
    public void onPause() {
        super.onPause();
        DswLog.e(TAG, "FaceEnroll onPause ");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FaceEnroll.this.setContentView(R.layout.common_textview);
        DswLog.e(TAG, "FrontCameraTest2 onActivityResult" + "requestCode = " + requestCode + " resultCode = " + resultCode);
        mFrontCamera = false;
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        Button mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);

        if (data == null) {

        } else {
            boolean isCapture = data.getBooleanExtra("capture", false);
            if (isCapture) {
                mRightBtn.setEnabled(true);
            }else {
                mRightBtn.setEnabled(false);

            }
        }
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
            case R.id.restart_btn: {
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


