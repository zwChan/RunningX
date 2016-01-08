package com.votors.runningx;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.votors.runningx.adapter.NavDrawerListAdapter;
import com.votors.runningx.model.NavDrawerItem;

import java.util.ArrayList;
import java.util.Date;


/**
 * Created by Jason on 2015/11/27 0027.
 */
public class MainButtonActivity extends Activity implements android.location.LocationListener {
    Boolean stop = true;
    Boolean saved = true;
    Boolean firstStart = true;
    float curr_speed = 0;
    long total_time = 0;
    long last_time = 0;
    float curr_distance = 0;
    Date startTime;
    ArrayList<GpsRec> locations = new ArrayList<>();
    // the current displaying fragment
    Fragment fragment = null;

    TextView text_dist = null;
    TextView text_speed = null;
    TextView text_time = null;
    RelativeLayout button_all;

    Button button_share;

    // Handler gets created on the UI-thread
    private Handler mHandler = new Handler();

    public final static String EXTRA_MESSAGE = "com.votors.runningx.MESSAGE";
    public final static String EXTRA_GpsRec = "com.votors.runningx.GpsRec";
    private static final String BC_INTENT = "com.votors.runningx.BroadcastReceiver.location";

    private final String TAG = "Button";

    //side menu
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    // nav drawer title
    private CharSequence mDrawerTitle;
    // used to store app title
    private CharSequence mTitle;
    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    BroadcastReceiver receiver_history = null;
    BroadcastReceiver receiver_conf = null;
    LocationManager manager;

