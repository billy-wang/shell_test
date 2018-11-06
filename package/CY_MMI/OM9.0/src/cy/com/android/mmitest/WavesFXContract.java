package cy.com.android.mmitest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
/**
 * Contract class for controlling Waves effect
 */
public final class WavesFXContract {

    public static final String CONFIG = "config";

    // column names
    public static final String COLUMN_NAME_ENABLE = "enable";
    public static final String COLUMN_NAME_MEDIA_TYPE = "media_type";
    public static final String COLUME_NAME_MAXXSENSE = "maxxsense";

    // column indexes
    public static final int COLUMN_INDEX_ENABLE = 0;
    public static final int COLUMN_INDEX_MEDIA_TYPE = 1;
    public static final int COLUMN_INDEX_MAXXSENSE = 2;

    // definition for COLUMN_NAME_ENABLE
    public static final int WavesFXState_DISABLED = 0;
    public static final int WavesFXState_ENABLE = 1;
    public static final int WavesFXState_EXIT = 2;
    public static final int WavesFXState_CURRENT = -1;

    // definition for COLUMN_NAME_MEDIA_TYPE
    public static final int MediaType_MUSIC = 0;
    public static final int MediaType_MOVIE = 1;
    public static final int MediaType_GAME = 2;
    public static final int MediaType_VOICE = 3;
    public static final int MediaType_CURRENT = -1;
    public static final boolean GN_MAXXAUDIO_SUPPORT = SystemProperties.get("ro.gn.maxxaudio.support").equals("yes");

    private static final Uri BASE_URI = Uri
            .parse("content://com.waves.maxxaudio.WavesFXProvider/");

    public static final Uri CONFIG_URI = Uri.withAppendedPath(BASE_URI, CONFIG);

    public static final String[] ALL_COLUMNS = {COLUMN_NAME_MEDIA_TYPE, COLUMN_NAME_ENABLE, COLUME_NAME_MAXXSENSE};
    
    
    public static void setWavesState(Context c,int value)
    {
        ContentResolver contentResolver = c.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(WavesFXContract.COLUMN_NAME_ENABLE, value);
        contentResolver.update(WavesFXContract.CONFIG_URI, values, null, null);
    }
    
    public static int getWavesState(Context ct)
    {
    	int value = -1;
    	int time = 5000;
        ContentResolver contentResolver = ct.getContentResolver();
        Cursor c = contentResolver.query(WavesFXContract.CONFIG_URI, null, null, null, null);
        while (c == null) {
            try {
                Thread.sleep((long) 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            c = contentResolver.query(WavesFXContract.CONFIG_URI, null, null, null, null);
            if(time > 0)
            {
            	time -=100;
            }
            else
            {
            	break;
            }
        }
        if ( (c != null)) {
        	if(1 == c.getCount())
        	{
                c.moveToNext();
                int index=c.getColumnIndex(WavesFXContract.COLUMN_NAME_ENABLE);
                value = c.getInt(index);
        	}
            c.close();
        }
        return  value;
    }
}
