package com.closeli.remoteTools;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by piaovalentin on 2017/4/6.
 */

public class CLNetworkData implements Parcelable{

    public static final String PASS_DATA_KEY = "userData";

    public String title;
    public int peerId;

    public CLNetworkData() {

    }

    protected CLNetworkData(Parcel in) {
        title = in.readString();
        peerId = in.readInt();
    }

    public static final Creator<CLNetworkData> CREATOR = new Creator<CLNetworkData>() {
        @Override
        public CLNetworkData createFromParcel(Parcel in) {
            return new CLNetworkData(in);
        }

        @Override
        public CLNetworkData[] newArray(int size) {
            return new CLNetworkData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeInt(peerId);
    }
}
