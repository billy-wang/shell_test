
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.io.BufferedReader;
import java.io.FileReader;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class OTGTest extends Activity implements OnClickListener {

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
    //Gionee <GN_BSP_MMI> <lifeilong> <20170314> add for ID 83954 begin
    private static final String OTG_NODE_NAME = "NODE_TYPE_OTG_CHARGE_SWITCH";///sys/devices/soc/soc:otg_switch/otg_mode
    private static final int OTG_OPTION_OPEN = 1;
    private static final int OTG_OPTION_CLOSE = 0;
    private boolean mOtgClosed = false;
    //Gionee <GN_BSP_MMI> <lifeilong> <20170314> add for ID 83954 end
    Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            BufferedReader bufferReader = null;
            String mFileName = "proc/mounts";
            String line = null;
            // Gionee zhangke 20151214 add for CR01598553 start
            Log.i(TAG, "mRunnable run ");
            // Gionee zhangke 20151214 add for CR01598553 end

            try {
                bufferReader = new BufferedReader(new FileReader(mFileName));
                line = bufferReader.readLine();
                while (line != null) {
                    // Gionee zhangke 20151214 add for CR01598553 start
                    // if(line.contains("/mnt/media_rw/usbotg")){
                    Log.i(TAG, "line=" + line);
                    if (line.contains("/dev/block/vold/public:8")) {
                        // Gionee zhangke 20151214 add for CR01598553 end
                        Log.e(TAG, " otg test success ");
                        promt.setText(R.string.test_right_otg);
                        mRightBtn.setEnabled(true);
                        mRightBtn.setVisibility(View.VISIBLE);
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
        //Gionee zhangke 20151215 modify for CR01609753 start
        /*
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        //lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);
        View view = getWindow().getDecorView();
        int visFlags = View.STATUS_BAR_DISABLE_BACK
                | View.STATUS_BAR_DISABLE_HOME
                | View.STATUS_BAR_DISABLE_RECENT
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        view.setSystemUiVisibility(visFlags);
        */
        TestUtils.setWindowFlags(this);
        //Gionee zhangke 20151215 modify for CR01609753 end

        setContentView(R.layout.otg_test);
        promt = (TextView) findViewById(R.id.promt);

        //Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setVisibility(View.INVISIBLE);
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
        /*
         * if (mOtgPluginReceiver != null) { IntentFilter intentFilter = new
         * IntentFilter(); intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
         * intentFilter.addDataScheme("file");
         * registerReceiver(mOtgPluginReceiver, intentFilter); }
         */

        mHandler.removeCallbacks(mRunnable);
        // Gionee zhangke 20151214 add for CR01598553 start
        Log.i(TAG, "onResume ");
        // Gionee zhangke 20151214 add for CR01598553 end
        mHandler.postDelayed(mRunnable, 3000);
        ////Gionee <GN_BSP_MMI> <lifeilong> <20170314> add for ID 83954 begin
        int nodeValue = TestUtils.getNodeState(this, OTG_NODE_NAME);
        Log.i(TAG, "onResume nodeValue="+nodeValue);
		
        if(nodeValue == OTG_OPTION_CLOSE){
            mOtgClosed = true;
            TestUtils.writeNodeState(this, OTG_NODE_NAME, OTG_OPTION_OPEN);
        }
       
        ////Gionee <GN_BSP_MMI> <lifeilong> <20170314> add for ID 83954 begin

    }

    @Override
    public void onPause() {
        super.onPause();
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
     * Log.i("aaaa","intent.getAction()="+intent.getAction()); Tag tagFromIntent
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
     * (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) { Log.d(TAG,
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
}
