package com.tagbox.taglink;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by Suhas on 11/9/2016.
 */

public class LocationService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    private String latitude;
    private String longitude;
    private LocationManager locationManager;
    private String provider;

    private LocationRequest mLocationRequest;

    final static int REQUEST_CHECK_SETTINGS = 99;

    public LocationService(Context context){
        mContext = context;

        // Create an instance of GoogleAPIClient. Using Google Play api services for getting location
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
        startLocationService();
    }

    public void stopLocationService(){
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently
    }

    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                                mGoogleApiClient);
            if (mLastLocation != null) {
                handleNewLocation(mLastLocation);
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException se) {
        } catch (Exception ex){
        }
    }

    private void handleNewLocation(Location location) {

        String timestamp = Utils.getUtcDatetimeAsString();

        if(location != null) {
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());

            ApplicationSettings appSettings = new ApplicationSettings(mContext);
            appSettings.setAppSetting(ApplicationSettings.LAST_KNOWN_LOCATION, latitude + "," + longitude);

            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void startLocationService() {

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000);        // 10 seconds, in milliseconds
                //.setFastestInterval(1 * 1000); // 1 second, in milliseconds
                //.setSmallestDisplacement(10);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result
                        .getLocationSettingsStates();
                String locationResultMessage = "";
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location
                        //locationResultMessage = "Location Settings satisfied.";
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be
                        // fixed by showing the user
                        try {
                            status.startResolutionForResult(
                                    (Activity)mContext,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (Exception ex) {
                            //Dont do anything here
                        }

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have
                        // no way to fix the
                        //locationResultMessage = "Phone does not allow location settings";
                        break;
                }
            }
        });
    }
}