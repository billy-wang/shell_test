package cy.com.android.mmitest.item;


import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


import android.content.ComponentName;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

public class FlashLight extends BaseActivity implements OnClickListener {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn,mToggleBtn;
    private TextView mTitleTv;

    private static final String TAG = "FlashLight";
    private boolean isFlash = false;
    private static final String FLASHLIGHT_PATH = "/sys/class/flashlightdrv/kd_camera_flashlight/torch";

    private boolean mIsFlashOpened = false;
    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean mFlashlightEnabled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开手电筒 @" + Integer.toHexString(hashCode()));
        TestUtils.checkToContinue(this);
        //Gionee <Oveasea_Bug> <tanbotao> <20161121> for CR01772500 begin
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.flash_light);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setEnabled(false);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mToggleBtn = (Button) findViewById(R.id.toggle_button);
		mToggleBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn.setOnClickListener(this);
        mTitleTv = (TextView) findViewById(R.id.flash_test_title);
        mTitleTv.setText(R.string.test_title);

        try {
            //Gionee <GN_BSP_MMI> <chengq> <20170411> modify for ID 109364 begin
            mToggleBtn.setVisibility(View.GONE);
            //Gionee <GN_BSP_MMI> <chengq> <20170411> modify for ID 109364 end
            IntentFilter filter = new IntentFilter("com.gionee.flashlightClick.MMITEST");
            registerReceiver(flashLightBroadcastReceiver, filter);
          //  Intent intent = getPackageManager().getLaunchIntentForPackage("com.cydroid.flashlight");

            ComponentName name = new ComponentName("com.cydroid.flashlight"
                    ,"com.cydroid.flashlight.FlashLightActivity");
            Intent intent = new Intent();
            intent.setComponent(name);
            startActivity(intent);

        } catch (Exception e) {

            DswLog.e(TAG, "Exception e-1");
            //Gionee <GN_BSP_MMI> <chengq> <20170411> modify for ID 109364 begin
            mToggleBtn.setVisibility(View.VISIBLE);
            //Gionee <GN_BSP_MMI> <chengq> <20170411> modify for ID 109364 end
            initdata(this);
            DswLog.e(TAG, "wrie NODE_TYPE_FLASHLIGHT_CAMERA_TORCH 401 ");

            isFlash=true;
            DswLog.e(TAG, "Exception e-2");
        }
    }

    private void turnOnFlashLight() {
        DswLog.i(TAG, "turnOnFlashLight");
        setFlashLight(true);
        mIsFlashOpened = true;

    }

    private void turnOffFlashLight() {
        DswLog.e(TAG, "turnOffFlashLight");
        setFlashLight(false);
        mIsFlashOpened = false;
    }


    private void initdata(Context context) {
        mCameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        tryInitCamera();
    }

    private void tryInitCamera() {
        try {
            mCameraId = getBackCameraId();
        } catch (CameraAccessException e) {
            DswLog.e(TAG, "Couldn't initialize. err="+e.getMessage());
        }

    }

    private void setFlashLight(boolean enabled) {
        DswLog.i(TAG, "setFlashLight: enable=" + enabled + ", current_state=" + mFlashlightEnabled);
        if (mCameraId == null) {
            tryInitCamera();
            if (mCameraId == null) {
                DswLog.e(TAG, "setFlashLight: camera unavailable or no camera facing back");
            }
        }
        if (mFlashlightEnabled != enabled) {
            mFlashlightEnabled = enabled;
            try {
                mCameraManager.setTorchMode(mCameraId, enabled);
            } catch (Exception e) {
                DswLog.e(TAG, "Couldn't set torch mode e=" + e.getMessage());
                mFlashlightEnabled = false;
            }
        }
    }


    private String getBackCameraId() throws CameraAccessException {
        String[] ids = mCameraManager.getCameraIdList();
        DswLog.i(TAG, "getBackCameraId length="+ids.length);
        for (String id : ids) {

            CameraCharacteristics cc = mCameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = cc.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = cc.get(CameraCharacteristics.LENS_FACING);

            DswLog.d(TAG, "getBackCameraId: flashAvailable = " +
                    flashAvailable + ", lensFacing = " + lensFacing + "..." + CameraCharacteristics.LENS_FACING_BACK);

            if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFlash) {
            turnOnFlashLight();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isFlash) {
            turnOffFlashLight();
        }
    }

    /* Gionee 20160907 tanbotao add for begin */
    private BroadcastReceiver flashLightBroadcastReceiver =new BroadcastReceiver (){
        @Override
        public void onReceive(Context context, Intent intent) {

            DswLog.e(TAG, "FlashLightBroadcastReceiver onReceive ");
            if (intent.getAction().equals("com.gionee.flashlightClick.MMITEST")) {
                boolean flashlight_clicked = intent.getBooleanExtra("flashlight_clicked", false);
                DswLog.e(TAG, "FlashLightBroadcastReceiver intent.getAction()+ flashlight_clicked ="+flashlight_clicked);

                mRightBtn.setEnabled(flashlight_clicked);
            }

        }
    };

    /* Gionee 20160907 tanbotao add for end */


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
                DswLog.d(TAG, "zhangxiaowei -restart");
                TestUtils.restart(this, TAG);
                break;
            }
            //Gionee <BP_BSP_MMI> <chengq> <201704005> add for ID 103642 begin
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
            //Gionee <BP_BSP_MMI> <chengq> <201704005> add for ID 103642 end
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    //Gionee zhangke 20151212 add for CR01608407 start
    private static final int MESSAGE_REFRESH_BUTTON_STATUE = 0;





    //Gionee zhangke 20151212 add for CR01608407 end

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flashLightBroadcastReceiver != null) {
            unregisterReceiver(flashLightBroadcastReceiver);
        }
        DswLog.d(TAG, "\n****************退出手电筒 @" + Integer.toHexString(hashCode()));
    }
}
