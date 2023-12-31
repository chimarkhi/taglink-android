package com.tagbox.taglink;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.tagbox.taglink.Constants.KEY_DATA_HEADER;
import static com.tagbox.taglink.Constants.KEY_GATEWAY_BATTERY;
import static com.tagbox.taglink.Constants.KEY_GATEWAY_ID;
import static com.tagbox.taglink.Constants.KEY_GATEWAY_TIMESTAMP;
import static com.tagbox.taglink.Constants.KEY_HUMIDITY_DATA_HEADER;
import static com.tagbox.taglink.Constants.KEY_IS_POWERED;
import static com.tagbox.taglink.Constants.KEY_NETWORK_INFO;
import static com.tagbox.taglink.Constants.KEY_NODE_BATTERY;
import static com.tagbox.taglink.Constants.KEY_NODE_HUMIDITY;
import static com.tagbox.taglink.Constants.KEY_NODE_ID;
import static com.tagbox.taglink.Constants.KEY_NODE_SIGNAL_STRENGTH;
import static com.tagbox.taglink.Constants.KEY_NODE_TEMPERATURE;
import static com.tagbox.taglink.Constants.KEY_NODE_TIMESTAMP;
import static com.tagbox.taglink.Constants.KEY_TEMPERATURE_DATA_HEADER;

/**
 * Created by Suhas on 11/23/2016.
 */

public class MessagingService {

    public static int MAX_PACKET_LENGTH = 49152;        //48 kB

    public MessagingService(){}

    public static List<JSONObject> formMessages(Context mContext, GatewayInfo gwInfo){
        List<JSONObject> messages = new ArrayList<JSONObject>();
        List<JSONObject> dataPackets = new ArrayList<>();

        DatabaseHandler db = new DatabaseHandler(mContext);
        //long count = db.getSenseDataCount();

        while (true) {
            List<SensorData> sensorData = db.getSenseData(1000);

            if(sensorData.size() <= 0) {
                break;
            }

            List<JSONObject> dataPacket = addDataToNode(sensorData);

            dataPackets.addAll(dataPacket);
        }

        //List<SensorData> sensorData = db.getAllSenseData();

        db.close();

        for(JSONObject dataPacket:dataPackets){
            JSONObject jsonPacket = addGatewayParameters(gwInfo);

            try {
                jsonPacket.put(KEY_DATA_HEADER, dataPacket);
            } catch (Exception e){
                Log.e("{}", e.toString());
            }
            messages.add(jsonPacket);
        }
        return messages;
    }

    private static List<JSONObject> addDataToNode(List<SensorData> sensorData){

        List<JSONObject> dataMessages = new ArrayList<>();

        JSONArray tempArray, humidArray;

        while (sensorData.size() > 0) {

            if (sensorData != null && sensorData.size() > 0) {
                tempArray = new JSONArray();
                humidArray = new JSONArray();
                formSenseData(tempArray, humidArray, sensorData);

                JSONObject dataPacket = new JSONObject();
                try {
                    if (tempArray.length() > 0) {
                        dataPacket.put(KEY_TEMPERATURE_DATA_HEADER, tempArray);
                    }
                    if (humidArray.length() > 0) {
                        dataPacket.put(KEY_HUMIDITY_DATA_HEADER, humidArray);
                    }
                } catch (Exception e) {
                    Log.e("{}", e.toString());
                }
                dataMessages.add(dataPacket);
            }
        }

        return dataMessages;
    }

    private static int formSenseData(JSONArray tempArray, JSONArray humidArray, List<SensorData> sensorData){
        int size = 0;

        JSONObject jObjTemp, jObjHum;
        SensorData currData;

        Iterator<SensorData> i = sensorData.iterator();
        while (i.hasNext()) {
            currData = i.next(); // must be called before you can call i.remove()

            jObjTemp = new JSONObject();
            jObjHum = new JSONObject();
            try {
                jObjTemp.put(KEY_NODE_ID, currData.getDeviceAddress());
                jObjTemp.put(KEY_NODE_TEMPERATURE, currData.getTempData());
                jObjTemp.put(KEY_NODE_TIMESTAMP, currData.getTimestamp());
                jObjTemp.put(KEY_NODE_BATTERY, currData.getBatteryLevel());
                //jObjTemp.put(KEY_NODE_SIGNAL_STRENGTH, currData.getRssi());
            } catch (Exception e){
                Log.e("{}", e.toString());
                continue;
            }
            try {
                jObjHum.put(KEY_NODE_ID, currData.getDeviceAddress());
                jObjHum.put(KEY_NODE_HUMIDITY, currData.getHumidityData());
                jObjHum.put(KEY_NODE_TIMESTAMP, currData.getTimestamp());
                jObjHum.put(KEY_NODE_BATTERY, currData.getBatteryLevel());
                //jObjHum.put(KEY_NODE_SIGNAL_STRENGTH, currData.getRssi());
            } catch (Exception e){
                Log.d("{}", e.toString());
                continue;
            }
            tempArray.put(jObjTemp);
            humidArray.put(jObjHum);
            size += jObjTemp.toString().length();
            size += jObjHum.toString().length();

            i.remove();

            if(size >= MAX_PACKET_LENGTH)
                break;
        }
        return size;
    }

    private static JSONObject addGatewayParameters(GatewayInfo gwInfo){
        JSONObject gwData = new JSONObject();
        String timeStamp = Utils.getUtcDatetimeAsString();
        try {
            gwData.put(KEY_GATEWAY_ID, gwInfo.getGatewayId());
            gwData.put(KEY_GATEWAY_BATTERY, gwInfo.getBatLevel());
            gwData.put(KEY_GATEWAY_TIMESTAMP, timeStamp);
            gwData.put(KEY_IS_POWERED,gwInfo.getIsCharging());
            if(gwInfo.getNetworkInfo() == null) {
                gwData.put(KEY_NETWORK_INFO, JSONObject.NULL);
            } else {
                gwData.put(KEY_NETWORK_INFO, gwInfo.getNetworkInfo());
            }
        } catch (Exception e){
            Log.d("{}", e.toString());
        }
        return gwData;
    }
}
