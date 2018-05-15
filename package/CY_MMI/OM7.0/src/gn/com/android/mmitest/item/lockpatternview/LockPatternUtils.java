package gn.com.android.mmitest.item.lockpatternview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gn.com.android.mmitest.item.lockpatternview.LockPatternView.Cell;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import gn.com.android.mmitest.utils.DswLog;

public class LockPatternUtils {

    //private static final String TAG = "LockPatternUtils";
    private final static String KEY_LOCK_PWD = "lock_pwd";


    private static Context mContext;

    private static SharedPreferences preference;

    //private final ContentResolver mContentResolver;

    public LockPatternUtils(Context context) {
        mContext = context;
        preference = PreferenceManager.getDefaultSharedPreferences(mContext);
        // mContentResolver = context.getContentResolver();
    }

    /**
     * Deserialize a pattern.
     *
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<LockPatternView.Cell> stringToPattern(String string) {
        List<LockPatternView.Cell> result = new ArrayList<LockPatternView.Cell>();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(LockPatternView.Cell.of(b / 3, b % 3));
        }
        return result;
    }

    /**
     * Serialize a pattern.
     *
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public static String patternToString(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        return Arrays.toString(res);
    }

    public void saveLockPattern(List<LockPatternView.Cell> pattern) {
        Editor editor = preference.edit();
        editor.putString(KEY_LOCK_PWD, patternToString(pattern));
        editor.commit();
    }

    public String getLockPaternString() {
        return preference.getString(KEY_LOCK_PWD, "[2, 1, 0, 3, 6, 7, 8, 5, 4]");
    }

    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
    public String getSkipEfuseLockPaternString(){
        return preference.getString(KEY_LOCK_PWD, "[6, 3, 0, 4, 8, 5, 2]");
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end

    public int checkPattern(List<LockPatternView.Cell> pattern) {
        DswLog.i("lockview", "checkPattern:" + patternToString(pattern));
        String stored = getLockPaternString();
        DswLog.i("lockview", "stored:" + stored);
        if (!stored.isEmpty()) {
            return stored.equals(patternToString(pattern)) ? 1 : 0;
        }
        return -1;
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 start
    public int checkEfusePattern(List<LockPatternView.Cell> pattern) {
        DswLog.i("lockview", "checkPattern:"+patternToString(pattern));
        String pass = getSkipEfuseLockPaternString();
        String go_on = getLockPaternString();
        DswLog.i("lockview", "pass:"+pass);
        if(go_on.equals(patternToString(pattern))){
             return 1;
        }else if(pass.equals(patternToString(pattern))){
             return 2;
        }else {
             return 0;
        }
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170405> add for ID 104468 end
    

    public void clearLock() {
        saveLockPattern(null);
    }


}
