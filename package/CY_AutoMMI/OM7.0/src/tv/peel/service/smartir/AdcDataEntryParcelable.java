// Copyright (c) 2015 Peel Technologies Inc. All Rights Reserved.
package tv.peel.service.smartir;

import android.os.Parcel;
import android.os.Parcelable;

public class AdcDataEntryParcelable implements Parcelable {
    private long timeStamp;
    private int isIrOn;
    private int isRedOn;
    private int adcValue;

    public AdcDataEntryParcelable(long timeStamp, int isIrOn, int isRedOn, int adcValue) {
        this.timeStamp = timeStamp;
        this.isIrOn = isIrOn;
        this.isRedOn = isRedOn;
        this.adcValue = adcValue;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean isIrOn() {
        return isIrOn == 1;
    }

    public boolean isRedOn() {
        return isRedOn == 1;
    }

    public int getAdcValue() {
        return adcValue;
    }

    public String toString() {
        return "[timeStamp = " + timeStamp + ", isIrOn = "+ isIrOn + ", isRedOn = "+ isRedOn + ", adcValue = " + adcValue +"]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(timeStamp);
        out.writeInt(isIrOn);
        out.writeInt(isRedOn);
        out.writeInt(adcValue);
    }

    public static final Creator<AdcDataEntryParcelable> CREATOR = new Creator<AdcDataEntryParcelable>() {
        @Override
        public AdcDataEntryParcelable createFromParcel(Parcel source) {
            return new AdcDataEntryParcelable(source);
        }

        @Override
        public AdcDataEntryParcelable[] newArray(int size) {
            return new AdcDataEntryParcelable[size];
        }
    };

    public AdcDataEntryParcelable(Parcel in) {
        timeStamp = in.readLong();
        isIrOn = in.readInt();
        isRedOn = in.readInt();
        adcValue = in.readInt();
    }
}
