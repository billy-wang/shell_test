
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

//Gionee zhangke 20151026 add for CR01574984 end



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

}

