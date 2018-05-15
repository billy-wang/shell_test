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
import android.os.SystemClock;
import android.util.Log;

public class Authenticator {

    private final long ID_VALID_TIMEOUT = 1500;
    private VerifyUserListener mVerifyUserListener;
    private IAuthenticator mService;
    private String TAG = "Authenticator";
    private Handler mHandler;
    private Object mIdValidLock = new Object();
    static final String SERVICE_NAME = "authenticator";
    private IdValid mIdValid = IdValid.UNDEFINED;

    private enum IdValid {
        UNDEFINED,
        VALUE_REQUESTED,
        TRUE,
        FALSE
    }

    private IUserIdValidListener mIUserIdValidListener = new IUserIdValidListener.Stub() {

        @Override
        public void onIsUserIDValidResult(final boolean isUserIDValid) throws RemoteException {
            synchronized (mIdValidLock) {
                if (mIdValid == IdValid.UNDEFINED) {
                    Log.e(TAG, "Unexpected user ID Valid result: " + String.valueOf(isUserIDValid)
                            + ". Ignoring.");
                } else {
                    mIdValid = isUserIDValid ? IdValid.TRUE : IdValid.FALSE;
                    mIdValidLock.notifyAll();
                }
            }
        }
    };
    private IVerifyUserListener mIVerifyUserListener = new IVerifyUserListener.Stub() {

        @Override
        public void onUserVerificationResult(final byte[] nonce,
                final int result, final String authenticatorName,
                final long userId, final long userEntityId,
                final byte[] encapsulatedResult) throws RemoteException {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVerifyUserListener.onUserVerificationResult(nonce,
                            result, authenticatorName, userId, userEntityId,
                            encapsulatedResult);
                    mVerifyUserListener = null;
                }
            });

        }

        @Override
        public void onUserVerificationHelp(final int result) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVerifyUserListener.onUserVerificationHelp(result);
                }
            });
        }
    };

    /**
     * This class is used to interact with the Fingerprint Extension Service to perform
     * authentication operations. Calling applications must have permission
     * com.fingerprints.service.ACCESS_EXTENSION_SERVICE
     * 
     * @author fpc
     */
    public Authenticator() throws Exception {
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
        IBinder authBinder = service.getService(SERVICE_NAME);
        if (authBinder == null) {
            throw new Exception("Authenticator API could not be loaded");
        }
        mService = IAuthenticator.Stub.asInterface(authBinder);

    }

    public int verifyUser(byte[] nonce, String secAppName, VerifyUserListener listener) {
        mVerifyUserListener = listener;
        try {
            mService.verifyUser(mIVerifyUserListener, nonce, secAppName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Cancel currently ongoing asynchronous action (such as verifyUser) */
    public void cancel() {
        try {
            mService.authenticatorCancel();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserIDValid(byte[] userID) {

        synchronized (mIdValidLock) {
            if (mIdValid == IdValid.VALUE_REQUESTED) {
                Log.e(TAG, "isUserIDValid called multiple times. Throwing RuntimeException.");
                throw new RuntimeException("Multiple calls to isUserIDValid not supported!");
            }
            try {
                mService.isUserIDValid(mIUserIdValidListener, userID);
            } catch (RemoteException e1) {
                Log.e(TAG, "No response from FPC Extension service. Throwing RuntimeException.");
                throw new RuntimeException("No response from FPC Extension service");
            }
            mIdValid = IdValid.VALUE_REQUESTED;
            long startTime = SystemClock.uptimeMillis();
            long timeLeftToWait = ID_VALID_TIMEOUT;
            do {
                try {
                    mIdValidLock.wait(timeLeftToWait);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                timeLeftToWait = Math.max(0,
                        startTime + ID_VALID_TIMEOUT - SystemClock.uptimeMillis());

            } while (mIdValid == IdValid.VALUE_REQUESTED && timeLeftToWait > 0);

            if (mIdValid == IdValid.VALUE_REQUESTED || mIdValid == IdValid.UNDEFINED) {
                Log.e(TAG, "No callback received from isUserIDValid. Throwing RuntimeException.");
                throw new RuntimeException("No callback received from isUserIDValid");
            }
            boolean retVal = mIdValid == IdValid.TRUE;
            mIdValid = IdValid.UNDEFINED;
            return retVal;
        }

    }

    public static interface VerifyUserListener {
        void onUserVerificationResult(byte[] nonce, int result,
                String authenticatorName, long userId, long userEntityId,
                byte[] encapsulatedResult);

        void onUserVerificationHelp(int result);
    }
}
