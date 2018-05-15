/*
*
* Copyright (c) 2015 Fingerprint Cards AB <tech@fingerprints.com>
*
* All rights are reserved.
* Proprietary and confidential.
* Unauthorized copying of this file, via any medium is strictly prohibited.
* Any use is subject to an appropriate license granted by Fingerprint Cards AB.
*
*/

package com.fingerprints.service;

import java.lang.reflect.Method;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import gn.com.android.mmitest.utils.DswLog;
/**
 * This class is used to interact with the Fingerprint Service to perform sensor test operations.
 *
 * @author fpc
 */
public class FingerprintSensorTest {
    private static final String TAG = "FingerprintSensorTest";
    static final String SERVICE_NAME = "sensor_test";

    public final boolean FPC_SELFTEST_PASSED = true;
    public final boolean FPC_SELFTEST_FAILED = false;

    public final int FPC_CHECKERBOARD_PASSED = 0;
    public final int FPC_CHECKERBOARD_TYPE1_MEDIAN_ERROR = 1;
    public final int FPC_CHECKERBOARD_TYPE2_MEDIAN_ERROR = 2;
    public final int FPC_CHECKERBOARD_DEAD_PIXELS = 4;
    public final int FPC_CHECKERBOARD_DEAD_PIXELS_FINGER_DETECT = 8;

    private IFingerprintSensorTest mService;
    private FingerprintSensorTestListener mSensorTestListener;
    private Handler mHandler;

    private IFingerprintSensorTestListener mISensorTestListener = new IFingerprintSensorTestListener.Stub() {

        @Override
        public void onSelfTestResult(final boolean result) throws RemoteException {
         DswLog.i(TAG,"onSelfTestResult result="+result);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSensorTestListener.onSelfTestResult(result);
                }
            });
        }

        @Override
        public void onCheckerboardTestResult(final int result) throws RemoteException {
        DswLog.i(TAG,"onCheckerboardTestResult result="+result);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSensorTestListener.onCheckerboardTestResult(result);
                }
            });
        }

        @Override
        public void onImagequalityTestResult(final int result) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSensorTestListener.onImagequalityTestResult(result);
                }
            });
        }
		
    	@Override
    	public void onCaptureTestResult(final int result) {
    		// TODO Auto-generated method stub
            DswLog.i(TAG,"onCaptureTestResult result="+result);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSensorTestListener.onCaptureTestResult(result);
                }
            });

    	}

    };

    /**
     * This class is is used to interact with the Fingerprint Extension Service to perform test
     * operations. Calling applications must have permission
     * com.fingerprints.service.ACCESS_EXTENSION_SERVICE
     * 
     * @author fpc
     */
    public FingerprintSensorTest() throws Exception {
        mHandler = new Handler();
        Class<?> servicemanager;
        IFingerprintService service = null;

        try {
            servicemanager = Class.forName("android.os.ServiceManager");

            Method getService = servicemanager.getMethod("getService", String.class);

            IBinder binder;

            binder = (IBinder) getService.invoke(null, "fingerprints_service");

            service = IFingerprintService.Stub.asInterface(binder);
        } catch (Exception e) {
            // TODO Add clearer Exception handling
            e.printStackTrace();
        }
        if (service == null) {
            throw new Exception("The FPC extension service could not be loaded");
        }

        IBinder sensorBinder = service.getService("sensor_test");
        if (sensorBinder == null) {
            throw new Exception("Sensor test API could not be loaded");
        }
        mService = IFingerprintSensorTest.Stub.asInterface(sensorBinder);
        DswLog.i(TAG, "FingerprintSensorTest:service="+service+";mService="+mService);

    }

    public void selfTest(FingerprintSensorTestListener listener) {
        mSensorTestListener = listener;
        try {
            mService.selfTest(mISensorTestListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void checkerboardTest(FingerprintSensorTestListener listener) {
        mSensorTestListener = listener;
        try {
            mService.checkerboardTest(mISensorTestListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void imagequalityTest(FingerprintSensorTestListener listener) {
        mSensorTestListener = listener;
        try {
            mService.imagequalityTest(mISensorTestListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /**
     * Capture an image
     *
     * @param waitForFinger Indicates if we should wait for finger present before capturing the
     *            image
     */
    public void captureImage(boolean waitForFinger, FingerprintSensorTestListener listener) {
        mSensorTestListener = listener;
        try {
            mService.captureImage(waitForFinger, mISensorTestListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    //Gionee <GN_BSP_MMI> <chengq> <20170105> add for ID 59512 begin
	public void fingertest(boolean waitForFinger,FingerprintSensorTestListener listener) {
		DswLog.d(TAG, "fingertest");
		mSensorTestListener = listener;
        try {
            mService.fingertest(waitForFinger,mISensorTestListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }		
	}
    //Gionee <GN_BSP_MMI> <chengq> <20170105> add for ID 59512 begin

    /**
     * Capture an uncalibrated image
     */
    public void captureImageUncalibrated() {
        try {
            mService.captureImageUncalibrated();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Cancel currently ongoing asynchronous action (such as captureImage) */
    public void cancel() {
        try {
            mService.sensorTestCancel();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static interface FingerprintSensorTestListener {
        void onSelfTestResult(boolean result);
        
        void onImagequalityTestResult(int result);

        void onCheckerboardTestResult(int result);

        void onCaptureTestResult(int result);
    }
}
