
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;
import java.io.BufferedReader;
import java.io.FileReader;

public class OTGTest extends Activity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    //Gionee <GN_BSP_MMI><lifeilong><20161208> modify for ID 41502 begin
    private static final String OTG_NODE_NAME = "NODE_TYPE_OTG_CHARGE_SWITCH";
    private static final int OTG_OPTION_OPEN = 1;
    private static final int OTG_OPTION_CLOSE = 0;
    //Gionee <GN_BSP_MMI><lifeilong><20161208> modify for ID 41502 end
    private static final String TAG = "OTGTest";

    TextView promt;
    private RelativeLayout mParent;
    private boolean mIsPressure;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;
    Handler mHandler = new Handler();

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
        TestUtils.setWindowFlags(this);
        setContentView(R.layout.otg_test);
        promt = (TextView) findViewById(R.id.promt);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mRightBtn.setEnabled(false);
        //Gionee <GN_BSP_MMI><lifeilong><20161208> modify for ID 41502 begin
        TestUtils.writeNodeState(OTGTest.this,OTG_NODE_NAME,OTG_OPTION_OPEN);
        Log.e(TAG,"NODE_TYPE_OTG_CHARGE_SWITCH --- > 1");
        //Gionee <GN_BSP_MMI><lifeilong><20161208> modify for ID 41502 end
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 3000);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mRunnable);

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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
