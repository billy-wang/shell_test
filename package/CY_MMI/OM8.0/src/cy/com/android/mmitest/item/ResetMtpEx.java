package cy.com.android.mmitest.item;
import android.media.AudioManager;
import cy.com.android.mmitest.BaseActivity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import cy.com.android.mmitest.utils.DswLog;
import android.widget.Button;
import android.os.Handler;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.KeyEvent;


public class ResetMtpEx extends BaseActivity implements OnClickListener{
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private TextView mTitleTv, mContentTv;
    private static final String TAG = "ResetMtpEx";
    private Handler handler = new Handler();
    private AudioManager audioManager;

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

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);
                mRightBtn.setOnClickListener(ResetMtpEx.this);
                mWrongBtn.setOnClickListener(ResetMtpEx.this);
                mRestartBtn.setOnClickListener(ResetMtpEx.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);


        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
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
    public void onStart() {
        super.onStart();

        Thread thread = new Thread(new Runnable() {
            public void run() {

                handler.postDelayed(new Runnable() {
                    public void run() {

                        audioManager.setParameters("resetMtpEx=true");
                        mRightBtn.setEnabled(true);
                    }
                },3000);
            }
        });
        thread.start();
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
