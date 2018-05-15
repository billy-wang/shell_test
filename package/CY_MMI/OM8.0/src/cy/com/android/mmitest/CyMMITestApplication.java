package cy.com.android.mmitest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
//Platform
//import amigo.provider.AmigoSettings;
import cy.com.android.mmitest.utils.DswLog;
import android.util.Log;
import com.goodix.device.FpDevice;
import cy.com.android.mmitest.utils.ProinfoUtil;

public class CyMMITestApplication extends Application {
    private final static String TAG = "CyMMITestApplication";
    private final static String TEST_RESULT_ACTIVITY = "TestResult";

    /**
     * add for CR01512325 begin
     * TO disable Control center in MMI test.
     * (1)When program normally quit, to revert the flags in {@link ActivityLifecycleCallbacks.onActivityDestroyed}}
     * (2)When program quit by uncaught exception, MMI should revert the flags in
     * {@link Thread.UncaughtExceptionHandler}
     */
    private final static String AMIGO_SETTING_CC_SWITCH = "control_center_switch";
    private final static int LOCK_CONTROL_CENTER = 0;
    private final static int UNLOCK_CONTOL_CENTER = 1;

    public static boolean isGoodix = false;

    private static FpDevice mDevice;

    public static FpDevice getFpDevice() {
        return mDevice;
    }
    /*Gionee huangjianqiang 20160624 add for CR01714100 begin*/
    private static CyMMITestApplication mApplication;
    public static final int FP_VERIFY = 26;
    public static final int FP_DEADPOINT = 28;
    public static final int MSG_PERFORMACE = 2;
    public static final int MSG_DEAD_POINT = 6;
    public static final int MSG_EXIT = 7;
    /*Gionee huangjianqiang 20160624 add for CR01714100 end*/
    public static int maxAudio = 1;
    public static int rotation = 0;
    public static int sound_effect = 1;
    public static String stereoEnable;
    public static int languageState = -1;

    Thread.UncaughtExceptionHandler mUncaughtHandler = new UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            DswLog.v(TAG, Log.getStackTraceString(ex));
            ProinfoUtil.revertDispatchAllKey();
            ContentResolver resolver = getContentResolver();
            //Platform
            //AmigoSettings.putInt(resolver, AMIGO_SETTING_CC_SWITCH, UNLOCK_CONTOL_CENTER);
            System.exit(0);
        }
    };

    /**
     * add for CR01512325 end
     */
    @Override
    public void onCreate() {
        super.onCreate();

        DswLog.setMMILogFile();
        DswLog.d(TAG, "\n\n\n****************CyMMITestApplication onCreate ***************");

        registerActivityLifecycleCallbacks(mCallBack);
        // add for CR01512325 begin
        ContentResolver resolver = getContentResolver();
        //Platform
        //AmigoSettings.putInt(resolver, AMIGO_SETTING_CC_SWITCH, LOCK_CONTROL_CENTER);
        Thread.setDefaultUncaughtExceptionHandler(mUncaughtHandler);
        // add for CR01512325 end
        isGoodix = isGoodix();
        DswLog.e(TAG, "isGoodix:" + isGoodix);
        if (isGoodix) {
            System.loadLibrary("fp_gf_mp");
            mDevice = FpDevice.open(new MyHandler());
        }
    /*Gionee huangjianqiang 20160624 add for CR01714100 begin*/
        mApplication = this;
    /*Gionee huangjianqiang 20160624 add for CR01714100 end*/
    }

    ActivityLifecycleCallbacks mCallBack = new ActivityLifecycleCallbacks() {
        final ArrayList<Activity> mActivities = new ArrayList<Activity>();

        @Override
        public void onActivityCreated(Activity activity,
                                      Bundle savedInstanceState) {
            String componentName = activity.getComponentName().getShortClassName();

            // TestResult activity runs in com.android.phone, so remove LifecycleCallbacks
            // to avoid size effect
            if (componentName.contains(TEST_RESULT_ACTIVITY)) {
                DswLog.v(TAG, "remove ActivityLifecycleCallbacks for TestResult");
                unregisterActivityLifecycleCallbacks(mCallBack);
                return;
            }
            DswLog.v(TAG, "ProcessId = " + Process.myPid()
                    + " add componentName" + componentName);
            mActivities.add(activity);
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            mActivities.remove(activity);
            String componentName = activity.getComponentName().getShortClassName();
            final int length = mActivities.size();
            DswLog.v(TAG, "ProcessId = " + Process.myPid()
                    + " Activity stackSize = " + length
                    + " remove componentName" + componentName);
            if (length == 0) {
                unregisterActivityLifecycleCallbacks(mCallBack);
                //add for CR01512325 begin
                ProinfoUtil.revertDispatchAllKey();
                ContentResolver resolver = getContentResolver();
                //Platform
                //AmigoSettings.putInt(resolver, AMIGO_SETTING_CC_SWITCH, UNLOCK_CONTOL_CENTER);
                //add for CR01512325 end
                Process.killProcess(Process.myPid());
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity,
                                                Bundle outState) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

    };

    private boolean isGoodix() {
        String path = "/sys/devices/platform/gn_device_check/name";
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        boolean isGoodix = false;
        try {
            File FilePath = new File(path);

            if (FilePath.exists()) {
                fileInputStream = new FileInputStream(FilePath);
                inputStreamReader = new InputStreamReader(fileInputStream);
                br = new BufferedReader(inputStreamReader);
                String data = null;
                while ((data = br.readLine()) != null) {
                    if (data.contains("Finger:")) {
                        isGoodix = data.contains("gf3118m");
                        break;
                    }
                }
            } else {
                return isGoodix;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                    inputStreamReader.close();
                    br.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
        return isGoodix;
    }

    private class MyHandler extends Handler { //Handler

        public MyHandler() {
        }

        public void handleMessage(Message msg) {
            DswLog.e(TAG, "handleMessage msg.what:" + msg.what + " msg.arg1:" + msg.arg1);

    /*Gionee huangjianqiang 20160624 modify for CR01714100 begin*/
            if(mListener !=null) {
                mListener.onMessage(msg.what,  msg.arg1);
            } else {
                DswLog.e(TAG,"mListener is null");
            }
    /*Gionee huangjianqiang 20160624 modify for CR01714100 end*/
        }
    }

    /*Gionee huangjianqiang 20160624 add for CR01714100 begin*/
    private static FingerPrintsListener mListener;

    public static interface FingerPrintsListener {
        void onMessage(int what, int arg1);
    }

    public static CyMMITestApplication getApplication() {
        return  mApplication;
    }
    public void setFingerPrintListener(FingerPrintsListener listener) {
        this.mListener = listener;
    }

    public void resetFingerPrintListener() {
        mListener = null;
    }
    /*Gionee huangjianqiang 20160624 add for CR01714100 end*/
}
