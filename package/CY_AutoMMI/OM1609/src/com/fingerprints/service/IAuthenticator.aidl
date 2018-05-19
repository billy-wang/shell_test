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

import android.os.IBinder;

import com.fingerprints.service.IVerifyUserListener;
import com.fingerprints.service.IUserIdValidListener;

/** {@hide} */
interface IAuthenticator {
    int verifyUser(IVerifyUserListener callback, in byte[] nonce, in String secAppName);
    void authenticatorCancel();
    void isUserIDValid(IUserIdValidListener callback, in byte[] userID);
}
