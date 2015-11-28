package com.votors.runningx;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Jason on 2015/11/27 0027.
 */
public class MainButtonActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    int count_start = 0;
    int count_map = 0;
    int count_stop = 0;
    int count_chart = 0;
    Boolean stop = true;
    Boolean firstStart = true;
    float curr_speed = 0;
    float curr_distance = 0;
    ArrayList<GpsRec> locations = new ArrayList<>();

    TextView text_dist = null;
    TextView text_speed = null;

    // Handler gets created on the UI-thread
    private Handler mHandler = new Handler();

    GoogleApiClient mGoogleApiClient = null;
    LocationRequest mLocationRequest = null;

    public final static String EXTRA_MESSAGE = "com.votors.runningx.MESSAGE";
    public final static String EXTRA_GpsRec = "com.votors.runningx.GpsRec";
    private static final String BC_INTENT = "com.votors.runningx.BroadcastReceiver.location";

    private final String TAG = "Button";
    private final int MIN_DISTANCE = 5;
    private final int INTERVAL_LOCATION = 5000;
    private final int DISTANCE_SHOWTOAST = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_button);

        // Get a reference to the Press Me Button
        final Button button_start = (Button) findViewById(R.id.button_start);
        //final Button button_pause = (Button) findViewById(R.id.button_pause);
        final Button button_map = (Button) findViewById(R.id.button_map);
        final Button button_stop = (Button) findViewById(R.id.button_stop);
        final Button button_chart = (Button) findViewById(R.id.button_chart);
        text_dist = (TextView) findViewById(R.id.button_distance);
        text_speed = (TextView) findViewById(R.id.button_speed);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();

        getLastLocation();

        // Called each time the user clicks the Button
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firstStart) {
                    firstStart = false;
                    locations.clear();
                    curr_distance = 0;
                    curr_speed = 0;
                }
                if (stop) {
                    //current is stop, we will start it
                    stop = false;
                    button_start.setText("PAUSE:" + ++count_start);
                    startLocationUpdates();
                } else {
                    stop = true;
                    button_start.setText("RESUME:" + ++count_start);
                    stopLocationUpdates();
                }
                Log.i(TAG, "start/pause/resume onclick..");
            }
        });

        final Intent intent = new Intent(this, MapActivity.class);
        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_map.setText("MAP:" + ++count_map);
                intent.putExtra(EXTRA_MESSAGE, locations);
                Log.i(TAG, "MAP onclick..");
                startActivity(intent);
            }
        });
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_stop.setText("STOP:" + ++count_stop);
                intent.putExtra(EXTRA_MESSAGE, locations);
                stop = true;
                firstStart = true;
                button_start.setText("START:" + ++count_start);
                stopLocationUpdates();
                Log.i(TAG, "stop onclick..");
                startActivity(intent);
            }
        });

        final Intent intent_chart = new Intent(this, ChartActivity.class);
        button_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_chart.setText("CHART:" + ++count_chart);
                Log.i(TAG, "chart onclick..");
                intent_chart.putExtra(EXTRA_MESSAGE, locations);
                startActivity(intent_chart);
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected!!!!!!!!!!!");
        //startLocationUpdates();
//        getLocation();
    }

    @Override
    public void onConnectionSuspended(int var1) {
        Log.i(TAG, "onConnectionSuspended!!!!!!!!!!!");
    }

    @Override
    public void onConnectionFailed(ConnectionResult var1) {
        Log.i(TAG, "onConnectionFailed!!!!!!!!!!!");
    }

    private void sleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    @Override
    public void onLocationChanged(Location location) {
        saveLocation(location);
    }


    synchronized private void getLastLocation() {
        Log.i(TAG, "call get last location.");

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            double lat = mLastLocation.getLatitude();
            double lon = mLastLocation.getLongitude();
            double alt = mLastLocation.getAltitude();
            saveLocation(mLastLocation);
        }
    }

    void saveLocation(Location l) {
        float dist = 0, speed = 0;
        Date date = new Date();
        if (locations.size() > 0) {
            GpsRec pre = locations.get(locations.size() - 1);
            dist = pre.loc.distanceTo(l);
            dist = Math.abs(dist);
            if (dist < MIN_DISTANCE) {
                Log.i(TAG, String.format("dist too small. %f", dist));
                return;
            }
            speed = dist / ((date.getTime() - pre.getDate().getTime()) / 1000);
        }

        final GpsRec gps = new GpsRec(date, l);
        gps.distance = dist;
        gps.speed = speed;
        locations.add(gps);
        curr_speed = speed;
        curr_distance += dist;
        Log.i(TAG, String.format("%s", gps.toString()));

        // Do something in the main thread about the views.
        final Boolean showToast = Math.floor(curr_distance/DISTANCE_SHOWTOAST) != Math.floor((curr_distance-dist)/DISTANCE_SHOWTOAST);
        mHandler.post(new Runnable() {
            public void run() {
                text_speed.setText(String.format("%.2f m/s", curr_speed));
                text_dist.setText(String.format("%.0f m, %d pt", curr_distance, locations.size()));
                if (showToast) {
                    Toast.makeText(getApplicationContext(), "!--COME ON--!", Toast.LENGTH_LONG).show();
                }
                Log.i(TAG, String.format("%.2f m, %.2f m/s, loc # %d", curr_distance, curr_speed, locations.size()));
            }
        });

        //broadcast a message
        Intent msg = new Intent(BC_INTENT);
        msg.putExtra(EXTRA_GpsRec, gps);
        sendOrderedBroadcast(msg,null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

}
