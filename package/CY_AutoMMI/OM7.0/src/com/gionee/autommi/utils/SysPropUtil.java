package com.gionee.autommi.utils;
import android.os.SystemProperties;
public class SysPropUtil {
    /**
     * 判断FM属性
     */
    public static final boolean FM_IS_BLU = "yes".equals(SystemProperties.get("gn.mmi.blu.fm", "no"));
}