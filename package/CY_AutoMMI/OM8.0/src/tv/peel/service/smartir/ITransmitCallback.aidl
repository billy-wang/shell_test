// Copyright (c) 2015 Peel Technologies Inc. All Rights Reserved.
package tv.peel.service.smartir;

import tv.peel.service.smartir.SmartIrFailure;

/*
 * Interface for transmit()
 *
 * onSuccess: Transmitted IR pattern
 * onFailure: Failed (STATUS_TRANSMIT_HAL_PROBLEM, STATUS_TRANSMIT_MODE_UNSUPPORTED, STATUS_TRANSMIT_CANCELED)
 */
oneway interface ITransmitCallback {
    void onSuccess();
    void onFailure(in SmartIrFailure failure);
}
