
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import cy.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.SharedPreferences;



import android.view.View.OnClickListener;

public class FMTest extends BaseActivity implements OnClickListener {

    private AudioManager mAM;

    private String TAG = "FMTest_billy";
    private Button mRightBtn, mWrongBtn, mRestartBtn,mToggleBtn;
    private TextView mTitleTv;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开FM @" + Integer.toHexString(hashCode()));
        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.flash_light);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setEnabled(false);
        mRightBtn.setOnClickListener(this);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mToggleBtn = (Button) findViewById(R.id.toggle_button);
        mToggleBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn.setOnClickListener(this);
        mTitleTv = (TextView) findViewById(R.id.flash_test_title);
        mTitleTv.setText(R.string.test_title);

        mToggleBtn.setVisibility(View.GONE);
        IntentFilter filter = new IntentFilter("chenyee.intent.action.END_FROM_FMRADIO");
        registerReceiver(FMTestBroadcastReceiver, filter);

        ComponentName name = new ComponentName("com.android.fmradio"
                ,"com.android.fmradio.FmMainActivity");
        Intent intent = new Intent();
        intent.putExtra("isFromMMI",true);
        //intent.putExtra("defaultHZ",875);
        intent.putExtra("defaultHZ",918);
        intent.setComponent(name);
        startActivity(intent);

        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int maxVol = mAM.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int muscVol = mAM.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (muscVol==0){
            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol/2, 0);
            DswLog.d(TAG, "maxVol = " + maxVol + " setStreamVolume[STREAM_MUSIC] = " + (maxVol/2));
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (FMTestBroadcastReceiver != null) {
            unregisterReceiver(FMTestBroadcastReceiver);
        }
        DswLog.d(TAG, "\n****************退出FM @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);

                if (TestUtils.mIsAutoMode) {
                    SharedPreferences.Editor editor = TestUtils.getSNSharedPreferencesEdit(this);
                    editor.putString("52", "P");
                    editor.commit();
                }
                //TestUtils.rightPress(TAG, this);
                TestUtils.rightPress("FMTest", this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                //TestUtils.wrongPress(TAG, this);
                TestUtils.wrongPress("FMTest", this);
                break;
            }

            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                //TestUtils.restart(this, TAG);
                TestUtils.restart(this, "FMTest");
                break;
            }

        }

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private BroadcastReceiver FMTestBroadcastReceiver =new BroadcastReceiver (){
        @Override
        public void onReceive(Context context, Intent intent) {

            DswLog.e(TAG, "onReceive From FMRadio " + intent.getAction());
            if (intent.getAction().equals("chenyee.intent.action.END_FROM_FMRADIO")) {
                boolean isFMPass = intent.getBooleanExtra("isFmPass", false);
                DswLog.e(TAG, "onReceive isFmPass="+isFMPass);

                mRightBtn.setEnabled(isFMPass);
            }
        }
    };

}



