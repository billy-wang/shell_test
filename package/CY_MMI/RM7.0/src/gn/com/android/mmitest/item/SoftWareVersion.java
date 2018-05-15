
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

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
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
// Gionee <mmi> zhangke 20160921 add for 2350 begin
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import android.text.format.Formatter;
import java.io.IOException;
import java.io.Reader;
// Gionee <mmi> zhangke 20160921 add for 2350 end
import android.provider.Settings;
import android.content.Intent;
import gn.com.android.mmitest.TestUtils;
import gn.com.android.mmitest.item.FeatureOption;

public class SoftWareVersion extends Activity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private TextView mContentTv;

    private static String TAG = "SoftWareVersion";
    // Gionee <mmi> zhangke 20160921 add for 2350 begin
    private final static String RAM_PATH = "/proc/meminfo";
    private final static String ROM_PATH = "/proc/partitions";
    // Gionee <mmi> zhangke 20160921 add for 2350 end
    WindowManager.LayoutParams mlp;
    
    private boolean mIsScreenBright = false;
    private boolean mIsScreenBrightStatus = false;
    private boolean softFlag = false;
    private Intent it;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        // Gionee zhangke 20151215 modify for CR01609753 end

        setContentView(R.layout.common_textview);
        it = this.getIntent();
        if(it != null){
            softFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"softFlag = " + softFlag);        
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mRightBtn.setEnabled(true);
        if(softFlag){
            FeatureOption.initMmiXml();
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }        

    }

    @Override
    public void onStart() {
        super.onStart();

        showSoftwareVersionInfo();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        /*if (mIsScreenBrightStatus) {
            Settings.System.putInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
        }*/
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

        case R.id.right_btn: {
            if(softFlag){
                TestUtils.asResult(TAG,"","1");
            }            
            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.rightPress(TAG, this);
            break;
        }

        case R.id.wrong_btn: {
            if(softFlag){
                TestUtils.asResult(TAG,"","0");
            }             
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

    // Gionee zhangke 20151008 add for CR01564534 start
    private String getAppInfo() {
        try {
            String pkName = this.getPackageName();
            String versionName = this.getPackageManager().getPackageInfo(pkName, 0).versionName;
            return versionName;
        } catch (Exception e) {
        }
        return null;
    }
    // Gionee zhangke 20151008 add for CR01564534 end

    // Gionee <mmi> zhangke 20160921 add for 2350 begin
    private void showSoftwareVersionInfo() {
        String gnvernumber = SystemProperties.get("ro.gn.gnznvernumber");
        if ("eng".equals(SystemProperties.get("ro.build.type"))) {
            gnvernumber += "_eng";
        }
        String buildTime = SystemProperties.get("ro.build.date");
        String uct = SystemProperties.get("ro.build.date.utc");
        String gnvernumberrel = SystemProperties.get("ro.gn.gnvernumberrel");
        ContentResolver cv = this.getContentResolver();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (uct != null && !uct.equals("")) {
            buildTime = sdf.format(Long.parseLong(uct) * 1000);
        }
        String btResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String ftResult = getResources().getString(R.string.gn_ft_bt_result_no);

        String wbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String wftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String cdmabtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String cdmaftResult = getResources().getString(R.string.gn_ft_bt_result_no);

        String tdbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String tdftResult = getResources().getString(R.string.gn_ft_bt_result_no);

        String lbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String lftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String lbtResult1 = getResources().getString(R.string.gn_ft_bt_result_no);
        String lftResult1 = getResources().getString(R.string.gn_ft_bt_result_no);
        String lftTddAntResult = getResources().getString(R.string.gn_ft_bt_result_no);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170412> add for ID 111291 begin
        String gm_3 = getResources().getString(R.string.gn_ft_bt_result_no);
        String gpsCoc = getResources().getString(R.string.gn_ft_bt_result_no);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170412> add for ID 111291 end
        String snInfo = getResources().getString(R.string.sn_info);
        TelephonyManager teleMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String sn = SystemProperties.get("gsm.serial");
        Log.d(TAG, " read sn = " + sn);
        if (sn != null) {
            char[] barcodes = sn.toCharArray();
            //Gionee <GN_BSP_MMI> <lifeilong> <20170412> add for ID 111291 begin
            if (barcodes != null && barcodes.length >= 26) {
                if ('P' == barcodes[25])
                    gm_3 = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[25])
                    gm_3 = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            //Gionee <GN_BSP_MMI> <lifeilong> <20170412> add for ID 111291 end
            //Gionee <GN_BSP_MMI> <lifeilong> <201710801> add for ID 175443 begin
            if (barcodes != null && barcodes.length >= 34) {
                if ('P' == barcodes[33])
                    gpsCoc = getResources().getString(R.string.gn_ft_bt_result_success);
                else if ('F' == barcodes[33])
                    gpsCoc = getResources().getString(R.string.gn_ft_bt_result_fail);
            }
            //Gionee <GN_BSP_MMI> <lifeilong> <201710801> add for ID 175443 end            
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
        } else {
            final boolean gnoverseaflag = SystemProperties.get("ro.gn.oversea.product").equals("yes");
            /*if (gnoverseaflag) {
                String[] gnvernumbers = gnvernumber.split("_");
                String splitGnvernumbers = "";
                int length = gnvernumbers.length;
                if (gnvernumbers != null && gnvernumbers.length > 0) {
                    for (int i = 0; i < length; i++) {
                        if (i == length - 1) {
                            splitGnvernumbers += gnvernumbers[i];
                        } else if (i == length - 2) {
                            if (!TextUtils.isEmpty(gnvernumberrel)) {
                                //Gionee <GN_BSP_MMI> <lifeilong> <20170919> modify for ID 217098 begin
                                splitGnvernumbers += gnvernumbers[i].substring(0, 1) + gnvernumberrel + "_";
                            } else {
                                if (gnvernumbers[i].length() > 2) {
                                    splitGnvernumbers += gnvernumbers[i].substring(0, 3) + "_";
                                }
                            }
                        } else {
                            splitGnvernumbers += gnvernumbers[i] + "_";
                        }
                        //Gionee <GN_BSP_MMI> <lifeilong> <20170919> modify for ID 217098 end
                    }
                    gnvernumber = splitGnvernumbers;
                }
            }*/
        }

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(getResources().getString(R.string.external_version));
        contentBuilder.append(":");
        contentBuilder.append(gnvernumber);
        contentBuilder.append("\n");
        contentBuilder.append(snInfo);
        contentBuilder.append("\nRAM:");
        contentBuilder.append(getRamInfo());
        contentBuilder.append("  ROM:");
        contentBuilder.append(getRomInfo());
        contentBuilder.append("\nGSM BT: ");
        contentBuilder.append(btResult);
        contentBuilder.append("\nGSM FT: ");
        contentBuilder.append(ftResult);

        if (FeatureOption.GN_RW_GN_MMI_WCDMA_SUPPORT) {
            contentBuilder.append("\nWCDMA BT:");
            contentBuilder.append(wbtResult);
            contentBuilder.append("\nWCDMA FT:");
            contentBuilder.append(wftResult);
        }
        if (FeatureOption.GN_RW_GN_MMI_CDMA_SUPPORT) {
            contentBuilder.append("\nCDMA BT:");
            contentBuilder.append(cdmabtResult);
            contentBuilder.append("\nCDMA FT:");
            contentBuilder.append(cdmaftResult);

        }
        if (FeatureOption.GN_RW_GN_MMI_TDSCDMA_SUPPORT) {
            contentBuilder.append("\nTD-SCDMA BT:");
            contentBuilder.append(tdbtResult);
            contentBuilder.append("\nTD-SCDMA FT:");
            contentBuilder.append(tdftResult);

        }
        if (FeatureOption.GN_RW_GN_MMI_LTETDD_SUPPORT) {
            contentBuilder.append("\nLTETDD BT:");
            contentBuilder.append(lbtResult);
            contentBuilder.append("\nLTETDD FT:");
            contentBuilder.append(lftResult);
        }
        if (FeatureOption.GN_RW_GN_MMI_LTEFDD_SUPPORT) {
            contentBuilder.append("\nLTEFDD BT:");
            contentBuilder.append(lbtResult1);
            contentBuilder.append("\nLTEFDD FT:");
            contentBuilder.append(lftResult1);
        }
        if (FeatureOption.GN_RW_GN_MMI_LTETDDANT_SUPPORT) {
            contentBuilder.append("\nLTETDDANT :");
            contentBuilder.append(lftTddAntResult);
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170412> add for ID 111291 begin
        if (FeatureOption.GN_RW_GN_MMI_GM3_SUPPORT) {
            contentBuilder.append("\nGM 3.0 :");
            contentBuilder.append(gm_3);
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170412> add for ID 111291 end


        //Gionee <GN_BSP_MMI> <lifeilong> <201710801> add for ID 175443 begin
        if (FeatureOption.GN_RW_GN_MMI_GM3_SUPPORT) {
            contentBuilder.append("\nGPSCOC :");
            contentBuilder.append(gpsCoc);
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <201710801> add for ID 175443 end


        contentBuilder.append("\n");
        contentBuilder.append(getResources().getString(R.string.mmi_version_name));
        contentBuilder.append(getAppInfo());
        Log.i(TAG, "mmi version name = " + getAppInfo());
        contentBuilder.append("\n");
        contentBuilder.append(getResources().getString(R.string.buildtime));
        contentBuilder.append(": ");
        contentBuilder.append(buildTime);
        mContentTv.setText(contentBuilder.toString());

    }

    String getRamInfo() {
        long memSize = 0;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(RAM_PATH);
            bufferedReader = new BufferedReader(fileReader);
            if (bufferedReader != null) {
                String line = bufferedReader.readLine();
                String[] arrays = line.split("\\s+");
                for (String value : arrays) {
                    Log.v(TAG, "mem value: " + value);
                }
                memSize = Long.valueOf(arrays[1]).longValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeQuiet(bufferedReader);
            closeQuiet(fileReader);
        }
        Log.i(TAG, "memSize1=" + memSize);
        double ram1 = (double) memSize / (1024 * 1024);
        String virturlValue = getVirturlValue(ram1);
        Log.i(TAG, "ram1=" + ram1 + ";virturlValue=" + virturlValue);
        return virturlValue;
    }

    String getRomInfo() {
        long romSize = 0;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(ROM_PATH);
            bufferedReader = new BufferedReader(fileReader);
            if (bufferedReader != null) {
                String line = null;
                // Line 1 is " major minor #blocks name "
                bufferedReader.readLine();
                // Line 2 is blank line.
                bufferedReader.readLine();
                boolean isAdd = false;
                while (!isAdd) {
                    line = bufferedReader.readLine();
                    Log.v(TAG, "line = " + line);
                    if (line != null && line.contains("mmcblk0")) {
                        String[] arrays = line.split("\\s+");
                        romSize += Long.valueOf(arrays[3]).longValue();
                        isAdd = true;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "romSize1=" + romSize);
        double rom1 = (double) romSize / (1024 * 1024);
        String virturlValue = getVirturlValue(rom1);
        Log.i(TAG, "rom1=" + rom1 + ";virturlValue=" + virturlValue);
        return virturlValue;
    }

    void closeQuiet(Reader reader) {
        try {
            if (reader != null)
                reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getVirturlValue(double value) {
        double virturlValue;
        if (value > 1 && value <= 2) {
            virturlValue = 2;
        } else if (value > 2 && value <= 3) {
            virturlValue = 3;
        } else if (value > 3 && value <= 4) {
            virturlValue = 4;
        } else if (value > 4 && value <= 6) {
            virturlValue = 6;
        } else if (value > 6 && value <= 8) {
            virturlValue = 8;
        } else if (value > 8 && value <= 10) {
            virturlValue = 10;
        } else if (value > 10 && value <= 12) {
            virturlValue = 12;
        } else if (value > 12 && value <= 16) {
            virturlValue = 16;
        } else if (value > 16 && value <= 32) {
            virturlValue = 32;
        } else if (value > 32 && value <= 64) {
            virturlValue = 64;
        } else if (value > 64 && value <= 128) {
            virturlValue = 128;
        } else if (value > 128 && value <= 256) {
            virturlValue = 256;
        } else {
            virturlValue = Math.ceil(value);
        }
        return virturlValue + " GB";
    }
    // Gionee <mmi> zhangke 20160921 add for 2350 end
}