    LinearRegression lr = new LinearRegression();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_button);
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Conf.init(getApplicationContext());

        // Get a reference to the Press Me Button
        button_all = (RelativeLayout) findViewById(R.id.button_all);
        final Button button_start = (Button) findViewById(R.id.button_start);
        //final Button button_pause = (Button) findViewById(R.id.button_pause);
        final Button button_map = (Button) findViewById(R.id.button_map);
        final Button button_stop = (Button) findViewById(R.id.button_stop);
        final Button button_chart = (Button) findViewById(R.id.button_chart);
        //button_share = (Button) findViewById(R.id.share);

        text_dist = (TextView) findViewById(R.id.button_distance);
        text_speed = (TextView) findViewById(R.id.button_speed);
        text_time = (TextView) findViewById(R.id.button_time);

        //getLastLocation();

        // Called each time the user clicks the Button
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildAlertMessageNoGps();
                last_time = System.currentTimeMillis();
                if (stop) {
                    //current is stop, we will start it
                    if (startLocationUpdates()) {
                        stop = false;
                        button_start.setText(getResources().getString(R.string.pause));
                    }else{
                        // start location listening fail.
                        return;
                    }
                } else {
                    stop = true;
                    button_start.setText(getResources().getString(R.string.resume));
                    stopLocationUpdates();
                }
                if (firstStart) {
                    firstStart = false;
                    locations.clear();
                    curr_distance = 0;
                    curr_speed = 0;
                    total_time = 0;
                    saved = false;
                    startTime = new Date();
                    button_stop.setText(getResources().getString(R.string.stop));
                }

                Log.i(TAG, "start/pause/resume onclick..");
            }
        });

        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_map.setText(getResources().getString(R.string.map));
                Intent intent = Conf.getMapIntent();
                intent.putExtra(EXTRA_MESSAGE, locations);
                Log.i(TAG, "MAP onclick..");
                startActivity(intent);
            }
        });
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stop) {
                    // from start to stop
                    Intent intent = Conf.getMapIntent();
                    intent.putExtra(EXTRA_MESSAGE, locations);
                    stop = true;
                    firstStart = true;
                    button_start.setText(getResources().getString(R.string.start));
                    stopLocationUpdates();
                    Log.i(TAG, "stop onclick..");
                    startActivity(intent);
                    button_stop.setText(getResources().getString(R.string.save));
                }else{
                    // from stop to save
                    if (!saved && locations.size()>0) {
                        saved = true;
                        Record record = new Record(getApplicationContext());
                        record.user = "default";
                        record.startTime = startTime;
                        record.usedTime = total_time;
                        record.distance = curr_distance;
                        record.gpsRecs = locations;
                        record.save();
                        button_stop.setText(getResources().getString(R.string.saved));
                    }else{
                        Toast.makeText(getBaseContext(), getResources().getString(R.string.no_data_save), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        final Intent intent_chart = new Intent(this, ChartActivity.class);
        button_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_chart.setText(getResources().getString(R.string.chart));
                Log.i(TAG, "chart onclick..");
                intent_chart.putExtra(EXTRA_MESSAGE, locations);
                startActivity(intent_chart);
            }
        });
        /*button_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //button_chart.setText(getResources().getString(R.string.chart));
                Log.i(TAG, "share onclick..");
                showShare();
            }
        });*/
        // update the time
        Thread thread = new Thread()
        {
            @Override
            public void run() {
            try {
                while(true) {
                    sleep(1000);
                    if (stop) continue;
                    total_time += System.currentTimeMillis() - last_time;
                    last_time = System.currentTimeMillis();
                    long total_time_tmp = total_time / 1000;
                    final String timeStr = String.format("%d:%02d:%02d", total_time_tmp / 3600, total_time_tmp % 3600 / 60, total_time_tmp % 3600 % 60);
                    mHandler.post(new Runnable() {
                        public void run() {
                            text_time.setText(timeStr);
                        }
                    });
                }
            } catch (InterruptedException e) {}
            }
        };
        thread.start();

        //side menu
        mTitle = mDrawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        // adding nav drawer items to array
        // Home
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), true, ""+Record.getRecords(this).size()));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));

        // Recycle the typed array
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        receiver_history = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                invalidateViews(mDrawerList);
                adapter.notifyDataSetChanged();
                Log.i(TAG, "get file changed message.");
            }
        };
        IntentFilter filter = new IntentFilter(Record.MSG_RECORD_CHANGED);
        this.registerReceiver(receiver_history, filter);

        receiver_conf = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!stop) {
                    Conf.read();
                    stopLocationUpdates();
                    startLocationUpdates();
                    Log.i(TAG, "get conf changed message.");
                }
            }
        };
        IntentFilter filter_conf = new IntentFilter(ConfFragment.CONF_MESSAGE);
        this.registerReceiver(receiver_conf, filter_conf);
    }
    private void buildAlertMessageNoGps() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.gps_prompt))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    /**
     * Slide menu item click listener
     * */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* *
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        //boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * enable or disable the whole layout
     */
    private void disableEnableControls(boolean enable, ViewGroup vg){
        for (int i = 0; i < vg.getChildCount(); i++){
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (enable)
                child.setVisibility(View.VISIBLE);
            else
                child.setVisibility(View.INVISIBLE);
            if (child instanceof ViewGroup){
                disableEnableControls(enable, (ViewGroup) child);
            }
        }
    }
    private void invalidateViews(ViewGroup vg){
        vg.invalidate();
        for (int i = 0; i < vg.getChildCount(); i++){
            View child = vg.getChildAt(i);
            child.invalidate();
            if (child instanceof ViewGroup){
                invalidateViews((ViewGroup) child);
            }
        }
    }
    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    private void displayView(int position) {
        // update the main content by replacing fragments
        //fragment = null;
        switch (position) {
            case 0:
                backMainView();
                mDrawerLayout.closeDrawer(mDrawerList);
                fragment = null;
                break;
            case 1:
                fragment = new TimelineFragment();
                disableEnableControls(false,button_all);
                break;
            case 2:
                fragment = new ConfFragment();
                disableEnableControls(false,button_all);
                break;
            case 3:
                fragment = new AboutUsFragment();
                disableEnableControls(false,button_all);
                break;

            default:
                fragment = null;
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            // error in creating fragment
            Log.i(TAG, "No fragment found");
        }
    }

    /**
     * Go back to main view when press 'back'.
     * @return true if we coma back to main view from other view, else return false
     */
    boolean backMainView() {
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .remove(fragment).commit();
            fragment = null;

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(0, true);
            mDrawerList.setSelection(0);
            setTitle(navMenuTitles[0]);
            mDrawerLayout.closeDrawer(mDrawerList);
            disableEnableControls(true, button_all);
            getActionBar().setTitle(mTitle);
            return true;
        } if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
            disableEnableControls(true, button_all);
            getActionBar().setTitle(mTitle);
            return true;
        } else {
            Log.i(TAG, "currently should already in main view.");
        }
        return false;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void sleep(int t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected boolean startLocationUpdates() {
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    Conf.INTERVAL_LOCATION * 1000L, Conf.MIN_DISTANCE * 0.1f, this);
            return true;
        } catch (SecurityException e) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.gps_no_permision), Toast.LENGTH_LONG).show();
            return false;
        }catch (Exception eall) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.gps_listen_fail) + eall.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
    protected boolean stopLocationUpdates() {
        try {
            manager.removeUpdates(this);
            return true;
        } catch (SecurityException e) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.gps_no_permision), Toast.LENGTH_LONG).show();
            return false;
        }catch (Exception eall) {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.gps_listen_fail) + eall.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        saveLocation(location);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){}
    @Override
    public void onProviderEnabled(String provider){}
    @Override
    public void onProviderDisabled(String provider){}

    synchronized private void getLastLocation() {
        Log.i(TAG, "call get last location.");
        try {
            Location mLastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (mLastLocation != null) {
                double lat = mLastLocation.getLatitude();
                double lon = mLastLocation.getLongitude();
                double alt = mLastLocation.getAltitude();
                saveLocation(mLastLocation);
            }
        }catch (SecurityException e) {}
    }

    long preLowAccuracyTime;
    int lowAccuracyCnt = 0;
    void saveLocation(Location l) {
        float dist = 0, speed = 0;
        double alt=0;
        if (l==null)return;
        Date date = new Date(l.getTime());
        if (Conf.GPS_ONLY && !l.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "Get location not from gps, drop it. "+l.getProvider());
            return;
        }

        if (l.hasAccuracy() && l.getAccuracy() > Conf.LOCATION_ACCURACY) {
            Log.i(TAG, String.format(getResources().getString(R.string.gps_precision_low), l.getAccuracy()));
            if  (date.getTime()-preLowAccuracyTime < Conf.INTERVAL_LOCATION * 5 * 1000) {
                lowAccuracyCnt++;
            } else {
                if (lowAccuracyCnt > 2) {
                    // low accuracy warning
                    mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.gps_precision_low2), Conf.LOCATION_ACCURACY), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                lowAccuracyCnt = 0;
                preLowAccuracyTime = date.getTime();
            }
            return;
        }

        final GpsRec gps = new GpsRec(date, l);
        if (locations.size() >= 3-1) {
            lr.fix(locations,gps, 3);
            l.setLatitude(gps.lat);
            l.setLongitude(gps.lng);
        }

        if (locations.size() > 0) {
            GpsRec pre = locations.get(locations.size() - 1);
            dist = pre.loc.distanceTo(l);
            dist = Math.abs(dist);
            /*if (dist < Conf.MIN_DISTANCE) {
                Log.i(TAG, String.format("dist too small. %f", dist));
                return;
            }*/

            //limit the distance acceleration with a factor.
/*            if (Conf.ACCELERATE_FACTOR > 0 && locations.size()>1) {
                float maxDistance = pre.speed * (date.getTime() - pre.date.getTime())/1000 * (1+Conf.ACCELERATE_FACTOR);
                float minDistance = pre.speed * (date.getTime() - pre.date.getTime())/1000 * (1/(1+Conf.ACCELERATE_FACTOR));
                if (dist > maxDistance || dist < minDistance) {
                    float ration = 0;
                    if (dist>maxDistance)ration = maxDistance/dist;
                    if (dist<minDistance)ration = dist/minDistance;
                    gps.lat = pre.lat + (gps.lat-pre.lat) * ration;
                    gps.lng = pre.lng + (gps.lng-pre.lng) * ration;
                    gps.loc.setLatitude(gps.lat);
                    gps.loc.setLongitude(gps.lng);
                    Log.i(TAG, String.format("location reset by factor: reduce %f, distance %f to %f", ration, dist,maxDistance));
                    dist = maxDistance;
                }
            }*/

            //get a temporal speed, and it will be corrected by a average speed.
            speed = dist / (1.0f * (date.getTime() - pre.getDate().getTime()) / 1000);
            if (l.hasAltitude()){
                alt = l.getAltitude();
            }else{
                alt = Conf.INVALID_ALT;
            }
        }

        // speed: get the avg speed of SPEED_AVG points
        if (Conf.SPEED_AVG > 0 && locations.size()>=Conf.SPEED_AVG-1) {
            /*float dist_avg = dist;
            for (int i=0; i<Conf.SPEED_AVG-1; i++) {
                dist_avg += locations.get(locations.size() - 1 - i).distance;
            }*/
            GpsRec preN = locations.get(locations.size() - (Conf.SPEED_AVG - 1));
            float dist_avg = l.distanceTo(preN.loc);
            dist_avg = Math.abs(dist_avg);
            speed = dist_avg / (1.0f * (date.getTime() - preN.getDate().getTime()) / 1000);
            //dist = dist_avg/Conf.SPEED_AVG;
        }

        gps.distance = dist;
        gps.speed = speed;
        gps.alt = alt;
        locations.add(gps);
        curr_speed = speed;
        curr_distance += dist;
        total_time += date.getTime() - last_time;
        last_time = date.getTime();
        Log.i(TAG, String.format("%s", gps.toString()));


        // Do something in the main thread about the views.
        //final Boolean showToast = (int)Math.floor(curr_distance/DISTANCE_SHOWTOAST) != (int)Math.floor((curr_distance-dist)/DISTANCE_SHOWTOAST);
        mHandler.post(new Runnable() {
            public void run() {
                text_speed.setText(String.format("%s %s", Conf.getSpeedString(curr_speed), Conf.getSpeedUnit()));
                text_dist.setText(String.format("%.2f %s", Conf.getDistance(curr_distance), Conf.getDistanceUnit()));
                Log.i(TAG, String.format("%.2f m, %.2f m/s, loc # %d", curr_distance, curr_speed, locations.size()));
            }
        });

        //broadcast a message
        Intent msg = new Intent(BC_INTENT);
        msg.putExtra(EXTRA_GpsRec, gps);
        sendOrderedBroadcast(msg,null);
    }

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    // To exit, you have to press back twice
    @Override
    public void onBackPressed()
    {
        if (backMainView()) {
            return;
        } else if (!stop || (!saved && locations.size()>0)) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getResources().getString(R.string.exit_prompt))
                    .setMessage(getResources().getString(R.string.exit_prompt))
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), null)
                    .show();

        } else  {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.exit_prompt2), Toast.LENGTH_SHORT).show();
            }
            mBackPressed = System.currentTimeMillis();
        }
    }


    @Override
    protected void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
