package com.tagbox.taglink;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private CompoundCtrlLblTv period_getvalidtags;
    private CompoundCtrlLblTv period_posttagdata;
    private CompoundCtrlLblTv period_probedata;
    private CompoundCtrlLblTv period_scantagon;
    private CompoundCtrlLblTv period_scantagoff;
    private CompoundCtrlLblTv gateway_id;
    private CompoundCtrlLblTv period_locationupdate;
    private CompoundCtrlLblTv tag_address_filter;
    private CompoundCtrlLblTv lid_tag_address_filter;

    private ApplicationSettings appSettings;
    private Switch switchLocation;
    private Switch switchNetwork;
    private Switch switchProbe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        appSettings = new ApplicationSettings(this);

        setInitialSettingsScreen();

        switchLocation = (Switch) findViewById(R.id.switchScanLocation);
        switchNetwork = (Switch) findViewById(R.id.switchSendDataToCloud);
        switchProbe = (Switch) findViewById(R.id.switchScanProbe);
        setSwitchButtons();*/
    }

    private void setSwitchButtons(){
        /*boolean value;

        value = appSettings.getAppSettingBoolean(BOOL_LOCATION_ENABLED);
        switchLocation.setChecked(value);

        switchLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                    appSettings.setAppSetting(BOOL_LOCATION_ENABLED, true);
                else
                    appSettings.setAppSetting(BOOL_LOCATION_ENABLED, false);
            }
        });

        value = appSettings.getAppSettingBoolean(BOOL_NETWORK_ENABLED);
        switchNetwork.setChecked(value);
        switchNetwork.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                    appSettings.setAppSetting(BOOL_NETWORK_ENABLED, true);
                else
                    appSettings.setAppSetting(BOOL_NETWORK_ENABLED, false);
            }
        });

        value = appSettings.getAppSettingBoolean(BOOL_PROBE_ENABLED);
        switchProbe.setChecked(value);
        switchProbe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                    appSettings.setAppSetting(BOOL_PROBE_ENABLED, true);
                else
                    appSettings.setAppSetting(BOOL_PROBE_ENABLED, false);
            }
        });*/
    }

    private void setInitialSettingsScreen(){
        /*period_getvalidtags = (CompoundCtrlLblTv)this.findViewById(R.id.settings_period_getvalidtags);
        period_getvalidtags.setLabel("Get Device List Interval(s) : ");

        period_posttagdata = (CompoundCtrlLblTv)this.findViewById(R.id.settings_period_posttagdata);
        period_posttagdata.setLabel("Post Data Interval(s) : ");

        period_scantagon = (CompoundCtrlLblTv)this.findViewById(R.id.settings_period_btscanon);
        period_scantagon.setLabel("Bluetooth Scan On Window(s) : ");

        period_scantagoff = (CompoundCtrlLblTv)this.findViewById(R.id.settings_period_btscanoff);
        period_scantagoff.setLabel("Bluetooth Scan Off Window(s) : ");

        period_locationupdate = (CompoundCtrlLblTv)this.findViewById(R.id.settings_location_update_interval);
        period_locationupdate.setLabel("Location Update Interval(s) : ");

        period_probedata = (CompoundCtrlLblTv)this.findViewById(R.id.settings_interval_probe_data);
        period_probedata.setLabel("Probe Update Interval(s) : ");

        gateway_id = (CompoundCtrlLblTv)this.findViewById(R.id.settings_gateway_id);
        gateway_id.setLabel("Gateway ID : ");

        tag_address_filter = (CompoundCtrlLblTv)this.findViewById(R.id.settings_tag_address_filter);
        tag_address_filter.setLabel("Tag MAC Address Filter : ");

        lid_tag_address_filter = (CompoundCtrlLblTv)this.findViewById(R.id.settings_lid_tag_address_filter);
        lid_tag_address_filter.setLabel("Door Tag MAC Address Filter : ");

        long value;
        String stringValue;

        value = appSettings.getAppSetting(ApplicationSettings.INT_GET_TAGS);
        period_getvalidtags.setValue(Long.toString(value));

        value = appSettings.getAppSetting(ApplicationSettings.INT_POST_TAG_DATA);
        period_posttagdata.setValue(Long.toString(value));

        value = appSettings.getAppSetting(ApplicationSettings.INT_BTSCAN_ON);
        period_scantagon.setValue(Long.toString(value));

        value = appSettings.getAppSetting(ApplicationSettings.INT_BTSCAN_OFF);
        period_scantagoff.setValue(Long.toString(value));

        value = appSettings.getAppSetting(ApplicationSettings.LONG_LOCATION_UPDATE_INTERVAL);
        period_locationupdate.setValue(Long.toString(value));

        value = appSettings.getAppSetting(ApplicationSettings.INT_PROBE_INTERVAL);
        period_probedata.setValue(Long.toString(value));

        stringValue = appSettings.getAppSettingString(ApplicationSettings.STRING_GATEWAY_ID);
        gateway_id.setValue(stringValue);

        stringValue = appSettings.getAppSettingString(ApplicationSettings.STRING_TAG_WHITELIST_AM);
        tag_address_filter.setValue(stringValue);

        stringValue = appSettings.getAppSettingString(ApplicationSettings.STRING_TAG_WHITELIST_DOOR_ACT);
        lid_tag_address_filter.setValue(stringValue);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveApplicationSettings(View view){
        /*String value;
        try {
            value = period_getvalidtags.getValue();
            appSettings.setAppSetting(ApplicationSettings.INT_GET_TAGS, Long.parseLong(value));

            value = period_posttagdata.getValue();
            appSettings.setAppSetting(ApplicationSettings.INT_POST_TAG_DATA, Long.parseLong(value));

            value = period_scantagon.getValue();
            appSettings.setAppSetting(ApplicationSettings.INT_BTSCAN_ON, Long.parseLong(value));

            value = period_scantagoff.getValue();
            appSettings.setAppSetting(ApplicationSettings.INT_BTSCAN_OFF, Long.parseLong(value));

            value = period_locationupdate.getValue();
            appSettings.setAppSetting(ApplicationSettings.LONG_LOCATION_UPDATE_INTERVAL, Long.parseLong(value));

            value = period_probedata.getValue();
            appSettings.setAppSetting(ApplicationSettings.INT_PROBE_INTERVAL, Long.parseLong(value));

            value = gateway_id.getValue().trim();
            appSettings.setAppSetting(ApplicationSettings.STRING_GATEWAY_ID, value);

            value = tag_address_filter.getValue();
            appSettings.setAppSetting(ApplicationSettings.STRING_TAG_WHITELIST_AM, value);

            value = lid_tag_address_filter.getValue();
            appSettings.setAppSetting(ApplicationSettings.STRING_TAG_WHITELIST_DOOR_ACT, value);

            Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(this, "Not a valid value", Toast.LENGTH_SHORT).show();
        }*/
    }
}
