package com.evgm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Intent serviceIntent;
    private ServiceConnection serviceConnection;
    private MyService myService;

    private TextView textView;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("EVGM", "Activity.onCreate was called");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.textView = (TextView)findViewById(R.id.textView);
        this.fab = (FloatingActionButton) findViewById(R.id.fab);

        serviceIntent = new Intent(this, MyService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d("EVGM", "Activity.onServiceConnected was called");
                myService = ((MyService.MyServiceBinder) iBinder).getService();

                if(!myService.isInProgress()) {
                    startService(serviceIntent);
                }

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (myService.isInProgress()) {
                            myService.stop();
                        } else {
                            myService.follow();
                            Snackbar.make(view, "Follower is starting", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }

                        GpsSensor.OnGpsSensorListener onGpsSensorListener = new GpsSensor.OnGpsSensorListener() {
                            @Override
                            public void onStateChanged(GpsSensor gpsSensor) {
                                if(gpsSensor.getState().equals(GpsState.FIXED)) {
                                    textView.setText("STATE: " + gpsSensor.getState() + "\nLOCATION: " + gpsSensor.getLastLocation().getTimeStamp() + " " + gpsSensor.getLastLocation().getLatitude() + " " + gpsSensor.getLastLocation().getLongitude());
                                } else {
                                    textView.setText("STATE: " + gpsSensor.getState()+ "\nLOCATION: null");
                                }
                            }

                            @Override
                            public void onLocationChanged(GpsSensor gpsSensor) {
                                textView.setText("STATE: "+gpsSensor.getState()+"\nLOCATION: "+ gpsSensor.getLastLocation().getTimeStamp() + " " + gpsSensor.getLastLocation().getLatitude() + " " + gpsSensor.getLastLocation().getLongitude());
                            }
                        };
                        myService.addOnGpsSensorListener(onGpsSensorListener);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d("EVGM", "Activity.onServiceDisconnected was called");
                myService = null;
                // TODO display "problem" view
            }
        };
    }

    @Override
    protected void onStart() {
        Log.d("EVGM", "Activity.onStart was called");
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d("EVGM", "Activity.onStop was called");
        if(isMyServiceBound()){
            unbindService(serviceConnection);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("EVGM", "Activity.onDestroy was called"+isMyServiceBound()+(!myService.isInProgress()));
        if(isMyServiceBound()) {
            if(!myService.isInProgress()) {
                stopService(serviceIntent);
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean isMyServiceBound(){
        return myService != null;
    }
}
