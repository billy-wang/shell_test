package gn.com.android.mmitest.item;
import android.media.AudioManager;
import gn.com.android.mmitest.BaseActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import gn.com.android.mmitest.utils.DswLog;
import android.widget.Button;
import android.os.Handler;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.KeyEvent;


public class ResetMtpEx extends BaseActivity implements OnClickListener{
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private TextView mTitleTv, mContentTv;
    private static final String TAG = "ResetMtpEx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\nResetMtpEx onCreate打开喇叭校准 @" + Integer.toHexString(hashCode()));
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.common_textview);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mContentTv = (TextView) findViewById(R.id.test_content);
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
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);
                mRightBtn.setEnabled(true);
                mRightBtn.setOnClickListener(ResetMtpEx.this);
                mWrongBtn.setOnClickListener(ResetMtpEx.this);
                mRestartBtn.setOnClickListener(ResetMtpEx.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);

        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        am.setParameters("resetMtpEx=true");

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\nResetMtpEx onDestroy退出喇叭校准 @" + Integer.toHexString(hashCode()));
    }

}
