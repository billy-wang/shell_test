/*
 * Copyright (c) 2015 Fingerprint Cards AB <tech@fingerprints.com>
 *
 * All rights are reserved.
 * Proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Any use is subject to an appropriate license granted by Fingerprint Cards AB.
 */

package com.fingerprints.service;

import android.os.IBinder;
import android.os.RemoteException;

import java.lang.reflect.Method;

public class LegacyFingerprint {
    public static final String EXTENSION_NAME ="legacy_fingerprint";

    private ILegacyFingerprint mService;

    public LegacyFingerprint() throws Exception {
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

        IBinder extension = service.getService(EXTENSION_NAME);
        if (extension == null) {
            throw new Exception(EXTENSION_NAME +" API could not be loaded");
        }
        mService = ILegacyFingerprint.Stub.asInterface(extension);

    }

    /* return a 64 bit challenge that must be used to sign a hw_auth_token with gatekeeper */
    public long preUpgrade() {
        try {
            return mService.preUpgrade();
        } catch (RemoteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* upgrade the legacy database belonging to user gid, the database will be associated with
       the secure android user that signed the hw_auth_token and exported to location path

     */
    public int[] upgrade(byte[] token, int gid, String path) {
        try {
            return mService.upgrade(token, gid, path);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }
}
