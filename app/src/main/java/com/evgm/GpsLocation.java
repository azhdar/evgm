package com.evgm;


import android.location.Location;

public class GpsLocation {


    /*
        FIELDS
     */
    private long timeStamp;
    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;


    /*
        CONSTRUCTORS
     */
    public GpsLocation(Location location) {
        this();

        if(location == null) {
            throw new NullPointerException("location is null");
        }

        this.timeStamp = location.getTime();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
        this.accuracy = location.getAccuracy();
    }

    private GpsLocation() {
        this.timeStamp = Long.MIN_VALUE;
        this.latitude = Double.MIN_VALUE;
        this.longitude = Double.MIN_VALUE;
        this.altitude = Double.MIN_VALUE;
        this.accuracy = -1.0f;
    }

    /*
        GETTERS AND SETTERS
     */

    public long getTimeStamp() {
        return timeStamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public float getAccuracy() {
        return accuracy;
    }


    /*
        PUBLIC METHODS
     */

    public boolean isValid() {
        return (timeStamp != Long.MIN_VALUE         //
                && this.latitude != Double.MIN_VALUE    //
                &&this.longitude != Double.MIN_VALUE);
    }

}
