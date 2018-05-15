/*
 * Copyright (c) 2015 Fingerprint Cards AB <tech@fingerprints.com>
 *
 * All rights are reserved.
 * Proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Any use is subject to an appropriate license granted by Fingerprint Cards AB.
 */

package com.fingerprints.service;

/** {@hide} */
interface ILegacyFingerprint {
    long preUpgrade();
    int[] upgrade(in byte[] token, int gid, String path);
}
