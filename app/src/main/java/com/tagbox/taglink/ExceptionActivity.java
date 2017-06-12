package com.tagbox.taglink;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExceptionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception);

        String data = getIntent().getStringExtra("data");
        ExceptionUploadTask task = new ExceptionUploadTask(data);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class ExceptionUploadTask extends AsyncTask<Void,Void,Void> {
        private final String message;

        ExceptionUploadTask(String msg) {
            message = msg;
        }

        @Override
        protected Void doInBackground(Void... params) {
            OkHttpClient client = OkHttpSingleton.getInstance().getOkHttpClient();

            MediaType type = MediaType.parse("text/plain");

            RequestBody requestBody = RequestBody.create(type, message);

            Request request = new Request.Builder()
                    .url("http://192.168.2.6:8080/restservice/v1/d2c/exception")
                    .method("POST", requestBody)
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
            } catch (Exception e) {
                Log.d("E", e.toString());
            }

            return null;
        }
    }
}
