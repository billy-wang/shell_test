
package gn.com.android.mmitest;

import android.content.BroadcastReceiver;
import gn.com.android.mmitest.utils.DswLog;
import android.os.SystemProperties;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;


public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        boolean isFirstBoot = SystemProperties.getBoolean("persist.mmi.first_boot", true);
        boolean isMtklogRunning = isMtklogRunning();
        DswLog.i(TAG, "action=" + action + ";isFirstBoot=" + isFirstBoot + ";isMtklogRunning=" + isMtklogRunning);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (isFirstBoot && !isMtklogRunning) {
                if (isMtklogExist()) {
                    DeleteMtkLog();
                }
            }
            SystemProperties.set("persist.mmi.first_boot", "false");
        }
    }

    private boolean isMtklogRunning() {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        if (list != null) {
            for (int j = 0; j < list.size(); j++) {
                if ("com.mediatek.mtklogger".equals(list.get(j).processName)
                        && list.get(j).importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                    //DswLog.i(TAG, "processName "+j+"="+list.get(j).processName+";importance="+list.get(j).importance);
                    isRunning = true;
                    break;
                }
            }
        }
        return isRunning;
    }

    private boolean isMtklogExist() {
        boolean isMtklogExist = false;
        File sdMtklog = new File("/mnt/sdcard/mtklog");
        File sd2Mtklog = new File("/mnt/sdcard2/mtklog");
        if (sdMtklog.exists() || sd2Mtklog.exists()) {
            isMtklogExist = true;
        }
        DswLog.i(TAG, "isMtklogExist=" + isMtklogExist);
        return isMtklogExist;
    }

    private void DeleteMtkLog() {
        try {
            dFile(new File("/mnt/sdcard/mtklog"));
            dFile(new File("/mnt/sdcard2/mtklog"));
        } catch (Exception e) {
            DswLog.e(TAG, "DeleteMtkLog error=" + e.getMessage());
        }
    }

    private void dFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
                return;
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                if (files == null) {
                    DswLog.e(TAG, file + " listFiles()" + " return null");
                    file.delete();
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    dFile(files[i]);
                }
                file.delete();
            }

        }
    }
}
