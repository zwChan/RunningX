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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.votors.runningx.adapter.NavDrawerListAdapter;
import com.votors.runningx.model.NavDrawerItem;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Jason on 2015/11/27 0027.
 */
public class MainButtonActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
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
    private final int SPEED_AVG = 5;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_button);

        // Get a reference to the Press Me Button
        button_all = (RelativeLayout) findViewById(R.id.button_all);
        final Button button_start = (Button) findViewById(R.id.button_start);
        //final Button button_pause = (Button) findViewById(R.id.button_pause);
        final Button button_map = (Button) findViewById(R.id.button_map);
        final Button button_stop = (Button) findViewById(R.id.button_stop);
        final Button button_chart = (Button) findViewById(R.id.button_chart);
        text_dist = (TextView) findViewById(R.id.button_distance);
        text_speed = (TextView) findViewById(R.id.button_speed);
        text_time = (TextView) findViewById(R.id.button_time);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        createLocationRequest();

        getLastLocation();

        // Called each time the user clicks the Button
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                last_time = System.currentTimeMillis();
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
                if (stop) {
                    //current is stop, we will start it
                    stop = false;
                    button_start.setText(getResources().getString(R.string.pause));
                    startLocationUpdates();
                } else {
                    stop = true;
                    button_start.setText(getResources().getString(R.string.resume));
                    stopLocationUpdates();
                }
                Log.i(TAG, "start/pause/resume onclick..");
            }
        });

        final Intent intent = new Intent(this, MapActivity.class);
        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_map.setText(getResources().getString(R.string.map));
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
                        // XXX: temperary processing
                        //mDrawerList.invalidate();
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
        // Find People
//        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        // Photos
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), true, ""+Record.getRecords(this).size()));
        // Communities, Will add a counter here
//        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        // Pages
//        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        // What's hot, We  will add a counter here
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));

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
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
                //mDrawerList.invalidate();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //mDrawerList.invalidate();
                invalidateViews(mDrawerList);
                adapter.notifyDataSetChanged();
                Log.i(TAG, "get file changed message.");
            }
        };
        IntentFilter filter = new IntentFilter(Record.MSG_RECORD_CHANGED);
        this.registerReceiver(receiver, filter);
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
                disableEnableControls(enable, (ViewGroup)child);
            }
        }
    }
    private void invalidateViews(ViewGroup vg){
        vg.invalidate();
        for (int i = 0; i < vg.getChildCount(); i++){
            View child = vg.getChildAt(i);
            child.invalidate();
            if (child instanceof ViewGroup){
                invalidateViews((ViewGroup)child);
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
//            case 1:
//                fragment = new FriendsFragment();
//                disableEnableControls(false,button_all);
//                break;
            case 1:
                fragment = new TimelineFragment();
                disableEnableControls(false,button_all);
                break;
//            case 3:
//                fragment = new AnalysisFragment();
//                disableEnableControls(false,button_all);
//                break;
//            case 4:
//                fragment = new ConfFragment();
//                disableEnableControls(false,button_all);
//                break;
            case 2:
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
        mLocationRequest.setInterval(INTERVAL_LOCATION*2);
        mLocationRequest.setFastestInterval(INTERVAL_LOCATION);
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
        double alt=0;
        Date date = new Date();
        if (locations.size() > 0) {
            GpsRec pre = locations.get(locations.size() - 1);
            dist = pre.loc.distanceTo(l);
            dist = Math.abs(dist);
            if (dist < MIN_DISTANCE) {
                Log.i(TAG, String.format("dist too small. %f", dist));
                return;
            }
            //get a temporal speed, and it will be corrected by a average speed.
            speed = dist / ((date.getTime() - pre.getDate().getTime()) / 1000);
            alt = l.getAltitude();
        }

        // speed: get the avg speed of SPEED_AVG points
        if (locations.size()>=SPEED_AVG) {
            float dist_avg = dist;
            double alt_avg = alt;
            for (int i=0; i<SPEED_AVG-1; i++) {
                dist_avg += locations.get(locations.size() - 1 - i).distance;
                alt_avg += locations.get(locations.size() - 1 - i).getAlt();
            }
            GpsRec preN = locations.get(locations.size() -SPEED_AVG);
            speed = dist_avg / (1.0f * (date.getTime() - preN.getDate().getTime()) / 1000);
            alt = alt_avg / (SPEED_AVG);
        }

        final GpsRec gps = new GpsRec(date, l);
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
        final Boolean showToast = (int)Math.floor(curr_distance/DISTANCE_SHOWTOAST) != (int)Math.floor((curr_distance-dist)/DISTANCE_SHOWTOAST);
        mHandler.post(new Runnable() {
            public void run() {
                text_speed.setText(String.format("%.2f m/s", curr_speed));
                text_dist.setText(String.format("%.0f m", curr_distance));
                if (showToast) {
                    //Toast.makeText(getApplicationContext(), "!--COME ON--!", Toast.LENGTH_LONG).show();
                }
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
        } else if (!saved && locations.size()>0) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("")
                    .setMessage("You have unsaved running record, continue to exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

        } else  {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), "Tap back button twice in order to exit", Toast.LENGTH_SHORT).show();
            }
            mBackPressed = System.currentTimeMillis();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

}
