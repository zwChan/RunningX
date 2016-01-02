package com.votors.runningx;

/**
 * Created by Jason on 2015/11/28 0028.
 */

import android.app.Activity;
import android.os.Bundle;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.util.ArrayList;

public class ChartActivity extends Activity{
    public final static String EXTRA_MESSAGE = "com.votors.runningx.MESSAGE";
    private static final String BC_INTENT = "com.votors.runningx.BroadcastReceiver.location";
    public final static String EXTRA_GpsRec = "com.votors.runningx.GpsRec";

    public static final String TAG = "ChartActivity";

    //private final MapActivity.LocationReceiver mReceiver = new MapActivity.LocationReceiver();
    //private final IntentFilter intentFilter = new IntentFilter(BC_INTENT);
    ArrayList<GpsRec> locations = null;
    float curr_dist = 0;
    float total_dist = 0;
    long total_time = 0; //second

    int movePointCnt = 0;

    private XYPlot plotPace;
    private XYPlot plotAlt;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Conf.init(getApplicationContext());
        // fun little snippet that prevents users from taking screenshots
        // on ICS+ devices :-)
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);*/
        setContentView(R.layout.main_chart);
        locations = (ArrayList<GpsRec>)getIntent().getSerializableExtra(EXTRA_MESSAGE);
        ArrayList<Float> xDist = new ArrayList<>();
        ArrayList<Float> yPace = new ArrayList<>();
        ArrayList<Integer> yAlt = new ArrayList<>();

        float pre_speed = 0;
        float alt = 0;
        float speed = 0;
        float yMaxPace = 0;
        int yMaxAlt = 0;
        ArrayList<Float> avgAlt = new ArrayList<>();
        ArrayList<Float> avgSpeed = new ArrayList<>();
        if (locations.size()>0)
            total_time = (locations.get(locations.size()-1).getDate().getTime()-locations.get(0).getDate().getTime())/1000;
        for(GpsRec r:locations) total_dist += r.distance;

        float avgSpeedAll = total_dist/total_time;


        for (GpsRec rec: locations) {
            curr_dist += rec.distance;

            avgSpeed.add(rec.speed);
            if (avgSpeed.size()>Conf.SPEED_AVG)avgSpeed.remove(0);
            speed = 0;
            for(float s: avgSpeed)speed += s;
            speed /= avgSpeed.size();
            if (Conf.ACCELERATE_FACTOR>0 && pre_speed>0 && speed>pre_speed*(1+Conf.ACCELERATE_FACTOR))speed=pre_speed*(1+Conf.ACCELERATE_FACTOR);
            if (Conf.ACCELERATE_FACTOR>0 && pre_speed>0 && speed<pre_speed*(1/(1+Conf.ACCELERATE_FACTOR)))speed=pre_speed*(1/(1+Conf.ACCELERATE_FACTOR));
            pre_speed = speed;

            if (rec.getAlt() != Conf.INVALID_ALT) {
                avgAlt.add((float)rec.getAlt());
                if (avgAlt.size()>Conf.SPEED_AVG)avgAlt.remove(0);
                alt = 0;
                for (float t: avgAlt) alt += t;
                alt /= avgAlt.size();
            }

            //xDist.add(Math.round(Conf.getDistance(getApplicationContext(), curr_dist)));
            if (locations.indexOf(rec)>= Conf.SPEED_AVG) {
                xDist.add((Conf.getDistance(curr_dist)));
                float y_pace = Conf.getSpeed( speed, avgSpeedAll);
                if (y_pace>yMaxPace) yMaxPace=y_pace;
                yPace.add(y_pace);
                int y_alt = Math.round(Conf.getAltitude(alt));
                if (y_alt>0) yMaxAlt = y_alt;
                yAlt.add(y_alt);
            } else {
                /*yPace.add(0f);
                yAlt.add(0);*/
            }
        }

        // initialize our XYPlot reference:
        plotPace = (XYPlot) findViewById(R.id.pace);
        plotPace.setTitle(Conf.SPEED_TYPE);
        plotPace.setRangeLabel(Conf.getSpeedUnit());
        plotPace.setDomainLabel(Conf.getDistanceUnit());

        plotAlt = (XYPlot) findViewById(R.id.altitude);
        plotAlt.setRangeLabel(Conf.getAltitudeUnit());
        plotAlt.setDomainLabel(Conf.getDistanceUnit());

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                xDist,          // SimpleXYSeries takes a List so turn our array into a List
                yPace,
                "");                             // Set the display title of the series
        XYSeries series2 = new SimpleXYSeries(
                xDist,          // SimpleXYSeries takes a List so turn our array into a List
                yAlt,
                "");                             // Set the display title of the series

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        plotPace.addSeries(series1, series1Format);
        plotAlt.addSeries(series2, series1Format);

        // reduce the number of range labels
        plotPace.setTicksPerRangeLabel(3);
        plotAlt.setTicksPerRangeLabel(3);
        plotPace.getGraphWidget().setDomainLabelOrientation(-45);
        plotPace.getGraphWidget().setRangeLabelOrientation(-45);
        plotAlt.getGraphWidget().setDomainLabelOrientation(-45);
        plotAlt.getGraphWidget().setRangeLabelOrientation(-45);

        plotPace.setDomainLeftMin(0);
        plotPace.setDomainRightMax(Math.floor(curr_dist));
        plotPace.setRangeBottomMin(0);
        plotPace.setRangeTopMax(Math.floor(yMaxPace));
    }

}
