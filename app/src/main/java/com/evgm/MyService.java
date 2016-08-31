package com.evgm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyService extends Service {

    /*
        PUBLIC INNER CLASS
     */
    public class MyServiceBinder extends Binder {
        public MyService getService(){
            Log.d("EVGM", "Service.TrackerServiceBinder.getService was called");
            return MyService.this;
        }
    }

    /*
        CONSTANTS
     */
    private static final int NOTIFICATION_ID = 3469;
    private static final String WAKE_LOCK_TAG = "edvg";

    /*
        FIELDS
     */

    private final IBinder myServiceBinder = new MyServiceBinder();
    private PowerManager.WakeLock wakeLock;

    private Follower follower;

    /*
        PUBLIC METHODS
     */

    @Override
    public void onCreate() {
        Log.d("EVGM", "Service.onCreate was called");

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        wakeLock.acquire();

        follower = new Follower(this);
        follower.startSensors();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("EVGM", "Service.onBind was called");
        return myServiceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("EVGM", "Service.onRebind was called");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("EVGM", "Service.onUnbind was called");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("EVGM", "Service.onStartCommand was called");
        if(!isInProgress()) {
            follow();
        }
        return START_STICKY; // TODO
    }

    @Override
    public void onDestroy() {
        Log.d("EVGM", "Service.onDestroy was called");
        follower.stopSensors();
        if(wakeLock!=null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onDestroy();
    }

    public void follow(){
        Log.d("EVGM", "Service.follow was called");
        startForeground(NOTIFICATION_ID, buildNotification("Title", "Text", "Ticker", false, MainActivity.class));
        follower.start();
    }

    public void stop(){
        Log.d("EVGM", "Service.stop was called");
        follower.stop();
        stopForeground(true);
    }

    public boolean isInProgress() {
        return follower.isInProgress();
    }

    public void addOnGpsSensorListener(GpsSensor.OnGpsSensorListener gpsSensorListener) {
        follower.addOnLocationSensorListener(gpsSensorListener);
    }

    private Notification buildNotification(String contentTitle, String contentText, String ticker, boolean displayTime, Class<MainActivity> mainActivityClass) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setTicker(ticker);

        if(displayTime) notificationBuilder.setWhen(System.currentTimeMillis());

        if(mainActivityClass != null){
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, mainActivityClass), PendingIntent.FLAG_CANCEL_CURRENT);
            notificationBuilder.setContentIntent(pendingIntent);
        }
        if(Build.VERSION.SDK_INT >= 16 ) return notificationBuilder.build();
        else return notificationBuilder.getNotification();
    }

}
