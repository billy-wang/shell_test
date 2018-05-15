
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Method;


public class NFCTest extends BaseActivity implements OnClickListener {

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "NFCTest";

    TextView promt;
    PendingIntent pendingIntent;
    private NfcAdapter mNfcAdapter;
    IntentFilter[] intentFiltersArray;
    String[][] techListsArray;
    //Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    //Gionee zhangke 20160428 modify for CR01687958 end


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.nfc_test);
        promt = (TextView) findViewById(R.id.promt);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Log.i(TAG, "mNfcAdapter == null");
            //Gionee zhangke 20151014 add for CR01568192 start
            TestUtils.wrongPress(TAG, this);
            return;
            //Gionee zhangke 20151014 add for CR01568192 end
        } else {
            Log.i(TAG, "mNfcAdapter != null");
        }
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        if (!mNfcAdapter.isEnabled()) {
            Log.i(TAG, "mNfcAdapter != isEnable");
            mNfcAdapter.enable();
        } else {
            Log.i(TAG, "mNfcAdapter = isEnable");
        }
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
                mIsTimeOver = true;
                if(mIsPass){
                    mRightBtn.setEnabled(true);
                }

                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(NFCTest.this);
                mWrongBtn.setOnClickListener(NFCTest.this);
                mRestartBtn.setOnClickListener(NFCTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef,};
        techListsArray = new String[][]{new String[]{NfcA.class.getName()},
                new String[]{NfcB.class.getName()},
                new String[]{NfcF.class.getName()},
                new String[]{NfcV.class.getName()},
                new String[]{Ndef.class.getName()},
                new String[]{NdefFormatable.class.getName()},
                new String[]{MifareClassic.class.getName()},
                new String[]{MifareUltralight.class.getName()},
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);

    }


    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                //mNfcAdapter.disable();
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                //mNfcAdapter.disable();
                break;
            }

            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                //Gionee zhangke 20160504 modify for CR01690699 start
                try{
                    Class<?> nfcAdapter = (Class<?>) Class.forName("android.nfc.NfcAdapter");
                    Method disableTagReaderMode = nfcAdapter.getMethod("disableTagReaderMode");
                    Method enableTagReaderMode = nfcAdapter.getMethod("enableTagReaderMode");
                    disableTagReaderMode.invoke(mNfcAdapter);
                    Thread.sleep(500);
                    enableTagReaderMode.invoke(mNfcAdapter);
                }catch(Exception e){
                    e.printStackTrace();
                }
                //Gionee zhangke 20160504 modify for CR01690699 start
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }

    public void onNewIntent(Intent intent) {
        Log.i(TAG, "intent.getAction()=" + intent.getAction());
        if (intent.getAction().equals("android.nfc.action.TECH_DISCOVERED") || intent.getAction().equals("android.nfc.action.NDEF_DISCOVERED") || intent.getAction().equals("android.nfc.action.TAG_DISCOVERED")) {
            promt.setText(R.string.test_right_nfc);
            //Gionee zhangke 20160428 modify for CR01687958 start
            mIsPass = true;
            if(mIsTimeOver){
                mRightBtn.setEnabled(true);
            }
            //Gionee zhangke 20160428 modify for CR01687958 end

        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
