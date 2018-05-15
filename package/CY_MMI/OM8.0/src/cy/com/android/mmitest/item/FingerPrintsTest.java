
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.CyMMITest;
import cy.com.android.mmitest.utils.DswLog;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.RemoteException;
import android.hardware.fingerprint.ICyFingerprintServiceReceiver;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.util.Log;
import android.os.IBinder;
import android.os.Binder;


public class FingerPrintsTest extends BaseActivity implements OnClickListener {
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    TextView titleTv;
    private static final String TAG = "FingerPrintsTest";
    private IBinder mToken = new Binder();
    private Object fingerprintmanager;
    private boolean fingerFlag = false;
    private Class<?> clazz;
    private Method testMethod;
    private Method stopMethod;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开指纹识别 @" + Integer.toHexString(hashCode()));
        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.fingerprints_textview);


        titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.fingerprints_note);

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
        startTest();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d(TAG, "\n****************退出软指纹识别 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
        DswLog.i(TAG, "onResume");
        try {
            if(testMethod != null) {
                DswLog.i(TAG, "MMI call FingerprintManager test(4) begin");
                testMethod.invoke(fingerprintmanager,mToken, 4, mICyFingerprintServiceReceiver);
                DswLog.i(TAG, "MMI call FingerprintManager test(4) end");
            }
        } catch (IllegalAccessException e) {
            DswLog.i(TAG, "MMI call FingerprintManager Fail #1");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            DswLog.i(TAG, "MMI call FingerprintManager Fail #2");
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
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

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    titleTv.setText(R.string.capturesucc);
                    mRightBtn.setEnabled(true);
                    break;
                case 5:
                    titleTv.setText(R.string.fingerprints_capture_not_enough);
                    break;
                default:
                    titleTv.setText(R.string.capturefailed);
                    break;
            }
        }
    };

    private void startTest() {
        titleTv.setText(R.string.fingerprints_note);
        mRightBtn.setEnabled(false);

        try {
            Class clazz = Class.forName("android.hardware.fingerprint.FingerprintManager");
            fingerprintmanager = (Object)this.getSystemService("fingerprint");
            DswLog.e(TAG,"fingerprintmanager=" + fingerprintmanager );
            DswLog.e(TAG,"clazz" + clazz );
            testMethod = clazz.getMethod("test",IBinder.class, int.class, ICyFingerprintServiceReceiver.class);
            DswLog.e(TAG,"testMethod=" + testMethod );
            stopMethod = clazz.getMethod("cancelTest",IBinder.class);
            DswLog.e(TAG,"stopMethod=" + stopMethod );
        } catch (ClassNotFoundException e) {
            DswLog.e(TAG,"MMI FingerPrintsTest startTest Failed #1");
            DswLog.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            DswLog.e(TAG,"MMI FingerPrintsTest startTest Failed #2");
            DswLog.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    private ICyFingerprintServiceReceiver mICyFingerprintServiceReceiver = new ICyFingerprintServiceReceiver.Stub(){
        public void onError(long deviceId, int errMsgId, int vendorCode){
            DswLog.d(TAG,"onError deviceId=" + deviceId + " errMsgId="+errMsgId + " vendorCode="+vendorCode);
        }

        public void onTestCmd(long deviceId, int cmdId, int result){
            DswLog.d(TAG,"onTestCmd cmdId="+cmdId+" result="+result);
            uiHandler.sendEmptyMessage(result);

            try {
                if(testMethod != null) {
                    DswLog.i(TAG, "MMI call FingerprintManager cancelTest() begin");
                    stopMethod.invoke(fingerprintmanager,mToken);
                    DswLog.i(TAG, "MMI call FingerprintManager cancelTest() end");
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    };
}
