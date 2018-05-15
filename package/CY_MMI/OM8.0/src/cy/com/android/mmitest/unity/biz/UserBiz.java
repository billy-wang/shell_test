package cy.com.android.mmitest.unity.biz;

import cy.com.android.mmitest.unity.bean.User;
import cy.com.android.mmitest.unity.biz.OnUnityListener;
import android.os.Bundle;
import android.content.Intent;
import java.util.List;
import android.content.BroadcastReceiver;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import cy.com.android.mmitest.utils.DswLog;
import android.os.SystemProperties;
import android.provider.Settings;
import java.util.Arrays;
import cy.com.android.mmitest.TestResult;
import cy.com.android.mmitest.NvRAMAgent;
import android.os.ServiceManager;
import android.os.IBinder;
import android.content.Intent;
import android.os.RemoteException;
/**
 * Created by qiang on 4/14/17.
 */
public class UserBiz implements IUserBiz {
    private static final String TAG = UserBiz.class.getName();
    private User mUser;

    private static String SDPATH = null;

    private  static String pConfigxml = "system/etc/GI/gnmmiConfig.xml";

    @Override
    public void login(final User user, final OnUnityListener loginListener) {
        //模拟子线程耗时操作
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mUser = user;
                if (startUnity(mUser)) {
                    mUser.setUnityResult("支持切换!");
                }else {
                    mUser.setUnityResult("不支持切换：");
                }
                loginListener.loginSuccess(mUser);
            }
        }.start();
    }

    @Override
    public void revert(final User user, final OnUnityRevertListener revertListener) {
        //模拟子线程耗时操作
        new Thread() {
            @Override
            public void run() {
                try {
                    updateSN();
                    revertListener.revertResult("标志位写入成功!");
                }catch (Exception e) {
                    revertListener.revertResult("标志位写入失败!");
                }

            }
        }.start();
    }


    private boolean startUnity(User user) {
        if (user.getError() == User.ERROR_IS_NOT_SUPPORT_UNITY) {
            return false;
        }
        if (user.isSameArea()) {
            user.setError(User.ERROR_IS_SAME_AREA);
            return false;
        }else {
            user.setError(User.ERROR_IS_SUCCED);
            return true;
        }

    }

    //检查MMI能否切换对应版本。
    private boolean initConfigxml(User user) {
        //目标文件覆盖源文件gnConfig.xml文件
        String AMIGOSETTING_DB_CONFIG_FILE = "/system/etc/gnmmiConfig.xml";
        String tmp = null;
        if (user.getIdCountry().equals("Default")) {
            tmp = AMIGOSETTING_DB_CONFIG_FILE;
        }else {//获取array中对应item
            tmp = "/system/etc/" + user.getIdCountry() + "/gnmmiConfig.xml";
        }
        File file = new File(tmp);
        if (!file.exists()) {
            user.setError(User.ERROR_IS_CONFIGXML_NOT_EXISTS);
            return false;
        }else {
            user.setIdConfigPath(tmp);
            user.setError(User.ERROR_IS_SUCCED);
            return true;
        }
    }


    private boolean updateSN() {
        EraseSD();

        byte[] sn_buff = new byte[510];
        try {
            System.arraycopy(TestResult.readINvramInfo(), 0, sn_buff, 0, 510);
            String oldSn = new String(sn_buff);
            if (oldSn == null || "".equals(oldSn)) {
                DswLog.i(TAG, "updateSN oldSn =" + oldSn);
                return false;
            }
        } catch (Exception e) {
            DswLog.i(TAG, "updateSN Exception =" + e.getMessage());
            return false;
        }
        sn_buff = TestResult.getNewSN(504, "S", sn_buff);
        sn_buff = TestResult.getNewSN(505, "C", sn_buff);
        sn_buff = TestResult.getNewSN(506, ":", sn_buff);
        sn_buff = TestResult.getNewSN(507, mUser.getIdCountry().substring(0,1), sn_buff);
        sn_buff = TestResult.getNewSN(508, mUser.getIdCountry().substring(1,2), sn_buff);

        TestResult.writeToNvramInfo(sn_buff);

        return true;
    }

   /* private byte[] getProductInfo() {
        IBinder binder = null;
        byte[] productInfoBuff = null;
        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            DswLog.e(TAG, "getService	NvRAMAgent binder is null");
        }
        if (null != binder) {
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                DswLog.i(TAG, "getProductInfo NvRAMAgent read");
                productInfoBuff = agent.readFileByName(ProinfoUtil.getProinfoPath());

            } catch (RemoteException ex) {
                DswLog.e(TAG, ex.toString());
            }
        }
        return productInfoBuff;
    }*/

    static {
        if (true == SystemProperties.get("ro.gn.oversea.product").equals("yes")) {
            //GIONEE lijinfang 2012-11-21 modify for CR00734894 start
            if (true == SystemProperties.get("ro.cy.custom").equals("AFRICA_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE lijinfang 2012-11-21 modify for CR00734894 end
                //GIONEE lijinfang 2013-02-27 modify for CR00774302 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("INDIA_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Amigo", "Apps"};
                //GIONEE lijinfang 2013-02-27 modify for CR00774302 end
                //GIONEE linggz 2013-06-29 modify for CR00831684 start
                //GIONEE yeduanwang 2013-10-24 modify for CR00933312 begin
                //GIONEE lijinfang 2014-07-19 modify for Nepal gionee start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("BENGALI_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
            } else if (true == SystemProperties.get("ro.cy.custom").equals("NEPAL_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE lijinfang 2014-07-19 modify for Nepal gionee end
            } else if (true == SystemProperties.get("ro.cy.custom").equals("VIETNAM_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
            } else if (true == SystemProperties.get("ro.cy.custom").equals("MYANMAR_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
            } else if (true == SystemProperties.get("ro.cy.custom").equals("HK_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE yeduanwang 2013-10-24 modify for CR00933312 end
            } else if (true == SystemProperties.get("ro.cy.custom").equals("TAIWAN_GPLUS")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE linggz 2013-06-29 modify for CR00831684 end
                //GIONEE lijinfang 2013-06-05 modify for CR00823172 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("PHILIPPINES_GIONEE")) {
                keepArray = new String[]{"Amigo", "mapbar", ".gn_apps.zip", "pctool", "APK", "Free games", "Free music", "Free videos", "Free pictures", "Gameloft", "Movies", "Music", "Pictures", "Video", "videos", "games", "pictures", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Apps"};
                //GIONEE lijinfang 2013-06-05 modify for CR00823172 end
                // Gionee yubo 2014-05-09 modify for CR01244512 begin
            } else if (true == SystemProperties.get("ro.cy.custom").equals("RUSSIA_PRESTIGIO")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Books", "Music", "Pictures", "Ringtones", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Video"};
                // Gionee yubo 2014-05-09 modify for CR01244512 end
                //GIONEE: caixf 2014-03-07 add for CR01101248 begin
            } else if (true == SystemProperties.get("ro.cy.custom").equals("VISUALFAN")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Pictures"};
                //GIONEE: caixf 2014-03-07 add for CR01101248 end
                // Gionee luoguangming 2014.05.20 modify for CR01246320 begin
            } else if (true == SystemProperties.get("ro.cy.custom").equals("PORTUGAL_SDT")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "sdt"};
                // Gionee luoguangming 2014.05.20 modify for CR01246320 end
                //Gionee caiqiaoling 2014-05-22 added for CR01269287 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("INDONESIA_MAXTRON")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Music", "gn_resources", "gntheme", "Theme", "ThemePark", "Changer", "Videos"};
                //Gionee caiqiaoling 2014-05-22 added for CR01269287 end
                //Gionee liuxr 2014-04-12 added for CR01185432 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("PAKISTAN_QMOBILE")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Musics", "Music", "gn_resources", "QMobile_resources", "gntheme", "Theme", "ThemePark", "Changer", "Videos"};
                //Gionee liuxr 2014-04-12 added for CR01185432 end

                //Gionee lucy 2014-06-18 add for CR01296261 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("BANGLADESH_WALTON")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Document", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Videos"};
                //Gionee lucy 2014-06-18 add for CR01296261 end
                //Gionee guanxiaowen 2014-06-23 added for CR01307949 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("ZIMBABWE_GTEL")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "Apps", "Videos", "Movies", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Wallpapers"};
                //Gionee guanxiaowen 2014-06-23 added for CR01307949 end
                //Gionee xuyongji 2014-07-12 added for CR01321309 start
            } else if (true == SystemProperties.get("ro.cy.custom").equals("SOUTH_AMERICA_BLU")) {
                keepArray = new String[]{"mapbar", ".gn_apps.zip", "pctool", "gn_resources", "gntheme", "Theme", "ThemePark", "Music", "Changer", "Wallpaper"};
                //Gionee xuyongji 2014-07-12 added for CR01321309 end
            } else {
                keepArray = new String[]{"mapbar", "music", "gn_resources", "video", "gntheme", "Theme", "ThemePark", ".gn_apps.zip", "pctool", "Music", "Changer"};
            }
        } else {
            //keepArray = new String[]{"mapbar", "music","gn_resources", "video","gntheme", "ThemePark","主题","随变主题",".gn_apps.zip", "pctool", "音乐", "视频", "随变", "锁屏"};
            keepArray = new String[]{"Amigo", "mapbar", "music", "gn_resources", "video", "ThemePark", ".gn_apps.zip", "pctool", "音乐", "视频", "锁屏"};

        }
    }

    // Gionee liuying 20121025 modify for CR00718158 end
    // Gionee xuming 20121024 modify for CR00703768 end
    private static String[] keepArray;
    private static List<String> keepList = Arrays.asList(keepArray);


    private static void EraseSD() {
        // Gionee xiaolin 20120620 modify for CR00626921 start
        SDPATH = "/mnt/sdcard";
        File sd = new File(SDPATH);
        if (sd.canWrite())
            dFile(new File(SDPATH));

        SDPATH = "/mnt/sdcard2";
        sd = new File(SDPATH);
        if (sd.canWrite())
            dFile(new File(SDPATH));
        // Gionee xiaolin 20120620 modify for CR00626921 end
        /*Gionee huangjianqiang 20160326 add for CR01623736 beign*/
        SDPATH = "/mnt/m_external_sd";
        sd = new File(SDPATH);
        if (sd.canWrite())
            dFile(new File(SDPATH));
        /*Gionee huangjianqiang 20160326 add for CR01623736 beign*/
    }

    private static void dFile(File file) {
        for (String item : keepList) {
            if ((SDPATH + "/" + item).equalsIgnoreCase(file.toString()))
                return;
        }

        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
                return;
            } else if (file.isDirectory()) {
                DswLog.e(TAG, "dir :" + file.toString());
                File files[] = file.listFiles();
                if (files == null) {
                    DswLog.e(TAG, file + " listFiles()" + " return null");
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    dFile(files[i]);
                }
            }

            if (!SDPATH.equals(file.toString())) {
                file.delete();
            }

        } else {
            DswLog.e(TAG, "delete file is not exist");
        }
    }
}