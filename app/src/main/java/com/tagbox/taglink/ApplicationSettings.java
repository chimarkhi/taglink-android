package com.tagbox.taglink;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Suhas on 10/24/2016.
 */

public class ApplicationSettings {

    public static String STRING_LOGIN_USERNAME = "user_name";
    public static String STRING_LOGIN_PASSWORD = "password";

    public static String LONG_LAST_LOGIN = "last_login";
    public static String LONG_LAST_SCAN = "last_scan";

    public static String LAST_KNOWN_LOCATION = "last_known_location";   //value is a comma separated lat/long

    public static String TAG_WHITELIST = "tag_whitelist";

    public static String LAST_KNOWN_EXCEPTION = "last_known_exception";

    public static long DEFAULT_LAST_LOGIN = 0;
    public static long DEFAULT_LAST_SCAN = 0;

    public static String DEFAULT_LAST_KNOWN_LOCATION = "";

    public final static String APP_VERSION = "TagLink 1.0.0";

    public final static String GATEWAY_ID =
            //"biocon_poc_1";
            "dev_biocon_1";
    public final static String IOTHUB_SAS_KEY =
            "SharedAccessSignature sr=sub-som-hub.azure-devices.net%2Fdevices%2Fdev_biocon_1&sig=gWpQDat1wtunTLzoz0cdnrkfoZnZdpoJOIzxyn7jo6Y%3D&se=1529391614";
            //"SharedAccessSignature sr=tbox-hub-bcn.azure-devices.net%2Fdevices%2Fbiocon_poc_1&sig=Ygykoh4UeNgZIgc42OmDv2INrW1y1XSM%2FRR3tKVwawk%3D&se=1528867659";

    public final static String IOTHUB_HOST =
            "sub-som-hub.azure-devices.net";
            //"tbox-hub-bcn.azure-devices.net";

    private final static String IP_ADDRESS =
            "104.215.248.40";
            //"52.187.24.249";

    public final static String LOGIN_URL =
                    "http://" + IP_ADDRESS + ":8080/restservice/v1/d2c/login";

    public final static String EXCEPTION_URL =
                    "http://" + IP_ADDRESS + ":8080/restservice/v1/d2c/exception";


    public static final String APP_PREFERENCES = "ApplicationPreference" ;
    public static SharedPreferences appSettings;

    public ApplicationSettings(Context context){
        appSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        populateAppSettings();
    }

    public static Long getAppSetting(String key) {
        return appSettings.getLong(key, 0);
    }

    public static String getAppSettingString(String key) {
        return appSettings.getString(key, "");
    }

    public boolean getAppSettingBoolean(String key) {
        return appSettings.getBoolean(key, false);
    }

    public void setAppSetting(String key, Long value) {
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public void setAppSetting(String key, String value) {
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void setAppSetting(String key, boolean value) {
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void deleteAppSetting(String key) {
        SharedPreferences.Editor editor = appSettings.edit();
        editor.remove(key);
        editor.commit();
    }

    public Boolean containsKey(String key) {
        return appSettings.contains(key);
    }

    private void populateAppSettings(){

        if(!appSettings.contains(ApplicationSettings.STRING_LOGIN_USERNAME)){
            setAppSetting(ApplicationSettings.STRING_LOGIN_USERNAME, "");
        }

        if(!appSettings.contains(ApplicationSettings.LONG_LAST_LOGIN)){
            setAppSetting(ApplicationSettings.LONG_LAST_LOGIN, DEFAULT_LAST_LOGIN);
        }

        if(!appSettings.contains(ApplicationSettings.LONG_LAST_SCAN)){
            setAppSetting(ApplicationSettings.LONG_LAST_SCAN, DEFAULT_LAST_SCAN);
        }

        if(!appSettings.contains(ApplicationSettings.LAST_KNOWN_LOCATION)) {
            setAppSetting(ApplicationSettings.LAST_KNOWN_LOCATION, DEFAULT_LAST_KNOWN_LOCATION);
        }

        if(!appSettings.contains(ApplicationSettings.TAG_WHITELIST)) {
            setAppSetting(ApplicationSettings.TAG_WHITELIST, "");
        }
    }
}
