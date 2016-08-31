package com.evgm;

import android.content.Context;
import android.util.Log;

import com.evgm.http.HttpPostConnection;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Time;

public class Follower implements GpsSensor.OnGpsSensorListener {

    private Context context;
    private GpsSensor gpsSensor;
    private Recorder recorder;

    private boolean inProgress;

    private long lastRecordTime;


    public Follower(Context context) {
        this.context = context;
        this.gpsSensor = new GpsSensor(context);
        this.inProgress = false;
        this.lastRecordTime = 0;
    }

    public void startSensors() {
        Log.v("EVGM", "Follower: startSensors was called");
        gpsSensor.start(this);
    }


    public void stopSensors() {
        Log.v("EVGM", "Follower: stopSensors was called");
        gpsSensor.stop();
    }

    public void start() {
        Log.e("EVGM", "Follower.start was called");
        if(!inProgress) {
            try {
                Log.e("EVGM", "new Recorder(context)");
                this.recorder = new Recorder(context);
            } catch (java.io.IOException e) {
                Log.e("EVGM", "", e);
                e.printStackTrace();
            }

            Log.e("EVGM", "logger is null?"+(recorder==null));
            inProgress = true;
        }
    }



    public void stop(){
        Log.e("EVGM", "recorder.stop was called");
        if(inProgress) {
            inProgress = false;
            try {
                if(this.recorder != null) this.recorder.close();
            } catch (java.io.IOException e) {
                Log.e("EVGM", "", e);
                e.printStackTrace();
            }
            this.recorder = null;
        }
    }

    public boolean isInProgress(){
        return inProgress;
    }

    @Override
    public void onLocationChanged(GpsSensor gpsSensor) {
        recordAndSendLocation(gpsSensor.getLastLocation());
    }

    @Override
    public void onStateChanged(GpsSensor gpsSensor) {
//        sendLocation(gpsSensor.getLastLocation());
    }

    public void addOnLocationSensorListener(GpsSensor.OnGpsSensorListener locationSensorListener) {
        gpsSensor.addOnGpsSensorListener(locationSensorListener);
    }

    /*
        PRIVATE METHODS
     */

    private void recordAndSendLocation(GpsLocation location){
        if(recorder==null) Log.e("EVGM", "recorder is null");
        else {
            try {
                long recordTime = System.currentTimeMillis();
                long intervalTime = recordTime - lastRecordTime;
                Log.e("EVGM", "intervalTime=" + intervalTime);
                if (location != null && intervalTime >= 10000) {
                    this.lastRecordTime = recordTime;
                    String coordinates = recorder.record(location);
                    Log.e("EVGM", "send " + coordinates);
                    HttpPostConnection httpPostConnection = new HttpPostConnection(context, new ByteArrayInputStream(coordinates.getBytes()));
                    httpPostConnection.execute();
                }
            } catch (IOException e) {
                Log.e("EVGM", "", e);
                e.printStackTrace();
            }
        }


    }
}
