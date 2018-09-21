
package cy.com.android.mmitest;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import cy.com.android.mmitest.utils.DswLog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.KeyEvent;
import cy.com.android.mmitest.item.FeatureOption;
import cy.com.android.mmitest.utils.FlagNvramUtil;

public class TestResult2 extends BaseActivity {
    private TextView mTitleTv, mContentTv, mSNTv;

    private HashMap<String, Integer> mResultSP2;
    private HashMap<String, String> mSNResultSP;
    Button mQuickBtn;
    private static final String TAG = "TestResult2";
    private static final int EVENT_RESPONSE_SN_WRITE = 1, EVENT_RESPONSE_SN_READ = 2, EVENT_RESPONSE_AUTO_MODE_READ = 3;
    SharedPreferences.Editor mSNEditor;
    private int mCount;
    private boolean mSecWrite;
    private boolean mSecRead, mAuToSecRead;
    private String mSNToWrite;
    private Resources mRes;
    public static String PRODUCT_INO_NAME = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";//see CFG_file_info_custom.h
    private final int SN_LENGTH = 25;
    private byte[] mSnByteArray = new byte[SN_LENGTH];
    private final int MMI_PCBA_MMI_TAG = 24;
    private final int MMI_PASS = 0x50;
    private final int MMI_FIAL = 0x46;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle(R.string.test_result2);
        setContentView(R.layout.test_result);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mQuickBtn = (Button) findViewById(R.id.quit_btn);
        mQuickBtn.setEnabled(true);
        mSNTv = (TextView) findViewById(R.id.snlog);
        mRes = this.getResources();
        mTitleTv.setText(R.string.wait);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        String time = getString(R.string.test_time) + sdf.format(new Date());
        ((TextView) findViewById(R.id.test_time)).setText(time);

        mQuickBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DswLog.e(TAG, "TestResult activity is finish");
                Intent it = new Intent(TestResult2.this, CyMMITest.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                TestResult2.this.startActivity(it);
                TestResult2.this.finish();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onStart() {
        super.onStart();

        mCount = 0;

        if (TestUtils.autoTestResult2.isEmpty()) {
            DswLog.d(TAG, "autoTestResult2 is empty");
            mResultSP2 = getTestResult("gn_mmi_test2.xml");
        } else {
            DswLog.d(TAG, "autoTestResult2 is use data");
            mResultSP2 = TestUtils.autoTestResult2;
        }

        mSNResultSP = getSNResult("gn_mmi_sn.xml");

        StringBuilder sb = new StringBuilder();

        int value = 1;

        //自动测试2
        sb.append(this.getResources().getString(R.string.result_from_test2) + "\n");
        if (null != mResultSP2) {
            for (String key : mResultSP2.keySet()) {
                value = mResultSP2.get(key);
                if (0 == value) {
                    mCount++;
                    sb.append(mCount + ":   " + key + "\n");
                }
            }
        }

        mTitleTv.setText(getResources().getString(R.string.test_result_count, mCount));
        mContentTv.setText(sb.toString());
        updateSN();

    }

    private HashMap<String, String> getSNResult(String spName) {
        HashMap<String, String> result = new HashMap<String, String>();
        String path = "/data/data/cy.com.android.mmitest/shared_prefs/";
        File f;
        Scanner sc;
        //DswLog.e(TAG, "start getSNResult");
        try {
            f = new File(path + spName);
            if (f.exists()) {
                //DswLog.e(TAG, f + " exists !!!");
            }
            if (f.canRead()) {
                //DswLog.e(TAG, f + " is readable !!!");
            }
            sc = new Scanner(f);
        } catch (FileNotFoundException ex) {
            return null;
        }
        while (sc.hasNextLine()) {
            String curline = sc.nextLine();
            //DswLog.e(TAG, "curline " + curline);
            Map<String, String> ls = processLine_2(curline);
            if (null != ls) {
                result.putAll(ls);
            }
        }

        sc.close();
        return result;
    }

    private Map<String, String> processLine_2(String line) {
        int keyStart = line.indexOf('"', 0);
        if (-1 == keyStart) {
            DswLog.e(TAG, line + " format is wrong!");
            return null;
        }
        int keyStop = line.indexOf('"', keyStart + 1);
        String key = line.substring(keyStart + 1, keyStop);
        String value = null;
        if (2 == key.length()) {
            int vi = line.indexOf('>', keyStop);
            value = line.substring(vi + 1, vi + 2);
        } else {
            int valStart = line.indexOf('"', keyStop + 1);
            int valStop = line.indexOf('"', valStart + 1);
            value = line.substring(valStart + 1, valStop);
        }
        HashMap<String, String> temp = new HashMap<String, String>();
        temp.put(key, value);
        return temp;

    }

    private HashMap<String, Integer> getTestResult(String spName) {
        // TODO Auto-generated method stub
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        String path = "/data/data/cy.com.android.mmitest/shared_prefs/";
        File f;
        Scanner sc;
        //DswLog.e(TAG, "start!!!");
        try {
            f = new File(path + spName);
            if (f.exists()) {
                DswLog.e(TAG, f + " exists !!!");
            }
            if (f.canRead()) {
                DswLog.e(TAG, f + " is readable !!!");
            }
            sc = new Scanner(f);
        } catch (FileNotFoundException ex) {
            return null;
        }
        //DswLog.e(TAG, "here!!!");
        //DswLog.e(TAG, "sc.hasNextLine() " + sc.hasNextLine());
        while (sc.hasNextLine()) {
            String curline = sc.nextLine();
            //DswLog.e(TAG, "curline " + curline);
            Map<String, Integer> ls = processLine(curline);
            if (null != ls) {
                result.putAll(ls);
            }
        }
        //Gionee zhangke 20151008 add for CR01562456 start
        sc.close();
        //Gionee zhangke 20151008 add for CR01562456 end

        return result;
    }

    private Map<String, Integer> processLine(String line) {
        int keyStart = line.indexOf('"', 0);
        if (-1 == keyStart) {
            DswLog.e(TAG, line + " format is wrong!");
            return null;
        }
        int keyStop = line.indexOf('"', keyStart + 1);
        String key = line.substring(keyStart + 1, keyStop);

        int valStart = line.indexOf('"', keyStop + 1);
        int valStop = line.indexOf('"', valStart + 1);
        String value = line.substring(valStart + 1, valStop);
        DswLog.e(TAG, "keyStart : " + keyStart + " keyStop : " + keyStop);
        HashMap<String, Integer> temp = new HashMap<String, Integer>();
        DswLog.e(TAG, "keys : " + key + " value " + Integer.valueOf(value));
        temp.put(key, Integer.valueOf(value));
        return temp;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private void updateSN() {

        mSnByteArray = FlagNvramUtil.readINvramInfo(SN_LENGTH);

        if ("true".equals(mSNResultSP.get("mIsAutoMode_2"))) {
            DswLog.d(TAG,"mIsAutoMode_2 is true");
            mSnByteArray = setNewSnNumber(mSnByteArray);
            FlagNvramUtil.writeToNvramInfo(mSnByteArray,SN_LENGTH);

            mSnByteArray = FlagNvramUtil.readINvramInfo(SN_LENGTH);

            checkFlagWriteSuccess(mSnByteArray);
        }

        setSnTextView(mSnByteArray);
        mQuickBtn.setEnabled(true);
    }

    private byte[] setNewSnNumber(byte[] snNumber) {

        if (mCount == 0) {
            snNumber = FlagNvramUtil.getNewSN(MMI_PCBA_MMI_TAG, "P", snNumber);
        } else {
            snNumber = FlagNvramUtil.getNewSN(MMI_PCBA_MMI_TAG, "F", snNumber);
        }
        return snNumber;
    }

    private void checkFlagWriteSuccess(byte[] mSnByteArray) {
        if (mCount == 0 && 'P' == mSnByteArray[MMI_PCBA_MMI_TAG]) {
            DswLog.i(TAG, "flag P writed succeed");
        }else if (mCount != 0 && 'F' == mSnByteArray[MMI_PCBA_MMI_TAG]) {
            DswLog.i(TAG, "flag F writed succeed");
        }else {
            DswLog.i(TAG, "flag writed Failed");
        }
    }


    private void setSnTextView(byte[] snNumber) {
        if (snNumber[MMI_PCBA_MMI_TAG] == ' ' || snNumber[MMI_PCBA_MMI_TAG] == '0' || snNumber[MMI_PCBA_MMI_TAG] == 0) {
            mTitleTv.setText(R.string.no_mmitest);
        } else if (snNumber[MMI_PCBA_MMI_TAG] == MMI_PASS) {
            mTitleTv.setText(R.string.mmitest_success);
        } else {
            mTitleTv.setText(R.string.mmitest_fail);
        }
    }

}
