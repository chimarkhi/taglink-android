package com.tagbox.taglink;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.ArrayMap;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.tagbox.taglink.Constants.BACKOFF_MULT;
import static com.tagbox.taglink.Constants.MAX_RETRIES;
import static com.tagbox.taglink.Constants.SOCKET_TIMEOUT_MS;

/**
 * Created by Suhas on 10/19/2016.
 */

public class CloudInterface {

    private static Context mContext;
    private static String TAG = "CloudInterface";
    private static String AZUREDATAURL;

    private String gatewayId, sasKey;

    private DatabaseHandler mDb;

    public CloudInterface(Context context){
        mContext = context;

        ApplicationSettings appSettings = new ApplicationSettings(mContext);
        gatewayId = ApplicationSettings.GATEWAY_ID;
        sasKey = ApplicationSettings.IOTHUB_SAS_KEY;

        String iotHub = ApplicationSettings.IOTHUB_HOST;
        AZUREDATAURL = "https://" + iotHub + "/devices/" + gatewayId + "/messages/events?api-version=2016-02-03";
    }

    public void postDataAzure(final JSONObject bodyData){

        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("Authorization", sasKey);
        mHeaders.put("Content-Type", "application/json; charset=utf-8");

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AZUREDATAURL, bodyData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Result handling
                        if(response != null){
                            System.out.println(response.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        PostMessageData data = new PostMessageData(bodyData.toString());
                        backupUnsentMessageDb(data);
                    }
                }) {
            public Map<String, String> getHeaders() {
                return mHeaders;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

                    JSONObject result = null;

                    if (jsonString != null && jsonString.length() > 0)
                        result = new JSONObject(jsonString);

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(
                SOCKET_TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(req);
    }

    public void postDataToCloud(List<JSONObject> messages){
        final Boolean result = checkInternetConnection();

        if(result) {
            //Utils.sendLocalBroadcast(mContext, PERIPHERAL_STATUS, INTERNET_CONNECTIVITY_STATUS, "Up");
        } else {
            //Utils.sendLocalBroadcast(mContext, PERIPHERAL_STATUS, INTERNET_CONNECTIVITY_STATUS, "Down");
            Log.d(TAG, "Internet connection not available");
            for(JSONObject message : messages) {
                PostMessageData data = new PostMessageData(message.toString());
                backupUnsentMessageDb(data);
            }
            return;
        }

        checkPrevMessages();

        for(JSONObject j : messages){
            postDataAzure(j);
        }
    }

    public Boolean postSynchronousToAzure(final JSONObject bodyData){

        boolean result = true;

        final Map<String, String> mHeaders = new ArrayMap<String, String>();
        mHeaders.put("Authorization", sasKey);
        mHeaders.put("Content-Type", "application/json; charset=utf-8");

        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AZUREDATAURL, bodyData, future, future) {
            public Map<String, String> getHeaders() {
                return mHeaders;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

                    JSONObject result = null;

                    if (jsonString != null && jsonString.length() > 0)
                        result = new JSONObject(jsonString);

                    return Response.success(result,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    return Response.error(new ParseError(je));
                }
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(
                SOCKET_TIMEOUT_MS,
                MAX_RETRIES,
                BACKOFF_MULT));

        /*RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        requestQueue.add(req);*/

        VolleySingleton.getInstance(mContext).addToRequestQueue(req);

        try {
            JSONObject response = future.get(15, TimeUnit.SECONDS); // this will block
        } catch (Exception ex) {
            PostMessageData data = new PostMessageData(bodyData.toString());
            backupUnsentMessageDb(data);
            result = false;
        }

        return result;
    }

    public Boolean postSynchronousToCloud(List<JSONObject> messages) {
        Boolean result = true;

        result = checkInternetConnection();

        if(!result) {
            for(JSONObject message : messages) {
                PostMessageData data = new PostMessageData(message.toString());
                backupUnsentMessageDb(data);
            }
            return false;
        }

        for(JSONObject j : messages){
            if(!postSynchronousToAzure(j)) {
                result = false;
            }
        }

        return result;
    }

    public void checkPrevMessages() {
        List<PostMessageData> prevPostMessages = getAllPostMessages();
        if(prevPostMessages != null) {
            for (PostMessageData data : prevPostMessages) {
                try {
                    JSONObject jsonObject = new JSONObject(data.getPostMessage());
                    postDataAzure(jsonObject);
                } catch (JSONException j) {}
            }
        }
    }

    private void backupUnsentMessageDb(PostMessageData data) {
        mDb = new DatabaseHandler(mContext);
        mDb.addPostMessage(data);
        mDb.close();
    }

    public long getUnsentPostMessageCount() {
        mDb = new DatabaseHandler(mContext);
        long count = mDb.getPostMessageCount();
        mDb.close();

        return count;
    }

    public List<PostMessageData> getAllPostMessages() {
        mDb = new DatabaseHandler(mContext);
        List<PostMessageData> prevPostMessages = mDb.getAllPostMessageData();
        mDb.close();
        return prevPostMessages;
    }

    private Boolean isConnectedToNetwork(){
        ConnectivityManager cm =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private Boolean pingServer(){
        Runtime runtime = Runtime.getRuntime();
        try {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();
            return(mExitValue==0);
        }
        catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return false;
    }

    public Boolean checkInternetConnection(){
        Boolean result = isConnectedToNetwork();
        if(result){
            result = pingServer();
            if(result)
                return true;
        }
        return false;
    }
}
