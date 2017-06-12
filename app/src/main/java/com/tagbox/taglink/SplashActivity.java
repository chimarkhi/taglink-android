package com.tagbox.taglink;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.tagbox.taglink.ApplicationSettings.EXCEPTION_URL;

public class SplashActivity extends Activity {

    private static final int INITIAL_REQUEST=1337;
    private static final int BLUETOOTH_REQUEST=INITIAL_REQUEST+1;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Boolean result = requestAppPermissions();

        int delay;

        if(result) {
            delay = 7000;       //permission is to be granted by user. delay launching the main activity
        } else {
            delay = 1500;
        }

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(SplashActivity.this, "This version of Android is not supported by the Application",
                    Toast.LENGTH_LONG).show();
        }

        //When application is created, set last known location to empty and whitelist to empty
        ApplicationSettings applicationSettings = new ApplicationSettings(this);
        applicationSettings.setAppSetting(ApplicationSettings.LAST_KNOWN_LOCATION, "");

        LocationService locationService = new LocationService(this);

        if(applicationSettings.containsKey(ApplicationSettings.LAST_KNOWN_EXCEPTION)) {
            String exceptionData = applicationSettings.getAppSettingString(ApplicationSettings.LAST_KNOWN_EXCEPTION);
            ExceptionUploadTask task = new ExceptionUploadTask(exceptionData.toString());
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            delay = 15000;  //there is an exception so given some time for the async thread to post data if possible
        }

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);
                SplashActivity.this.startActivity(myIntent);

                finish();
            }
        }, delay);
    }

    private Boolean requestAppPermissions() {

        Boolean result = false;

        int accessCoarseLocation = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int accessFineLocation   = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        List<String> listRequestPermission = new ArrayList<String>();

        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (accessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listRequestPermission.isEmpty()) {
            result = true;

            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You need to allow access to Location permission for the application to function. ")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("Cancel", dialogClickListener).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, strRequestPermission, LOCATION_REQUEST);
            }
        }

        return result;
        /*if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                        != PackageManager.PERMISSION_GRANTED ))   {
            // Check Permissions Now

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.BLUETOOTH_ADMIN)) {
                // Display UI and wait for user interaction
            } else {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                        BLUETOOTH_REQUEST);
            }
        }*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0) {
                    for (int gr : grantResults) {
                        // Check if request is granted or not
                        if (gr != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(SplashActivity.this, "Location information is required for normal functioning of the app",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
            default:
                return;
        }
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    ActivityCompat.requestPermissions(SplashActivity.this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_REQUEST);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    Toast.makeText(SplashActivity.this, "Taglink application will not function as Location permission has been denied",
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    private class ExceptionUploadTask extends AsyncTask<Void,Void,Integer> {
        private final String message;

        ExceptionUploadTask(String msg) {
            message = msg;
        }

        @Override
        protected Integer doInBackground(Void... params) {

            int result = 1;

            OkHttpClient client = OkHttpSingleton.getInstance().getOkHttpClient();

            MediaType type = MediaType.parse("text/plain");

            RequestBody requestBody = RequestBody.create(type, message);

            Request request = new Request.Builder()
                    .url(EXCEPTION_URL)
                    .method("POST", requestBody)
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if(response.isSuccessful()) {
                    result = 0;
                }
            } catch (Exception e) {
                Log.d("E", e.toString());
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Integer success) {

            if (success == 0) {
                ApplicationSettings appSettings = new ApplicationSettings(SplashActivity.this);
                appSettings.deleteAppSetting(ApplicationSettings.LAST_KNOWN_EXCEPTION);
            }
        }
    }
}
