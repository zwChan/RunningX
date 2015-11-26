package com.votors.runningx;
//EarthQuake

import android.util.Log;
import 	java.util.Date;

/**
 * Created by Jason on 2015/11/26 0026.
 */
public class GpsRec {
    private double lat, lng, magnitude;
    private Date date;
    public static final String TAG = "GpsRec";

    protected GpsRec(Date date, double lat, double lng, double magnitude) {
        super();
        this.lat = lat;
        this.lng = lng;
        this.magnitude = magnitude;
        this.date = date;
        Log.i(TAG, String.format("Location: %.2f, %.2f, %2f", lat,lng, magnitude));
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getMagnitude() {
        return magnitude;
    }
    public Date  getDate() {return date;}
}
