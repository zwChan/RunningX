package com.votors.runningx;
//EarthQuake

import android.util.Log;
import 	java.util.Date;

/**
 * Created by Jason on 2015/11/26 0026.
 */
public class GpsRec {
    private double lat, lng, alt;
    private Date date;
    public static final String TAG = "GpsRec";

    protected GpsRec(Date date, double lat, double lng, double alt) {
        super();
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.date = date;
        Log.i(TAG, String.format("Location: %s, %.2f, %.2f, %2f", date, lat,lng, alt));
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getAlt() {
        return alt;
    }
    public Date  getDate() {return date;}
}
