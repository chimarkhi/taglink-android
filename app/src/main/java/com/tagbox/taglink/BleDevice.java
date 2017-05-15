package com.tagbox.taglink;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Suhas on 10/31/2016.
 */

public class BleDevice implements Parcelable {
    public String address;
    public String friendlyName;
    public int rssi;
    public int bondState;

    public BleDevice(String address, int bondState, int rssi) {
        this.address = address;
        this.friendlyName = "";
        this.bondState = bondState;
        this.rssi = rssi;
    }

    public BleDevice(String address, String friendlyName, int bondState, int rssi) {
        this.address = address;
        this.friendlyName = friendlyName;
        this.bondState = bondState;
        this.rssi = rssi;
    }

    public BleDevice(Parcel source) {
        address = source.readString();
        friendlyName = source.readString();
        rssi = source.readInt();
        bondState = source.readInt();
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

    public int getRssi()
    {
        return this.rssi;
    }

    public int getBondState()
    {
        return this.bondState;
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

    public void setRssi(int rssi)
    {
        this.rssi = rssi;
    }

    public void setIsBonded(int bondState){
        this.bondState = bondState;
    }


    public int describeContents() {
        return this.hashCode();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(friendlyName);
        dest.writeInt(rssi);
        dest.writeInt(bondState);
    }

    public static final Creator CREATOR
            = new Creator() {
        public BleDevice createFromParcel(Parcel in) {
            return new BleDevice(in);
        }

        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };
}
