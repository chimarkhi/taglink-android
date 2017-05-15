package com.tagbox.taglink;

import org.json.JSONObject;

/**
 * Created by Suhas on 12/1/2016.
 */

public class GatewayInfo {
    private String gatewayId;
    private boolean isCharging;
    private int batLevel;
    private JSONObject networkInfo;
    //private int signalStrengthDm;

    public GatewayInfo(){}
    /*public GatewayInfo(String id, int batLevel, int signalStrengthDm) {
        this.gatewayId = id;
        this.batLevel = batLevel;
        this.networkInfo = signalStrengthDm;
    }*/

    /*********** Get Methods ******************/

    public String getGatewayId()
    {
        return this.gatewayId;
    }
    public int getBatLevel()
    {
        return this.batLevel;
    }
    public boolean getIsCharging(){return this.isCharging; }
    public JSONObject getNetworkInfo()
    {
        return this.networkInfo;
    }

    /*********** Set Methods ******************/
    public void setGatewayId(String id)
    {
        this.gatewayId = id;
    }
    public void setBatLevel(int batLevel)
    {
        this.batLevel = batLevel;
    }
    public void setIsCharging(boolean isCharging)
    {
        this.isCharging = isCharging;
    }
    public void setNetworkInfo(JSONObject networkInfo) {this.networkInfo = networkInfo;}

}
