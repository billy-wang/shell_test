package gn.com.android.mmitest;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import gn.com.android.mmitest.utils.DswLog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.provider.Settings;
import android.graphics.Color;

/**
 * Created by hjq on 2016/5/17.
 */
public class BaseActivity extends Activity {
    public int wavesState;
    public boolean wavesEnable;
    public static String TAG = "MMI BaseActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowFlags();
    }


    //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 begin
    public void initData() {
        closeMaxAudio();
    }

    public void closeMaxAudio () {
	if (!wavesEnable)
	    return;
        if (WavesFXContract.GN_MAXXAUDIO_SUPPORT){
            wavesState = WavesFXContract.getWavesState(getApplicationContext());
            DswLog.d(TAG, "before setting: wavesState="+wavesState);
            if (wavesState != 0){
                WavesFXContract.setWavesState(getApplicationContext(),0);
            }
            DswLog.d(TAG, "after setting : wavesState="+WavesFXContract.getWavesState(getApplicationContext()));
        }else {
            DswLog.d(TAG, "devices not support maxaudio");
        }
    }

    public void revertMaxAudio() {
        if (!wavesEnable)
            return;
        if (WavesFXContract.GN_MAXXAUDIO_SUPPORT){
            WavesFXContract.setWavesState(this,wavesState);
            DswLog.d(TAG, "revert the maxaudio wavesState="+WavesFXContract.getWavesState(getApplicationContext()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
	initData();
    }

    //Gionee <GN_BSP_MMI> <chengq> <20170216> modify for ID 70046 end

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
        DswLog.e("BaseActivity", "onConfigurationChanged");
    }

    protected void setWindowFlags() {
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        //*Gionee huangjianqiang 20160310 add for CR01638778 begin*/
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
        /*Gionee huangjianqiang 20160310 add for CR01638778 end*/
        window.setAttributes(lp);

        View view = window.getDecorView();
        int visFlags = View.STATUS_BAR_DISABLE_BACK
                | View.STATUS_BAR_DISABLE_HOME
                | View.STATUS_BAR_DISABLE_RECENT
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | 0x00004000;//View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED

        view.setSystemUiVisibility(visFlags);
    }
}
