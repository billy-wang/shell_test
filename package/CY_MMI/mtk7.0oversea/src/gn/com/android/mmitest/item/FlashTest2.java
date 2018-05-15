package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class FlashTest2 extends BaseActivity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "FlashTest2";

    public static final int NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1 = 40; ///sys/class/flashlightdrv/kd_camera_flashlight/torch1


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.checkToContinue(this);
        setContentView(R.layout.common_textview);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mRightBtn.setEnabled(true);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.flash2_note);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Gionee <GN_BSP_MMI> <chengq> <20170112> modify for ID 57510 begin
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 40);
        //Gionee <GN_BSP_MMI> <chengq> <20170112> modify for ID 57510 end
        Log.e(TAG, "open flash2");

    }

    @Override
    public void onPause() {
        super.onPause();
        //Gionee <GN_BSP_MMI> <chengq> <20170112> modify for ID 57510 begin
        writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH", 0);
        //Gionee <GN_BSP_MMI> <chengq> <20170112> modify for ID 57510 end
        Log.e(TAG, "close flash2");
    }

    public void writeGestureNodeValue(String nodeType, int value) {
        Object pm = (Object) (getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
            method.invoke(pm, f.get(null), value);
        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e);
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
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
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
