
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import java.lang.reflect.Method;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.fingerprints.service.IFingerprintService;
import com.fingerprints.service.IFingerprintSensorTest;
import com.fingerprints.service.IFingerprintSensorTestListener;
import com.fingerprints.service.FingerprintSensorTest;
import com.fingerprints.service.FingerprintSensorTest.FingerprintSensorTestListener;
//Gionee <GN_BSP_MMI><lifeilong><20161224> add for ID 54296 begin
import gn.com.android.mmitest.BaseActivity;
//Gionee <GN_BSP_MMI><lifeilong><20161224> add for ID 54296 end
public class FingerPrintsTest3 extends BaseActivity implements OnClickListener  {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private ImageView mImageView;
    TextView titleTv;
    private static final String TAG = "FingerPrintsTest3";
    private IFingerprintSensorTest mService;
    FingerprintSensorTest mFingerprintSensorTest;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.checkToContinue(this);
        //Gionee zhangke 20151215 modify for CR01609753 start
        /*
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        getWindow().setAttributes(lp);
     	View view = getWindow().getDecorView();
	    int visFlags = View.STATUS_BAR_DISABLE_BACK
               | View.STATUS_BAR_DISABLE_HOME
               | View.STATUS_BAR_DISABLE_RECENT
               | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
               |View.SYSTEM_UI_FLAG_IMMERSIVE;
        view.setSystemUiVisibility(visFlags);
        */
        TestUtils.setWindowFlags(this);
	    //Gionee zhangke 20151215 modify for CR01609753 end
        setContentView(R.layout.fingerprints_textview);

        titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.fingerprints_test);
        mImageView  = (ImageView) findViewById(R.id.imgview);
        try{
            mFingerprintSensorTest = new FingerprintSensorTest();
        }catch(Exception e){
            Log.i(TAG, "mFingerprintSensorTest e="+e.getMessage());
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
				mWrongBtn.setEnabled(true);
				mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(FingerPrintsTest3.this);
                mWrongBtn.setOnClickListener(FingerPrintsTest3.this);
                mRestartBtn.setOnClickListener(FingerPrintsTest3.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee zhangke 20160428 modify for CR01687958 end

    }

    private FingerprintSensorTestListener mFingerprintSensorTestListener = new FingerprintSensorTestListener() {
        
        @Override
        public void onSelfTestResult(boolean result) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onSelfTestResult result="+result);
        }
        
        @Override
        public void onImagequalityTestResult(int result) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onImagequalityTestResult result="+result);
        }
        
        @Override
        public void onCheckerboardTestResult(int result) {
            // TODO Auto-generated method stub
            Log.i(TAG, "onCheckerboardTestResult result="+result);
        }

    	@Override
    	public void onCaptureTestResult(int result) {
    		// TODO Auto-generated method stub
    		Log.i(TAG, "onCaptureTestResult result="+result);
            //Gionee zhangke 20161112 add for ID21863 begin
            if(result == 0){
				titleTv.setText(R.string.fingertestsucc);
				mRightBtn.setEnabled(true);
            }else{
                titleTv.setText(R.string.fingertestfaild);
            }
            //Gionee zhangke 20161112 add for ID21863 end
    	}

    };

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume mFingerprintSensorTest selfTest :mFingerprintSensorTest="+mFingerprintSensorTest);
        try{
            Log.i(TAG, "onResume begin captureImage");
            mFingerprintSensorTest.fingertest(true,mFingerprintSensorTestListener);
            Log.i(TAG, "onResume end captureImage");
        }catch(Exception e){
            titleTv.setText(R.string.fingerprints_load_fail);
            Log.e(TAG, "captureImage Exception="+e.getMessage());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mFingerprintSensorTest != null){
            mFingerprintSensorTest.cancel();
        }
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
    //Gionee <GN_BSP_MMI> <chengq> <20170111> modify for ID 63096 begin
    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        return true;
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170111> modify for ID 63096 end
}
