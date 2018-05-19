package com.cydroid.autommi;

import android.app.Activity;
import android.os.Bundle;
import com.cydroid.util.DswLog;
import android.os.SystemProperties;
import android.view.WindowManager;
import android.content.Intent;

//Gionee zhangke 20160325 add for CR01660596 start
import android.os.PowerManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.TextView;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.pm.PackageManager;
import android.widget.Toast;
import com.cydroid.autommi.AutoMMI;
import com.cydroid.util.SystemUtil;
//Gionee zhangke 20160325 add for CR01660596 end

//Gionee <GN_BSP_MMI> <chengq> <20170209> modify for ID 65087 begin
public class Dumb extends BaseActivity {

	private static final String TAG = "Dumb";
	//Gionee zhangke 20160325 add for CR01660596 start
	private PowerManager.WakeLock mWakeLock;
	private PowerManager mPowerManager;
	//Gionee zhangke 20160325 add for CR01660596 end

    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Gionee zhangke 20160325 delete for CR01660596 start
        /*
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED  | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON; 
        getWindow().setAttributes(lp);
        */
        //Gionee zhangke 20160325 delete for CR01660596 end
    }
//Gionee <GN_BSP_MMI> <chengq> <20170209> modify for ID 65087 end

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		SystemUtil.revertDispatchAllKey();
        //Gionee zhangke 20160226 delete for CR01632945 start
        /*
        Intent stopi = new Intent();
        stopi.setAction("com.mediatek.mtklogger.ADB_CMD");
        Bundle bundle = new Bundle();
        bundle.putString("cmd_name", "stop");
        bundle.putInt("cmd_target", 7);
        stopi.putExtras(bundle);
        sendBroadcast(stopi);
        DswLog.e(TAG, "stop mtk mmi logcat ");
        */
        //Gionee zhangke 20160226 delete for CR01632945 end
        //Gionee zhangke 20160325 delete for CR01660596 start

       //Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 begin
        try{
           Thread.sleep(50);
        }catch(Exception e){
           DswLog.d(TAG,"onStart Thread.sleep() Error");
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170314> modify for ID 81978 begin

        DswLog.i(TAG, "onCreate:start wake and unlock screen");
        mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "AutoMMI"); 
        mWakeLock.setReferenceCounted(false);
        mWakeLock.acquire(5000); 
        sendBroadcast(new Intent("com.chenyee.action.DISABLE_KEYGUARD"));
        //Gionee zhangke 20160325 delete for CR01660596 end
	}

        //Gionee <GN_BSP_MMI> <chengq> <20170209> modify for ID 65087 begin
        @Override
        protected void onStop() {
        super.onStop();
        DswLog.i(TAG, "onStop()");
        
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170209> modify for ID 65087 end


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.main, menu);
            return super.onCreateOptionsMenu(menu);
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mtklog:
                switchMTKLog();
                break;
            case R.id.about:
                showAboutDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.dialog_about, null, false);
        TextView version = (TextView) content.findViewById(R.id.version);

        try {
            String name = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            version.setText(getString(R.string.version) + " " + name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setView(content);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void switchMTKLog() {
        if (AutoMMI.isMTKLogOpened) {
            stopMtkLog();
            AutoMMI.isMTKLogOpened = false;
        }else {
            setMtkLogSize();
            startMtkLog();
            AutoMMI.isMTKLogOpened = true;
        }
    }


    /**
     * Start MTKLog
     */
    private void startMtkLog() {
        Intent starti = new Intent();
        starti.setAction("com.mediatek.mtklogger.ADB_CMD");
        Bundle bundle = new Bundle();
        bundle.putString("cmd_name", "start");
        bundle.putInt("cmd_target", 1);
        starti.putExtras(bundle);
        sendBroadcast(starti);
        Toast.makeText(this, "AutoMMI Start MTKLog   ^_^",Toast.LENGTH_SHORT).show();
        DswLog.e(TAG, "start mtk mmi logcat ");
    }

    /**
     * Stop MTKLog
     */
    private void stopMtkLog() {
        Intent stopi = new Intent();
        stopi.setAction("com.mediatek.mtklogger.ADB_CMD");
        Bundle bundle = new Bundle();
        bundle.putString("cmd_name", "stop");
        bundle.putInt("cmd_target", 1);
        stopi.putExtras(bundle);
        sendBroadcast(stopi);
        Toast.makeText(this, "AutoMMI Stop MTKLog  -_-",Toast.LENGTH_SHORT).show();
    }

    /**
     * GIONEE add for CR01548302
     * Enlarge MTKLOG total size
     */
    private void setMtkLogSize() {
        Intent starti = new Intent();
        starti.setAction("com.mediatek.mtklogger.ADB_CMD");
        Bundle bundle = new Bundle();
        bundle.putString("cmd_name", "set_total_log_size_3000");
        bundle.putInt("cmd_target", 1);
        starti.putExtras(bundle);
        sendBroadcast(starti);
        DswLog.e(TAG, "setMtkLogSize  to 3000M");
    }

}
