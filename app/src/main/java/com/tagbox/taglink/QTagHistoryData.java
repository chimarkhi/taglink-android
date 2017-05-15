package com.tagbox.taglink;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Suhas on 5/14/2017.
 */

public class QTagHistoryData {
    public String address, friendlyName;
    private Long uploadTimestamp;

    public QTagHistoryData() {}

    public QTagHistoryData(String address) {
        this.address = address;
        this.friendlyName = "";
    }

    public QTagHistoryData(Parcel source) {
        address = source.readString();
        friendlyName = source.readString();
        uploadTimestamp = source.readLong();
    }

    /*********** Get Methods ******************/

    public String getTagAddress()
    {
        return this.address;
    }

    public String getFriendlyName()
    {
        return this.friendlyName;
    }

    public long getUploadTimestamp() {
        return this.uploadTimestamp;
    }

    /*********** Set Methods ******************/
    public void setTagAddress(String address)
    {
        this.address = address;
    }

    public void setFriendlyName(String friendlyName)
    {
        this.friendlyName = friendlyName;
    }

    public void setUploadTimestamp(long timestamp) {
        this.uploadTimestamp = timestamp;
    }

    public int describeContents() {
        return this.hashCode();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(friendlyName);
        dest.writeLong(uploadTimestamp);
    }

    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public QTagHistoryData createFromParcel(Parcel in) {
            return new QTagHistoryData(in);
        }

        public QTagHistoryData[] newArray(int size) {
            return new QTagHistoryData[size];
        }
    };
}
