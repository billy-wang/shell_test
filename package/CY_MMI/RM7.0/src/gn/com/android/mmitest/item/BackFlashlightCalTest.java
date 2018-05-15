package gn.com.android.mmitest.item;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.app.Activity;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.content.SharedPreferences;
import gn.com.android.mmitest.item.lockpatternview.LockPatternView;
import gn.com.android.mmitest.item.lockpatternview.LockPatternUtils;
import gn.com.android.mmitest.item.lockpatternview.LockPatternView.Cell;
import gn.com.android.mmitest.item.lockpatternview.LockPatternView.DisplayMode;
import gn.com.android.mmitest.item.lockpatternview.LockPatternView.OnPatternListener;
import java.util.List;
//Gionee <GN_BSP_MMI> <lifeilong> <20170518> modify for ID 143819 bgein
import android.view.KeyEvent;
//Gionee <GN_BSP_MMI> <lifeilong> <20170518> modify for ID 143819 end

public class BackFlashlightCalTest extends Activity implements OnClickListener {
    public static final String TAG = "BackFlashlightCalTest";
    private static final String START_CAMERA_ACTIVITY_BROADCAST = "autommi.engineermode.camera.start";
    private static final String END_CAMERA_ACTIVITY_BROADCAST = "autommi.engineermode.camera.end";
    private static final String GET_FLASHLIGHT_CAL_RESULT_BROADCAST = "autommi.back.flashlight.cal.result";
    private static final String TEST_IS_PASS = "isPass";
    private boolean mIsPass = false;
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private LockPatternView mLockPatternView;
    private LockPatternUtils mLockPatternUtils;
    private TextView mTitleTv;
    private int mCount = 3;
    private Button mBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");

        IntentFilter filter = new IntentFilter();
        filter.addAction(GET_FLASHLIGHT_CAL_RESULT_BROADCAST);
        registerReceiver(mReceiver, filter);
        setContentView(R.layout.back_flash_cal);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mLockPatternView = (LockPatternView) findViewById(R.id.lock_view);
        mLockPatternUtils = new LockPatternUtils(this);
        mLockPatternView.setOnPatternListener(mOnPatternListener);
        mTitleTv.setText(String.format(getString(R.string.lock_pattern_view_note), mCount));
        mBackBtn = (Button) findViewById(R.id.btn_back);
        mBackBtn.setOnClickListener(this);

        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        TestUtils.setWindowFlags(this);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Log.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.e(TAG, "onDestroy");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "mReceiver:action=" + intent.getAction());
            if (GET_FLASHLIGHT_CAL_RESULT_BROADCAST.equals(intent.getAction())) {
                boolean isPass = intent.getBooleanExtra(TEST_IS_PASS, false);
                Log.e(TAG, "isPass=" + isPass);
                mRightBtn.setEnabled(isPass);
                mWrongBtn.setEnabled(!isPass);
            }
        }
    };

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

        case R.id.right_btn: {
            if (TestUtils.mIsAutoMode) {
                SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                editor.putString("27", "P");
                editor.commit();
            }

            mRightBtn.setEnabled(false);
            mWrongBtn.setEnabled(false);
            mRestartBtn.setEnabled(false);
            TestUtils.rightPress(TAG, this);
            break;
        }
        case R.id.btn_back:
        case R.id.wrong_btn: {
            if (TestUtils.mIsAutoMode) {
                SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                editor.putString("27", "F");
                editor.commit();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private OnPatternListener mOnPatternListener = new OnPatternListener() {

        public void onPatternStart() {

        }

        public void onPatternDetected(List<Cell> pattern) {

            int result = mLockPatternUtils.checkPattern(pattern);
            if (result != 1) {
                if (result == 0) {
                    mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                    mLockPatternView.clearPattern();
                    if (mCount == 0) {
                        Toast.makeText(BackFlashlightCalTest.this, getString(R.string.lock_pattern_view_toast4),
                                Toast.LENGTH_SHORT).show();
                        TestUtils.wrongPress(TAG, BackFlashlightCalTest.this);
                    } else {
                        if (mCount == 3) {
                            Toast.makeText(BackFlashlightCalTest.this, getString(R.string.lock_pattern_view_toast1),
                                    Toast.LENGTH_SHORT).show();
                        } else if (mCount == 2) {
                            Toast.makeText(BackFlashlightCalTest.this, getString(R.string.lock_pattern_view_toast2),
                                    Toast.LENGTH_SHORT).show();
                        } else if (mCount == 1) {
                            Toast.makeText(BackFlashlightCalTest.this, getString(R.string.lock_pattern_view_toast3),
                                    Toast.LENGTH_SHORT).show();
                        }

                        mTitleTv.setText(String.format(getString(R.string.lock_pattern_view_note), --mCount));
                    }
                } else {
                    mLockPatternView.clearPattern();

                }

            } else {
                mLockPatternView.setVisibility(View.GONE);
                mBackBtn.setVisibility(View.GONE);
                mRightBtn.setOnClickListener(BackFlashlightCalTest.this);
                mWrongBtn.setOnClickListener(BackFlashlightCalTest.this);
                mRestartBtn.setOnClickListener(BackFlashlightCalTest.this);

                mRightBtn.setVisibility(View.VISIBLE);
                mWrongBtn.setVisibility(View.VISIBLE);
                mRestartBtn.setVisibility(View.VISIBLE);
                mRightBtn.setEnabled(false);
                mTitleTv.setText(getString(R.string.test_title));
                Intent intent = new Intent(START_CAMERA_ACTIVITY_BROADCAST);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivityForResult(intent, 0);

            }

        }

        public void onPatternCleared() {

        }

        public void onPatternCellAdded(List<Cell> pattern) {

        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170518> modify for ID 143819 bgein
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170518> modify for ID 143819 end
}
