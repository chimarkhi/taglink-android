package com.tagbox.taglink;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONObject;

import java.util.Date;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.tagbox.taglink.ApplicationSettings.EXCEPTION_URL;

/**
 * Created by Suhas on 5/28/2017.
 */

public class ApplicationTaglink extends Application {

    public enum Tag_State {
        UNAVAILABLE, ADVERTISING, SYNCED, BREACHED
    }

    private Thread.UncaughtExceptionHandler defaultUEH;

    // handler listener
    private Thread.UncaughtExceptionHandler _unCaughtExceptionHandler =
            new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {

                    Date now = new Date();
                    String currTime = Utils.getISO8601StringForDate(now);

                    String uniqueDeviceId =
                        Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

                    String stackTrace = Log.getStackTraceString(ex);

                    JSONObject requestData = new JSONObject();
                    try {
                        requestData.put("source", uniqueDeviceId);
                        requestData.put("message", stackTrace);
                        requestData.put("timestamp", currTime);
                    } catch (Exception e) {}

                    ApplicationSettings applicationSettings = new ApplicationSettings(getApplicationContext());

                    applicationSettings.setAppSetting(ApplicationSettings.LAST_KNOWN_EXCEPTION, requestData.toString());

                    defaultUEH.uncaughtException(thread, ex);

                    //System.exit(2);


                    // re-throw critical exception further to the os (important)
                }
            };

    public ApplicationTaglink() {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();

        // setup handler for uncaught exception
        Thread.setDefaultUncaughtExceptionHandler(_unCaughtExceptionHandler);
    }
}
