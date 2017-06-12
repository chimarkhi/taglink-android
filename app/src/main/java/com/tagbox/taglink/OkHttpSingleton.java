package com.tagbox.taglink;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Suhas on 6/3/2017.
 */

public class OkHttpSingleton {
    private OkHttpClient mOkHttpClient;

    private static OkHttpSingleton ourInstance;

    public static OkHttpSingleton getInstance() {

        if (ourInstance == null) {
            ourInstance = new OkHttpSingleton();
        }

        return ourInstance;
    }

    private OkHttpSingleton() {

        OkHttpClient.Builder builderOkhttp = new OkHttpClient.Builder();
        builderOkhttp.connectTimeout(20, TimeUnit.SECONDS);
        builderOkhttp.readTimeout(15, TimeUnit.SECONDS);
        builderOkhttp.writeTimeout(15, TimeUnit.SECONDS);

        mOkHttpClient = builderOkhttp.build();

        setOkHttpClient(mOkHttpClient);
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public void setOkHttpClient(OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
    }

}
