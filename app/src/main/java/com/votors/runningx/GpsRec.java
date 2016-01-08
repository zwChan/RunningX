package com.votors.runningx;
//EarthQuake

import android.location.Location;
import android.util.Log;

import com.votors.runningx.transform.GCJPointer;
import com.votors.runningx.transform.WGSPointer;

import java.io.Serializable;
import 	java.util.Date;

/**
 * Created by Jason on 2015/11/26 0026.
 */
public class GpsRec implements Serializable {
    public double lat, lng, alt;
    public float distance;
    public float speed;
    public Date date;
    transient public Location loc;  // only for computing
    public static final String TAG = "GpsRec";
    private GCJPointer gcjPointer;
    protected GpsRec(){}
    protected GpsRec(Date date, Location l) {
        super();
        this.lat = l.getLatitude();
        this.lng = l.getLongitude();
        this.alt = l.getAltitude();
        this.date = date;
        this.loc = l;
        this.gcjPointer = new WGSPointer(l.getLatitude(), l.getLongitude()).toGCJPointer();
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }
    public double getLatChina() {
        return gcjPointer.getLatitude();
    }

    public double getLngChina() {
        return gcjPointer.getLongitude();
    }

    public double getAlt() {
        return alt;
    }
    public Date  getDate() {return date;}

    @Override
    public String toString() {
        return  String.format("Location: %s, %.6f, %.6f, %.2f, dist=%.2f, sp=%f", date, lat,lng, alt, distance,speed);
    }
}
