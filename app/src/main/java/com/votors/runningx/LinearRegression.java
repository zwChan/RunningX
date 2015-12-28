package com.votors.runningx;

import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Jason on 2015/12/24 0024.
 */
public class LinearRegression {

    public static final String TAG = "LinearRegression";

    class Point {
        double x; double y;
        Point(double x, double y){this.x=x;this.y=y;}
    }
    // see http://introcs.cs.princeton.edu/java/97data/LinearRegression.java
    // directly correct the new point
    public  void fix(ArrayList<GpsRec> recs, GpsRec newP, int avgN) {
        int MAXN = avgN;
        int n = 0;
        double[] x = new double[MAXN];
        double[] y = new double[MAXN];

        // first pass: read in data, compute xbar and ybar
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        int ii=0;
        while(ii<MAXN-1) {
            x[n] = recs.get(ii).lat;
            y[n] = recs.get(ii).lng;
            sumx  += x[n];
            sumx2 += x[n] * x[n];
            sumy  += y[n];
            n++;
            ii++;
        }

        // add the new one
        x[n] = newP.lat;
        y[n] = newP.lng;
        sumx  += x[n];
        sumx2 += x[n] * x[n];
        sumy  += y[n];
        n++;

        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;

        Point first = new Point(recs.get(0).lat, recs.get(0).lat*beta1+beta0);
        Point last = new Point(newP.lat, newP.lat*beta1+beta0);

        Point fixPoint = getProjectedPointOnLineFast(first,last, new Point(newP.lat,newP.lng));
        Log.i(TAG, "y   = " + beta1 + " * x + " + beta0);
        Log.i(TAG, String.format("fix: input (%f,%f), output(%f,%f)", newP.lat,newP.lng,fixPoint.x,fixPoint.y));
        newP.lat = fixPoint.x;
        newP.lng = fixPoint.y;
    }

    // FIXME: RETURN NAN.
    /** see http://www.sunshine2k.de/coding/java/PointOnLine/PointOnLine.html
     * Get projected point P' of P on line e1. Faster version.
     * @return projected point p.
     */
    /*private Point getProjectedPointOnLine(Point v1,Point v2,Point p)
    {
        // get dot product of e1, e2
        Point e1 = new Point(v2.x - v1.x, v2.y - v1.y);
        Point e2 = new Point(p.x - v1.x, p.y - v1.y);
        double valDp = dotProduct(e1, e2);
        // get length of vectors
        double lenLineE1 = Math.sqrt(e1.x * e1.x + e1.y * e1.y);
        double lenLineE2 = Math.sqrt(e2.x * e2.x + e2.y * e2.y);
        double cos = valDp / (lenLineE1 * lenLineE2);
        // length of v1P'
        double projLenOfLine = cos * lenLineE2;
        Point pp = new Point((v1.x + (projLenOfLine * e1.x) / lenLineE1),
                (v1.y + (projLenOfLine * e1.y) / lenLineE1));
        return pp;
    }*/
    public Point getProjectedPointOnLineFast(Point v1,Point v2,Point p)
    {
        // get dot product of e1, e2
        Point e1 = new Point(v2.x - v1.x, v2.y - v1.y);
        Point e2 = new Point(p.x - v1.x, p.y - v1.y);
        double valDp = dotProduct(e1, e2);
        // get squared length of e1
        double len2 = e1.x * e1.x + e1.y * e1.y;
        Log.i(TAG, "get projection len2 " + len2);
        if (len2 < 0.00000001) return p;
        Point pp = new Point((v1.x + (valDp * e1.x) / len2),
                (v1.y + (valDp * e1.y) / len2));
        return pp;
    }
    private double dotProduct (Point p1, Point p2) {
        return p1.x*p2.x+p1.y*p2.y;
    }
}
