package gn.com.android.mmitest.item;

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

public class FlashTest2 extends Activity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn, mToggleBtn;
    private boolean mIsFlashOpened = false;
    private static final String TAG = "FlashTest2";

    public static final int NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1 = 40; ///sys/class/flashlightdrv/kd_camera_flashlight/torch1


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.checkToContinue(this);
        //Gionee zhangke 20151215 modify for CR01609753 start
        /*
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        getWindow().setAttributes(lp);
        View view = getWindow().getDecorView();
        int visFlags = View.STATUS_BAR_DISABLE_BACK
                | View.STATUS_BAR_DISABLE_HOME
                | View.STATUS_BAR_DISABLE_RECENT
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        view.setSystemUiVisibility(visFlags);
        */
        TestUtils.setWindowFlags(this);
        //Gionee zhangke 20151215 modify for CR01609753 end
        setContentView(R.layout.flashlight_test);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170414> modify for ID 113638 begin
        mRightBtn.setEnabled(false);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mToggleBtn = (Button) findViewById(R.id.toggle_button);
        mToggleBtn.setOnClickListener(FlashTest2.this);
        TextView titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.flash2_note);
        turnOnFlashLight();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170414> modify for ID 113638 end
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "open flash2");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "close flash2");
        turnOffFlashLight();
    }

    public boolean writeGestureNodeValue(String nodeType, int value) {
        Object pm = (Object) (getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
            method.invoke(pm, f.get(null), value);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e);
        }
        return false;
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170414> modify for ID 113638 begin
    public void turnOnFlashLight(){
        mIsFlashOpened = writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 40);
    }

    public void turnOffFlashLight(){
        mIsFlashOpened = writeGestureNodeValue("NODE_TYPE_FLASHLIGHT_CAMERA_TORCH_1", 0);
        mIsFlashOpened = false;
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170414> modify for ID 113638 end

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
            //Gionee <GN_BSP_MMI> <lifeilong> <20170414> modify for ID 113638 begin
            case R.id.toggle_button: {
                mRightBtn.setEnabled(true);
                if(mIsFlashOpened){
                    turnOffFlashLight();
                    mToggleBtn.setText(getString(R.string.toggle_on));
                }else{
                    turnOnFlashLight();
                    mToggleBtn.setText(getString(R.string.toggle_off));
                }
                break;
            }
            //Gionee <GN_BSP_MMI> <lifeilong> <20170414> modify for ID 113638 end
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
