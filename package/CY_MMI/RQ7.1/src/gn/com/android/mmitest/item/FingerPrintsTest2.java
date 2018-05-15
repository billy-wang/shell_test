
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

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
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

public class FingerPrintsTest2 extends Activity implements OnClickListener {
    Button mToneBt;

    private Button mRightBtn, mWrongBtn, mRestartBtn;
    private Object mObj;
    TextView titleTv;
    private int sensorTestType = -1;
    private Context mContext;
    private static final String TAG = "FingerPrintsTest2";
    private IFingerprintSensorTest mService;

    FingerprintSensorTest mFingerprintSensorTest;
    // Gionee zhangke 20160428 modify for CR01687958 start
    private boolean mIsTimeOver = false;
    private boolean mIsPass = false;
    // Gionee zhangke 20160428 modify for CR01687958 end
    private Class<?> clazz;
	private Object o;
	private Method testMethod;
	private Method stopMethod;
    private IBinder mToken = new Binder();
	private Class<?> servicemanager;
	private IFingerprintService service = null;
    private Object fingerprintmanager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtils.setWindowFlags(this);
        setContentView(R.layout.fingerprints_textview);
        titleTv = (TextView) findViewById(R.id.test_title);
        titleTv.setText(R.string.fingerprints_note2);
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 begin
        try {
            Class clazz = Class.forName("android.hardware.fingerprint.FingerprintManager");
            Constructor[] constructors = clazz.getConstructors();
			Constructor c = null;
            for (int i = 0; i < constructors.length; i++) {
                Log.e(TAG,"" + constructors[i]);
				c = constructors[i];
            }
			c.setAccessible(true);
            o = c.newInstance(FingerPrintsTest2.this,service);
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
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 end

        // Gionee zhangke 20160428 modify for CR01687958 start
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRightBtn.setEnabled(false);
        mWrongBtn.setEnabled(true);
        mRestartBtn.setEnabled(true);

        Log.i(TAG, "onCreate handler start");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Log.i(TAG, "hand enable button");
                mIsTimeOver = true;
                if (mIsPass) {
                    mRightBtn.setEnabled(true);
                }

                mWrongBtn.setEnabled(true);
                mRestartBtn.setEnabled(true);

                mRightBtn.setOnClickListener(FingerPrintsTest2.this);
                mWrongBtn.setOnClickListener(FingerPrintsTest2.this);
                mRestartBtn.setOnClickListener(FingerPrintsTest2.this);
            }
        }, TestUtils.BUTTON_ENABLED_DELAY_TIME);
        // Gionee zhangke 20160428 modify for CR01687958 end

    }

    @Override
    public void onResume() {
        super.onResume();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 begin
        try {
            if(testMethod != null) {
                testMethod.invoke(fingerprintmanager,mToken, 2, mGnFingerprintServiceReceiver);
            }
        } catch (IllegalAccessException e) {  			
             e.printStackTrace();			
        } catch (InvocationTargetException e) {				  
             e.printStackTrace();				  
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 end
    }

    @Override
    public void onPause() {
        super.onPause();
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 begin
        try {
            if(testMethod != null) {
                stopMethod.invoke(fingerprintmanager,mToken);
            }
        } catch (IllegalAccessException e) {  			
             e.printStackTrace();			
        } catch (InvocationTargetException e) {				  
             e.printStackTrace();				  
        }
        //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 end
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
    //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 begin
	private IGnFingerprintServiceReceiver mGnFingerprintServiceReceiver = new IGnFingerprintServiceReceiver.Stub(){
		public void onError(long deviceId, int errMsgId){
			Log.d(TAG,"onError	errMsgId="+errMsgId);
		} 
	
		public void onTestCmd(long deviceId, int cmdId, int result){
			Log.d(TAG,"onTestCmd cmdId="+cmdId+" result="+result);
            if (result == 0) {
                uiHandler.sendEmptyMessage(0);
            } else {
                uiHandler.sendEmptyMessage(1);
            }
		}	
	};
	
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
			mWrongBtn.setEnabled(true);
			mRestartBtn.setEnabled(true);
            switch (msg.what) {
                case 0:
					titleTv.setText(R.string.sensortest_deadpixel_test_success);
					mIsPass = true;
					if (mIsTimeOver) {
						mRightBtn.setEnabled(true);
					}
                    break;
                case 1:					
					titleTv.setText(R.string.sensortest_deadpixel_test_fail);
					break;
            }
        }
    };
    //Gionee <GN_BSP_MMI> <lifeilong> <20170419> modify for ID 115419 end

}
