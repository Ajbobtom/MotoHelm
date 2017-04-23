package com.example.android.MotoHelm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;


public class LocationSErvice extends Service {
    private static final String TAG = "LocationService";
    private LocationManager mLocationManager = null;
    private static final int Location_Interval = 300; //miliseconds
    private static final float Locaion_Distance = 10; //meters
    public double Speed; //Default is MPH for now.
    public double PLat = 0, PLang = 0, NLat, NLang;
    DirectionStuff directions;
    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);

        }

        @Override
        public void onLocationChanged(Location location) {

            Log.e(TAG, "onLocationChanged: " + location);


            mLastLocation.set(location);
            NLat=SharedValues.CurrentLat = mLastLocation.getLatitude(); //new loc
            NLang =SharedValues.CurrentLang= mLastLocation.getLongitude();

           if(SharedValues.DirecRequest){
            if (SharedValues.FirstTime == false) { //we run this if we have started navigating

                Speed = mLastLocation.getSpeed();
                Speed = Speed * 2.23694;
                PLat = NLat; //perm
                PLang = NLang;//old loc

                SharedValues.Speed =String.valueOf(mLastLocation.getSpeed() * 2.23694) + "mph";

                //only gonna send speed, and manuevers direcitons nothing else
                Log.d("LOCAITONS: ", "Not the first time homie");
        if(SharedValues.ArrayFilled || directions.getStatus() == AsyncTask.Status.FINISHED ){ Log.d("Finished: ", "Position: " + String.valueOf(SharedValues.StepNumber));   SharedValues.CheckBounds();}

            }
            //We not itterate throuhg the direction. Check the current Lat.Lang with the next direciton, and if its in the right heading, get the next
            //direction. I.e. if (first direction - next >0 ) then we are going
            else { //we just started and we need idrections
                PLat = NLat;
                PLang = NLang;
                SharedValues.Speed = String.valueOf(mLastLocation.getSpeed() * 2.23694) + "mph"; //mph

                SharedValues.StartLoc = new LatLng(PLat, PLang); //set lat and lang for direcionts request
                SharedValues.StepNumber = 0;
                //request something Plz from maps
                //we will have to move the location service later once a dest is sent

                directions.execute();


            }
            SharedValues.FirstTime = false;
            Log.d(TAG, "onLocationChanged: skipped");}
            else{}
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider + " Status: " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "OnProviderDisabled: " + provider);
        }
    }

    android.location.LocationListener[] mLocationListeners = new android.location.LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public LocationSErvice() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        SharedValues.isFinished = false;
        SharedValues.FirstTime = true;
        initializeLocationManager();
        directions = new DirectionStuff();

        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Location_Interval, Locaion_Distance, mLocationListeners[0]);

        } catch (SecurityException ex) {

        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Location_Interval, Locaion_Distance, mLocationListeners[0]);

        } catch (SecurityException ex) {
            Log.i(TAG, "Fail to request location update, ignore", ex);

        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        }

    }

    public double Distance(double lat1, double lng1, double lat2, double lng2) { //may not even need...wtf
        double Distance = 0;
        double Time = Location_Interval / 10000; //now in seconds
        lat1 = lat1 * Math.PI / 180.0;
        lng1 = lng1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;
        lng2 = lng2 * Math.PI / 180.0;
        //To radians
        //now
        double R = 6378100; //rad of earth in m
        //P
        double rho1 = R * Math.cos(lat1);
        double z1 = R * Math.sin(lat1);
        double x1 = rho1 * Math.cos(lng1);
        double y1 = rho1 * Math.sin(lng1);
        //Q
        double rho2 = R * Math.cos(lat2);
        double z2 = R * Math.sin(lat2);
        double x2 = rho2 * Math.cos(lng2);
        double y2 = rho2 * Math.sin(lng2);

        //Dot prod
        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
        double cos_theta = dot / (R * R);
        double theta = Math.acos(cos_theta);

        //Distance
        Distance = R * theta;
        return Distance;
    }
}