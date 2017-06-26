package com.tagbox.taglink;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.tagbox.taglink.Constants.COMPANY_IDENTIFIER;
import static com.tagbox.taglink.Constants.QTAG_ADV;
import static com.tagbox.taglink.Constants.QTAG_ADV_EXTRA;
import static com.tagbox.taglink.Constants.QTAG_UUID;

/**
 * Created by Suhas on 10/31/2016.
 */


public class BtDeviceScan {

    Context mContext;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private Handler mCallBackHandler;
    private Looper mLooper;
    HandlerThread mHandlerThread;

    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private List<String> whitelistAM;

    public BtDeviceScan(Context context) {
        this.mContext = context;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        getBluetoothSettings();

        mHandlerThread = new HandlerThread("BtCallbackThread");
        mHandlerThread.start();
        mLooper = mHandlerThread.getLooper();
        mHandler = new Handler(mLooper);
        mCallBackHandler = new Handler(mLooper);

        /*bleList = new ArrayList<>();
        qTagList = new ArrayList<>();*/

        startBluetoothDiscovery();
    }

    private void getBluetoothSettings() {/*
        ApplicationSettings appSettings = new ApplicationSettings(mContext);

        String tags;

        tags = appSettings.getAppSettingString(ApplicationSettings.STRING_TAG_WHITELIST_AM);
        whitelistAM = Arrays.asList(tags.split(";"));*/
    }

    public void startBluetoothScan() {
        scanLeDevice(true);
    }

    public void stopBluetoothScan(){
        if(mLEScanner != null && (mBluetoothAdapter.isEnabled())) {
            mLEScanner.stopScan(mScanCallback);
        }
        if(mHandler != null) {
            mHandler.removeCallbacks(startScan);
            mHandler.removeCallbacks(stopScan);
        }
        //startStopScan = false;
    }

    public void stopBluetooth(){
        stopBluetoothScan();
        //mContext.unregisterReceiver(mReceiver);
        //wait for bluetooth callbacks to be processed
        Utils.delay(1, new Utils.DelayCallback() {
            @Override
            public void afterDelay() {
                // Do something after delay
                mHandlerThread.quit();
            }
        });
    }

    public void startBluetoothDiscovery(){

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        //Wait for some time for bluetooth to be enabled
        Utils.delay(3, new Utils.DelayCallback() {
            @Override
            public void afterDelay() {
                // Do something after delay
                if(mBluetoothAdapter.isEnabled()){
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                            .build();
                    filters = new ArrayList<ScanFilter>();
                    mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    scanLeDevice(true);
                }
            }
        });
    }

    void notifyQTag(QTagData tag) {
        Intent intent = new Intent(QTAG_ADV);
        intent.putExtra(QTAG_ADV_EXTRA, tag);
        LocalBroadcastManager.getInstance(this.mContext).sendBroadcast(intent);
    }

    private Runnable startScan = new Runnable() {
        @Override
        public void run() {
            mLEScanner.startScan(filters, settings, mScanCallback);
            //mHandler.postDelayed(stopScan, scanTagOn);
        }
    };

    private Runnable stopScan = new Runnable() {
        @Override
        public void run() {
            mLEScanner.stopScan(mScanCallback);
            //mHandler.postDelayed(startScan, scanTagOff);
        }
    };

