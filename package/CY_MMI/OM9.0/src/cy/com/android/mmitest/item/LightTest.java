
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class LightTest extends BaseActivity implements View.OnClickListener {
    private Button mRightBtn, mWrongBtn, mRBt, mGBt, mBBt, mRestartBtn;

    private static final String TAG = "LightTest";

    TextView mNoteTv;

//    NotificationManager mNM;
//    Notification mN;

    Camera mCamera;

    final int ID_LED = 31415;

    boolean mIsFlashOpen, mRLedPress, mGLedPress, mBLedPress, mFlashPress;

    int mPressCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        setContentView(R.layout.common_textview_norestart);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);

        TextView contentView = (TextView) findViewById(R.id.test_content);
        contentView.setText(R.string.led_light_note);
    }


    @Override
    public void onStart() {
        turnLightOn();
        super.onStart();
        TestUtils.acquireWakeLock(this);
    }

    @Override
    public void onStop() {
        turnLightOff();
        if (null != mCamera) {
            mCamera.release();
            mCamera = null;
        }
        super.onStop();
        TestUtils.releaseWakeLock();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                //mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                //mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }

        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }


    private void turnLightOn() {
        if (false == mIsFlashOpen) {
            if (null == mCamera) {
                try {
                    DswLog.e(TAG, "open");
                    mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
                } catch (RuntimeException e) {
                    try {
                        Thread.sleep(500);
                        DswLog.e(TAG, "second open");
                        mCamera = Camera.open(CameraInfo.CAMERA_FACING_BACK);
                    } catch (Exception sube) {
                        DswLog.e(TAG, "fail to open camera");
                        sube.printStackTrace();
                        mCamera = null;
                    }
                }
            }
            if (null != mCamera) {
                Parameters parameters = mCamera.getParameters();
                if (parameters == null) {
                    return;
                }
                List<String> flashModes = parameters.getSupportedFlashModes();
                // Check if camera flash exists
                if (flashModes == null) {
                    return;
                }
                String flashMode = parameters.getFlashMode();
                if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                    // Turn on the flash
                    if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
                        parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
                        mCamera.startPreview();
                        mCamera.setParameters(parameters);
                        mRightBtn.setEnabled(true);
                    } else {
                        DswLog.e(TAG, "FLASH_MODE_TORCH not supported");
                    }
                }
            }
        }
    }

    private void turnLightOff() {
        if (mIsFlashOpen) {
            mIsFlashOpen = false;
            if (mCamera == null) {
                return;
            }
            Parameters parameters = mCamera.getParameters();
            if (parameters == null) {
                return;
            }
            List<String> flashModes = parameters.getSupportedFlashModes();
            String flashMode = parameters.getFlashMode();
            // Check if camera flash exists
            if (flashModes == null) {
                return;
            }
            if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                // Turn off the flash
                if (flashModes.contains(Parameters.FLASH_MODE_OFF)) {
                    parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                    mCamera.stopPreview();
                } else {
                    DswLog.e(TAG, "FLASH_MODE_OFF not supported");
                }
            }
        }
    }
}
