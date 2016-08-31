package com.evgm;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

public class GpsSensor implements LocationListener, GpsStatus.Listener {

    /*
        LISTENERS
     */

    private ArrayList<OnGpsSensorListener> onGpsSensorListeners;
    public interface OnGpsSensorListener {
        void onLocationChanged(GpsSensor gpsSensor);
        void onStateChanged(GpsSensor gpsSensor);
    }

    /*
        FIELDS
     */

    private Context context;
    private boolean isStarted;

    private LocationManager locationManager;

    private GpsState state;
    private GpsLocation lastLocation;

    private int satellites;

    public GpsSensor(Context context){
        this();
        Log.v("BTS", "LocationSensor: cstr was called");
        this.context = context;
        this.isStarted = false;
        this.onGpsSensorListeners = new ArrayList<>();
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private GpsSensor() {
        this.state = GpsState.UNAVAILABLE;
        this.lastLocation = null;
    }

    /*
        GETTERS ANS SETTERS
     */

    public GpsState getState() {
        return state;
    }

    public GpsLocation getLastLocation() {
        return lastLocation;
    }

    /*
        PUBLIC METHODS
     */

    public void start(OnGpsSensorListener onGpsSensorListener) {
        Log.d("BTS", "LocationSensor: start was called");
        if (!isStarted()) {
            isStarted = true;

            addOnGpsSensorListener(onGpsSensorListener);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.addGpsStatusListener(this);
        }
    }

    public void stop() {
        if (isStarted()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            locationManager.removeUpdates(this);
            locationManager.removeGpsStatusListener(this);

            removeAllOnGpsSensorListeners();

            isStarted = false;
        }
    }
    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
//        Log.v("BTS", "LocationSensor: onLocationChanged was called() (location=" + location.getLatitude() + ";" + location.getLongitude() + ")");
        setLastLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
//        Log.v("BTS", "LocationSensor: onStatusChanged was called() (provider=" + provider + " status=" + status + ")");
        if(status == LocationProvider.AVAILABLE) {
//            Log.v("BTS", "LocationSensor: onStatusChanged set FIXED?");
            // TODO setState(LocationState.FIXED);
        } else if(status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
//            Log.v("BTS", "LocationSensor: onStatusChanged set SCANNING?");
            setState(GpsState.SCANNING);
        } else if(status == LocationProvider.OUT_OF_SERVICE) {
//            Log.v("BTS", "LocationSensor: onStatusChanged set UNAVAILABLE?");
            setState(GpsState.UNAVAILABLE);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        setState(GpsState.SCANNING);
    }

    @Override
    public void onProviderDisabled(String provider) {
        setState(GpsState.UNAVAILABLE);
    }

    @Override
    public void onGpsStatusChanged(int event) {
//        Log.v("BTS", "LocationSensor: onGpsStatusChanged was called()");
        // compute satellites count
        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        Iterator<GpsSatellite> satelliteIterator = gpsStatus.getSatellites().iterator();
        int satellites = 0;
        while(satelliteIterator.hasNext()){
            GpsSatellite gpsSatellite = satelliteIterator.next();
            if(gpsSatellite.usedInFix()) {
                satellites++;
            }
        }

        this.satellites = satellites;
//        Log.v("BTS", "LocationSensor: satellites=" + satellites);

        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
//                Log.v("BTS", "LocationSensor: even=GPS_EVENT_STARTED");
                setState(GpsState.SCANNING);
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
//                Log.v("BTS", "LocationSensor: even=GPS_EVENT_STOPPED");
                // TODO if the previous state was AVAILABLE?
                setState(GpsState.UNAVAILABLE);
                break;

            case GpsStatus.GPS_EVENT_FIRST_FIX:
//                Log.v("BTS", "LocationSensor: even=GPS_EVENT_FIRST_FIX");
                if(satellites < 3) {
                    setState(GpsState.SCANNING);
                } else {
//                    setState(LocationState.FIXED);
                }
                break;

            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
//                Log.v("BTS", "LocationSensor: even=GPS_EVENT_SATELLITE_STATUS");
                // TODO if satellites < 0?
                if(satellites < 3) {
                    setState(GpsState.SCANNING);
                } else {
//                    setState(LocationState.FIXED);
                    // TODO compare when state become AVAILABLE and when a new location occurred (onLocationChanged)
                }
                break;

            default:
                break;
        }
    }

    public void addOnGpsSensorListener(OnGpsSensorListener onGpsSensorListener) {
        if(this.onGpsSensorListeners != null && onGpsSensorListener !=null) {
            onGpsSensorListeners.add(onGpsSensorListener);
        }
        fireOnStateChanged(this);
    }


    public void removeOnGpsSensorListener(OnGpsSensorListener onGpsSensorListener){
        if(this.onGpsSensorListeners!=null && onGpsSensorListener!=null){
            this.onGpsSensorListeners.remove(onGpsSensorListener);
        }
    }

     /*
        PRIVATE METHODS
     */

    private void setState(GpsState state) {
        if(!this.state.equals(state)) {
//            Log.d("BTS", "LocationSensor: setState=" + state);
            this.state = state;
            if(!this.state.equals(GpsState.FIXED)) {
                lastLocation = null;
            }
            fireOnStateChanged(this);
        }
    }

    public void setLastLocation(android.location.Location lastLocation) {
        if(satellites >= 3) {
//            Log.d("BTS", "LocationSensor: setLastLocation=" + lastLocation);
            this.lastLocation = new GpsLocation(lastLocation);
            setState(GpsState.FIXED);
            fireOnLocationChanged(this);
        }
    }

    private void fireOnStateChanged(GpsSensor locationSensor){
        if(this.onGpsSensorListeners!=null){
            for(int i=0; i<this.onGpsSensorListeners.size(); i++) this.onGpsSensorListeners.get(i).onStateChanged(locationSensor);
        }
    }

    private void fireOnLocationChanged(GpsSensor locationSensor){
        if(this.onGpsSensorListeners!=null){
            for(int i=0; i<this.onGpsSensorListeners.size(); i++) this.onGpsSensorListeners.get(i).onLocationChanged(locationSensor);
        }
    }

    private void removeAllOnGpsSensorListeners(){
        if(this.onGpsSensorListeners!=null){
            while(!this.onGpsSensorListeners.isEmpty()){
                this.onGpsSensorListeners.remove(0);
            }
        }
    }
}
