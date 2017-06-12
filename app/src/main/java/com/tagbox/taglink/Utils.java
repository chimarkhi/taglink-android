package com.tagbox.taglink;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by Suhas on 10/25/2016.
 */

public class Utils {
    // Delay mechanism

    public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    public interface DelayCallback{
        void afterDelay();
    }

    public static void delay(int secs, final DelayCallback delayCallback){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                delayCallback.afterDelay();
            }
        }, secs * 1000); // afterDelay will be executed after (secs*1000) milliseconds.
    }

    public static void sendLocalBroadcast(Context mContext, String msgTitle, String message){
        Intent intent = new Intent(msgTitle);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public static void sendLocalBroadcast(Context mContext, String msgTitle, long message){
        Intent intent = new Intent(msgTitle);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public static void sendLocalBroadcast(Context mContext, String msgTitle, String contentTitle, String message){
        Intent intent = new Intent(msgTitle);
        intent.putExtra(contentTitle, message);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public static String convertByteArraytoString(byte[] packet) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : packet) {
            builder.append(String.format("%02x", b));
        }
        String answer = builder.toString();
        return answer;
    }

    public static String intToAscii(int value) {
        int length = 2;
        StringBuilder builder = new StringBuilder(length);
        for (int i = length - 1; i >= 0; i--) {
            builder.append((char) ((value >> (8 * i)) & 0xFF));
        }
        return builder.toString();
    }

    public static byte[] intToAsciiByteArray(int value) {
        byte[] result = new byte[2];

        result[1] = (byte) ((value >> 8) & 0xFF);
        result[0] = (byte)((value) & 0xFF);

        return result;
    }

    public static <C> List<C> ConvertSparseArrayToList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<C>(sparseArray.size());

        for (int i = 0; i < sparseArray.size(); i++)
            arrayList.add(sparseArray.valueAt(i));
        return arrayList;
    }

    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    public static List<BleDevice> getBondedDevices(Context mContext){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        ArrayList<BleDevice> bondedDevices = new ArrayList<BleDevice>();
        for(BluetoothDevice bt : pairedDevices) {
            BleDevice currDevice = new BleDevice(bt.getAddress(),bt.getBondState(),0);
            currDevice.setFriendlyName(bt.getName());
            bondedDevices.add(currDevice);
        }
        return bondedDevices;
    }

    /*public static Boolean isAppRunning(Context mContext, String appName){
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService( ACTIVITY_SERVICE );
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if(procInfos != null){
            for(int i = 0; i < procInfos.size(); i++) {
                if(procInfos.get(i).processName.equals(appName)) {
                    return true;
                }
            }
        }
        return false;
    }*/

    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.process.equals(packageName)){
                return true;
            }
        }
        return false;
    }

    public static void startApplication(Context context, String packageName){
        PackageManager pm = context.getPackageManager();
        Intent appStartIntent = pm.getLaunchIntentForPackage(packageName);
        if (null != appStartIntent)
        {
            appStartIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(appStartIntent);
        }
    }

    public static boolean isServiceRunning(Context mContext, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getUtcDatetimeAsString()
    {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String utcTime = sdf.format(new Date());

        return utcTime;
    }

    public static String getDeviceName(){
        String name = android.os.Build.MANUFACTURER + android.os.Build.MODEL;
        if(name == null){
            return "";  //empty string
        }
        else {
            return name;
        }
    }

    public static String getDateTimeFromUnixTimestamp(long timestamp) {
        long unixTimestamp = Long.valueOf(timestamp)*1000;
        Date df = new java.util.Date(unixTimestamp);
        String dateTime = new SimpleDateFormat("dd/MM/yy hh:mm a").format(df);
        return dateTime;
    }

    public static String getUtcDatetimeFromUnixTimestamp(long timestamp)
    {
        long unixTimestamp = Long.valueOf(timestamp)*1000;
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date df = new java.util.Date(unixTimestamp);
        String utcTime = sdf.format(df);

        return utcTime;
    }

    public static String getISO8601StringForDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

    public static boolean hasPermission(Context context, String permission) {

        int res = context.checkCallingOrSelfPermission(permission);

        return res == PackageManager.PERMISSION_GRANTED;

    }

    /** Determines if the context calling has the required permissions
     * @param context - the IPC context
     * @param permissions - The permissions to check
     * @return true if the IPC has the granted permission
     */
    public static boolean hasPermissions(Context context, String... permissions) {

        boolean hasAllPermissions = true;

        for(String permission : permissions) {
            //return false instead of assigning, but with this you can log all permission values
            if (! hasPermission(context, permission)) {hasAllPermissions = false; }
        }

        return hasAllPermissions;
    }
}
