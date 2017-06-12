package com.tagbox.taglink;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.Tag;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.google.common.primitives.Ints.toByteArray;
import static com.tagbox.taglink.Constants.CONNECT_TAG_INTERVAL_SECONDS;
import static com.tagbox.taglink.Constants.NOTIFICATION_UPDATE_WINDOW;
import static com.tagbox.taglink.Constants.QTAG_ADDR;
import static com.tagbox.taglink.Constants.QTAG_ADV;
import static com.tagbox.taglink.Constants.QTAG_ADV_EXTRA;
import static com.tagbox.taglink.Constants.QTAG_ADV_LIST;
import static com.tagbox.taglink.Constants.QTAG_ADV_LIST_EXTRA;
import static com.tagbox.taglink.Constants.QTAG_ALERT;
import static com.tagbox.taglink.Constants.QTAG_DATA_END;
import static com.tagbox.taglink.Constants.SCAN_WINDOW;
import static com.tagbox.taglink.Constants.SERVICE_STOP_MSG;
import static com.tagbox.taglink.Constants.UART_DATA_TIMEOUT_MILLISECONDS;

public class TagLinkService extends Service {

    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    private Looper mServiceLooper;
    private Handler mServiceHandler;
    HandlerThread mHandlerthread;

    private BtDeviceScan mBtScan;

    private int qTagProcessflag = 0;

    private BluetoothDevice mDevice = null;

    private static final int ONGOING_NOTIFICATION_ID = 1;
    public static final int FOREGROUND_MQTT_SERVICE_ID = 102;
    private Notification.Builder builder;

    private NrfUartService mService = null;
    private int mState = UART_PROFILE_DISCONNECTED;

    private TagLinkProcess currTagLinkProcess;

    private DatabaseHandler mDbHandler;

    private boolean packetReceived = false;
    private Handler mTimerHandler;

    private CloudInterface ci;

    private ArrayList<QTagData> qTagDataList = null;

    private List<String> qTagWhiteList = null;
    private int countdown = 0;

    public TagLinkService() {
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        TagLinkService getService() {
            return TagLinkService.this;
        }
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();


    @Override
    public IBinder onBind(Intent intent) {
        /*// TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");*/
        return mBinder;
    }

    @Override
    public void onCreate() {

        mHandlerthread = new HandlerThread("ServiceStartArguments");
        mHandlerthread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = mHandlerthread.getLooper();
        mServiceHandler = new Handler(mServiceLooper);

        mTimerHandler = new Handler();

        ci = new CloudInterface(TagLinkService.this);
        mDbHandler = new DatabaseHandler(TagLinkService.this);

        setQTagWhitelist();

        startBluetoothService();

        stopTagLinkService();
        registerForLocalBroadcast();

        //checkBackupDataForUpload();

        registerCustomNotification();

        qTagDataList = new ArrayList<>();

        service_init();
    }

    @Override
    public void onDestroy()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.cancelAll();

        if(mBtScan != null){
            //mBtScan.stopBluetooth();
        }

        mHandlerthread.quit();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;

        super.onDestroy();
    }

    private void registerForLocalBroadcast(){
        // This registers mMessageReceiver to receive messages.
        IntentFilter filter = new IntentFilter(QTAG_ADV);
        // This registers mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                filter);
    }

    public void registerCustomNotification(){
        builder  = new Notification.Builder(this)
                .setContentTitle("TagLink Service")
                .setContentText("Started")
                .setSmallIcon(R.drawable.ic_stat_name)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(false);
        Notification n = builder.build();

        startForeground(FOREGROUND_MQTT_SERVICE_ID, n);
    }

    private void updateCustomNotification(String message) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setContentText(message);

