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

    public final static String LOGIN_USERNAME = "user";
    public final static String LOGIN_PASSWORD = "123";

    public static long DEFAULT_LAST_LOGIN = 0;
    public static long DEFAULT_LAST_SCAN = 0;

    public final static String APP_VERSION = "TagLink 0.51";

    public final static String GATEWAY_ID =
            "dev_biocon_1";
    public final static String IOTHUB_SAS_KEY =
            "SharedAccessSignature sr=sub-som-hub.azure-devices.net%2Fdevices%2Fdev_biocon_1&sig=MWAA3KGiBXXmlLrHeCHqiOp4O1eK5maGJRtUv6uZfmo%3D&se=1524071801";
    public final static String IOTHUB_HOST =
            "sub-som-hub.azure-devices.net";
    public final static String C2D_RESTSERVICE_URL =
                    "http://104.215.248.40:8080/restservice/v1/d2c/cmdresponse";

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

    public Boolean containsKey(String key) {
        return appSettings.contains(key);
    }

    private void populateAppSettings(){

        //if(!appSettings.contains(ApplicationSettings.STRING_LOGIN_USERNAME)){
            setAppSetting(ApplicationSettings.STRING_LOGIN_USERNAME, LOGIN_USERNAME);
        //}
        //if(!appSettings.contains(ApplicationSettings.STRING_LOGIN_PASSWORD)){
            setAppSetting(ApplicationSettings.STRING_LOGIN_PASSWORD, LOGIN_PASSWORD);
        //}

        if(!appSettings.contains(ApplicationSettings.LONG_LAST_LOGIN)){
            setAppSetting(ApplicationSettings.LONG_LAST_LOGIN, DEFAULT_LAST_LOGIN);
        }

        if(!appSettings.contains(ApplicationSettings.LONG_LAST_SCAN)){
            setAppSetting(ApplicationSettings.LONG_LAST_SCAN, DEFAULT_LAST_SCAN);
        }
    }
}
