// Copyright (c) 2015 Peel Technologies Inc. All Rights Reserved.
package tv.peel.service.smartir;

import tv.peel.service.smartir.SmartIrFailure;

/*
 * Interface for receive()
 *
 * onSuccess: Received IR pattern
 * onFailure: Failed (STATUS_RECEIVE_HAL_PROBLEM, STATUS_RECEIVE_TIMEOUT, STATUS_RECEIVE_CORRUPT_DATA, STATUS_RECEIVE_CANCELED)
 */
oneway interface IReceiveCallback {
    void onSuccess(in int frequency, in String pattern);
    void onFailure(in SmartIrFailure failure);
}
