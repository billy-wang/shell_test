
package gn.com.android.mmitest.item;

import gn.com.android.mmitest.BaseActivity;
import gn.com.android.mmitest.GnMMITest;
import gn.com.android.mmitest.GnMMITestApplication;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import gn.com.android.mmitest.utils.DswLog;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodix.device.FpDevice;
import com.fingerprints.service.IFingerprintSensorTest;
import com.fingerprints.service.FingerprintSensorTest;
import com.fingerprints.service.FingerprintSensorTest.FingerprintSensorTestListener;

    /*Gionee huangjianqiang 20160624 add for CR01714100 begin*/
import gn.com.android.mmitest.GnMMITestApplication.FingerPrintsListener;
    /*Gionee huangjianqiang 20160624 add for CR01714100 end*/


/*Gionee huangjianqiang 20160624 modify for CR01714100 begin*/
public class FingerPrintsTest extends BaseActivity implements OnClickListener, FingerPrintsListener {
    /*Gionee huangjianqiang 20160624 modify for CR01714100 end*/
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private ImageView mImageView;
    TextView titleTv;
    private static final String TAG = "FingerPrintsTest";
    private IFingerprintSensorTest mService;
    FingerprintSensorTest mFingerprintSensorTest;
    /*Gionee huangjianqiang 20160503 add for CR01688435 being*/
    private static FpDevice mDevice;
    /*Gionee huangjianqiang 20160503 add for CR01688435 end*/
    /*Gionee huangjianqiang 20160603 add for CR01712997 begin*/
    boolean isPass = false;
    /*Gionee huangjianqiang 20160603 add for CR01712997 end*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开指纹识别 @" + Integer.toHexString(hashCode()));
        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.fingerprints_textview);


        titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.fingerprints_note);
        mImageView = (ImageView) findViewById(R.id.imgview);

        //Gionee <GN_BSP_MMI> <chengq> <20170214> modify for ID 68572 begin
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

                mRightBtn.setOnClickListener(FingerPrintsTest.this);
                mWrongBtn.setOnClickListener(FingerPrintsTest.this);
                mRestartBtn.setOnClickListener(FingerPrintsTest.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        //Gionee <GN_BSP_MMI> <chengq> <20170214> modify for ID 68572 end

        /*Gionee huangjianqiang 20160503 modify for CR01688435 being*/
        if (GnMMITestApplication.isGoodix) {
    /*Gionee huangjianqiang 20160624 modify for CR01714100 begin*/
            GnMMITestApplication.getApplication().setFingerPrintListener(this);
    /*Gionee huangjianqiang 20160624 modify for CR01714100 end*/
            mDevice = GnMMITestApplication.getFpDevice();// FpDevice.open(mHandler);
        } else {
            try {
                mFingerprintSensorTest = new FingerprintSensorTest();
            } catch (Exception e) {
                DswLog.i(TAG, "mFingerprintSensorTest e=" + e.getMessage());
            }

        }/*Gionee huangjianqiang 20160503 modify for CR01688435 end*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出软指纹识别 @" + Integer.toHexString(hashCode()));
    }

    private FingerprintSensorTestListener mFingerprintSensorTestListener = new FingerprintSensorTestListener() {

        @Override
        public void onSelfTestResult(boolean result) {
            // TODO Auto-generated method stub
            DswLog.i(TAG, "onSelfTestResult result=" + result);
        }

        @Override
        public void onImagequalityTestResult(int result) {
            // TODO Auto-generated method stub
            DswLog.i(TAG, "onImagequalityTestResult result=" + result);
        }

        @Override
        public void onCheckerboardTestResult(int result) {
            // TODO Auto-generated method stub
            DswLog.i(TAG, "onCheckerboardTestResult result=" + result);
        }

        @Override
        public void onCaptureTestResult(int result) {
            // TODO Auto-generated method stub
            DswLog.i(TAG, "onCaptureTestResult result=" + result);
            if (result == 0) {
                titleTv.setText(R.string.capturesucc);
                mRightBtn.setEnabled(true);
            } else if (result == 5) {
                titleTv.setText(R.string.fingerprints_capture_not_enough);
            } else {
                titleTv.setText(R.string.capturefailed);
            }
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        /*Gionee huangjianqiang 20160503 modify for CR01688435 being*/
        if (GnMMITestApplication.isGoodix) {
            //TODO send test cmd 录入26 坏点28
            startTest();
        } else {
            try {
                mFingerprintSensorTest.captureImage(true, mFingerprintSensorTestListener);
            } catch (Exception e) {
                titleTv.setText(R.string.fingerprints_load_fail);
            }
        }/*Gionee huangjianqiang 20160503 modify for CR01688435 end*/

    }

    /*Gionee huangjianqiang 20160503 add for CR01688435 being*/
    private void startTest() {
        titleTv.setText(R.string.fingerprints_note);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(true);
        //goodix-jicai modify 20160513 start
        if (GnMMITestApplication.isGoodix) {
            mRestartBtn.setEnabled(false);
        } else {
            mRestartBtn.setEnabled(true);
        }
        //goodix-jicai modify 20160513 end

        new Thread() {
            @Override
            public void run() {
                DswLog.e(TAG, "onResume: SendCmd:26, 30000 ，null");
    /*Gionee huangjianqiang 20160624 modify for CR01714100 begin*/
                mDevice.SendCmd(GnMMITestApplication.FP_VERIFY, 30000 + "", null);//10000 + ""
    /*Gionee huangjianqiang 20160624 modify for CR01714100 end*/
            }
        }.start();
    }/*Gionee huangjianqiang 20160503 add for CR01688435 end*/

    @Override
    public void onPause() {
        super.onPause();
    /*Gionee huangjianqiang 20160624 add for CR01714100 begin*/
        if(GnMMITestApplication.isGoodix) {
            GnMMITestApplication.getApplication().resetFingerPrintListener();
        }
    /*Gionee huangjianqiang 20160624 add for CR01714100 end*/

        /*Gionee huangjianqiang 20160314 add for CR01652494 begin*/
        if (mFingerprintSensorTest != null) {
            mFingerprintSensorTest.cancel();
        }
        /*Gionee huangjianqiang 20160314 add for CR01652494 end*/

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

    /*Gionee huangjianqiang 20160624 add for CR01714100 begin*/
    @Override
    public void onMessage(int what, int arg1) {
        DswLog.e(TAG,"msg.what:" +what + " arg1:" + arg1);
        if(what == GnMMITestApplication.MSG_PERFORMACE) {
            if (arg1 == 0) {
                titleTv.setText(R.string.capturesucc);
                        /*Gionee huangjianqiang 20160603 add for CR01712997 begin*/
                isPass = true;
                        /*Gionee huangjianqiang 20160603 add for CR01712997 end*/


            } else {
                titleTv.setText(R.string.capturefailed);
                        /*Gionee huangjianqiang 20160603 add for CR01712997 begin*/
                isPass = false;
                        /*Gionee huangjianqiang 20160603 add for CR01712997 end*/

            }
        } else if(what == GnMMITestApplication.MSG_EXIT) {

            //goodix-jicai modify 20160513 start
            mRestartBtn.setEnabled(true);
            //goodix-jicai modify 20160513 end
                        /*Gionee huangjianqiang 20160603 add for CR01712997 begin*/
            if(isPass) {
                mRightBtn.setEnabled(true);
            } else {
                mRightBtn.setEnabled(false);
            }
                    /*Gionee huangjianqiang 20160603 add for CR01712997 end*/
        }
    }
    /*Gionee huangjianqiang 20160624 add for CR01714100 end*/

}
