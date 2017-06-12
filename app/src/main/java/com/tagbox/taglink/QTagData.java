package com.tagbox.taglink;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Suhas on 4/13/2017.
 */

public class QTagData implements Parcelable {
    private String address;
    private String friendlyName;
    private int rssi;
    private long unixTimestamp, recordKey, ticks;
    private Float temp, humidity;
    private Integer breach;
    private String status;

    public QTagData(String address) {
        this.address = address;
        this.friendlyName = "";
        this.rssi = 0;
        //this.timestamp = 0;
        this.temp = 0f;
        this.humidity = 0f;
        this.ticks = 0;
    }

    public QTagData(String address, int rssi) {
        this.address = address;
        this.friendlyName = "";
        this.rssi = rssi;
        //this.timestamp = 0;
        this.temp = 0f;
        this.humidity = 0f;
        this.ticks = 0;
    }

    public QTagData(long ticks, Float temp, Float humidity, long recordKey) {
        this.address = address;
        this.friendlyName = friendlyName;
        this.rssi = rssi;
        //this.timestamp = timestamp;
        this.ticks = ticks;
        this.temp = temp;
        this.humidity = humidity;
        this.recordKey = recordKey;
    }

    public QTagData(String address, String friendlyName, int rssi, long timestamp, float temp, float humidity) {
        this.address = address;
        this.friendlyName = friendlyName;
        this.rssi = rssi;
        //this.timestamp = timestamp;
        this.temp = temp;
        this.humidity = humidity;
    }

    public QTagData(Parcel source) {
        address = source.readString();
        friendlyName = source.readString();
        rssi = source.readInt();
        unixTimestamp = source.readLong();
        temp = source.readFloat();
        humidity = source.readFloat();
        recordKey = source.readLong();
        ticks = source.readLong();
        breach = source.readInt();
        status = source.readString();
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

    public String getTemperature() {
        if(temp == null) {
            return null;
        } else {
            return Float.toString(temp);
        }
    }

    public void setTemperature(Float temperature) {
        this.temp = temperature;
    }

    public Float getTemperatureFl() {
        if(temp == null) {
            return null;
        } else {
            return temp;
        }
    }

    public String getHumidity() {
        if(humidity == null) {
            return null;
        } else {
            return Float.toString(humidity);
        }
    }

    public Float getHumidityFl() {
        if(humidity == null) {
            return null;
        } else {
            return humidity;
        }
    }

    public String getStatus() {
        if(this.status == null) {
            return null;
        } else {
            return this.status;
        }
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public long getTicks() {
        return this.ticks;
    }


    public long getUnixTimestamp() {
        return this.unixTimestamp;
    }

    public Integer getBreach() {
        return this.breach;
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

    public void setUnixTimestamp(long timestamp) {
        this.unixTimestamp = timestamp;
    }

    public void setTicks(int rssi)
    {
        this.ticks = ticks;
    }

    public void setBreach(int breach) {
        this.breach = breach;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int describeContents() {
        return this.hashCode();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(friendlyName);
        dest.writeInt(rssi);
        dest.writeLong(unixTimestamp);
        dest.writeFloat(temp);
        dest.writeFloat(humidity);
        dest.writeLong(recordKey);
        dest.writeLong(ticks);
        dest.writeInt(breach);
        dest.writeString(status);
    }

    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public QTagData createFromParcel(Parcel in) {
            return new QTagData(in);
        }

        public QTagData[] newArray(int size) {
            return new QTagData[size];
        }
    };
}
