
package com.gionee.autommi;



import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
//Gionee zhangke 20151026 add for CR01574984 start
import com.gionee.util.DswLog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.os.SystemProperties;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
//Gionee zhangke 20151026 add for CR01574984 end



public class TestUtils {
    public static WakeLock mWakeLock;
    //Gionee zhangke 20151026 add for CR01574984 start
    private static final String TAG = "TestUtils";
    private static final String AMIGOSETTING_DB_CONFIG_FILE  = "/system/etc/gnmmiConfig.xml";   
    private static String stereoEnable;
    //Gionee zhangke 20151026 add for CR01574984 end
    public static void acquireWakeLock(Activity activity) {
        if (mWakeLock == null || false == mWakeLock.isHeld()) {
            PowerManager powerManager = (PowerManager) (activity.getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE));
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Single Test");

        }
        if (false == mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    public static void releaseWakeLock() {
        if (null != mWakeLock && true == mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    //Gionee zhangke 20151026 add for CR01574984 start
    public	static String  getStreamVoice( String a) {
        FileReader dbReader;
        String value = null;
        final File dbConfigFile = new File(AMIGOSETTING_DB_CONFIG_FILE);
	
        try {
            dbReader = new FileReader(dbConfigFile);
        } catch (FileNotFoundException e) {
            DswLog.e(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
            return null;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(dbReader);
            XmlUtils.beginDocument(parser, "gnmmi");
            while (true) {
                XmlUtils.nextElement(parser);
                String name = parser.getName();
                if (!"gnmmi".equals(name)) {
                    return null;
                }
				
                String name1 = parser.getAttributeValue(null, "name");
                if (a.equals(name1)) {
                     value = parser.getAttributeValue(null, "value");
                     DswLog.i(TAG, "name=" + name1+";value=" + value);
					 dbReader.close();
					 break;
				}
			}
		} catch (XmlPullParserException e) {
			DswLog.e(TAG, "Exception	config parser " + e);
		} catch (IOException e) {
			DswLog.e(TAG, "Exception in font config parser " + e);
		} 
		return value;
    }
    //Gionee zhangke 20151026 add for CR01574984 end
    //Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 begin
    private static void setStereoEable() {
        if ( SystemProperties.get("ro.gn.stereo.support").equals("yes")) {
            stereoEnable = SystemProperties.get("persist.sys.gn.stereo.enable");
            SystemProperties.set("persist.sys.gn.stereo.enable", "no");
            DswLog.d(TAG,"start mmi persist.sys.gn.stereo.enable="+SystemProperties.get("persist.sys.gn.stereo.enable"));
        }
    }

    private static void revertStereoEable() {
        if ( SystemProperties.get("ro.gn.stereo.support").equals("yes")) {
            SystemProperties.set("persist.sys.gn.stereo.enable", stereoEnable);
            DswLog.d(TAG,"exit mmi persist.sys.gn.stereo.enable="+SystemProperties.get("persist.sys.gn.stereo.enable"));
        }
    }

    public static void startAudioSetter() {
        setStereoEable();
    }

    public static void stopAudioSettter() {
        revertStereoEable();
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170321> modify for ID 89433 end

    //Gionee <GN_BSP_AutoMMI> <chengq> <20170505> add for ID 125964 begin
    public static boolean writeNodeState(Context context, String nodeType, int value) {
        Object pm = (Object) (context.getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
            method.invoke(pm, f.get(null), value);
            DswLog.i(TAG,"writeGestureNodeValue "+nodeType+" "+f.get(null)+":"+value);
            return true;
        } catch (Exception e) {
            DswLog.e(TAG, "Exception :" + e);
        }
        return false;
    }

    public static int getNodeState(Context context, String nodeType) {
        Object pm = (Object) (context.getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("GetNodeState", int.class);
            Field f = cls.getField(nodeType);
            int value = (int)method.invoke(pm, f.get(null));
            DswLog.i(TAG,"getNodeValue "+nodeType+" "+f.get(null)+":"+value);
            return value;
        } catch (Exception e) {
            DswLog.e(TAG, "Exception :" + e);
        }
        return -1;
    }
    //Gionee <GN_BSP_AutoMMI> <chengq> <20170505> add for ID 125964 end
}

