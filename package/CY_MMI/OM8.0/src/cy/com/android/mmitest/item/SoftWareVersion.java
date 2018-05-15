
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import cy.com.android.mmitest.utils.DswLog;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
/*Gionee futao 20150217 modfy for CR01635407 begin*/
import java.lang.reflect.Method;
import java.util.Locale;
/*Gionee futao 20150217 modfy for CR01635407 end*/
import android.content.res.Configuration;
import android.os.SystemProperties;
import android.provider.Settings;
import cy.com.android.mmitest.utils.CountryUitl;
import cy.com.android.mmitest.utils.ProinfoUtil;

public class SoftWareVersion extends BaseActivity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private TextView mContentTv;

    private static String TAG = "SoftWareVersion";
    /* Gionee huangjianqiang 20160125 add for CR01628438 begin*/
    private boolean mInterVer = false;
    private boolean mCaliInfo = false;
    /* Gionee huangjianqiang 20160125 add for CR01628438 end*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开软件版本检测 @" + Integer.toHexString(hashCode()));
        TestUtils.setCurrentAciticityTitle(TAG,this);
        // Gionee xiaolin 20120924 add for CR00693542 start
        /* Gionee huangjianqiang 20160125 add for CR01628438 begin*/
        mInterVer = getIntent().getBooleanExtra("InternalVersion", false);
        mCaliInfo = getIntent().getBooleanExtra("CalibrationInfo", false);
        /* Gionee huangjianqiang 20160125 add for CR01628438 end*/
        /* Gionee huangjianqiang 20160125 modify for CR01628438 begin*/
        if (!mInterVer && !mCaliInfo) {
            TestUtils.checkToContinue(this);
        }

        /* Gionee huangjianqiang 20160125 modify for CR01628438 end*/

        // Gionee xiaolin 20120924 add for CR00693542 end
        setContentView(R.layout.common_textview);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);

        /* Gionee huangjianqiang 20160125 add for CR01628438 begin*/
        if (mInterVer || mCaliInfo) {
            findViewById(R.id.test_title).setVisibility(View.GONE);
            mRightBtn.setText("OK");
            mWrongBtn.setVisibility(View.GONE);
            mRestartBtn.setVisibility(View.GONE);
            /* Gionee huangjianqiang 20160216 add for CR01635480 begin*/
            //Gionee <GN_BSP_MMI> <chengq> <20170418> modify for ID 118082 begin
            TestUtils.setAppContext(SoftWareVersion.this);
            TestUtils.initConfigPath();
            //Gionee <GN_BSP_MMI> <chengq> <20170418> modify for ID 118082 end
            cy.com.android.mmitest.item.FeatureOption.initMmiXml();
            /* Gionee huangjianqiang 20160216 add for CR01635480 end*/
        }
        if (mCaliInfo) {
            this.setTitle("Calibration Info");
        }
        /* Gionee huangjianqiang 20160125 add for CR01628438 end*/

        mContentTv = (TextView) findViewById(R.id.test_content);
        // Gionee xiaolin 20120827 add for CR00680743 start
        //Gionee zhangke 20151017 delete for platform start
        /*
        try {
            IWindowManager wm = IWindowManager.Stub.asInterface(
                                    ServiceManager.getService(Context.WINDOW_SERVICE));
            wm.freezeRotation(Surface.ROTATION_0); 
        } catch (RemoteException exc) {
             DswLog.e(TAG, "Unable to set Surface.ROTATION_0");
        }
        */
        //Gionee zhangke 20151017 delete for platform end
        // Gionee xiaolin 20120827 add for CR00680743 end
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出软件版本检测 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Gionee xiaolin 20130115 modify for CR00763354 start
        // Gionee xiaolin 20130305 modify for CR00778810 start
        /*Gionee futao 20160227 modify for CR01635407 begin*/
        //String gnvernumber = SystemProperties.get("ro.gn.gnznvernumber");
        String gnvernumber = SystemProperties.get("ro.cy.znvernumber");
        /*Gionee futao 20160227 modify for CR01635407 end*/
        String buildType = SystemProperties.get("ro.build.type");

        // Gionee xiaolin 20130305 modify for CR00778810 end
        // Gionee xiaolin 20130115 modify for CR00763354 end
        String buildTime = SystemProperties.get("ro.build.date");
        String uct = SystemProperties.get("ro.build.date.utc");
        String gnvernumberrel = SystemProperties.get("ro.cy.vernumber.rel");
        ContentResolver cv = this.getContentResolver();
        //GIONEE: luohui 2012-06-19 modify for CR00625161 minute wrong start->             
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:dd");
        /*Gionee huangjianqiang 20160531 modify for CR01711022 begin*/
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.ENGLISH);
        /*Gionee huangjianqiang 20160531 modify for CR01711022 end*/
        //GIONEE: luohui 2012-06-19 modify for CR00625161 minute wrong end<-
        if (uct != null && !uct.equals("")) {
            buildTime = sdf.format(Long.parseLong(uct) * 1000);
        }
        String gpsclockResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String bandtype = getResources().getString(R.string.gn_ft_bt_result_no);
        String btResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String ftResult = getResources().getString(R.string.gn_ft_bt_result_no);

        String wbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String wftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        //Gionee zhangke 20151008 add for CR01562334 start
        String cdmabtResult = getResources().getString(R.string.gn_ft_bt_result_no);

        String cdmaftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        //Gionee zhangke 20151008 add for CR01562334 end
        //Gionee <niejn><2013-04-19> add for CR00798345 start
        String tdbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String tdftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        //Gionee <niejn><2013-04-19> add for CR00798345 start
        String lbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String lftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String lbtResult1 = getResources().getString(R.string.gn_ft_bt_result_no);
        String lftResult1 = getResources().getString(R.string.gn_ft_bt_result_no);
        String lftTddAntResult = getResources().getString(R.string.gn_ft_bt_result_no);
        //Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 91383 begin
        String gm_3 = getResources().getString(R.string.gn_ft_bt_result_no);
        String gm_value = getResources().getString(R.string.gn_ft_bt_result_no);
        //Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 91383 end
        String efuse = getResources().getString(R.string.gn_ft_bt_result_no);
        String rpmb_status = getResources().getString(R.string.gn_ft_bt_result_no);
        String snInfo = getResources().getString(R.string.sn_info);
        TelephonyManager teleMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String sn = SystemProperties.get("gsm.serial");

        /* Gionee huangjianqiang 20160125 add for CR01629117 begin */
        String mMemoryinfo;
        GnMemoryinfo Memoryinfo = new GnMemoryinfo();
        mMemoryinfo = "\n" + " " + "\n" + "RAM: " + Memoryinfo.getTotalRam(this);
        mMemoryinfo += "   ROM: " + Memoryinfo.getTotalRom(this) + "\n";
        /* Gionee huangjianqiang 20160125 add for CR01629117 end */

        DswLog.d(TAG, " read sn = " + sn);
        if (sn != null) {
            char[] barcodes = sn.toCharArray();
            //Gionee zhangke 20151008 add for CR01562334 start
            //Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 91383 begin
            if (barcodes != null && barcodes.length >= 26) {
                if ('P' == barcodes[25] && ProinfoUtil.isGm3Pass())
                    gm_3 = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[25])
                    gm_3 = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            //Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 91383 end
            if (FeatureOption.GN_RW_GN_MMI_GM3_SUPPORT) {
                gm_value = "" + ProinfoUtil.getGm3Value();
            }
            if (FeatureOption.GN_RW_GN_MMI_EFUSE_SUPPORT && TestUtils.getSbcFlag()) {
                efuse = getResources().getString(R.string.gn_ft_bt_result_success);
            }
            if (FeatureOption.GN_RW_GN_MMI_RPMB_SUPPORT && ProinfoUtil.isWriteRpmbTag()) {
                rpmb_status = getResources().getString(R.string.gn_ft_bt_result_success);
            }
            if (barcodes != null && barcodes.length >= 34) {
                if ('P' == barcodes[33])
                    gpsclockResult = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[33])
                    gpsclockResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            if (barcodes != null && barcodes.length >= 36) {
                if ('P' == barcodes[35])
                    cdmabtResult = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[35])
                    cdmabtResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            if (barcodes != null && barcodes.length >= 37) {
                if ('P' == barcodes[36])
                    cdmaftResult = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[36])
                    cdmaftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }

            //Gionee zhangke 20151008 add for CR01562334 end
            if (barcodes != null && barcodes.length >= 41) {
                if ('P' == barcodes[40])
                    lbtResult1 = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[40])
                    lbtResult1 = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            if (barcodes != null && barcodes.length >= 42) {
                if ('P' == barcodes[41])
                    lftResult1 = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[41])
                    lftResult1 = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            if (barcodes != null && barcodes.length >= 43) {
                if ('P' == barcodes[42])
                    lbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[42])
                    lbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            if (barcodes != null && barcodes.length >= 44) {
                if ('P' == barcodes[43])
                    lftResult = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[43])
                    lftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            //Gionee <niejn><2013-04-19> add for CR00798345 start		
            if (barcodes != null && barcodes.length >= 47) {
                if ('P' == barcodes[46])
                    tdbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[46])
                    tdbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            if (barcodes != null && barcodes.length >= 48) {
                if ('P' == barcodes[47])
                    tdftResult = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[47])
                    tdftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            //Gionee <niejn><2013-04-19> add for CR00798345 end	
            if (barcodes != null && barcodes.length >= 59) {
                if ('P' == barcodes[58])
                    wbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[58])
                    wbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            if (barcodes != null && barcodes.length >= 60) {
                if ('P' == barcodes[59])
                    wftResult = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[59])
                    wftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }

            if (barcodes != null && barcodes.length >= 62) {
                if ('1' == barcodes[60] && '0' == barcodes[61]) {
                    btResult = getResources().getString(R.string.gn_ft_bt_result_success);
                } else if ('0' == barcodes[60] && '1' == barcodes[61]) {
                    btResult = getResources().getString(R.string.gn_ft_bt_result_fail);
                }
            }
            if (barcodes != null && barcodes.length >= 63) {
                if ('P' == barcodes[62]) {
                    ftResult = getResources().getString(R.string.gn_ft_bt_result_success);
                } else if ('F' == barcodes[62]) {
                    ftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
                }
            }
            if (barcodes != null && barcodes.length >= 58) {
                if ('P' == barcodes[57])
                    lftTddAntResult = getResources().getString(R.string.gn_ft_bt_result_success);
                if ('F' == barcodes[57])
                    lftTddAntResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            }

            if (sn.length() > 17) {
                snInfo = snInfo + sn.substring(0, 18);
            }
        }

        if (buildTime == null || buildTime.equals("")) {
            buildTime = getResources().getString(R.string.isnull);
        }

        if (gnvernumber == null || gnvernumber.equals("")) {
            gnvernumber = getResources().getString(R.string.isnull);
        }

        if (buildType == null || buildType.equals("")) {
            buildType = getResources().getString(R.string.isnull);
        }
        
        /*Gionee futao 20150217 modfy for CR01635407 begin*/
        /*Gionee huangjianqiang 20160419 modify for CR01675381 begin*/
        String content = "";
        if (!mCaliInfo) {
            content += getResources().getString(R.string.internal_version) + ":" + gnvernumber
                    + "\n" + getResources().getString(R.string.buildtype) + buildType
                    + "\n" + snInfo;

            String area = SystemProperties.get("persist.sys.gn.area");
            if (area != null && area.length() > 0)
                content += "\n" + getResources().getString(R.string.country_area) + CountryUitl .getCountry(SoftWareVersion.this,area);
            content += mMemoryinfo;
        }

        if (ProinfoUtil.isCheckVersion("CSW1707")) {
            //default true
            FeatureOption.GN_RW_GN_MMI_GSM_SUPPORT = true;
            FeatureOption.GN_RW_GN_MMI_WCDMA_SUPPORT = true;
            FeatureOption.GN_RW_GN_MMI_CDMA_SUPPORT = true;
            FeatureOption.GN_RW_GN_MMI_TDSCDMA_SUPPORT = true;
            FeatureOption.GN_RW_GN_MMI_LTETDD_SUPPORT = true;
            FeatureOption.GN_RW_GN_MMI_LTEFDD_SUPPORT = true;

            String curModem = ProinfoUtil.switchModem2();
            if (curModem.equals("B")) {
                FeatureOption.GN_RW_GN_MMI_CDMA_SUPPORT = false;
                FeatureOption.GN_RW_GN_MMI_TDSCDMA_SUPPORT = false;
                FeatureOption.GN_RW_GN_MMI_LTETDD_SUPPORT = false;
                bandtype = getResources().getString(R.string.softversion_band_b);
            }else if (curModem.equals("C")) {
                FeatureOption.GN_RW_GN_MMI_TDSCDMA_SUPPORT = false;
                bandtype = getResources().getString(R.string.softversion_band_c);
            }else if (curModem.equals("A")) {
                FeatureOption.GN_RW_GN_MMI_TDSCDMA_SUPPORT = false;
                bandtype = getResources().getString(R.string.softversion_band_a);
            } else if (curModem.equals("D")) {
                FeatureOption.GN_RW_GN_MMI_CDMA_SUPPORT = false;
                FeatureOption.GN_RW_GN_MMI_TDSCDMA_SUPPORT = false;
                bandtype = getResources().getString(R.string.softversion_band_d);
            } else {
                DswLog.d(TAG,"wrong band "+curModem);
            }
            content +=  "\nBandType:" + bandtype;
        }
        /*Gionee futao 20160330 modify for CR01663704*/
        if (FeatureOption.GN_RW_GN_MMI_GSM_SUPPORT) {
            content += "\nGSM BT: " + btResult + "\nGSM FT: " + ftResult;
        }
       /*Gionee futao 20150217 modfy for CR01635407 end*/

        //Gionee zhangke 20151019 add for CR01571097 start 
        if (FeatureOption.GN_RW_GN_MMI_WCDMA_SUPPORT) {
            content += "\nWCDMA BT:" + wbtResult + "\nWCDMA FT:" + wftResult;
        }
        //Gionee zhangke 20151008 add for CR01562334 start
        if (FeatureOption.GN_RW_GN_MMI_CDMA_SUPPORT) {
            content += "\nCDMA BT:" + cdmabtResult + "\nCDMA FT:" + cdmaftResult;
        }
        //Gionee zhangke 20151008 add for CR01562334 end
        //Gionee <niejn><2013-04-19> add for CR00798345 start
        if (FeatureOption.GN_RW_GN_MMI_TDSCDMA_SUPPORT) {
            content += "\nTD-SCDMA BT:" + tdbtResult + "\nTD-SCDMA FT:" + tdftResult;
        }
        if (FeatureOption.GN_RW_GN_MMI_LTETDD_SUPPORT) {
            content += "\nLTETDD BT:" + lbtResult + "\nLTETDD FT:" + lftResult;
        }
        if (FeatureOption.GN_RW_GN_MMI_LTEFDD_SUPPORT) {
            content += "\nLTEFDD BT:" + lbtResult1 + "\nLTEFDD FT:" + lftResult1;
        }
        //Gionee <Oversea_Bug> <tanbotao> <20161124> for #28369 beign
        if (FeatureOption.GN_RW_GN_MMI_ANT_SUPPORT) {
            content += "\nANT :" + lftTddAntResult;
        }
        //Gionee <Oversea_Bug> <tanbotao> <20161124> for #28369 end
        //Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 91383 begin
        if (FeatureOption.GN_RW_GN_MMI_GM3_SUPPORT) {
            content += "\nGM 3.0 :" + gm_3;
            content += "\nGM val :" + gm_value;
        }
        if (FeatureOption.GN_RW_GN_MMI_GPS_COC_SUPPORT) {
            content += "\nGPSCOC :" + gpsclockResult;
        }
        //Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 91383 end
        //Gionee <niejn><2013-04-19> add for CR00798345 end
        if (FeatureOption.GN_RW_GN_MMI_EFUSE_SUPPORT) {
            content += "\nEFUSE :" + efuse;
        }
        if (FeatureOption.GN_RW_GN_MMI_RPMB_SUPPORT) {
            content += "\nRPMB :" + rpmb_status;
        }

        //Gionee zhangke 20151019 add for CR01571097 end 
       /*Gionee futao 20150217 modfy for CR01635407 begin*/
        if (!mCaliInfo) {
            if (!mInterVer) {
                content += "\n" + getResources().getString(R.string.mmi_version_name) + getAppInfo();
                DswLog.i(TAG, "mmi version name = " + getAppInfo());
            } else {
                String AmigoFrameworkVersion = getAmigoFrameworkVersion();
                if (!AmigoFrameworkVersion.equals("")) {
                    content += "\n" + "widget: " + AmigoFrameworkVersion;
                }
            }
        /*Gionee futao 20150217 modfy for CR01635407 end*/
            content += "\n" + getResources().getString(R.string.buildtime) + ": " + buildTime;
        }

        /*Gionee huangjianqiang 20160419 modify for CR01675381 end*/
        mContentTv.setText(content);
        mRightBtn.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                /* Gionee huangjianqiang 20160125 modify for CR01628438 begin*/
                if (mInterVer || mCaliInfo) {
                    this.finish();
                } else {
                    TestUtils.rightPress(TAG, this);
                }
               /* Gionee huangjianqiang 20160125 modify for CR01628438 end*/
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }

            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    //Gionee zhangke 20151008 add for CR01564534 start
    private String getAppInfo() {
        try {
            String pkName = this.getPackageName();
            String versionName = this.getPackageManager().getPackageInfo(pkName, 0).versionName;
            return versionName;
        } catch (Exception e) {
        }
        return null;
    }
    //Gionee zhangke 20151008 add for CR01564534 end

    /* Gionee huangjianqiang 20160125 add  begin*/
    @Override
    protected void onPause() {
        super.onPause();
        if (mInterVer || mCaliInfo) {
            this.finish();
        }
    }

    /* Gionee huangjianqiang 20160125 add  end*/
     /*Gionee futao 20150217 modfy for CR01635407 begin*/
    private String getAmigoFrameworkVersion() {
        String version = "";
        Class<?> verClass = null;

        try {
            verClass = Class.forName("amigo.widget.AmigoWidgetVersion");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Method method = verClass.getMethod("getVersion");
            version = (String) method.invoke(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return version;
    }
     /*Gionee futao 20150217 modfy for CR01635407 end*/
    //Gionee <BP_BSP_MMI> <chengq> <20170401> modify for ID 101802 begin
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    //Gionee <BP_BSP_MMI> <chengq> <20170401> modify for ID 101802 end
}
