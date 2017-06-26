package com.tagbox.taglink;

/**
 * Created by Suhas on 11/19/2016.
 */

public final class Constants {

    public static final int SOCKET_TIMEOUT_MS = 5000;
    public static final int MAX_RETRIES = 2;
    public static final int BACKOFF_MULT = 2;

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
    public static final String KEY_NODE_ID = "NdId";
    public static final String KEY_NODE_TIMESTAMP = "NdTs";
    public static final String KEY_NODE_BATTERY = "NdBat";
    public static final String KEY_NODE_TEMPERATURE = "Temp";
    public static final String KEY_NODE_HUMIDITY = "Hum";
    public static final String KEY_NODE_SIGNAL_STRENGTH = "NdRssi";

    public static final String BT_SCAN_MSG = "BluetoothScanChange";
    public static final String QTAG_ADV = "QtagAdv";
    public static final String QTAG_ALERT = "QtagAlert";
    public static final String QTAG_ADV_EXTRA = "QtagAdvExtra";
    public static final String QTAG_ADDR = "QtagAddr";
    public static final String QTAG_LIST_SAVED = "QtagListSaved";
    public static final String QTAG_ADV_LIST = "QtagAdvList";
    public static final String QTAG_ADV_LIST_EXTRA = "QtagAdvListExtra";
    public static final String SERVICE_STOP_MSG = "ServiceStopMessage";

    public static final String ACTION_ON_EXCEPTIOM = "com.tagbox.onexception";

    public static final long CONNECT_TAG_INTERVAL_SECONDS = 900;
    public static final long UART_DATA_TIMEOUT_MILLISECONDS = 20000;
    public static final long SCAN_WINDOW = 1800000;

    public static long SESSION_TIME_SECONDS = 1800; //30 minutes

    public static final long NOTIFICATION_UPDATE_WINDOW = 15000;

    public static final String QTAG_UUID = "04AB";
    public static final String COMPANY_IDENTIFIER = "8B12";

    public static final String QTAG_DATA_END = "ffffffffffffffff";
    public static final String QTAG_EMPTY = "0000000000000000";
}