        mNotificationManager.notify(FOREGROUND_MQTT_SERVICE_ID, builder.build());
    }

    public void setQTagWhitelist() {

        if(qTagWhiteList == null) {
            qTagWhiteList = new ArrayList<>();
        }

        ApplicationSettings applicationSettings = new ApplicationSettings(TagLinkService.this);
        String whitelist = applicationSettings.getAppSettingString(ApplicationSettings.TAG_WHITELIST);

        if(whitelist != null && whitelist != "") {
            try {
                JSONObject list = new JSONObject(whitelist);

                for(int i = 0; i < list.names().length(); i++) {
                    //JSONObject object = list.getJSONObject(i);
                    String macId = list.names().getString(i);
                    qTagWhiteList.add(macId);
                }
            } catch (Exception ex) {}
        }
    }

    public ArrayList<QTagData> getQTagDataList() {
        return qTagDataList;
    }

    private void initCurrentTaglinkProcess() {
        if(currTagLinkProcess == null) {
            currTagLinkProcess = new TagLinkProcess();
        }
        currTagLinkProcess.recentTimestamp = null;
        currTagLinkProcess.recentTick = null;
        currTagLinkProcess.lastRecordedCounter = 0x2223;
        currTagLinkProcess.breach = 0;
    }

    private void startBluetoothService(){

        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                mBtScan = new BtDeviceScan(TagLinkService.this);
            }
        });
    }

    private void stopTagLinkService(){

        mServiceHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                countdown += NOTIFICATION_UPDATE_WINDOW;
                //some tag sync process is going on. stop the scan only when it is completed
                if(countdown < SCAN_WINDOW || qTagProcessflag == 1) {
                    mServiceHandler.postDelayed(this, NOTIFICATION_UPDATE_WINDOW);

                    /*if(countdown < SCAN_WINDOW) {
                        long interval = (SCAN_WINDOW - countdown)/1000;
                        updateCustomNotification(Long.toString(interval) + " seconds Remaining ..." );
                    } else {
                        updateCustomNotification(Long.toString(NOTIFICATION_UPDATE_WINDOW) + " seconds Remaining ...");
                    }*/

                    return;
                }

                if(mBtScan != null) {
                    mBtScan.stopBluetooth();
                }
                long currUnixTime = System.currentTimeMillis()/1000;
                ApplicationSettings appSettings = new ApplicationSettings(TagLinkService.this);
                appSettings.setAppSetting(ApplicationSettings.LONG_LAST_SCAN, currUnixTime);
                notifyServiceStop();
                TagLinkService.this.stopSelf();
            }
        }, NOTIFICATION_UPDATE_WINDOW);
    }

    /*void checkBackupDataForUpload(){
        mServiceHandler.post(new Runnable() {
            @Override
            public void run() {
                ci.checkPrevMessages();
                //mServiceHandler.postDelayed(this, 60000);
            }
        });
    }*/

    void notifyServiceStop(){
        Intent intent = new Intent(SERVICE_STOP_MSG);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // handler for received Intents for the "my-integer" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent

            if(QTAG_ADV.equals(intent.getAction())){
                QTagData qTagData = intent.getParcelableExtra(QTAG_ADV_EXTRA);
                String deviceAddress = qTagData.getTagAddress();

                int index;

                if(!qTagDataListContains(deviceAddress)){
                    qTagDataList.add(qTagData);
                    index = 0;
                }
                else {
                    index = getQTagDataListIndex(deviceAddress);
                    if(index >= 0) {
                        QTagData currItem = qTagDataList.get(index);
                        currItem.setTemperature(qTagData.getTemperatureFl());
                        currItem.setHumidity(qTagData.getHumidityFl());
                    }
                }

                if(qTagProcessflag == 0 && qTagWhiteList.contains(deviceAddress)) {
                    qTagProcessflag = 1;

                    initCurrentTaglinkProcess();

                    DatabaseHandler db = new DatabaseHandler(TagLinkService.this);
                    TagLogData tagLog = db.getTagLogData(deviceAddress);
                    db.close();

                    if(tagLog != null) {
                        //dont read from node if u have already read from it recently
                        if((qTagData.getUnixTimestamp() - tagLog.uploadTimestamp) < CONNECT_TAG_INTERVAL_SECONDS) {
                            qTagProcessflag = 0;
                            //updateTagStatus(currTagLinkProcess.tagAddress, "Tag data is synced");
                            return;
                        }
                        currTagLinkProcess.lastRecordedCounter = tagLog.lastCounter;
                    }

                    currTagLinkProcess.tagAddress = deviceAddress;
                    currTagLinkProcess.tagFriendlyName = qTagData.getFriendlyName();
                    currTagLinkProcess.recentTimestamp = qTagData.getUnixTimestamp();
                    currTagLinkProcess.recentTick = qTagData.getTicks();

                    //mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    mService.connect(deviceAddress);

                    updateTagStatus(currTagLinkProcess.tagAddress, "Attempting to connect to Tag");

                    //mBtScan.stopBluetoothScan();
                }

                notifyQtagChange();
            }
        }
    };

    void notifyQtagChange() {
        Intent intent = new Intent(QTAG_ADV_LIST);
        intent.putParcelableArrayListExtra(QTAG_ADV_LIST_EXTRA, qTagDataList);
        LocalBroadcastManager.getInstance(TagLinkService.this).sendBroadcast(intent);
    }

    public boolean qTagDataListContains(String bleAddress) {
        for(QTagData o : qTagDataList) {
            if(o != null && o.getTagAddress().equals(bleAddress)) {
                return true;
            }
        }
        return false;
    }

    public int getQTagDataListIndex(String address) {

        if(address == null) {   //invalid input
            return -1;
        }

        for (int i = 0; i < qTagDataList.size(); i++) {
            QTagData data = qTagDataList.get(i);
            String devAddress = data.getTagAddress();
            if (devAddress.equals(address)) {
                return i;
            }
        }
        return -1;
    }

    private void service_init() {
        Intent bindIntent = new Intent(TagLinkService.this, NrfUartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NrfUartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(NrfUartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(NrfUartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(NrfUartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(NrfUartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((NrfUartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                TagLinkService.this.stopSelf();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService.close();
            mService = null;
        }
    };

    private void startPostDataToCloud(){
        GatewayInfo gwInfo = new GatewayInfo();

        String gatewayId = ApplicationSettings.GATEWAY_ID;
        gwInfo.setGatewayId(gatewayId);
        //gwInfo.setBatLevel(batLevel);

        List<JSONObject> messages = MessagingService.formMessages(TagLinkService.this, gwInfo);
        if(messages != null && messages.size() > 0) {
            String message = Integer.toString(messages.size()) + " - Messages to be sent";
            //ci.postDataToCloud(messages);
            updateTagStatus(currTagLinkProcess.tagAddress, "Uploading Data to Cloud");
            Boolean result = ci.postSynchronousToCloud(messages);
            if(result) {
                updateTagStatus(currTagLinkProcess.tagAddress, "Uploading Data to Cloud completed successfully");
            } else {
                updateTagStatus(currTagLinkProcess.tagAddress, "Failed to data to cloud");
            }
        }
    }

    private void startUartTimerHandler() {
        mTimerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mService != null) {
                    if(packetReceived) {
                        packetReceived = false;
                        mTimerHandler.postDelayed(this, UART_DATA_TIMEOUT_MILLISECONDS);
                    } else {
                        closeUartService(true);
                    }
                }
            }
        }, UART_DATA_TIMEOUT_MILLISECONDS);
    }

    private void updateTagStatus(String address, String status) {
        int index = getQTagDataListIndex(address);
        if(index >= 0) {
            qTagDataList.get(index).setStatus(status);
            notifyQtagChange();
        }

        String message;

        if(status == "") {
            message = "Scanning for Tags";
        } else {
            if (qTagDataList.get(index).getFriendlyName() == null
                    || qTagDataList.get(index).getFriendlyName() == "") {
                message = status;
            } else {
                message = qTagDataList.get(index).getFriendlyName() + " - " + status;
            }
        }
        updateCustomNotification(message);
    }

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            Log.d(TAG, intent.toString());

            //*********************//
            if (action.equals(NrfUartService.ACTION_GATT_CONNECTED)) {
                mState = UART_PROFILE_CONNECTED;
            }

            //*********************//
            if (action.equals(NrfUartService.ACTION_GATT_DISCONNECTED)) {
                mState = UART_PROFILE_DISCONNECTED;
                closeUartService(false);
            }

            //*********************//
            if (action.equals(NrfUartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();

                Utils.delay(1, new Utils.DelayCallback() {
                    @Override
                    public void afterDelay() {
                        byte[] value;
                        try {
                            value = Utils.intToAsciiByteArray((currTagLinkProcess.lastRecordedCounter + 1));
                            mService.writeRXCharacteristic(value);
                            startUartTimerHandler();
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(NrfUartService.ACTION_DATA_AVAILABLE)) {
                //first time packetReceived will be false
                if(!packetReceived) {
                    updateTagStatus(currTagLinkProcess.tagAddress, "Retrieving data from Tag");
                }

                packetReceived = true;

                final byte[] txValue = intent.getByteArrayExtra(NrfUartService.EXTRA_DATA);
                Collections.reverse(Bytes.asList(txValue));
                byte[] tempValue;
                String tempHexString;
                int recordKey;
                float temp, humidity;
                long ticks;
                try {
                    String text = new String(txValue, "UTF-8");

                    if(text.contains(QTAG_DATA_END)){
                        mTimerHandler.removeCallbacksAndMessages(null);
                        mServiceHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                closeUartService(true);
                            }
                        });

                        return;
                    }

                    tempValue = Arrays.copyOfRange(txValue, 0, 2);
                    temp = Shorts.fromByteArray(tempValue)/100f;

                    tempValue = Arrays.copyOfRange(txValue, 2, 4);
                    tempHexString = Utils.convertByteArraytoString(tempValue);
                    humidity = Integer.parseInt(tempHexString, 16);

                    tempValue = Arrays.copyOfRange(txValue, 4, 8);
                    tempHexString = Utils.convertByteArraytoString(tempValue);
                    ticks = Long.parseLong(tempHexString, 16);

                    tempValue = Arrays.copyOfRange(txValue, 10, 12);
                    tempHexString = Utils.convertByteArraytoString(tempValue);
                    recordKey = Integer.parseInt(tempHexString, 16);

                    long ts;
                    if((currTagLinkProcess.recentTick - ticks) > 0) {
                        ts = currTagLinkProcess.recentTimestamp - (currTagLinkProcess.recentTick - ticks);
                    } else if((currTagLinkProcess.recentTick - ticks) < 0) {
                        ts = currTagLinkProcess.recentTimestamp + (ticks - currTagLinkProcess.recentTick);
                    } else {
                        ts = currTagLinkProcess.recentTimestamp;
                    }

                    currTagLinkProcess.lastRecordedCounter = recordKey;
                    currTagLinkProcess.lastRecordedTimestamp = ts;

                    if(humidity < 0 || humidity > 150) {
                        //invalid values. so discard and return here itself
                        return;
                    }

                    SensorData sd = new SensorData();
                    sd.setDeviceAddress(currTagLinkProcess.tagAddress);
                    sd.setTempData(Float.toString(temp));
                    sd.setHumidityData(Float.toString(humidity));
                    sd.setTimestamp(Utils.getUtcDatetimeFromUnixTimestamp(ts));

                    mDbHandler.addSenseData(sd);

                    /*String log = Utils.convertByteArraytoString(txValue);
                    Log.d(TAG, log);*/

                    if(temp > 10 || temp < -5) {
                        currTagLinkProcess.breach = 1;
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            }
            //*********************//
            if (action.equals(NrfUartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                Log.d(TAG, "Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }
        }
    };

    private void closeUartService(boolean updateDataFlag) {
        if(updateDataFlag) {
            if (currTagLinkProcess.tagAddress != null && currTagLinkProcess.lastRecordedCounter != null
                    && currTagLinkProcess.lastRecordedTimestamp != null) {
                startPostDataToCloud();
                mDbHandler.addTagLogData(currTagLinkProcess.tagAddress, currTagLinkProcess.tagFriendlyName, currTagLinkProcess.lastRecordedCounter,
                        currTagLinkProcess.lastRecordedTimestamp);
                if(currTagLinkProcess.breach != null) {
                    Intent intent = new Intent(QTAG_ALERT);
                    intent.putExtra(QTAG_ADDR, currTagLinkProcess.tagAddress);
                    intent.putExtra(QTAG_ADV_EXTRA, currTagLinkProcess.breach);
                    LocalBroadcastManager.getInstance(TagLinkService.this).sendBroadcast(intent);

                    int index = getQTagDataListIndex(currTagLinkProcess.tagAddress);
                    qTagDataList.get(index).setBreach(currTagLinkProcess.breach);
                }
            }
        }
        mService.close();
        updateTagStatus(currTagLinkProcess.tagAddress, "");     //process completed for current tag. remove status of tag
        qTagProcessflag = 0;
    }
}