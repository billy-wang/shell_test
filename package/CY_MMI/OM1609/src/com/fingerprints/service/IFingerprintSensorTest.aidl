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

import com.fingerprints.service.IFingerprintSensorTestListener;

/** {@hide} */
interface IFingerprintSensorTest {
    void selfTest(IFingerprintSensorTestListener listener);
    void checkerboardTest(IFingerprintSensorTestListener listener);
    void captureImage(boolean waitForFinger, IFingerprintSensorTestListener listener);
    void captureImageUncalibrated();
    void sensorTestCancel();
    void imagequalityTest(IFingerprintSensorTestListener listener);
    //Gionee <GN_BSP_MMI> <chengq> <20170105> add for ID 59512 begin
    void fingertest(boolean waitForFinger,IFingerprintSensorTestListener listener);
    //Gionee <GN_BSP_MMI> <chengq> <20170105> add for ID 59512 begin

}
