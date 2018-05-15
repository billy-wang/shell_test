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

public class BackCameraTest2 extends Activity implements OnClickListener {
    String TAG = "BackCameraTest2";
    Button mRightBtn, mWrongBtn;
    private boolean mBackCamera = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "BackCameraTest2 oncreate");
        TestUtils.setWindowFlags(this);
        Intent localIntent;
        localIntent = new Intent("gn.com.android.mmitest.item.BackCameraTest2");
        Log.e(TAG, "BackCameraTest sendintent gn.com.android.mmitest.item.BackCameraTest2"); 
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        BackCameraTest2.this.setContentView(R.layout.common_textview);
        Log.e(TAG,"BackCameraTest onActivityResult" + "requestCode = " + requestCode + " resultCode = " + resultCode);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);

        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);

        //Gionee zhangke 20160811 add for CR01745293 start
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
        //Gionee zhangke 20160811 add for CR01745293 end


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

