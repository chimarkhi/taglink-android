package com.tagbox.taglink;

/**
 * Created by Suhas on 11/19/2016.
 */

public final class Constants {

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;

    public static final int FOREGROUND_SERVICE_ID = 101;

    public static final String PACKAGE_NAME =
            "com.tagbox.tagboxandroidgateway";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String RESULT_ADDRESS = PACKAGE_NAME + ".RESULT_ADDRESS";
    public static final String LOCATION_LATITUDE_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_LATITUDE_DATA_EXTRA";
    public static final String LOCATION_LONGITUDE_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_LONGITUDE_DATA_EXTRA";
    public static final String LOCATION_NAME_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_NAME_DATA_EXTRA";
    public static final String FETCH_TYPE_EXTRA = PACKAGE_NAME + ".FETCH_TYPE_EXTRA";

    public static final String AM_TAG_DATA_EXTRA = "am_tag_data_extra";
    public static final String DOOR_ACT_DATA_EXTRA = "door_act_data_extra";

    public static final int DOOR_SENSE_COUNT = 10;

    public static final int START_ON_REBOOT_DELAY = 10000;      //in milliseconds
    public static final int GATEWAY_MONITORING_APP_CHECK_INTERVAL = 300000; //30 seconds
    public static final int SOCKET_TIMEOUT_MS = 5000;
    public static final int MAX_RETRIES = 2;
    public static final int BACKOFF_MULT = 2;

    public static final String IGRILL_LOG = "TboxIgrillLog.csv";
    public static final String TBOX_APP_LOG = "TBoxAppLog.txt";

    public static final String KEY_GATEWAY_ID = "GwId";
    public static final String KEY_IS_POWERED = "GwIsPwr";
    public static final String KEY_GATEWAY_BATTERY = "GwBat";
    public static final String KEY_GATEWAY_TIMESTAMP = "GwTs";
    public static final String KEY_NETWORK_INFO = "GwNetInfo";
    public static final String KEY_CONNECTED_NETWORK_TYPE = "NetType";
    public static final String KEY_WIFI_SIGNAL_STRENGTH = "dBWifiSig";
    public static final String KEY_MOBILE_SIGNAL_STRENGTH = "dBCellSig";
    public static final String KEY_DATA_HEADER = "Data";
    public static final String KEY_TEMPERATURE_DATA_HEADER = "TempData";
    public static final String KEY_HUMIDITY_DATA_HEADER = "HumData";
    public static final String KEY_LOCATION_DATA_HEADER = "LocData";
    public static final String KEY_DOOR_ACTIVITY_DATA_HEADER = "DoorActData";
    public static final String KEY_NODE_ID = "NdId";
    public static final String KEY_NODE_TIMESTAMP = "NdTs";
    public static final String KEY_NODE_BATTERY = "NdBat";
    public static final String KEY_NODE_TEMPERATURE = "Temp";
    public static final String KEY_NODE_HUMIDITY = "Hum";
    public static final String KEY_NODE_SIGNAL_STRENGTH = "NdRssi";

    public static final String KEY_LATITUDE = "Lat";
    public static final String KEY_LONGITUDE = "Long";
    public static final String KEY_DOOR_STATUS = "DoorSts";
    public static final String KEY_EXCEPTION_DATA = "ExData";
    public static final String KEY_EXCEPTION_TYPE = "ExType";
    public static final String KEY_EXCEPTION_MESSAGE = "ExMsg";

    public static final String BT_SCAN_MSG = "BluetoothScanChange";
    public static final String QTAG_ADV = "QtagAdv";
    public static final String QTAG_ALERT = "QtagAlert";
    public static final String QTAG_ADV_EXTRA = "QtagAdvExtra";
    public static final String QTAG_ADDR = "QtagAddr";
    public static final String QTAG_LIST_SAVED = "QtagListSaved";
    public static final String QTAG_ADV_LIST = "QtagAdvList";
    public static final String QTAG_ADV_LIST_EXTRA = "QtagAdvListExtra";
    public static final String NOTIFICATION_MSG = "GatewayServiceMessage";
    public static final String TAGADDRESS_MSG = "AddressMessage";
    public static final String SERVICE_STOP_MSG = "ServiceStopMessage";
    public static final String HUMIDITY_MSG = "HumidityMessage";
    public static final String OUTPUT_MSG = "GatewayServiceOutputMessage";
    public static final String AM_TAG_DATA = "AmTagData";
    public static final String BLE_CONNECTION_STATUS = "BleConnectionStatus";
    public static final String BLE_GATT_READ_CHARACTERISTIC = "BleReadCharacteristic";
    public static final String TAG_ADVERTIMENT_COUNT = "JaaleeAdvertisementCount";
    public static final String PERIPHERAL_STATUS = "PeripheralStatus";
    public static final String PERIPHERAL_BT_STATUS = "PeripheralBtStatus";
    public static final String PERIPHERAL_WIFI_STATUS = "PeripheralWifiStatus";
    public static final String PERIPHERAL_GPRS_STATUS = "PeripheralGprsStatus";
    public static final String INTERNET_CONNECTIVITY_STATUS = "InternetConnectivityStatus";
    public static final String LOCATION_DATA = "LocationData";
    public static final String LOCATION_DATA_LAT = "LocationDataLatitude";
    public static final String LOCATION_DATA_LONG = "LocationDataLongitude";
    public static final String CLOSE_APP = "StopTagboxGateway";
    public static final String FLAG_SEND_DATA_CLOUD = "FlagPostDataToCloud";
    public static final String DOOR_ACT_MSG = "DoorActivityMessage";

    public static final String ACTION_ON_EXCEPTIOM = "com.tagbox.onexception";

    public static final String GATEWAY_MONITOR_PACKAGE_NAME = "com.tagbox.androidgatewaymonitor";

    public static final long SCAN_WINDOW = 300000;

    public static final String DOOR_STATUS_SERVICE_UUID = "CDAB";

    public static final String QTAG_UUID = "04AB";
    public static final String COMPANY_IDENTIFIER = "8B12";
    public static final String UNIXTIME_SERVICE_UUID = "01AB";
    public static final String HUMIDITY_SERVICE_UUID = "02AB";
    public static final String RECKEY_SERVICE_UUID = "03AB";
    public static final String BLE_UUID_HEALTH_THERMOMETER_SERVICE = "0918";

    public static final String QTAG_DATA_END = "FFFF";
}
