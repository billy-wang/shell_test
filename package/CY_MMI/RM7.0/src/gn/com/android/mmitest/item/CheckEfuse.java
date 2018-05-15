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
import android.view.KeyEvent;
import gn.com.android.mmitest.GnMMITest;

public class CheckEfuse extends Activity implements OnClickListener {
    public static final String TAG = "CheckEfuse";
    private boolean mIsPass = false;
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
        TestUtils.setWindowFlags(this);
        setContentView(R.layout.check_efuse);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mLockPatternView = (LockPatternView) findViewById(R.id.lock_view);
        mLockPatternUtils = new LockPatternUtils(this);
        mLockPatternView.setOnPatternListener(mOnPatternListener);
        mTitleTv.setText(R.string.check_efuse_password);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();

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
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private OnPatternListener mOnPatternListener = new OnPatternListener() {

        public void onPatternStart() {

        }

        public void onPatternDetected(List<Cell> pattern) {

            int result = mLockPatternUtils.checkEfusePattern(pattern);
			if (result == 1){
                  Intent intent = new Intent();
				  intent.setClass(CheckEfuse.this,GnMMITest.class);				  
				  intent.putExtra("result","1");
                  setResult(1, intent);
				  finish();
            } else if (result == 2){
                  Intent intent = new Intent();
				  intent.setClass(CheckEfuse.this,GnMMITest.class);				  
				  intent.putExtra("result","2");
                  setResult(2, intent);
				  finish();               
			}else if (result != 1) {
                if (result == 0) {
                    mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                    mLockPatternView.clearPattern();
                    if (mCount == 0) {
						Intent intent = new Intent();
						intent.setClass(CheckEfuse.this,GnMMITest.class);				
						intent.putExtra("result","3");
						setResult(3, intent);
						finish();
                    } else {
                        if (mCount == 3) {
                            Toast.makeText(CheckEfuse.this, getString(R.string.lock_pattern_view_toast1),
                                    Toast.LENGTH_SHORT).show();
                        } else if (mCount == 2) {
                            Toast.makeText(CheckEfuse.this, getString(R.string.lock_pattern_view_toast2),
                                    Toast.LENGTH_SHORT).show();
                        } else if (mCount == 1) {
                            Toast.makeText(CheckEfuse.this, getString(R.string.lock_pattern_view_toast3),
                                    Toast.LENGTH_SHORT).show();
                        }

                        mTitleTv.setText(String.format(getString(R.string.lock_pattern_view_note), --mCount));
                    }
                }
            }

        }

        public void onPatternCleared() {

        }

        public void onPatternCellAdded(List<Cell> pattern) {

        }
    };

    //Gionee <GN_BSP_MMI> <lifeilong> <20170406> modify for ID 105848 begin
    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        return true;
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170406> modify for ID 105848 end

}
