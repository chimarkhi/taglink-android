package com.tagbox.taglink;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.tagbox.taglink.Constants.SERVICE_STOP_MSG;
import static com.tagbox.taglink.Constants.SESSION_TIME_SECONDS;

public class MainActivity extends AppCompatActivity {

    private TextView tvBluetoothStatus;
    private TextView tvNetworkStatus;
    private TextView tvNotification;

    private LinearLayout llBluetooth;
    private LinearLayout llNetwork;

    private MenuItem syncItem;
    private MenuItem retryItem;

    private BluetoothAdapter mBluetoothAdapter;

    private CloudInterface ci;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check whether device supports BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported. Shutting down",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        tvBluetoothStatus = (TextView) findViewById(R.id.tv_bt_status);
        tvNetworkStatus = (TextView) findViewById(R.id.tv_net_status);
        tvNotification = (TextView) findViewById(R.id.tv_notification);

        llBluetooth = (LinearLayout) findViewById(R.id.ll_bluetooth);
        llNetwork = (LinearLayout) findViewById(R.id.ll_network);

        setPhoneSettings();

        ApplicationSettings appSettings = new ApplicationSettings(this);

        long currUnixTime = System.currentTimeMillis() / 1000;
        long lastLogin = appSettings.getAppSetting(ApplicationSettings.LONG_LAST_LOGIN);

        boolean result = Utils.isServiceRunning(MainActivity.this, TagLinkService.class);

