package com.votors.runningx;
/**
 * Created by Jason on 2015/11/26 0026.
 */

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
import java.util.Date;

public class MapActivity extends AppCompatActivity {

    // Coordinates used for centering the Map
    public final static String EXTRA_MESSAGE = "com.votors.runningx.MESSAGE";
    public final static int DISTAN_MARK = 1000;
    public final static int ZOOM_LEVEL = 15;
    // The Map Object
    private GoogleMap mMap;

    public static final String TAG = "MapActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_map);
        ArrayList<GpsRec> locations = (ArrayList<GpsRec>)getIntent().getSerializableExtra(EXTRA_MESSAGE);

        Log.i(TAG, "location numbler: " + locations.size());

        // Get Map Object
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        double center_lat = 0;
        double center_lng = 0;
        double dist = 0;

        final PolylineOptions polylines = new PolylineOptions();
        polylines.color(Color.BLUE).width(5);
        /*line = map.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.RED)*/

        if (null != mMap && locations != null) {
            // Add a marker for every earthquake
            for (GpsRec rec: locations) {
                Log.i(TAG, rec.toString());

                // Add a new marker
                MarkerOptions mk = new MarkerOptions()
                        // Set the Marker's position
                        .position(new LatLng(rec.getLat(), rec.getLng()));

                if (Math.floor(dist/DISTAN_MARK) != Math.floor(dist+rec.distance/DISTAN_MARK)) {
                    // Set the title of the Marker's information window
                    mk.title(String.valueOf(Math.floor(dist+rec.distance)));
                }

                // Set the color for the Marker
                mk.icon(BitmapDescriptorFactory.defaultMarker(getMarkerColor(rec.speed)));

                mMap.addMarker(mk);

                dist += rec.distance;
                center_lat += rec.getLat();
                center_lng += rec.getLng();

                polylines.add(new LatLng(rec.getLat(),rec.getLng()));
            }
        }

        // Center the map
        // Should compute map center from the actual data
        mMap.addPolyline(polylines);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(center_lat / locations.size(), center_lng / locations.size())));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(ZOOM_LEVEL));
    }

    private float getMarkerColor(float speed) {

        if (speed < 1) {
            speed = 1;
        } else if (speed > 10) {
            speed = 10;
        }

        return (36 * speed);
    }


}
