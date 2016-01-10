package com.votors.runningx;
//EarthQuake

import android.location.Location;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

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
    transient static private GCJPointer gcjPointer; //  only for location transform.

    protected GpsRec() {
    }

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

    synchronized public double getLatChina() {
        if (gcjPointer == null) {
            gcjPointer = new GCJPointer(lat, lng);
        } else {
            gcjPointer.latitude = lat;
            gcjPointer.longitude = lng;
        }
        return gcjPointer.getLatitude();
    }

    synchronized public double getLngChina() {
        if (gcjPointer == null) {
            gcjPointer = new GCJPointer(lat, lng);
        } else {
            gcjPointer.latitude = lat;
            gcjPointer.longitude = lng;
        }
        return gcjPointer.getLongitude();
    }

    public double getAlt() {
        return alt;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return String.format("Location: %s, %.6f, %.6f, %.2f, dist=%.2f, sp=%f", date, lat, lng, alt, distance, speed);
    }

    private static class GCJPointer extends GeoPointer {


        public GCJPointer() {
        }

        public GCJPointer(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        private WGSPointer toWGSPointer() {
            if (TransformUtil.outOfChina(this.latitude, this.longitude)) {
                return new WGSPointer(this.latitude, this.longitude);
            }
            double[] delta = TransformUtil.delta(this.latitude, this.longitude);
            return new WGSPointer(this.latitude - delta[0], this.longitude - delta[1]);
        }

        public WGSPointer toExactWGSPointer() {
            final double initDelta = 0.01;
            final double threshold = 0.000001;
            double dLat = initDelta, dLng = initDelta;
            double mLat = this.latitude - dLat, mLng = this.longitude - dLng;
            double pLat = this.latitude + dLat, pLng = this.longitude + dLng;
            double wgsLat, wgsLng;
            WGSPointer currentWGSPointer = null;
            for (int i = 0; i < 30; i++) {
                wgsLat = (mLat + pLat) / 2;
                wgsLng = (mLng + pLng) / 2;
                currentWGSPointer = new WGSPointer(wgsLat, wgsLng);
                GCJPointer tmp = currentWGSPointer.toGCJPointer();
                dLat = tmp.getLatitude() - this.getLatitude();
                dLng = tmp.getLongitude() - this.getLongitude();
                if ((Math.abs(dLat) < threshold) && (Math.abs(dLng) < threshold)) {
                    return currentWGSPointer;
                } else {
                    System.out.println(dLat + ":" + dLng);
                }
                if (dLat > 0) {
                    pLat = wgsLat;
                } else {
                    mLat = wgsLat;
                }
                if (dLng > 0) {
                    pLng = wgsLng;
                } else {
                    mLng = wgsLng;
                }
            }
            return currentWGSPointer;
        }
    }

    private static class GeoPointer {
        static DecimalFormat df = new DecimalFormat("0.000000");
        double longitude;
        double latitude;

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else {
                if (other instanceof GeoPointer) {
                    GeoPointer otherPointer = (GeoPointer) other;
                    return df.format(latitude).equals(df.format(otherPointer.latitude))
                            && df.format(longitude).equals(df.format(otherPointer.longitude));
                } else {
                    return false;
                }
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("latitude:" + latitude);
            sb.append(" longitude:" + longitude);
            return sb.toString();
        }

        public double distance(GeoPointer target) {
            double earthR = 6371000;
            double x =
                    Math.cos(this.latitude * Math.PI / 180) * Math.cos(target.latitude * Math.PI / 180)
                            * Math.cos((this.longitude - target.longitude) * Math.PI / 180);
            double y = Math.sin(this.latitude * Math.PI / 180) * Math.sin(target.latitude * Math.PI / 180);
            double s = x + y;
            if (s > 1) {
                s = 1;
            }
            if (s < -1) {
                s = -1;
            }
            double alpha = Math.acos(s);
            double distance = alpha * earthR;
            return distance;
        }
    }

    private static class TransformUtil {
        public static boolean outOfChina(double lat, double lng) {
            if ((lng < 72.004) || (lng > 137.8347)) {
                return true;
            }
            if ((lat < 0.8293) || (lat > 55.8271)) {
                return true;
            }
            return false;
        }

        public static double transformLat(double x, double y) {
            double ret =
                    -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
            return ret;
        }

        public static double transformLon(double x, double y) {
            double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret +=
                    (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
            return ret;
        }

        public static double[] delta(double lat, double lng) {
            double[] delta = new double[2];
            double a = 6378245.0;
            double ee = 0.00669342162296594323;
            double dLat = transformLat(lng - 105.0, lat - 35.0);
            double dLng = transformLon(lng - 105.0, lat - 35.0);
            double radLat = lat / 180.0 * Math.PI;
            double magic = Math.sin(radLat);
            magic = 1 - ee * magic * magic;
            double sqrtMagic = Math.sqrt(magic);
            delta[0] = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
            delta[1] = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
            return delta;
        }
    }

    public static class WGSPointer extends GeoPointer {

        public WGSPointer() {
        }

        public WGSPointer(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public GCJPointer toGCJPointer() {
            if (TransformUtil.outOfChina(this.latitude, this.longitude)) {
                return new GCJPointer(this.latitude, this.longitude);
            }
            double[] delta = TransformUtil.delta(this.latitude, this.longitude);
            return new GCJPointer(this.latitude + delta[0], this.longitude + delta[1]);
        }
    }
}
