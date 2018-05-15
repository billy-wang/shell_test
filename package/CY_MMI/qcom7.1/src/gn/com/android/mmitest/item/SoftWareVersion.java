
package gn.com.android.mmitest.item;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;
//Gionee zjy 2011-12-13 add for  CR00475554  start
import android.text.TextUtils;
import android.util.Log;
//Gionee zjy 2011-12-13 add for  CR00475554  end 

// add by zhangxiaowei start
//import com.android.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
// add by zhangxiaowei end
// Gionee <mmi> zhangke 20160921 add for 2350 begin
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import android.text.format.Formatter;
import java.io.IOException;
import java.io.Reader;
// Gionee <mmi> zhangke 20160921 add for 2350 end

public class SoftWareVersion extends Activity implements OnClickListener, QcRilHookCallback {

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private TextView mContentTv;

    private static String TAG = "SoftWareVersion";
    // Gionee xiaolin 20130827 modify for CR00845883 start
    QcNvItems nvItems = null;
    // Gionee xiaolin 20130827 modify for CR00845883 end
    // Gionee <mmi> zhangke 20160921 add for 2350 begin
    private final static String RAM_PATH = "/proc/meminfo";
    private final static String ROM_PATH = "/proc/partitions";
    // Gionee <mmi> zhangke 20160921 add for 2350 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gionee xiaolin 20130827 modify for CR00845883 start
        nvItems = new QcNvItems(this, this);
        // Gionee xiaolin 20130827 modify for CR00845883 end
        Log.i(TAG, "onCreate nvItems="+nvItems);
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.common_textview);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mContentTv = (TextView) findViewById(R.id.test_content);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onQcRilHookReady() {
        // Gionee zhangxiaowei 20131102 modify for CR00935758 start

        StringBuffer stringBuffer = new StringBuffer();
        String gnznvernumber = SystemProperties.get("ro.gn.gnznvernumber");
        String type = SystemProperties.get("ro.build.type");// eng or user
        String typePart = ("user".equals(type) ? "" : "_" + type);
        stringBuffer.append(gnznvernumber).append(typePart);
        String gnvernumber = stringBuffer.toString();
        Log.d(TAG, "gnvernumber = " + gnvernumber);

        // Gionee zhangxiaowei 20131102 modify for CR00935758 end
        String buildTime = SystemProperties.get("ro.build.date");
        String uct = SystemProperties.get("ro.build.date.utc");
        // Gionee zjy 2011-12-13 add for CR00475554 start
        String gnvernumberrel = SystemProperties.get("ro.gn.gnvernumberrel");
        // Gionee zjy 2011-12-13 add for CR00475554 end
        ContentResolver cv = this.getContentResolver();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:dd");
        if (uct != null && !uct.equals("")) {
            buildTime = sdf.format(Long.parseLong(uct) * 1000);
        }
        String btResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String ftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String cdmabtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String cdmaftResult = getResources().getString(R.string.gn_ft_bt_result_no);

        String gbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String gftResult = getResources().getString(R.string.gn_ft_bt_result_no);

        // Gionee zhangxiaowei 20131116 modify for CR00935598 start
        String tdbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String tdftResult = getResources().getString(R.string.gn_ft_bt_result_no);
        // Gionee zhangxiaowei 20131116 modify for CR00935598 end
        String lbtResult = getResources().getString(R.string.gn_ft_bt_result_no);
        String lftResult = getResources().getString(R.string.gn_ft_bt_result_no);

        String lbtResult1 = getResources().getString(R.string.gn_ft_bt_result_no);
        String lftResult1 = getResources().getString(R.string.gn_ft_bt_result_no);

        String lftTddAntResult = getResources().getString(R.string.gn_ft_bt_result_no);

        String snInfo = getResources().getString(R.string.sn_info);

        String sn = null;
        try {
            sn = nvItems.getEgmrResult();
            SystemProperties.set("gsm.serial", sn);
            Log.e(TAG, "sn112:" + SystemProperties.get("gsm.serial"));
            Log.d(TAG, "sn:" + sn);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        snInfo = snInfo + sn;

        String factoryResult = null;
        try {

            factoryResult = nvItems.getFactoryResult();
            Log.d(TAG, factoryResult + " : " + factoryResult.length());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (null != factoryResult) {
            char[] barcodes = factoryResult.toCharArray();
            if ('P' == barcodes[5])
                gbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[5])
                gbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);

            if ('P' == barcodes[6])
                gftResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[6])
                gftResult = getResources().getString(R.string.gn_ft_bt_result_fail);

            if ('P' == barcodes[7])
                btResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[7])
                btResult = getResources().getString(R.string.gn_ft_bt_result_fail);

            if ('P' == barcodes[8])
                ftResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[8])
                ftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            if ('P' == barcodes[9])
                cdmabtResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[9])
                cdmabtResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            if ('P' == barcodes[10])
                cdmaftResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[10])
                cdmaftResult = getResources().getString(R.string.gn_ft_bt_result_fail);
            // Gionee zhangxiaowei 20131116 modify for CR00935598 start
            if ('P' == barcodes[26])
                tdbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[26])
                tdbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);

            if ('P' == barcodes[27])
                tdftResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[27])
                tdftResult = getResources().getString(R.string.gn_ft_bt_result_fail);

            if ('P' == barcodes[28])
                lbtResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[28])
                lbtResult = getResources().getString(R.string.gn_ft_bt_result_fail);

            if ('P' == barcodes[29])
                lftResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[29])
                lftResult = getResources().getString(R.string.gn_ft_bt_result_fail);

            if ('P' == barcodes[30])
                lbtResult1 = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[30])
                lbtResult1 = getResources().getString(R.string.gn_ft_bt_result_fail);

            if ('P' == barcodes[31])
                lftResult1 = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[31])
                lftResult1 = getResources().getString(R.string.gn_ft_bt_result_fail);

            if ('P' == barcodes[13])
                lftTddAntResult = getResources().getString(R.string.gn_ft_bt_result_success);
            if ('F' == barcodes[13])
                lftTddAntResult = getResources().getString(R.string.gn_ft_bt_result_fail);
        }

        if (buildTime == null || buildTime.equals("")) {
            buildTime = getResources().getString(R.string.isnull);
        }

        if (gnvernumber == null || gnvernumber.equals("")) {
            gnvernumber = getResources().getString(R.string.isnull);
        }

        // Gionee <mmi> zhangke 20160921 add for 2350 begin
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
        contentBuilder.append(gbtResult);
        contentBuilder.append("\nGSM FT: ");
        contentBuilder.append(gftResult);

        if (FeatureOption.GN_RW_GN_MMI_WCDMA_SUPPORT) {
            contentBuilder.append("\nWCDMA BT:");
            contentBuilder.append(btResult);
            contentBuilder.append("\nWCDMA FT:");
            contentBuilder.append(ftResult);
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

        contentBuilder.append("\n");
        contentBuilder.append(getResources().getString(R.string.mmi_version_name));
        contentBuilder.append(getAppInfo());
        Log.i(TAG, "mmi version name = " + getAppInfo());
        contentBuilder.append("\n");
        contentBuilder.append(getResources().getString(R.string.buildtime));
        contentBuilder.append(": ");
        contentBuilder.append(buildTime);
        mContentTv.setText(contentBuilder.toString());
        // Gionee <mmi> zhangke 20160921 add for 2350 end
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
            TestUtils.rightPress(TAG, this);
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
        Log.i(TAG, "rom1=" + rom1 + "virturlValue=" + virturlValue);
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
        return virturlValue+" GB";
    }

    // Gionee <mmi> zhangke 20160921 add for 2350 end

    //Gionee <GN_BSP_MMI><zhangke><20161109> add for ID21183 begin
    @Override
    public void onDestroy() {
        nvItems.dispose();
        super.onDestroy();
    }
    //Gionee <GN_BSP_MMI><zhangke><20161109> add for ID21183 end

}
