
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import java.io.BufferedReader;
import java.io.FileReader;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cy.com.android.mmitest.utils.NodeNameUtil;
import cy.com.android.mmitest.item.FeatureOption;

public class OTGTest extends BaseActivity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "OTGTest";

    // private OtgPluginReceiver mOtgPluginReceiver;

    TextView promt;
    private RelativeLayout mParent;
    private boolean mIsPressure;
    PendingIntent pendingIntent;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;

    Handler mHandler = new Handler();
    private boolean otgswitch = false;
    //Gionee <GN_BSP_MMI> <chengq> <20170412> add for ID 111730 end
    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            BufferedReader bufferReader = null;
            String mFileName = "proc/mounts";
            String line = null;
            // Gionee zhangke 20151214 add for CR01598553 start
            DswLog.i(TAG, "mRunnable run ");
            // Gionee zhangke 20151214 add for CR01598553 end

            try {
                bufferReader = new BufferedReader(new FileReader(mFileName));
                line = bufferReader.readLine();
                while (line != null) {
                    // Gionee zhangke 20151214 add for CR01598553 start
                    // if(line.contains("/mnt/media_rw/usbotg")){
                    DswLog.i(TAG, "line=" + line);
                    if (line.contains("/dev/block/vold/public:8")) {
                        // Gionee zhangke 20151214 add for CR01598553 end
                        DswLog.e(TAG, " otg test success ");
                        promt.setText(R.string.test_right_otg);
                        mRightBtn.setEnabled(true);
                        return;
                    }
                    line = bufferReader.readLine();
                }
                // promt.setText(R.string.otg_test_failure);
                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, 500);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开OTG @" + Integer.toHexString(hashCode()));
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.otg_test);
        promt = (TextView) findViewById(R.id.promt);

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(OTGTest.this);
                mWrongBtn.setOnClickListener(OTGTest.this);
                mRestartBtn.setOnClickListener(OTGTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
        // mOtgPluginReceiver = new OtgPluginReceiver();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (FeatureOption.GN_RW_GN_MMI_OTG_SUPPORT)
            init_OTGTest();
        /*
         * if (mOtgPluginReceiver != null) { IntentFilter intentFilter = new
         * IntentFilter(); intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
         * intentFilter.addDataScheme("file");
         * registerReceiver(mOtgPluginReceiver, intentFilter); }
         */

        mHandler.removeCallbacks(mRunnable);
        // Gionee zhangke 20151214 add for CR01598553 start
        DswLog.i(TAG, "onResume ");
        // Gionee zhangke 20151214 add for CR01598553 end
        mHandler.postDelayed(mRunnable, 3000);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (FeatureOption.GN_RW_GN_MMI_OTG_SUPPORT) {
            switchSkyLlight(otgswitch);
        }
        /*
         * if (null != mOtgPluginReceiver) {
         * unregisterReceiver(mOtgPluginReceiver); }
         */

    }

    @Override
    public void onStop() {
        super.onStop();
        // Gionee zhangke 20151214 add for CR01598553 start
        mHandler.removeCallbacks(mRunnable);
        // Gionee zhangke 20151214 add for CR01598553 end
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                // mNfcAdapter.disable();
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                // mNfcAdapter.disable();
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

    /*
     * public void onNewIntent(Intent intent) {
     * DswLog.i("aaaa","intent.getAction()="+intent.getAction()); Tag tagFromIntent
     * = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
     * if(intent.getAction().equals("android.nfc.action.TECH_DISCOVERED")||
     * intent.getAction().equals("android.nfc.action.NDEF_DISCOVERED")||intent.
     * getAction().equals("android.nfc.action.TAG_DISCOVERED")) {
     * promt.setText(R.string.test_right_nfc); mRightBtn.setEnabled(true);
     * 
     * } }
     */
    /*
     * private class OtgPluginReceiver extends BroadcastReceiver {
     * 
     * @Override public void onReceive(Context context, Intent intent) { if
     * (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) { DswLog.d(TAG,
     * "receiver --> ACTION_MEDIA_MOUNTED");
     * promt.setText(R.string.test_right_otg); mRightBtn.setEnabled(true);
     * 
     * } }
     * 
     * }
     */

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出OTG @" + Integer.toHexString(hashCode()));
    }

    private void init_OTGTest() {
        statusSkyLight();
        switchSkyLlight(true);
    }

    //Gionee <GN_BSP_MMI> <chengq> <20170512> modify for ID 138674 end
    private void statusSkyLight() {
        otgswitch = TestUtils.getNodeState(this,NodeNameUtil.OTG_NODE_NAME) == NodeNameUtil.ACTION_TURN_ON;
    }

    private void switchSkyLlight(boolean enable) {

        DswLog.d(TAG, "befor otgswitch nodeType="+TestUtils.getNodeState(this,NodeNameUtil.OTG_NODE_NAME));
        TestUtils.writeNodeState(this, NodeNameUtil.OTG_NODE_NAME,enable? NodeNameUtil.ACTION_TURN_ON:NodeNameUtil.ACTION_TURN_OFF);
        DswLog.d(TAG, "after otgswitch nodeType="+TestUtils.getNodeState(this,NodeNameUtil.OTG_NODE_NAME));
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170512> modify for ID 138674 end
}
