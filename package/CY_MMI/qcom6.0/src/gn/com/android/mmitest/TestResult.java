package gn.com.android.mmitest;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import android.os.SystemProperties;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View.OnClickListener;

import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
//Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 begin
import gn.com.android.mmitest.item.FeatureOption;
//Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 end

public class TestResult extends Activity implements QcRilHookCallback {
    private TextView mTitleTv, mContentTv, mSNTv;

    private SharedPreferences mResultSP;
    private SharedPreferences mSNResultSP;
    private ArrayList<String> mResultList;
    private Handler mUiHandler;
    Button mQuickBtn;
    private static final String TAG = "TestResult";
    private static final int EVENT_RESPONSE_SN_WRITE = 1, EVENT_RESPONSE_SN_READ = 2, EVENT_RESPONSE_AUTO_MODE_READ = 3;
    SharedPreferences.Editor mSNEditor;
    private int mCount;
    private boolean mSecWrite;
    private boolean mSecRead, mAuToSecRead;
    private String mSNToWrite;
    private Resources mRes;
    // Gionee xiaolin 20130827 modify for CR00845883 start
    private QcNvItems nvItems = null;
    // Gionee xiaolin 20130827 modify for CR00845883 end
    //Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 begin
	private static final int MMI_WCHAT_SOTER_TAG = 32;
	private static final int MMI_IFAA_KEY_TAG = 33;
    private static final int MMI_PASS = 0x50;
    private static final int MMI_FIAL = 0x46;
    //Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nvItems = new QcNvItems(this, this);
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.test_result);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mQuickBtn = (Button) findViewById(R.id.quit_btn);
        mQuickBtn.setEnabled(true);
        mSNTv = (TextView) findViewById(R.id.snlog);
        mRes = this.getResources();
        mTitleTv.setText(R.string.test_title);

        mQuickBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e(TAG, "TestResult activity is finish");
                SystemProperties.set("persist.radio.dispatchAllKey", "false");
                Log.e(TAG, "persist.radio.dispatchAllKey = "
                        + SystemProperties.get("persist.radio.dispatchAllKey", "false"));
                Intent it = new Intent(TestResult.this, GnMMITest.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                TestResult.this.startActivity(it);
                TestResult.this.finish();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mSNEditor.clear();
        mSNEditor.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
    }

    public void onQcRilHookReady() {
        mCount = 0;
        mResultSP = getSharedPreferences("gn_mmi_test", Context.MODE_WORLD_WRITEABLE);
        mSNResultSP = getSharedPreferences("gn_mmi_sn", Context.MODE_WORLD_WRITEABLE);
        mSNEditor = mSNResultSP.edit();
        // Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin
        if (TestUtils.mIsAutoMode_2) {
            mResultList = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.auto_test_items_2)));
        } else {
            mResultList = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.auto_test_items)));
        }
        if (TestUtils.mIsAutoMode_3) {
            mResultList = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.auto_test_items_3)));
        } else {
            mResultList = new ArrayList(Arrays.asList(getResources().getStringArray(R.array.auto_test_items)));
        }
        // Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end

        StringBuilder sb = new StringBuilder();
        int value = 1;
        sb.append(this.getResources().getString(R.string.result_from_test) + "\n");
        for (int i = 0; i < mResultList.size(); i++) {
            value = mResultSP.getInt(mResultList.get(i), 1);
            if (0 == value) {
                mCount++;
                sb.append(mCount + ":   " + mResultList.get(i) + "\n");
                Log.e(TAG, "mCount``" + mCount + ":   " + mResultList.get(i) + "\n");
            }
        }

        mContentTv.setText(sb.toString());
        /*******************/
        String factoryResult = "";
        try {
            Log.e(TAG, "TestUtils.mIsAutoMode = " + TestUtils.mIsAutoMode);
            if (TestUtils.mIsAutoMode) {
                String oFS = nvItems.getFactoryResult();
                Log.e(TAG, "oFS= " + oFS);
                String nFS = getNewFactorySet(oFS);
                Log.e(TAG, "nFS= " + nFS);
                nvItems.setFactoryResult(nFS + "0");

            }
            factoryResult = nvItems.getFactoryResult();
            Log.d(TAG, "factoryResult = " + factoryResult + " : " + factoryResult.length());
            String factoryResult11 = nvItems.getFactoryResult();
            Log.d(TAG, "factoryResul1 = " + factoryResult11 + " : " + factoryResult11.length());
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder fnLog = new StringBuilder("\n");
        char[] barcodes = factoryResult.toCharArray();
        if (factoryResult.length() == 32 || factoryResult.length() == 64) {
            Log.e(TAG, "barcodes[12] = " + barcodes[12]);
            Log.e(TAG, "barcodes[14] = " + barcodes[14]);
            Log.e(TAG, "barcodes[15] = " + barcodes[15]);
            Log.e(TAG, "barcodes[18] = " + barcodes[18]);

            if ('F' == barcodes[12]) {
                // if ('F' == factoryResult.charAt(12)){
                mTitleTv.setText(R.string.mmitest_fail);
                Log.e(TAG, "FFFFFFFFFF");
            }
            // else if ('P' == factoryResult.charAt(12)){
            else if ('P' == barcodes[12]) {
                mTitleTv.setText(R.string.mmitest_success);
                Log.e(TAG, "PPPPPPPPPPP");
            } else {
                mTitleTv.setText(R.string.no_mmitest);
                Log.e(TAG, "nnnnnnnnnnnnnn");
            }
            if ('P' == barcodes[14]) {
                fnLog.append(mRes.getString(R.string.gps) + ": " + mRes.getString(R.string.right) + "\n");
            }
            if ('F' == barcodes[14]) {
                fnLog.append(mRes.getString(R.string.gps) + ": " + mRes.getString(R.string.wrong) + "\n");
            }
            if ('P' == barcodes[15]) {
                fnLog.append(mRes.getString(R.string.wifi) + ": " + mRes.getString(R.string.right) + "\n");
            }
            if ('F' == barcodes[15]) {
                fnLog.append(mRes.getString(R.string.wifi) + ": " + mRes.getString(R.string.wrong) + "\n");
            }
            if ('P' == barcodes[18]) {
                fnLog.append(mRes.getString(R.string.bluetooth) + ": " + mRes.getString(R.string.right) + "\n");
            }
            if ('F' == barcodes[18]) {
                fnLog.append(mRes.getString(R.string.bluetooth) + ": " + mRes.getString(R.string.wrong) + "\n");
            }
            //Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 begin
            if (FeatureOption.GN_RW_GN_MMI_WCHAT_SOTER_SUPPORT) {
                if (barcodes[MMI_WCHAT_SOTER_TAG] == MMI_PASS) {
                    fnLog.append(mRes.getString(R.string.wchat_soter) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    fnLog.append(mRes.getString(R.string.wchat_soter) + ": " + mRes.getString(R.string.wrong) + "\n");
                }

            }
            if (FeatureOption.GN_RW_GN_MMI_IFAA_KEY_SUPPORT) {
                if (barcodes[MMI_IFAA_KEY_TAG] == MMI_PASS) {
                    fnLog.append(mRes.getString(R.string.ifaa_key) + ": " + mRes.getString(R.string.right) + "\n");
                } else {
                    fnLog.append(mRes.getString(R.string.ifaa_key) + ": " + mRes.getString(R.string.wrong) + "\n");
                }
            }
            //Gionee <GN_BSP_MMI><zhangke><20161106> add for ID19681 end

            /*
             * for (String key : TestUtils.factoryFlag.keySet()) { String
             * testResult = ""; int loc =
             * Integer.parseInt(TestUtils.factoryFlag.get(key)); if
             * (factoryResult.charAt(loc) == 'P') { testResult =
             * mRes.getString(R.string.right); } else if
             * (factoryResult.charAt(loc) == 'F') { testResult =
             * mRes.getString(R.string.wrong); } else { continue; }
             * fnLog.append(key + ": " + testResult + "\n"); Log.e(TAG,
             * " key = " + key + "testResult = " + testResult); }
             */
        }
        // Log.e(TAG, "112"+ fnLog.toString());
        mSNTv.setText(fnLog);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private String getNewFactorySet(String old) {
        StringBuilder sb = new StringBuilder(old);

        char mmi_result = (mCount == 0) ? 'P' : 'F';
        Log.e(TAG, " mmi_result = " + mmi_result);
        if (mmi_result != old.charAt(12)) {
            sb.setCharAt(12, mmi_result);
            Log.e(TAG, " sb    = " + sb.toString());
        }

        for (String key : TestUtils.factoryFlag.keySet()) {
            String loc = TestUtils.factoryFlag.get(key);
            char nV = mSNResultSP.getString(loc, "F").charAt(0);
            char oV = old.charAt(Integer.parseInt(loc));
            if (nV != oV) {
                sb.setCharAt(Integer.parseInt(loc), nV);
                Log.e(TAG, " sb111 = " + sb.toString());
            }
        }

        return sb.toString();
    }

    //Gionee <GN_BSP_MMI><zhangke><20161109> add for ID21183 begin
    @Override
    public void onDestroy() {
    	nvItems.dispose();
    	super.onDestroy();
    }
    //Gionee <GN_BSP_MMI><zhangke><20161109> add for ID21183 end

}
