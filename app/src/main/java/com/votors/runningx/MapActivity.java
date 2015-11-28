package com.votors.runningx;
/**
 * Created by Jason on 2015/11/26 0026.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity {

    // Coordinates used for centering the Map
    public final static String EXTRA_MESSAGE = "com.votors.runningx.MESSAGE";
    private static final String BC_INTENT = "com.votors.runningx.BroadcastReceiver.location";
    public final static String EXTRA_GpsRec = "com.votors.runningx.GpsRec";

    public final static int DISTAN_MARK = 100;
    public final static int ZOOM_LEVEL = 15;
    // The Map Object
    private GoogleMap mMap;

    public static final String TAG = "MapActivity";

    private final LocationReceiver mReceiver = new LocationReceiver();
    private final IntentFilter intentFilter = new IntentFilter(BC_INTENT);
    ArrayList<GpsRec> locations = null;
    double total_dist = 0;
    double center_lat = 0;
    double center_lng = 0;

    int movePointCnt = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_map);
        locations = (ArrayList<GpsRec>)getIntent().getSerializableExtra(EXTRA_MESSAGE);

//        intentFilter.setPriority(3);
        registerReceiver(mReceiver, intentFilter);

        Log.i(TAG, "location numbler: " + locations.size());

        // Get Map Object
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        final PolylineOptions polylines = new PolylineOptions();
        polylines.color(Color.BLUE).width(10);
        /*line = map.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.RED)*/

        if (null != mMap && locations != null) {
            // Add a marker for every earthquake
            int cnt = 0;
            for (GpsRec rec: locations) {
                Log.i(TAG, rec.toString());
                cnt++;
                if (cnt==1 || cnt == locations.size() || Math.floor(total_dist/DISTAN_MARK) != Math.floor(total_dist+rec.distance/DISTAN_MARK)) {
                    // Add a new marker
                    MarkerOptions mk = new MarkerOptions()
                            .position(new LatLng(rec.getLat(), rec.getLng()));

                    // Set the title of the Marker's information window
                    if (cnt==1) {
                        mk.title(String.valueOf("start"));
                    }else{
                        mk.title(String.format("%.0fm,%.1fm/s", Math.floor(total_dist + rec.distance), rec.speed));
                    }

                    // Set the color for the Marker
                    mk.icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(rec.speed)));
                    mMap.addMarker(mk);
                }
                total_dist += rec.distance;
                center_lat += rec.getLat();
                center_lng += rec.getLng();

                polylines.add(new LatLng(rec.getLat(),rec.getLng()));
            }
        }

        // Center the map, draw the path
        // Should compute map center from the actual data
        mMap.addPolyline(polylines);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(center_lat / locations.size(), center_lng / locations.size())));
        int zoom = ZOOM_LEVEL;
        if (total_dist < 500) zoom++;
        if (total_dist < 5000) zoom++;
        if (total_dist > 5000) zoom--;
        if (total_dist > 50000) zoom--;
        mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // hue: [0,360)
    private float getMarkerColor(float speed) {

        if (speed < 1) {
            speed = 1;
        } else if (speed > 9.9) {
            speed = 9.9F;
        }

        return (36 * speed);
    }

    public class LocationReceiver extends BroadcastReceiver {
        private final String TAG = "LocationReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            GpsRec rec = (GpsRec)intent.getSerializableExtra(EXTRA_GpsRec);
            Log.i(TAG, "LocationReceiver, location " + rec.toString());

            final PolylineOptions polylines = new PolylineOptions();
            GpsRec last;
            polylines.color(Color.BLUE).width(10);
            if (locations.size()>0) {
                last = locations.get(locations.size() - 1);
                polylines.add(new LatLng(last.getLat(),last.getLng()));
            }
            if (Math.floor(total_dist/DISTAN_MARK) != Math.floor(total_dist+rec.distance/DISTAN_MARK)) {
                // Add a new marker
                MarkerOptions mk = new MarkerOptions()
                        .position(new LatLng(rec.getLat(), rec.getLng()));

                // Set the title of the Marker's information window
                mk.title(String.format("%.0fm,%.1fm/s",Math.floor(total_dist + rec.distance),rec.speed));
                // Set the color for the Marker
                mk.icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(rec.speed)));
                mMap.addMarker(mk);
            }
            movePointCnt++;
            total_dist += rec.distance;
            center_lat += rec.getLat();
            center_lng += rec.getLng();
            locations.add(rec);

            polylines.add(new LatLng(rec.getLat(), rec.getLng()));
            mMap.addPolyline(polylines);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(rec.getLat(), rec.getLng())));
            if (movePointCnt == 1)mMap.moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL+2));
        }

    }
}
