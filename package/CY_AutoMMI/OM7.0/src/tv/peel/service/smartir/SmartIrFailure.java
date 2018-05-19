// Copyright (c) 2015 Peel Technologies Inc. All Rights Reserved.
package tv.peel.service.smartir;

import android.os.Parcel;
import android.os.Parcelable;

public class SmartIrFailure implements Parcelable {
    //--------------------------------------------------------------------------------
    // WARNING!!!
    //
    // Number here is pretty much permanent. This is used here for the client and also used
    // in static library that is integrated in Android OS (SmartIr system service, ConsumerIr system service)
    //--------------------------------------------------------------------------------
    public final static int STATUS_TRANSMIT_HAL_PROBLEM = 1;
    public final static int STATUS_TRANSMIT_MODE_UNSUPPORTED = 2;
    public final static int STATUS_TRANSMIT_CANCELED = 3;
    public final static int STATUS_RECEIVE_HAL_PROBLEM = 101;
    public final static int STATUS_RECEIVE_TIMEOUT = 102;
    public final static int STATUS_RECEIVE_CORRUPT_DATA = 103;
    public final static int STATUS_RECEIVE_CANCELED = 104;

    public final static String MSG_TRANSMIT_HAL_PROBLEM = "Transmit HAL problem";
    public final static String MSG_TRANSMIT_MODE_UNSUPPORTED = "Transmit mode unsupported";
    public final static String MSG_TRANSMIT_CANCELED = "Transmit canceled";
    public final static String MSG_RECEIVE_HAL_PROBLEM = "Receiving HAL problem";
    public final static String MSG_RECEIVE_TIMEOUT = "Receiving timeout";
    public final static String MSG_RECEIVE_CORRUPT_DATA = "Receiving corrupt data";
    public final static String MSG_RECEIVE_CANCELED = "Receiving canceled";

    private int statusCode;
    private String message;

    public SmartIrFailure(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(statusCode);
        out.writeString(message);
    }

    public static final Creator<SmartIrFailure> CREATOR = new Creator<SmartIrFailure>() {
        @Override
        public SmartIrFailure createFromParcel(Parcel source) {
            return new SmartIrFailure(source);
        }

        @Override
        public SmartIrFailure[] newArray(int size) {
            return new SmartIrFailure[size];
        }
    };

    public SmartIrFailure(Parcel in) {
        statusCode = in.readInt();
        message = in.readString();
    }

}
