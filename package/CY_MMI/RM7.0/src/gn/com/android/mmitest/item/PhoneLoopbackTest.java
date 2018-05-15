
package gn.com.android.mmitest.item;

import java.util.concurrent.atomic.AtomicBoolean;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.content.Intent;

public class PhoneLoopbackTest extends Activity implements OnClickListener {
    private static String TAG = "PhoneLoopbackTest";
    String aString = null;

    Button mRightBtn, mWrongBtn, mRestartBtn;
    TextView mContentTv;

    AudioManager mAM;
    //RecordThread mRecThread;
    AtomicBoolean mIsRunning = new AtomicBoolean(false);
    private boolean phoneloopbackFlag = false;
    private Intent it;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TestUtils.checkToContinue(this);
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.common_textview);
        it = this.getIntent();
        if(it != null){
            phoneloopbackFlag=  it.getBooleanExtra("as", false);
        }
        Log.d(TAG,"phoneloopbackFlag = " + phoneloopbackFlag);        
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        if(phoneloopbackFlag){
            mRestartBtn.setVisibility(View.INVISIBLE);
            TestUtils.asResult(TAG,"","2");
        }        
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(false);
        mRestartBtn.setEnabled(false);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mRightBtn.setEnabled(true);
                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(PhoneLoopbackTest.this);
                mWrongBtn.setOnClickListener(PhoneLoopbackTest.this);
                mRestartBtn.setOnClickListener(PhoneLoopbackTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

        TextView recordTitle = (TextView) findViewById(R.id.test_title);

        mAM = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //mRecThread = new RecordThread();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_btn: {
                if(phoneloopbackFlag){
                    TestUtils.asResult(TAG,"","1");
                }                
                mIsRunning.set(false);
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                if(phoneloopbackFlag){
                    TestUtils.asResult(TAG,"","0");
                }                
                mIsRunning.set(false);
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
        mIsRunning.set(true);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170615> modif for ID 158102 begin
        //mAM.setMode(AudioManager.MODE_NORMAL);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170615> modif for ID 158102 end
            aString = TestUtils.getStreamVoice("lb_mmi_PhoneLoopbackTest");
            if (aString != null && aString.length() > 0 ) {
                mAM.setParameters("SET_LOOPBACK_TYPE=21,1,"+aString);
            }else {
                mAM.setParameters("SET_LOOPBACK_TYPE=21,1");
            }
            Log.e(TAG, "onStart: setParameters SET_LOOPBACK_TYPE=21,1"+aString);
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsRunning.set(false);
        mAM.setParameters("SET_LOOPBACK_TYPE=0,0");
        Log.e(TAG, " onPause setParameters SET_LOOPBACK_TYPE=0,0");
            try {
                //Gionee <GN_BSP_MMI> <lifeilong> <20170524> modify for ID 146803 begin 
                Thread.sleep(1000);
                //Gionee <GN_BSP_MMI> <lifeilong> <20170524> modify for ID 146803 end
            } catch (Exception e) {
                System.out.println(e);
            }
        Log.e(TAG, "onPause set mode --> mode_normal");
        if(phoneloopbackFlag){
           this.finish();
           Log.d(TAG,"onStop as_record_finish_self");
        }        
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
}
