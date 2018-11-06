package cy.com.android.mmitest.item;


import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

public class IRLight extends BaseActivity implements OnClickListener {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn,mToggleBtn;

    private static final String TAG = "IRLight";
    private static final String FLASHLIGHT_PATH = "/sys/class/flashlightdrv/kd_camera_flashlight/torch";
    private final int FLASH_TORCH_1 = 1;
    private final int FLASH_TORCH = 2;

    private boolean mIsFlashOpened = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开前红外灯 @" + Integer.toHexString(hashCode()));

        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.flash_light);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mRightBtn.setEnabled(false);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mToggleBtn = (Button) findViewById(R.id.toggle_button);
        mToggleBtn.setOnClickListener(this);
        TextView titleTv = (TextView) findViewById(R.id.flash_test_title);
        titleTv.setText(R.string.test_title);

        turnOnFlashLight();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出前红外灯 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onPause() {
        super.onPause();
        turnOffFlashLight();
    }

    //Gionee <GN_BSP_MMI> <chengq> <20170221> modify for ID 69609 begin
    public boolean writeGestureNodeValue(String nodeType, int value) {
        Object pm = (Object) (getSystemService("chenyeeserver"));
        try {
            Class cls = Class.forName("android.os.chenyeeserver.ChenyeeServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
            method.invoke(pm, f.get(null), value);
            return true;
        } catch (Exception e) {
            DswLog.e(TAG, "Exception :" + e);
        }
        return false;
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170221> modify for ID 69609 end
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }

            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
            case R.id.toggle_button: {
                if(mIsFlashOpened){
                    mRightBtn.setEnabled(true);
                    turnOffFlashLight();
                    mToggleBtn.setText(getString(R.string.toggle_on));
                }else{
                    turnOnFlashLight();
                    mToggleBtn.setText(getString(R.string.toggle_off));
                }
                break;
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private void turnOnFlashLight() {
        DswLog.i(TAG, "turnOnFlashLight");
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 20);

        mIsFlashOpened = true;
    }

    private void turnOffFlashLight() {
        DswLog.e(TAG, "turnOffFlashLight");
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 0);

        mIsFlashOpened = false;
    }
}
