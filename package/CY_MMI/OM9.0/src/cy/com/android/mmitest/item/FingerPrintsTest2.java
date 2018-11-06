
package cy.com.android.mmitest.item;

import cy.com.android.mmitest.BaseActivity;
import cy.com.android.mmitest.CyMMITest;
import cy.com.android.mmitest.utils.DswLog;
import cy.com.android.mmitest.R;
import cy.com.android.mmitest.TestUtils;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.RemoteException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.util.Log;
import android.os.Binder;
import cy.com.android.mmitest.utils.HelPerformUtil;
import cy.com.android.mmitest.bean.OnPerformListen;
import android.content.Context;
import java.lang.ref.WeakReference;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.hardware.fingerprint.Fingerprint;

public class FingerPrintsTest2 extends BaseActivity implements OnClickListener ,OnPerformListen{
    private Button mRightBtn, mWrongBtn, mRestartBtn;
    TextView titleTv;
    private static final String TAG = "FingerPrintsTest2";
    private FingerprintManager fingerprintmanager;
    private boolean fingerFlag = false;
    private Class<?> clazz;
    public Method testMethod = getStartTestMethod("test",new Class[] {CancellationSignal.class,int.class});
    public CancellationSignal cancellationSignal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DswLog.d(TAG, "\n\n\n****************打开指纹坏点检测 @" + Integer.toHexString(hashCode()));
        TestUtils.checkToContinue(this);
        TestUtils.setCurrentAciticityTitle(TAG,this);
        setContentView(R.layout.fingerprints_textview);

        titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.fingerprints_note2);


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

                mRightBtn.setOnClickListener(FingerPrintsTest2.this);
                mWrongBtn.setOnClickListener(FingerPrintsTest2.this);
                mRestartBtn.setOnClickListener(FingerPrintsTest2.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);

        initTestRst();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mRespirationFingerprintRstObserver);
        fingerprintmanager = null;

        DswLog.d(TAG, "\n****************退出指纹坏点检测 @" + Integer.toHexString(hashCode()));
    }

    @Override
    public void onResume() {
        super.onResume();
        DswLog.i(TAG, "onResume");
        DswLog.i(TAG, "MMI call FingerprintManager test(2) begin");

        startTest();
        //DswLog.i(TAG, "MMI call FingerprintManager test(2) end");
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
				if(cancellationSignal != null){
				    cancellationSignal.cancel();
				}
                uiHandler.sendEmptyMessageDelayed(MESSAGE_RESTART_TEST, 1000);
                break;
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

    private static final int MESSAGE_SUCCESS = 0;
    private static final int MESSAGE_FAIL_UNCLEAN = 1;
    private static final int MESSAGE_RESTART_TEST = 6;
    private static final int DEFAULT_RST_VALUE = -1;

    public Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SUCCESS:
                    titleTv.setText(R.string.sensortest_deadpixel_test_success);
                    mRightBtn.setEnabled(true);

                    if (TestUtils.mIsAutoMode) {
                        HelPerformUtil.getInstance().performDelayed(FingerPrintsTest2.this, 600);
                    }
                    break;
                case MESSAGE_FAIL_UNCLEAN:
                    titleTv.setText(R.string.sensortest_deadpixel_unclean);
                    break;
                case MESSAGE_RESTART_TEST:
                    startTest();
                    break;
                default:
                    titleTv.setText(R.string.sensortest_deadpixel_test_fail);
                    break;
            }
        }
    };


    private void startTest() {
        titleTv.setText(R.string.fingerprints_note2);
        mRightBtn.setEnabled(false);
        cancellationSignal  = new CancellationSignal();
        fingerprintmanager   = (FingerprintManager)this.getSystemService(Context.FINGERPRINT_SERVICE);
        //fingerprintmanager.test(cancellationSignal,2);
        try {
            if(null != testMethod){
                testMethod.invoke(fingerprintmanager, new Object[] {cancellationSignal,2});
            }
        } catch (IllegalAccessException e) {
            DswLog.i(TAG, "MMI call FingerprintManager Fail #1");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            DswLog.i(TAG, "MMI call FingerprintManager Fail #2");
            e.printStackTrace();
        }
    }

    private static final String CY_MMI_FINGERPRINT_TEST_RST = "cy_mmi_fingerprint_test_rst";

    private static final String CY_FINGERPRINT_MANAGER = "android.hardware.fingerprint.FingerprintManager";

    private Method getStartTestMethod(String func, Class[] cls) {
        try {
            Class<?> appFingerprintManager = Class.forName(CY_FINGERPRINT_MANAGER);
            Method method = appFingerprintManager.getDeclaredMethod(func, cls);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void initTestRst(){
        Settings.Global.putInt(getContentResolver(),
                CY_MMI_FINGERPRINT_TEST_RST, DEFAULT_RST_VALUE);
        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(CY_MMI_FINGERPRINT_TEST_RST),
                true,mRespirationFingerprintRstObserver);
    }

    private ContentObserver mRespirationFingerprintRstObserver = new ContentObserver(new Handler()){
        @Override
        public void onChange(boolean selfChange) {
            Log.e(TAG,"rst onChange:" + selfChange);
            int rst = Settings.Global.getInt(getContentResolver(),CY_MMI_FINGERPRINT_TEST_RST,DEFAULT_RST_VALUE);
            if (DEFAULT_RST_VALUE != rst) {
                SendTestResult(rst);
            }
        }
    };

    public void SendTestResult(int errMsgId){
        //do something
        if (null == cancellationSignal) return;
        DswLog.d(TAG, "onTestResult errMsgId=" + errMsgId);
        uiHandler.sendEmptyMessage(errMsgId);
        cancellationSignal.cancel();
        cancellationSignal = null;
        Settings.Global.putInt(getContentResolver(),
                CY_MMI_FINGERPRINT_TEST_RST, DEFAULT_RST_VALUE);
    }

    @Override
    public void OnButtonPerform() {
        HelPerformUtil.getInstance().unregisterPerformListen();
        DswLog.i(TAG, "OnButtonPerform");
        mRightBtn.performClick();
    }
}
