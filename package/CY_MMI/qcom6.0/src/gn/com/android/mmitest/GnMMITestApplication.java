package gn.com.android.mmitest;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.os.SystemProperties;
//Platform
//import amigo.provider.AmigoSettings;
import android.util.Log;
//Gionee zhangke 20160913 add for CR01760585 start
import gn.com.android.mmitest.item.GnReflectionMethods;
//Gionee zhangke 20160913 add for CR01760585 end

public class GnMMITestApplication extends Application {
    private final static String TAG = "GnMMITestApplication";
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

    Thread.UncaughtExceptionHandler mUncaughtHandler = new UncaughtExceptionHandler() {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.v(TAG, Log.getStackTraceString(ex));
            SystemProperties.set("persist.radio.dispatchAllKey", "false");
            ContentResolver resolver = getContentResolver();
            //Platform
            //AmigoSettings.putInt(resolver, AMIGO_SETTING_CC_SWITCH, UNLOCK_CONTOL_CENTER);
            //Gionee zhangke 20160913 add for CR01760585 start
			try{
				GnReflectionMethods gnMethod = new GnReflectionMethods(
					"amigo.provider.AmigoSettings",
					"putInt", new Class[]{ContentResolver.class, String.class,int.class}, 
					new Object[]{resolver, AMIGO_SETTING_CC_SWITCH, UNLOCK_CONTOL_CENTER});
				//Gionee <GN_BSP_MMI><lifeilong><20161203> modidy for ID37777 begin
                TestUtils.writeNodeState(GnMMITestApplication.this,TestUtils.CHARGING_SWITCH_ON,0);
				Log.e(TAG,"GnMMITestApplication  GnMMITestApplication -- > CHARGING_SWITCH_ON -- > 0 ");
				//Gionee <GN_BSP_MMI><lifeilong><20161203> modidy for ID37777 begin
				
				gnMethod.getInvokeResult1(GnMMITestApplication.this);

                Log.i(TAG, "AmigoSettings putInt control_center_switch 1");
			}catch(Exception e){
				Log.e(TAG, "Exception = "+e.getMessage());
			}
            //Gionee zhangke 20160913 add for CR01760585 end
            System.exit(0);
        }
    };

    /**
     * add for CR01512325 end
     */
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        registerActivityLifecycleCallbacks(mCallBack);
        // add for CR01512325 begin
        ContentResolver resolver = getContentResolver();
        //Platform
        //AmigoSettings.putInt(resolver, AMIGO_SETTING_CC_SWITCH, LOCK_CONTROL_CENTER);
        //Gionee zhangke 20160913 add for CR01760585 start
        try{
            GnReflectionMethods gnMethod = new GnReflectionMethods(
                "amigo.provider.AmigoSettings",
                "putInt", new Class[]{ContentResolver.class, String.class,int.class}, 
                new Object[]{resolver, AMIGO_SETTING_CC_SWITCH, LOCK_CONTROL_CENTER});
            
            gnMethod.getInvokeResult1(this);
            Log.i(TAG, "AmigoSettings putInt control_center_switch 0");
        }catch(Exception e){
            Log.e(TAG, "Exception = "+e.getMessage());
        }
        //Gionee zhangke 20160913 add for CR01760585 end

        Thread.setDefaultUncaughtExceptionHandler(mUncaughtHandler);
        // add for CR01512325 end
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
                Log.v(TAG, "remove ActivityLifecycleCallbacks for TestResult");
                unregisterActivityLifecycleCallbacks(mCallBack);
                return;
            }
            Log.v(TAG, "ProcessId = " + Process.myPid()
                    + " add componentName" + componentName);
            mActivities.add(activity);
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            mActivities.remove(activity);
            String componentName = activity.getComponentName().getShortClassName();
            final int length = mActivities.size();
            Log.v(TAG, "ProcessId = " + Process.myPid()
                    + " Activity stackSize = " + length
                    + " remove componentName" + componentName);
            if (length == 0) {
                unregisterActivityLifecycleCallbacks(mCallBack);
                //add for CR01512325 begin
                SystemProperties.set("persist.radio.dispatchAllKey", "false");
                ContentResolver resolver = getContentResolver();
                //Platform
                //AmigoSettings.putInt(resolver, AMIGO_SETTING_CC_SWITCH, UNLOCK_CONTOL_CENTER);
                //Gionee zhangke 20160913 add for CR01760585 start
                try{
                    GnReflectionMethods gnMethod = new GnReflectionMethods(
                        "amigo.provider.AmigoSettings",
                        "putInt", new Class[]{ContentResolver.class, String.class,int.class}, 
                        new Object[]{resolver, AMIGO_SETTING_CC_SWITCH, UNLOCK_CONTOL_CENTER});
                    
                    gnMethod.getInvokeResult1(GnMMITestApplication.this);
                    Log.i(TAG, "AmigoSettings putInt control_center_switch 1");
                }catch(Exception e){
                    Log.e(TAG, "Exception = "+e.getMessage());
                }
                //Gionee zhangke 20160913 add for CR01760585 end

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
}
