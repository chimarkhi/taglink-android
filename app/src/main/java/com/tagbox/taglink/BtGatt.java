//package com.tagbox.taglink;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothProfile;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Handler;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//
//import java.util.List;
//
//import static com.tagbox.tagboxandroidgateway.Constants.BLE_CONNECTION_STATUS;
//import static com.tagbox.tagboxandroidgateway.Constants.BLE_GATT_READ_CHARACTERISTIC;
//
///**
// * Created by Suhas on 10/31/2016.
// */
//
//public class BtGatt {
///**
// * Class for managing connection and data communication with a GATT server hosted on a
// * given Bluetooth LE device.
// */
//    Context mContext;
//    private final static String TAG = BtGatt.class.getSimpleName();
//
//    //private List<IBluetoothData> listeners = new ArrayList<IBluetoothData>();
//
//    private BluetoothManager mBluetoothManager;
//    private BluetoothAdapter mBluetoothAdapter;
//    private String mBluetoothDeviceAddress;
//    private BluetoothGatt mBluetoothGatt;
//    private int mConnectionState = STATE_DISCONNECTED;
//    private Handler mHandler;
//
//    private static final int STATE_DISCONNECTED = 0;
//    private static final int STATE_CONNECTING = 1;
//    private static final int STATE_CONNECTED = 2;
//
//    public final static String ACTION_GATT_CONNECTED =
//            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
//    public final static String ACTION_GATT_DISCONNECTED =
//            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
//    public final static String ACTION_GATT_SERVICES_DISCOVERED =
//            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
//    public final static String ACTION_DATA_AVAILABLE =
//            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
//    public final static String ACTION_STATE_CONNECTING =
//            "State Connecting";
//    public final static String ACTION_DATA_READ =
//            "com.example.bluetooth.le.ACTION_DATA_READ";
//    public final static String EXTRA_DATA =
//            "com.example.bluetooth.le.EXTRA_DATA";
//
//    // Implements callback methods for GATT events that the app cares about.  For example,
//    // connection change and services discovered.
//    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            final String intentAction;
//            final String devAddress = gatt.getDevice().getAddress();
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                intentAction = ACTION_GATT_CONNECTED;
//                mConnectionState = STATE_CONNECTED;
//                // Do something after 5s = 5000ms
//                notifyGattStatusChange(devAddress, intentAction);
//
//                /*Utils.delay(1, new Utils.DelayCallback() {
//                    @Override
//                    public void afterDelay() {
//
//                    }
//                });*/
//
//                //mBluetoothGatt.discoverServices();
//
//                Log.i(TAG, "Connected to GATT server.");
//                // Attempts to discover services after successful connection.
//                /*Log.i(TAG, "Attempting to start service discovery:" +
//                        mBluetoothGatt.discoverServices());*/
//
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                intentAction = ACTION_GATT_DISCONNECTED;
//                mConnectionState = STATE_DISCONNECTED;
//                Log.i(TAG, "Disconnected from GATT server.");
//                notifyGattStatusChange(devAddress, intentAction);
//            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            String intentAction;
//            String devAddress = gatt.getDevice().getAddress();
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                intentAction = ACTION_GATT_SERVICES_DISCOVERED;
//                notifyGattStatusChange(devAddress, intentAction);
//                //List<BluetoothGattService> services = gatt.getServices();
//                //Log.i("onServicesDiscovered", services.toString());
//                //gatt.readCharacteristic(services.get(1).getCharacteristics().get
//            } else {
//                Log.w(TAG, "onServicesDiscovered received: " + status);
//            }
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt,
//                                         BluetoothGattCharacteristic characteristic,
//                                         int status) {
//            String devAddress = gatt.getDevice().getAddress();
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                notifyCharacteristicRead(devAddress, characteristic.getUuid().toString(), characteristic.getValue());
//                Log.i("onCharacteristicRead", characteristic.toString());
//            }
//        }
//
///*        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt,
//                                            BluetoothGattCharacteristic characteristic) {
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//        }*/
//    };
//
//    void notifyCharacteristicRead(final String address, final String uuid, final byte[] data){
//        /*for(IBluetoothData listener : listeners){
//            listener.setCharacteristicRead(address, uuid, data);
//        }*/
//        Intent intent = new Intent(BLE_GATT_READ_CHARACTERISTIC);
//        intent.putExtra("DeviceAddress", address);
//        intent.putExtra("Uuid", uuid);
//        intent.putExtra("Value", data);
//        LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(intent);
//    }
//
//    void notifyGattStatusChange(String address, String action){
//        /*for(IBluetoothData listener : listeners){
//            listener.setGattStatus(action);
//        }*/
//        Intent intent = new Intent(BLE_CONNECTION_STATUS);
//        intent.putExtra("DeviceAddress", address);
//        intent.putExtra("Status", action);
//        LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(intent);
//    }
//
//    public BtGatt(Context context){
//        this.mContext = context;
//        // For API level 18 and above, get a reference to BluetoothAdapter through
//        // BluetoothManager.
//        if (mBluetoothManager == null) {
//            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
//            if (mBluetoothManager == null) {
//                Log.e(TAG, "Unable to initialize BluetoothManager.");
//            }
//        }
//
//        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        if (mBluetoothAdapter == null) {
//            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
//        }
//    }
//
//    /**
//     * Connects to the GATT server hosted on the Bluetooth LE device.
//     *
//     * @param address The device address of the destination device.
//     *
//     * @return Return true if the connection is initiated successfully. The connection result
//     *         is reported asynchronously through the
//     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
//     *         callback.
//     */
//    public boolean connect(final String address) {
//        if (mBluetoothAdapter == null || address == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
//            return false;
//        }
//
//        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                mConnectionState = STATE_CONNECTING;
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        if (device == null) {
//            Log.w(TAG, "Device not found.  Unable to connect.");
//            return false;
//        }
//        Log.d(TAG, "Trying to create a new connection.");
//        mBluetoothDeviceAddress = address;
//        mConnectionState = STATE_CONNECTING;
//        notifyGattStatusChange(mBluetoothDeviceAddress, ACTION_STATE_CONNECTING);
//
//        // We want to directly connect to the device, so we are setting the autoConnect
//        // parameter to false.
//        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
//
//        return true;
//    }
//
//    /**
//     * Disconnects an existing connection or cancel a pending connection. The disconnection result
//     * is reported asynchronously through the
//     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
//     * callback.
//     */
//    public void disconnect() {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.disconnect();
//    }
//
//    /**
//     * After using a given BLE device, the app must call this method to ensure resources are
//     * released properly.
//     */
//    public void close() {
//        if (mBluetoothGatt == null) {
//            return;
//        }
//        mBluetoothGatt.close();
//        mBluetoothGatt = null;
//    }
//
//    /**
//     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
//     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
//     * callback.
//     *
//     * @param characteristic The characteristic to read from.
//     */
//    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.readCharacteristic(characteristic);
//    }
//
//    /**
//     * Enables or disables notification on a give characteristic.
//     *
//     * @param characteristic Characteristic to act on.
//     * @param enabled If true, enable notification.  False otherwise.
//     */
//    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
//                                              boolean enabled) {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//
//        // This is specific to Heart Rate Measurement.
// /*       if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }*/
//    }
//
//    public void discoverServices() {
//        if (mBluetoothGatt == null) return;
//
//        mBluetoothGatt.discoverServices();
//    }
//
//    /**
//     * Retrieves a list of supported GATT services on the connected device. This should be
//     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
//     *
//     * @return A {@code List} of supported services.
//     */
//    public List<BluetoothGattService> getSupportedGattServices() {
//        if (mBluetoothGatt == null) return null;
//
//        return mBluetoothGatt.getServices();
//    }
//}
//