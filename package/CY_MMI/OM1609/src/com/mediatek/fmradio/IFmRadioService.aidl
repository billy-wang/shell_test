package com.mediatek.fmradio;
import com.mediatek.fmradio.IFmRadioServiceCallback;

/**
 * Created by kevanchik on 2/9/2016.
 */
interface IFmRadioService {

    boolean isPowerUp();
    void tuneDirection (int direction);
    void tuneStationAsync(float frequency);
    void initService(int iCurrentStation);
    boolean isAntennaAvailable();
    boolean stopScan();
    void setMuteAsync(boolean mute);
    void seekStationAsync(float frequency, boolean isUp);
    int getFrequency();
    int[] getHardwareVersion();
    boolean isServiceInited();
    boolean isModeNormal();
    boolean isDeviceOpen();
    void powerUpAsync(float frequency);
    void powerDownAsync();
    void registerCallback(IFmRadioServiceCallback cb);
    void setSpeakerPhoneOn(boolean isSpeaker);
}
