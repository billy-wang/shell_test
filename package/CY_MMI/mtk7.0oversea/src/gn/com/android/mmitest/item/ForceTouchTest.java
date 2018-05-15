
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;
//Gionee zhangke 20151218 modify for CR01611622 start
import android.os.IBinder;
import android.os.ServiceManager;

import gn.com.android.mmitest.NvRAMAgent;

import android.os.RemoteException;

import gn.com.android.mmitest.TestResult;
//Gionee zhangke 20151218 modify for CR01611622 end
//Gionee zhangke 20160115 add for CR01624124 start
import gn.com.android.mmitest.item.lockpatternview.LockPatternView;
import gn.com.android.mmitest.item.lockpatternview.LockPatternUtils;
import gn.com.android.mmitest.item.lockpatternview.LockPatternView.Cell;
import gn.com.android.mmitest.item.lockpatternview.LockPatternView.DisplayMode;
import gn.com.android.mmitest.item.lockpatternview.LockPatternView.OnPatternListener;

import android.widget.Toast;

import java.util.List;

import android.os.Vibrator;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

import java.io.IOException;

//Gionee zhangke 20160115 add for CR01624124 end

public class ForceTouchTest extends BaseActivity implements OnClickListener {
    private TextView mTitleTv;
    private TextView mContentTv;
    private Button mRightBtn;
    private Button mWrongBtn;
    private Button mRestartBtn;
    private static final String TAG = "ForceTouchTest";
    private Resources mRs;
    private static final int REQUEST_CODE = 0;
    //Gionee zhangke 20160115 add for CR01624124 start
    private boolean mIsAutoMode = false;
    private LockPatternView mLockPatternView;
    private LockPatternUtils mLockPatternUtils;
    private int mCount = 3;
    private boolean opFLag = true;
    private Button mBackBtn;
    //Gionee zhangke 20160115 add for CR01624124 end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        // Gionee xiaolin 20120924 add for CR00693542 start
        TestUtils.checkToContinue(this);
        // Gionee xiaolin 20120924 add for CR00693542 end
        Log.e(TAG, "setWindowFlags");
        mRs = getResources();
        setContentView(R.layout.force_touch_pre);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        //Gionee zhangke 20160115 add for CR01624124 start
        mBackBtn = (Button) findViewById(R.id.btn_back);
        mBackBtn.setOnClickListener(this);
        mLockPatternView = (LockPatternView) findViewById(R.id.lock_view);
        mLockPatternUtils = new LockPatternUtils(this);
        mLockPatternView.setOnPatternListener(mOnPatternListener);
        mIsAutoMode = TestUtils.mIsAutoMode_2 || TestUtils.mIsAutoMode;
        Log.e(TAG, "mIsAutoMode:" + mIsAutoMode);
        if (mIsAutoMode) {
            mTitleTv.setText(String.format(getString(R.string.lock_pattern_view_note), mCount));
            mRightBtn.setVisibility(View.GONE);
            mWrongBtn.setVisibility(View.GONE);
            mRestartBtn.setVisibility(View.GONE);
        } else {
            mLockPatternView.setVisibility(View.GONE);
            mBackBtn.setVisibility(View.GONE);
            Intent intent = new Intent(ForceTouchTest.this, com.hideep.zcalib.standalone.activity.ViewActivity.class);
            intent.putExtra("isAutoMode", mIsAutoMode);
            Log.e(TAG, "startActivityForResult:");
            startActivityForResult(intent, REQUEST_CODE);

            mRightBtn.setVisibility(View.VISIBLE);
            mWrongBtn.setVisibility(View.VISIBLE);
            mRestartBtn.setVisibility(View.VISIBLE);
            mTitleTv.setText(getString(R.string.test_title));
        }
        //Gionee zhangke 20160115 add for CR01624124 end

    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode_2) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("33", "P");
                    editor.commit();
                }

                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                if (TestUtils.mIsAutoMode_2) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("33", "F");
                    editor.commit();
                }

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
            //Gionee zhangke 20160115 add for CR01624124 start
            case R.id.btn_back: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }
            //Gionee zhangke 20160115 add for CR01624124 end

        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (resultCode) {
            case RESULT_OK:
                boolean isPass = intent.getBooleanExtra("isPass", false);
                Log.i(TAG, "3d touch isPass=" + isPass);
                if (isPass) {
                    mRightBtn.setEnabled(true);
                }
                break;
            default:
                break;
        }
    }

    // Gionee zhangke 20151218 modify for CR01611622 start
    private boolean is3DTouchPass() {
        byte[] productInfoBuff = getProductInfo();
        if (productInfoBuff != null && productInfoBuff.length > 32) {
            Log.i(TAG, "isFactoryResetBoot:productInfoBuff[32]=" + productInfoBuff[32]);
            return 'P' == productInfoBuff[32];
        }
        return false;
    }

    private byte[] getProductInfo() {
        IBinder binder = null;
        byte[] productInfoBuff = null;
        byte[] temp = new byte[64];

        binder = ServiceManager.getService("NvRAMAgent");
        if (null == binder) {
            Log.e(TAG, "getService	NvRAMAgent binder is null");
        }
        if (null != binder) {
            NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
            try {
                Log.i(TAG, "getProductInfo NvRAMAgent read");
                productInfoBuff = agent.readFileByName(TestResult.PRODUCT_INO_NAME);

            } catch (RemoteException ex) {
                Log.e(TAG, ex.toString());
            }
        }
        return productInfoBuff;
    }
    // Gionee zhangke 20151218 modify for CR01611622 end

    //Gionee zhangke 20160115 add for CR01624124 start
    private OnPatternListener mOnPatternListener = new OnPatternListener() {

        public void onPatternStart() {

        }

        public void onPatternDetected(List<Cell> pattern) {
            if (opFLag) {
                int result = mLockPatternUtils.checkPattern(pattern);
                if (result != 1) {
                    if (result == 0) {
                        mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                        mLockPatternView.clearPattern();
                        if (mCount == 0) {
                            Toast.makeText(ForceTouchTest.this, getString(R.string.lock_pattern_view_toast4), Toast.LENGTH_SHORT).show();
                            TestUtils.wrongPress(TAG, ForceTouchTest.this);
                        } else {
                            if (mCount == 3) {
                                Toast.makeText(ForceTouchTest.this, getString(R.string.lock_pattern_view_toast1), Toast.LENGTH_SHORT).show();
                            } else if (mCount == 2) {
                                Toast.makeText(ForceTouchTest.this, getString(R.string.lock_pattern_view_toast2), Toast.LENGTH_SHORT).show();
                            } else if (mCount == 1) {
                                Toast.makeText(ForceTouchTest.this, getString(R.string.lock_pattern_view_toast3), Toast.LENGTH_SHORT).show();
                            }

                            mTitleTv.setText(String.format(getString(R.string.lock_pattern_view_note), --mCount));
                        }
                    } else {
                        mLockPatternView.clearPattern();
                        Toast.makeText(ForceTouchTest.this, "no old pattern to compare", Toast.LENGTH_LONG).show();
                    }

                } else {
                    //Gionee zhangke 20151218 modify for CR01611622 start
                    mLockPatternView.setVisibility(View.GONE);
                    mBackBtn.setVisibility(View.GONE);
                    //is3DTouchPass();
                    Intent intent = new Intent(ForceTouchTest.this, com.hideep.zcalib.standalone.activity.ViewActivity.class);
                    intent.putExtra("isAutoMode", mIsAutoMode);
                    startActivityForResult(intent, REQUEST_CODE);
                    mRightBtn.setVisibility(View.VISIBLE);
                    mWrongBtn.setVisibility(View.VISIBLE);
                    mRestartBtn.setVisibility(View.VISIBLE);
                    mTitleTv.setText(getString(R.string.test_title));
                    //Gionee zhangke 20151218 modify for CR01611622 end

                }
            } else {
                mLockPatternUtils.saveLockPattern(pattern);
                Toast.makeText(ForceTouchTest.this, "save", Toast.LENGTH_LONG).show();
                mLockPatternView.clearPattern();
                opFLag = true;
            }

        }

        public void onPatternCleared() {

        }

        public void onPatternCellAdded(List<Cell> pattern) {

        }
    };
    //Gionee zhangke 20160115 add for CR01624124 end
}