//        mGoogleApiClient.disconnect();
        this.unregisterReceiver(receiver_history);
        this.unregisterReceiver(receiver_conf);
    }
    @Override
    public void onResume() {
        super.onResume();
    }
}

// no used
/*
class KalmanLatLong {
    private final float MinAccuracy = 1;

    private float Q_metres_per_second;
    private long TimeStamp_milliseconds;
    private double lat;
    private double lng;
    private float variance; // P matrix.  Negative means object uninitialised.  NB: units irrelevant, as long as same units used throughout

    public KalmanLatLong(float Q_metres_per_second) { this.Q_metres_per_second = Q_metres_per_second; variance = -1; }

    public long get_TimeStamp() { return TimeStamp_milliseconds; }
    public double get_lat() { return lat; }
    public double get_lng() { return lng; }
    public float get_accuracy() { return (float)Math.sqrt(variance); }

    public void SetState(double lat, double lng, float accuracy, long TimeStamp_milliseconds) {
        this.lat=lat; this.lng=lng; variance = accuracy * accuracy; this.TimeStamp_milliseconds=TimeStamp_milliseconds;
    }

    /// <summary>
    /// Kalman filter processing for lattitude and longitude
    /// </summary>
    /// <param name="lat_measurement_degrees">new measurement of lattidude</param>
    /// <param name="lng_measurement">new measurement of longitude</param>
    /// <param name="accuracy">measurement of 1 standard deviation error in metres</param>
    /// <param name="TimeStamp_milliseconds">time of measurement</param>
    /// <returns>new state</returns>
    public void Process(double lat_measurement, double lng_measurement, float accuracy, long TimeStamp_milliseconds) {
        if (accuracy < MinAccuracy) accuracy = MinAccuracy;
        if (variance < 0) {
            // if variance < 0, object is unitialised, so initialise with current values
            this.TimeStamp_milliseconds = TimeStamp_milliseconds;
            lat=lat_measurement; lng = lng_measurement; variance = accuracy*accuracy;
        } else {
            // else apply Kalman filter methodology

            long TimeInc_milliseconds = TimeStamp_milliseconds - this.TimeStamp_milliseconds;
            if (TimeInc_milliseconds > 0) {
                // time has moved on, so the uncertainty in the current position increases
                variance += TimeInc_milliseconds * Q_metres_per_second * Q_metres_per_second / 1000;
                this.TimeStamp_milliseconds = TimeStamp_milliseconds;
                // TO DO: USE VELOCITY INFORMATION HERE TO GET A BETTER ESTIMATE OF CURRENT POSITION
            }

            // Kalman gain matrix K = Covarariance * Inverse(Covariance + MeasurementVariance)
            // NB: because K is dimensionless, it doesn't matter that variance has different units to lat and lng
            float K = variance / (variance + accuracy * accuracy);
            // apply K
            lat += K * (lat_measurement - lat);
            lng += K * (lng_measurement - lng);
            // new Covarariance  matrix is (IdentityMatrix - K) * Covarariance
            variance = (1 - K) * variance;
        }
    }
}*/
