
package com.gionee.autommi;



import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
//Gionee zhangke 20151026 add for CR01574984 start
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
//Gionee <GN_BGP_AutoMMI><lifeilong><20161112> modify for 21007 begin
import java.util.HashMap;
import java.util.Map;
//Gionee <GN_BGP_AutoMMI><lifeilong><20161112> modify for 21007 end

//Gionee zhangke 20151026 add for CR01574984 end
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class TestUtils {
    public static WakeLock mWakeLock;
    //Gionee zhangke 20151026 add for CR01574984 start
    private static final String TAG = "TestUtils";
    private static final String AMIGOSETTING_DB_CONFIG_FILE  = "/system/etc/gnmmiConfig.xml";   
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
	//Gionee <GN_BGP_AutoMMI><lifeilong><20161112> modify for 21007 begin

	public static Map<String, String> factoryFlag = new HashMap<String, String>();
	static {
		//Gionee <GN_BGP_AutoMMI><lifeilong><20161112> modify for 23333 begin
		factoryFlag.put(IfaaActivity.FACTORY_IF,"33");
		//Gionee <GN_BGP_AutoMMI><lifeilong><20161112> modify for 23333 begin
		factoryFlag.put(WChatKeyTest.FACTORY_WC,"32");
	}
	//Gionee <GN_BGP_AutoMMI><lifeilong><20161112> modify for 21007 end


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
            Log.e(TAG, "Can't open " + AMIGOSETTING_DB_CONFIG_FILE);
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
                     Log.i(TAG, "name=" + name1+";value=" + value);
					 dbReader.close();
					 break;
				}
			}
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Exception	config parser " + e);
		} catch (IOException e) {
			Log.e(TAG, "Exception in font config parser " + e);
		} 
		return value;
    }
    //Gionee zhangke 20151026 add for CR01574984 end
    public static boolean runTestCommand(String cmd) {
        Process process = null;
        boolean result = false;
        
        try {
            // process = Runtime.getRuntime().exec("ps");
            process = Runtime.getRuntime().exec(cmd);//("/system/bin/diag_sendcmd -"+cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer output = new StringBuffer();
            int exeReturn = process.waitFor();
            if(exeReturn == 0){
                result = true;
            }
			String data = null;
            while ((data = br.readLine()) != null) {
                Log.i(TAG,"data="+data);
            }
            Log.d(TAG, "send command return:"+exeReturn);
        } catch (Exception e) {
            Log.d(TAG, "Unexpected error: " + e.getMessage());
            return false;
        } finally {
            process.destroy();
        }
        
        return result;
    }

    //Gionee <GN_AutoMMI><lifeilong><20161209> modify for ID 43323 begin
    public static boolean writeNodeState(Context context, String nodeType, int value) {
        Object pm = (Object) (context.getSystemService("amigoserver"));
        try {
            Class cls = Class.forName("android.os.amigoserver.AmigoServerManager");
            Method method = cls.getMethod("SetNodeState", int.class, int.class);
            Field f = cls.getField(nodeType);
		    method.invoke(pm, f.get(null), value);
            Log.i(TAG,"writeGestureNodeValue "+nodeType+" "+f.get(null)+":"+value);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e);
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
        Log.i(TAG,"getNodeValue "+nodeType+" "+f.get(null)+":"+value);
        return value;
    } catch (Exception e) {
        Log.e(TAG, "Exception :" + e);
    }
        return -1;
    }
    //Gionee <GN_AutoMMI><lifeilong><20161209> modify for ID 43323 end

}

