package com.example.tim.runningapptest14;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class DistanceService extends Service {
    private static double distanceInMeters;
    private static double distanceLastSecond;
    private static Location lastLocation = null;
    //need to be accessible from the MainActivity:
    static LocationListener listener;
    static LocationManager locManager;

    //The binder is used to connect the activity with the service
    //I need to define my own Binder, DistanceBinder(), which getDistanceBinder() method returns a DistanceService
    private final IBinder binder = new DistanceBinder();

    public class DistanceBinder extends Binder {
        DistanceService getDistanceBinder() {
            return DistanceService.this;
        }
    }

    @Override
    public void onCreate() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (lastLocation == null) {
                    lastLocation = location;
                }

                distanceInMeters += location.distanceTo(lastLocation);
                distanceLastSecond = location.distanceTo(lastLocation);
                lastLocation = location;
            }

            //The next three methods have to be overwritten but can be left empty
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        //register the LocationListener with the Android location service
        //requestLocationUpdates() updates the Location minimum every 1000 milliseconds
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, listener);
    }

    //onBind returns an IBinder, which is our DistanceBinder() object that returns the DistanceService object if getDistanceBinder() is called
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    //Getter
    public double getDistanceInMeters(){
        return this.distanceInMeters;
    }

    public double getDistanceLastSecond(){
        return this.distanceLastSecond;
    }

    //Setter
    public void setDistanceInMeters(double meters){
        this.distanceInMeters= meters;
    }
}