        //if service is running and session time not expired then dont go to login activity
        if(!result && ((currUnixTime - lastLogin) > SESSION_TIME_SECONDS )) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            MainActivity.this.startActivity(intent);
            finish();
        } else {
            appSettings.setAppSetting(ApplicationSettings.LONG_LAST_LOGIN, currUnixTime); //keep updating session time

            /*long lastScan = appSettings.getAppSetting(ApplicationSettings.LONG_LAST_SCAN);
            if(lastScan <= 0) {
                tvLastScan.setText("Click 'Sync' to extract TagLink data");
            } else {
                String dateTime = Utils.getDateTimeFromUnixTimestamp(lastScan);
                tvLastScan.setText("Last sync at " + dateTime);
            }*/

            Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
            setSupportActionBar(toolbar);

            ci = new CloudInterface(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerForLocalBroadcast();

        this.registerReceiver(mBtReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        this.registerReceiver(mNetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        //isServiceRunning();
        this.unregisterReceiver(mBtReceiver);
        this.unregisterReceiver(mNetReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        syncItem = menu.getItem(0);
        retryItem = menu.getItem(1);

        setMenuItems();
        //checkServiceRunning();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sync:
                onClickSync();
                break;
            case R.id.retry:
                onClickRetry();
                break;
        }
        return(super.onOptionsItemSelected(item));
    }

    private void registerForLocalBroadcast(){
        // This registers mMessageReceiver to receive messages.
        IntentFilter filter = new IntentFilter(SERVICE_STOP_MSG);
        // This registers mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                filter);
    }

    private void setPhoneSettings() {
        checkBluetoothStatus();

        checkNetworkStatus();
    }

    private void checkBluetoothStatus(){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if(mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth Not Supported. Shutting down",
                    Toast.LENGTH_LONG).show();
            finish();
        }

        if (mBluetoothAdapter.isEnabled()) {
            setBluetoothStatusOn();
        } else {
            setBluetoothStatusOff();

            Toast.makeText(MainActivity.this, "Require bluetooth connection for uploading Tag Data",
                    Toast.LENGTH_LONG).show();
        }
    }

    private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // It means the user has changed his bluetooth state.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                /*if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
                    tvBluetoothStatus.setText("");
                    Utils.sendLocalBroadcast(mContext, PERIPHERAL_STATUS, PERIPHERAL_BT_STATUS, "Turning Off");
                }*/

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                    // The user bluetooth is already disabled.
                    setBluetoothStatusOff();

                    Toast.makeText(MainActivity.this, "Require bluetooth connection for uploading Tag Data",
                            Toast.LENGTH_LONG).show();
                }

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    setBluetoothStatusOn();
                }
            }
        }
    };

    private final BroadcastReceiver mNetReceiver = new BroadcastReceiver()  {

        @Override
        public void onReceive(Context context, Intent intent) {
            checkNetworkStatus();
        }
    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent

            if(SERVICE_STOP_MSG.equals(intent.getAction())){
                //btStart.setEnabled(true);
                finish();
                startActivity(getIntent());
            }
        }
    };

    private void checkNetworkStatus() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            setNetworkOn();
        } else {
            setNetworkOff();
            Toast.makeText(MainActivity.this, "Require network connection for uploading Tag Data",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void setMenuItems() {
        setRetryItemOff();

        boolean hasRequiredPermissions = Utils.hasPermissions(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        boolean result = Utils.isServiceRunning(MainActivity.this, TagLinkService.class);

        if(result || !hasRequiredPermissions) {
            //btStart.setEnabled(false);
            setSyncItemDisable();

            if(!hasRequiredPermissions) {
                tvNotification.setText("Application requires location permission to function. Enable permission and restart the application");
            }
        } else {
            //btStart.setEnabled(true);
            setSyncItemOn();

            CloudInterface ci = new CloudInterface(this);
            long count = ci.getUnsentPostMessageCount();

            if(count > 0) {
                setRetryItemOn();
                tvNotification.setText("Data is pending to be uploaded to cloud. Click 'Retry Upload' option in the menu to complete the process");
            }
        }
    }

    public void onClickSync() {

        //btStart.setEnabled(false);

        setSyncItemDisable();

        boolean result = Utils.isServiceRunning(MainActivity.this, TagLinkService.class);
        if(!result) {
            Intent intent = new Intent(this, TagLinkService.class);
            startService(intent);
            // Wait for a second and check if the gateway service has been started
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean result = Utils.isServiceRunning(MainActivity.this, TagLinkService.class);
                    //if the gateway service has started, then disable the button
                    if(result == true) {
                        Toast.makeText(MainActivity.this, "Service started", Toast.LENGTH_SHORT).show();

                        //need to refresh the fragment so list is updated
                        Fragment frg = getFragmentManager().findFragmentById(R.id.tags_fragment);
                        final FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.detach(frg);
                        ft.attach(frg);
                        ft.commit();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Unable to start service", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            }, 1000);
        }
        else {
            Toast.makeText(this, "Service already Running", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickRetry(){
        DataUploadTask task = new DataUploadTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setBluetoothStatusOff() {
        llBluetooth.setBackgroundColor(Color.RED);
        //btBluetoothEnable.setVisibility(View.VISIBLE);
        tvBluetoothStatus.setText("Off");
    }

    private void setBluetoothStatusOn() {
        llBluetooth.setBackgroundColor(Color.GREEN);
        //btBluetoothEnable.setVisibility(View.INVISIBLE);
        tvBluetoothStatus.setText("On");
    }

    private void setNetworkOff() {
        llNetwork.setBackgroundColor(Color.RED);
        tvNetworkStatus.setText("Down");
    }

    private void setNetworkOn() {
        llNetwork.setBackgroundColor(Color.GREEN);
        tvNetworkStatus.setText("Up");
    }

    private void setSyncItemOn() {
        if(syncItem != null) {
            syncItem.setEnabled(true);
            syncItem.setTitle("Sync");
        }
    }

    private void setSyncItemDisable(){
        if(syncItem != null) {
            syncItem.setEnabled(false);
            syncItem.setTitle("Sync");
        }

        if(retryItem != null) {
            retryItem.setVisible(false);
        }
    }

    /*private void setSyncItemDisable() {
        if(syncItem != null) {
            syncItem.setEnabled(false);
            syncItem.setTitle("Syncing");
        }
    }*/

    private void setRetryItemOn() {
        if(retryItem != null) {
            retryItem.setVisible(true);
            retryItem.setEnabled(true);
        }
    }

    private void setRetryItemOff(){
        if(retryItem != null) {
            retryItem.setVisible(false);
        }
    }

    private class DataUploadTask extends AsyncTask<Void,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            setSyncItemDisable();       //prevent user from clicking on sync item when retrying data upload

            Boolean result;

            List<PostMessageData> messages = ci.getAllPostMessages();

            List<JSONObject> data = new ArrayList<>();

            if(messages != null) {
                for (PostMessageData message : messages) {
                    try {
                        JSONObject jsonObject = new JSONObject(message.getPostMessage());
                        data.add(jsonObject);
                    } catch (JSONException j) {}
                }
            }
            result = ci.postSynchronousToCloud(data);

            setSyncItemOn();

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                tvNotification.setText("Failed to upload data");
            } else {
                tvNotification.setText("Successfully uploaded data");
            }
        }
    }
}