    private void scanLeDevice(final boolean enable) {
        if(enable) {
            //if(!startStopScan) {
                startScan.run();
                //startStopScan = true;
            //}
        }
        else{
            mLEScanner.stopScan(mScanCallback);
            //startStopScan = false;
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            //Log.i("ScanResult - Results", result.toString());
            mCallBackHandler.post(new Runnable() {
                @Override
                public void run() {
                    processScanResult(result);
                }
            });
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            mCallBackHandler.post(new Runnable() {
                @Override
                public void run() {
                    for (ScanResult sr : results) {
                        processScanResult(sr);
                        //Log.i("ScanResult - Results", sr.toString());
                    }
                }
            });
        }

        @Override
        public void onScanFailed(int errorCode) {
            //Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private void processScanResult(ScanResult result){
        long currTime = System.currentTimeMillis()/1000;

        BluetoothDevice btDevice = result.getDevice();
        int rssi = result.getRssi();
        String bleAddress = btDevice.getAddress().toString();
        String friendlyName = result.getScanRecord().getDeviceName();

        byte[] advertisementPacket = result.getScanRecord().getBytes();

        if(isQTag(advertisementPacket)){
            QTagData tagData = parseQTagData(advertisementPacket);
            tagData.getTicks();
            if(tagData != null) {
                if(friendlyName == null){
                    tagData.setFriendlyName("");
                }
                else {
                    tagData.setFriendlyName(friendlyName);
                }
                tagData.setTagAddress(bleAddress);
                tagData.setRssi(rssi);
                tagData.setUnixTimestamp(currTime);

                notifyQTag(tagData);
            }
        }
    }

    private boolean isQTag(byte[] advertisementPacket) {
        boolean result = false;
        int position = 0;

        do {
            byte length = advertisementPacket[position];
            if(length <= 0)
                break;
            byte type = advertisementPacket[position+1];
            byte[] value = Arrays.copyOfRange(advertisementPacket, position + 2, position + length + 1);

            if(type == 22){         //0x16
                byte[] uuid = Arrays.copyOfRange(value, 0, 2);
                String uuidHex = Utils.convertByteArraytoString(uuid);

                String tempHexString;

                if(uuidHex.equalsIgnoreCase(QTAG_UUID)) {
                    byte[] tempValue;
                    tempValue = Arrays.copyOfRange(value, 2, 4);
                    tempHexString = Utils.convertByteArraytoString(tempValue);
                    if(tempHexString.equalsIgnoreCase(COMPANY_IDENTIFIER)) {
                        result = true;
                    }
                }
            }

            if(position >= advertisementPacket.length) {
                break;
            }
            else {
                position += (length + 1);
            }
        } while (true);

        return result;
    }

    private QTagData parseQTagData(byte[] advertisementPacket) {
        QTagData qTagData = null;
        long ticks = -1, recordKey = -1;
        Float temp = null, humidity = null, battery = null;
        int position = 0;
        int longCapacity = Longs.BYTES;

        do {
            byte length = advertisementPacket[position];
            if(length <= 0)
                break;
            byte type = advertisementPacket[position+1];
            byte[] value = Arrays.copyOfRange(advertisementPacket, position + 2, position + length + 1);

            if(type == 22){         //0x16
                byte[] uuid = Arrays.copyOfRange(value, 0, 2);
                String uuidHex = Utils.convertByteArraytoString(uuid);

                String tempHexString;

                if(uuidHex.equalsIgnoreCase(QTAG_UUID)) {
                    byte[] tempValue;
                    tempValue = Arrays.copyOfRange(value, 4, 6);
                    Collections.reverse(Bytes.asList(tempValue));
                    tempHexString = Utils.convertByteArraytoString(tempValue);
                    recordKey = Long.parseLong(tempHexString, 16);

                    tempValue = Arrays.copyOfRange(value, 6, 10);
                    Collections.reverse(Bytes.asList(tempValue));
                    tempHexString = Utils.convertByteArraytoString(tempValue);
                    ticks = Long.parseLong(tempHexString, 16);

                    tempValue = Arrays.copyOfRange(value, 10, 12);
                    Collections.reverse(Bytes.asList(tempValue));
                    temp = Shorts.fromByteArray(tempValue)/100f;

                    /*tempValue = Arrays.copyOfRange(value, 12, 14);
                    Collections.reverse(Bytes.asList(tempValue));*/
                    tempValue = new byte[] { value[13] };
                    tempHexString = Utils.convertByteArraytoString(tempValue);
                    humidity = Long.parseLong(tempHexString, 16)/1f;

                    tempValue = new byte[] { value[12] };
                    tempHexString = Utils.convertByteArraytoString(tempValue);
                    battery = Long.parseLong(tempHexString, 16)/1f;
                }
            }

            if(position >= advertisementPacket.length) {
                break;
            }
            else {
                position += (length + 1);
            }
        } while (true);

        qTagData = new QTagData(ticks, temp, humidity, recordKey);
        return qTagData;
    }
}