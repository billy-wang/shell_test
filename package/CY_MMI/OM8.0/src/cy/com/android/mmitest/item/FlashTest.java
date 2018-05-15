package cy.com.android.mmitest.item;

import cy.com.android.mmitest.R;
import cy.com.android.mmitest.R.string;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cy.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.media.AudioSystem;
import android.media.AudioManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cy.com.android.mmitest.BaseActivity;

public class FlashTest extends BaseActivity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    // public static final int  NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_0 = 39; ///sys/class/flashlightdrv/kd_camera_flashlight/torch0
    private static final String TAG = "FlashTest";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开闪光灯 @" + Integer.toHexString(hashCode()));

        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.common_textview);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.flash_note);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出闪光灯 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_0", 40);
        DswLog.e(TAG, "open flash");
    }

    @Override
    public void onPause() {
        super.onPause();
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_0", 0);
        DswLog.e(TAG, "close flash");
    }

    public void writeGestureNodeValue(String nodeType, int value) {
        Object pm = (Object) (getSystemService("chenyeeserver"));
        try {
            Class cls = Class.forName("android.os.chenyeeserver.ChenyeeServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
            method.invoke(pm, f.get(null), value);
        } catch (Exception e) {
            DswLog.e(TAG, "Exception :" + e);
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.right_btn: {
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }

            case R.id.restart_btn: {
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
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
