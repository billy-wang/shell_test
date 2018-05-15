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

/** {@hide} */
interface IVerifyUserListener {
	void onUserVerificationResult(in byte[] nonce, int result, in String authenticatorName,
				long userId, long userEntityId, in byte[] encapsulatedResult);
    void onUserVerificationHelp(int result);
}
