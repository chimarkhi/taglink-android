package com.tagbox.taglink;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static com.tagbox.taglink.Constants.SERVICE_STOP_MSG;

public class MainActivity extends AppCompatActivity {

    private static final int INITIAL_REQUEST=1337;
    private static final int BLUETOOTH_REQUEST=INITIAL_REQUEST+1;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+2;
    private static final int STORAGE_REQUEST=INITIAL_REQUEST+3;

    private TextView tvBluetoothStatus;
    private TextView tvNetworkStatus;
    private TextView tvLastScan;

    //private Button btBluetoothEnable;
    //private Button btStart;

    private LinearLayout llBluetooth;
    private LinearLayout llNetwork;

    private MenuItem syncItem;

    private BluetoothAdapter mBluetoothAdapter;

    private static long SESSION_TIME_SECONDS = 60000;

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

        ApplicationSettings appSettings = new ApplicationSettings(this);

        long currUnixTime = System.currentTimeMillis() / 1000;

        long lastLogin = appSettings.getAppSetting(ApplicationSettings.LONG_LAST_LOGIN);
        if((currUnixTime - lastLogin) > SESSION_TIME_SECONDS){
            Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(myIntent);
            finish();
        }

        tvBluetoothStatus = (TextView)findViewById(R.id.tv_bt_status);
        tvNetworkStatus = (TextView)findViewById(R.id.tv_net_status);

        tvLastScan = (TextView)findViewById(R.id.tv_last_scan);
        long lastScan = appSettings.getAppSetting(ApplicationSettings.LONG_LAST_SCAN);
        /*if(lastScan <= 0) {
            tvLastScan.setText("Click 'Sync' to extract TagLink data");
        } else {
            String dateTime = Utils.getDateTimeFromUnixTimestamp(lastScan);
            tvLastScan.setText("Last sync at " + dateTime);
        }*/

        llBluetooth = (LinearLayout)findViewById(R.id.ll_bluetooth);
        llNetwork = (LinearLayout)findViewById(R.id.ll_network);

        //btBluetoothEnable = (Button)findViewById(R.id.bt_enable);
        //btStart = (Button)findViewById(R.id.bt_start);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        requestAppPermissions();

        //checkServiceRunning();

        setPhoneSettings();
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
        checkServiceRunning();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.sync:
                onClickSync();
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

        checkNetworkStatus();
    }

    private void requestAppPermissions() {
        //Get Permission for app to use location services
        if ((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED )){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Display UI and wait for user interaction
            } else {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_REQUEST);
            }
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
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
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED))   {
            // Check Permissions Now

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Display UI and wait for user interaction
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST);
            }
        }
    }

    private final BroadcastReceiver mBtReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // It means the user has changed his bluetooth state.
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
/*
                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
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

    public void checkServiceRunning() {
        boolean result = Utils.isServiceRunning(MainActivity.this, TagLinkService.class);
        if(result) {
            //btStart.setEnabled(false);
            setActionButtonDisable();
        } else {
            //btStart.setEnabled(true);
            setActionButtonOn();
        }
    }

    public void onClickSync() {

        //btStart.setEnabled(false);
        setActionButtonDisable();

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

    private void setBluetoothStatusOff() {
        llBluetooth.setBackgroundColor(Color.RED);
        //btBluetoothEnable.setVisibility(View.VISIBLE);
        tvBluetoothStatus.setText("Off");
        //btStart.setEnabled(false);
        //setActionButtonOff();
    }

    private void setBluetoothStatusOn() {
        llBluetooth.setBackgroundColor(Color.GREEN);
        //btBluetoothEnable.setVisibility(View.INVISIBLE);
        tvBluetoothStatus.setText("On");
        //btStart.setEnabled(true);
        //setActionButtonOn();
    }

    private void setNetworkOff() {
        llNetwork.setBackgroundColor(Color.RED);
        tvNetworkStatus.setText("Down");
        //btStart.setEnabled(false);
        //setActionButtonOff();
    }

    private void setNetworkOn() {
        llNetwork.setBackgroundColor(Color.GREEN);
        tvNetworkStatus.setText("Up");
        //btStart.setEnabled(true);
        //setActionButtonOn();
    }

    private void setActionButtonOn() {
        if(syncItem != null) {
            syncItem.setEnabled(true);
            syncItem.setTitle("Sync");
        }
    }

    private void setActionButtonOff(){
        if(syncItem != null) {
            syncItem.setEnabled(false);
            syncItem.setTitle("Sync");
        }
    }

    private void setActionButtonDisable() {
        if(syncItem != null) {
            syncItem.setEnabled(false);
            syncItem.setTitle("Syncing");
        }
    }
}