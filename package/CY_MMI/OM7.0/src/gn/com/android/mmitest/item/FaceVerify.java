package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import android.app.Activity;
import gn.com.android.mmitest.utils.DswLog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.content.ActivityNotFoundException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FaceVerify extends BaseActivity implements OnClickListener {
    String TAG = "FaceVerify";
    Button mRightBtn, mWrongBtn;
    private boolean mFrontCamera = false;
    private String enrollPath = "/mnt/sdcard/enroll.flag";
    private String enrollResultPath = "/mnt/sdcard/face_result.txt";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************人脸识别 @" + Integer.toHexString(hashCode()));

        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);

        try {
            File file = new File(enrollPath);
            if (file.exists())
                file.delete();

            File result = new File(enrollResultPath);
            if (result.exists())
                result.delete();
        } catch (Exception e) {
            DswLog.i(TAG,"Error can't delete /mnt/sdcard/enroll.flag");
            e.printStackTrace();
        }

        Intent localIntent;
        localIntent = new Intent("gn.com.android.mmitest.item.BackCameraTest");
        DswLog.e(TAG, "FaceVerify sendintent gn.com.android.mmitest.item.BackCameraTest");
        localIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try {
            startActivityForResult(localIntent, 0);
        } catch (ActivityNotFoundException ex) {

        }
        DswLog.e(TAG, "FaceVerify  startActivityForResult");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出前摄像头2 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
        DswLog.e(TAG, "FaceVerify onResume ");

    }

    @Override
    public void onPause() {
        super.onPause();
        DswLog.e(TAG, "FaceVerify onPause ");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        FaceVerify.this.setContentView(R.layout.common_textview);
        DswLog.e(TAG, "FaceVerify onActivityResult" + "requestCode = " + requestCode + " resultCode = " + resultCode);
        mFrontCamera = false;
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        Button mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);

        if (isEnrollResult()) {
            mRightBtn.setEnabled(true);
        }else {
            mRightBtn.setEnabled(false);

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

    private boolean isEnrollResult() {
        File result  = new File(enrollResultPath);
        if (!result.exists()) {
            DswLog.e(TAG, "enrollPath is not exists ");
            return false;
        }
        File file = new File(enrollPath);
        if (file.exists()) {
            DswLog.e(TAG, "Error:FaceVerify enrollPath is exists ");
            return false;
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(enrollResultPath));
            String data = null;
            while ((data = br.readLine()) != null) {
                DswLog.i(TAG,"data =" + data);
                if (data.equals("1") )
                    return true;
            }
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            DswLog.e(TAG, "DevicesInfo IO Error"+ e.getMessage());
        } catch (Exception e) {
            DswLog.e(TAG, "DevicesInfo Error"+ e.getMessage());
        }
        return false;
    }
}


