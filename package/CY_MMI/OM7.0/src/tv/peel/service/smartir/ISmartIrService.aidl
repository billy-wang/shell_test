// Copyright (c) 2015 Peel Technologies Inc. All Rights Reserved.
package tv.peel.service.smartir;

import tv.peel.service.smartir.ITransmitCallback;
import tv.peel.service.smartir.IReceiveCallback;
import tv.peel.service.smartir.AdcDataEntryParcelable;

interface ISmartIrService {
    /*
     * Format: A.B.C (three numbers delimited by periods)
     */
    String getVersion();

    /*
     * List of strings that show what the service can do
     */
    String[] getCapabilities();

    /*
     * Maybe we need to start something (i.e. onResume)
     */
    int enableSmartIr();

    /*
     * Maybe we need to stop something (i.e. during onPause to conserve battery)
     */
    int disableSmartIr();

    /*
     * Transmit IR either once or repetitively
     *
     * carrierFrequency: frequency of the device's IR receiver
     * mainPattern: Main IR pattern (if mode is TRANSMIT_MODE_ONCE, then it will only use this and
     *                 not repeatPattern)
     *              Delimited by comma. For example, "23,64,33"
     * repeatPattern: IR pattern (if null, then mainPattern will be repeated in
     *                TRANSMIT_MODE_REPEAT_FOREVER or TRANSMIT_MODE_REPEAT_N_TIMES)
     *                Delimited by comma. For example, "23,64,33"
     * intervalType: PATTERN_FREQUENCY_PULSES, PATTERN_MICROSECONDS
     * mode: TRANSMIT_MODE_ONCE, TRANSMIT_MODE_REPEAT_FOREVER, TRANSMIT_MODE_REPEAT_N_TIMES
     * repeatCount: how many times to repeat. Only valid in TRANSMIT_MODE_REPEAT_N_TIMES.
     *              0 will transmit only once with no repeats.
     */
    int startTransmitting(in int carrierFrequency, in String mainPattern, in String repeatPattern,
            in int intervalType, in int transmitMode, in int repeatCount);

    /*
     * Cancel continuous transmitting of IR
     */
    int stopTransmitting();

    /*
     * Register ITransmitCallback for startTransmitting()
     */
    int registerTransmitCallback(ITransmitCallback cb);

    /*
     * Unregister ITransmitCallback for startTransmitting()
     *
     * Return: RESPONSE_NULL_CALLBACK, RESPONSE_ALL_OK
     */
    int unregisterTransmitCallback(ITransmitCallback cb);

     /*
      * Receive IR
      *
      * timeoutSeconds: when to stop receiving
      */
    int startReceiving(int timeoutSeconds);

    /*
     * Cancel receiving IR. Okay to call even when timed out in startReceiving
     */
    int stopReceiving();

    /*
     * Register IReceiveCallback for startReceiving()
     *
     * Return: RESPONSE_NULL_CALLBACK, RESPONSE_ALL_OK
     */
    int registerReceiveCallback(IReceiveCallback cb);

    /*
     * Unregister IReceiveCallback for startReceiving()
     *
     * Return: RESPONSE_NULL_CALLBACK, RESPONSE_ALL_OK
     */
    int unregisterReceiveCallback(IReceiveCallback cb);

    int startPwm(int frequency, int dutyCycle, int ledType);
    int stopPwm();
    int updatePwm(int frequency, int dutyCycle, int ledType);
    int startAdc();
    int stopAdc();
    AdcDataEntryParcelable[] readAdc();
    int enableLedSystem();
    int disableLedSystem();
    int setLedOn(int ledType);
    int setLedOff(int ledType);
    int setLedIntensity(int ledType, int intensity);
    int startProgramLeds(int ledType1, int time1, int ledType2, int time2,
            int ledType3, int time3, int ledType4, int time4);
    int stopProgramLeds();
}