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
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Jason on 2015/11/27 0027.
 */
public class MainButtonActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    int count_start = 0;
    int count_pause = 0;
    int count_map = 0;
    int count_stop = 0;
    int count_chart = 0;
    Boolean stop = false;
    Boolean pause = false;
    Boolean start_thread = false;
    float curr_speed = 0;
    float curr_distance = 0;
    ArrayList<GpsRec> locations = new ArrayList<>();

    TextView text_dist = null;
    TextView text_speed = null;

    // Handler gets created on the UI-thread
    private Handler mHandler = new Handler();

    GoogleApiClient mGoogleApiClient = null;

    public final static String EXTRA_MESSAGE = "com.votors.runningx.MESSAGE";
    private final String TAG = "Button";
    private final int MIN_DISTANCE = 5;
    private final int INTERVAL = 5000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_button);

        // Get a reference to the Press Me Button
        final Button button_start = (Button) findViewById(R.id.button_start);
        final Button button_pause = (Button) findViewById(R.id.button_pause);
        final Button button_map = (Button) findViewById(R.id.button_map);
        final Button button_stop = (Button) findViewById(R.id.button_stop);
        final Button button_chart = (Button) findViewById(R.id.button_chart);
        text_dist = (TextView) findViewById(R.id.button_distance);
        text_speed = (TextView) findViewById(R.id.button_speed);
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        // Called each time the user clicks the Button
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_start.setText("START:" + ++count_start);
                stop = false;
                locations.clear();
                curr_distance = 0;
                getLocation();
                Log.i(TAG, "start onclick..");
            }
        });
        button_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pause) {
                    button_pause.setText("PAUSE:" + ++count_start);
                    pause = false;
                    stop = false;
                    getLocation();

                } else {
                    button_pause.setText("RESUME:" + ++count_start);
                    pause = true;
                    stop = true;
                }

                Log.i(TAG, "pause/resume onclick..");
            }
        });
        final Intent intent = new Intent(this, MapActivity.class);
        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_map.setText("MAP:" + ++count_stop);
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
                Log.i(TAG, "stop onclick..");
                startActivity(intent);
            }
        });
        button_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_chart.setText("CHART:" + ++count_chart);
                Log.i(TAG, "chart onclick..");
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected!!!!!!!!!!!");
        getLocation();
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

    private void getLocation() {
        Log.i(TAG, "call get location.");
        if (start_thread)
            return;

        start_thread = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int cnt = 0;
                while (true) {
                    sleep(INTERVAL);
                    if (stop) continue;

                    Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mLastLocation != null) {
                        double lat = mLastLocation.getLatitude();
                        double lon = mLastLocation.getLongitude();
                        double alt = mLastLocation.getAltitude();
                        saveLocation(mLastLocation);
                    }
                    Log.i(TAG, "cnt=" + cnt++);
                }
            }
        }).start();
    }

    void saveLocation(Location l) {
        float dist = 0, speed = 0;
        Date date = new Date();
        if (locations.size() > 0) {
            GpsRec pre = locations.get(locations.size() - 1);
            dist = pre.loc.distanceTo(l);
            if (dist < MIN_DISTANCE) {
                return;
            }
            speed = dist / ((date.getTime() - pre.getDate().getTime() * 1000));
        }

        GpsRec gps = new GpsRec(date, l);
        gps.distance = dist;
        gps.speed = speed;
        locations.add(gps);
        curr_speed = speed;
        curr_distance += dist;

        mHandler.post(new Runnable() {
            public void run() {
                text_speed.setText(String.format("%.2f m/s", curr_speed));
                text_dist.setText(String.format("%.2f m", curr_distance));
                Log.i(TAG, String.format("%.2f m, %.2f m/s, loc # %d", curr_distance, curr_speed, locations.size()));
            }
        });
    }
}