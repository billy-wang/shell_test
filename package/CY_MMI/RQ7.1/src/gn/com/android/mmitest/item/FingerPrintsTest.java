
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
import android.content.Context;
import java.lang.reflect.Constructor;
import android.hardware.fingerprint.IGnFingerprintServiceReceiver;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import android.os.IBinder;
import android.os.Binder;

public class FingerPrintsTest extends Activity implements OnClickListener {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private ImageView mImageView;
    TextView titleTv;
    private static final String TAG = "FingerPrintsTest";
    private IFingerprintSensorTest mService;
    FingerprintSensorTest mFingerprintSensorTest;
    private Handler handler;
    private static final int TEST_CMD_DEADPIXEL = 2;
    private static final int TEST_CMD_CAPTURE = 4;
    private static final int TEST_CMD_TEST = 30;
    private Class<?> clazz;
    private Object o;
    private Method testMethod;
    private Method stopMethod;
    private IBinder mToken = new Binder();
    private Class<?> servicemanager;
    private IFingerprintService service = null;
    private Object fingerprintmanager;
    private static final int TEST_TIMER_OUT = 10000;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);

        setContentView(R.layout.fingerprints_textview);

        titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.fingerprints_note);
        mImageView = (ImageView) findViewById(R.id.imgview);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 begin
        try {
            Class clazz = Class.forName("android.hardware.fingerprint.FingerprintManager");
            Constructor[] constructors = clazz.getConstructors();
            Constructor c = null;
            for (int i = 0; i < constructors.length; i++) {
                Log.e(TAG,"" + constructors[i]);
                c = constructors[i];
            }
            c.setAccessible(true);
            o = c.newInstance(FingerPrintsTest.this,service);
            testMethod = clazz.getMethod("test",IBinder.class, int.class, IGnFingerprintServiceReceiver.class);
            stopMethod = clazz.getMethod("cancelTest",IBinder.class);
            fingerprintmanager = (Object)this.getSystemService("fingerprint");
            } catch (ClassNotFoundException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (InstantiationException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (IllegalAccessException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        } catch (InvocationTargetException e) {
        Log.v(TAG, Log.getStackTraceString(e));
            e.printStackTrace();
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 end

        // Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(true);
        mRestartBtn.setEnabled(true);

        mRightBtn.setOnClickListener(FingerPrintsTest.this);
        mWrongBtn.setOnClickListener(FingerPrintsTest.this);
        mRestartBtn.setOnClickListener(FingerPrintsTest.this);
        handler = new Handler();
        // Gionee zhangke 20160428 modify for CR01687958 end
        handler.postDelayed(mRunnable, TEST_TIMER_OUT);
    }
    //Gionee <GN_BSP_MMI> <lifeilong> <20170411> modify for ID 83883 begin
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.i(TAG, "hand enable button  time out !");
            uiHandler.sendEmptyMessage(6);
            titleTv.setText(R.string.fingerprint_capture_image_timeout);
        }
    };
    
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            mWrongBtn.setEnabled(true);
            mRestartBtn.setEnabled(true);
            switch (msg.what) {
            //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 begin
            case 0:
                titleTv.setText(R.string.capturesucc);
                mRightBtn.setEnabled(true);
                break;
            case 1:
                titleTv.setText(R.string.fingerprint_capture_image_noise);
                break;
            case 2:
                titleTv.setText(R.string.fingerprints_capture_not_enough);
                break;
            case 3:
                titleTv.setText(R.string.fingerprint_capture_image_timeout);
                break;
            case 4:
            titleTv.setText(R.string.fingerprint_capture_image_fail);
                break;
            case 5:
                titleTv.setText(R.string.capturefailed);
                break;
            case 6:
                try {
                    if(testMethod != null) {
                        stopMethod.invoke(fingerprintmanager,mToken);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                break;
            //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 end
        }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170411> modify for ID 83883 end

    //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 begin
        private IGnFingerprintServiceReceiver mGnFingerprintServiceReceiver = new IGnFingerprintServiceReceiver.Stub(){
            public void onError(long deviceId, int errMsgId){
                Log.d(TAG,"onError errMsgId="+errMsgId);
            }
            public void onTestCmd(long deviceId, int cmdId, int result){
                Log.d(TAG,"onTestCmd cmdId="+cmdId+" result="+result);
                if (result == 0) {
                    uiHandler.sendEmptyMessage(0);
                } else if (result == -1) {
                    uiHandler.sendEmptyMessage(1);
                } else if (result == -4) {
                    uiHandler.sendEmptyMessage(4);
                } else if (result == -3) {
                    uiHandler.sendEmptyMessage(3);
                } else if (result == -2) {
                    uiHandler.sendEmptyMessage(2);
                } else {
                    uiHandler.sendEmptyMessage(5);
                }
                handler.removeCallbacks(mRunnable);
            }
	};
    //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 end
    @Override
    public void onResume() {
        super.onResume();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 begin
        try {
            if(testMethod != null) {
                Log.d(TAG,"testMethod = " + testMethod);
                testMethod.invoke(fingerprintmanager,mToken, 4, mGnFingerprintServiceReceiver);
            }
        } catch (IllegalAccessException e) {
             e.printStackTrace();
        } catch (InvocationTargetException e) {
             e.printStackTrace();
        }
        Log.i(TAG, "onResume end captureImage");
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 end
    }

    @Override
    public void onPause() {
        super.onPause();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 begin
        try {
            if(testMethod != null) {
                stopMethod.invoke(fingerprintmanager,mToken);
            }
        } catch (IllegalAccessException e) {
             e.printStackTrace();
        } catch (InvocationTargetException e) {
             e.printStackTrace();
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115539 end
        handler.removeCallbacks(mRunnable);

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
            handler.removeCallbacks(mRunnable);
            TestUtils.restart(this, TAG);
            break;
        }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }

}
