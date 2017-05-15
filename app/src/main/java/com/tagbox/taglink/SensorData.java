package com.tagbox.taglink;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Suhas on 10/23/2016.
 */

public class SensorData implements Parcelable{

    //private variables
    String _devAddress;
    String _tempData;
    String _humidityData;
    String _dateTime;
    String _rssi;
    String _battery;

    // Empty constructor
    public SensorData(){

    }
    // constructor
    /*public SensorData(int id, String devAddress, String tempData, String humidityData, String dateTime){
        this._devAddress = devAddress;
        this._tempData = tempData;
        this._humidityData = humidityData;
        this._dateTime = dateTime;
    }*/

    // constructor
    public SensorData(String tempData, String humidityData){
        this._tempData = tempData;
        this._humidityData = humidityData;
    }

    public SensorData(String tempData, String humidityData, String batteryLevel){
        this._tempData = tempData;
        this._humidityData = humidityData;
        this._battery = batteryLevel;
    }

    public SensorData(Parcel source) {
        this._devAddress = source.readString();
        this._tempData = source.readString();
        this._humidityData = source.readString();
        this._dateTime = source.readString();
        this._rssi = source.readString();
        this._battery = source.readString();
    }

    // getting device address
    public String getDeviceAddress(){
        return this._devAddress;
    }

    // setting device address
    public void setDeviceAddress(String devAddress){
        this._devAddress = devAddress;
    }

    // getting temperature data
    public String getTempData(){
        return this._tempData;
    }

    // setting temperature data
    public void setTempData(String tempData){
        this._tempData = tempData;
    }

    // getting humididty data
    public String getHumidityData(){
        return this._humidityData;
    }

    // setting humidity data
    public void setHumidityData(String humidityData){
        this._humidityData = humidityData;
    }

    // getting humididty data
    public String getTimestamp(){
        return this._dateTime;
    }

    // setting humidity data
    public void setTimestamp(String timestamp){
        this._dateTime = timestamp;
    }


    public String getRssi(){
        return this._rssi;
    }

    // setting humidity data
    public void setRssi(String rssi){
        this._rssi = rssi;
    }

    public String getBatteryLevel(){
        return this._battery;
    }

    // setting humidity data
    public void setBatteryLevel(String batteryLevel){
        this._battery = batteryLevel;
    }

    public int describeContents() {
        return this.hashCode();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_devAddress);
        dest.writeString(_tempData);
        dest.writeString(_humidityData);
        dest.writeString(_dateTime);
        dest.writeString(_rssi);
        dest.writeString(_battery);
    }

    public static final Creator CREATOR
            = new Creator() {
        public SensorData createFromParcel(Parcel in) {
            return new SensorData(in);
        }

        public SensorData[] newArray(int size) {
            return new SensorData[size];
        }
    };
}