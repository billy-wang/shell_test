// Copyright (c) 2015 Peel Technologies Inc. All Rights Reserved.
package tv.peel.service.smartir;


// WARNING: These are same define values as the static library's "smartir_helper.h"
public class SmartIrResponse {
    public final static int RESPONSE_ALL_OK = 0;
    public final static int RESPONSE_INIT_FAILURE = 1;
    public final static int RESPONSE_WRITE_BITS_FAILURE = 2;
    public final static int RESPONSE_READ_MODE_FAILURE = 3;
    public final static int RESPONSE_WRITE_MODE_FAILURE = 4;
    public final static int RESPONSE_READ_MSG_FAILURE = 5;
    public final static int RESPONSE_WRITE_MSG_FAILURE = 6;
    public final static int RESPONSE_TRANSMIT_FAILURE = 7;
    public final static int RESPONSE_TRANSMIT_IN_PROGRESS = 100;
    public final static int RESPONSE_TRANSMIT_ALREADY_CANCELED = 101;
    public final static int RESPONSE_RECEIVE_NOT_ENOUGH_MEMORY = 102;
    public final static int RESPONSE_RECEIVE_FAILURE = 103;
    public final static int RESPONSE_RECEIVE_IN_PROGRESS = 104;
    public final static int RESPONSE_RECEIVE_ALREADY_CANCELED = 105;
    public final static int RESPONSE_NULL_CALLBACK = 106;
    public final static int RESPONSE_FAILED_TO_ALLOCATE = 107;
    public final static int RESPONSE_FAILED_TO_OPEN_FILE = 108;
    public final static int RESPONSE_GENERAL_FAILURE = 109;
    public final static int RESPONSE_DRIVER_FAILURE = 110;
    public final static int RESPONSE_PHOTON_IN_PROGRESS = 111;
    public final static int RESPONSE_PHOTON_ALREADY_CANCELED = 112;
    public final static int RESPONSE_PHOTON_LED_FAILURE = 113;
    public final static int RESPONSE_PHOTON_ADC_FAILURE = 114;
    public final static int RESPONSE_MISSING_CAPABILITY = 115;
}
