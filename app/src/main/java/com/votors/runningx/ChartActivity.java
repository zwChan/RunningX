package com.votors.runningx;

/**
 * Created by Jason on 2015/11/28 0028.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.util.ArrayList;
import java.util.Arrays;

public class ChartActivity extends Activity{
    public final static String EXTRA_MESSAGE = "com.votors.runningx.MESSAGE";
    private static final String BC_INTENT = "com.votors.runningx.BroadcastReceiver.location";
    public final static String EXTRA_GpsRec = "com.votors.runningx.GpsRec";

    public static final String TAG = "ChartActivity";

    //private final MapActivity.LocationReceiver mReceiver = new MapActivity.LocationReceiver();
    //private final IntentFilter intentFilter = new IntentFilter(BC_INTENT);
    ArrayList<GpsRec> locations = null;
    float total_dist = 0;

    int movePointCnt = 0;

    private XYPlot plotPace;
    private XYPlot plotAlt;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // fun little snippet that prevents users from taking screenshots
        // on ICS+ devices :-)
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);*/
        setContentView(R.layout.main_chart);
        locations = (ArrayList<GpsRec>)getIntent().getSerializableExtra(EXTRA_MESSAGE);
        ArrayList<Integer> xDist = new ArrayList<>();
        ArrayList<Float> yPace = new ArrayList<>();
        ArrayList<Integer> yAlt = new ArrayList<>();

        for (GpsRec rec: locations) {
            total_dist += rec.distance;
            xDist.add(Math.round(total_dist));
            if (locations.indexOf(rec)>= Conf.SPEED_AVG) {
                yPace.add(Math.round(rec.speed * 10) * 1.0f / 10);
                yAlt.add((int) Math.round(rec.getAlt()));
            } else {
                yPace.add(0f);
                yAlt.add(0);
            }
        }
        // initialize our XYPlot reference:
        plotPace = (XYPlot) findViewById(R.id.pace);
        plotAlt = (XYPlot) findViewById(R.id.altitude);

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
        plotPace.getGraphWidget().setRangeLabelOrientation(-90);
        plotAlt.getGraphWidget().setDomainLabelOrientation(-45);
        plotAlt.getGraphWidget().setRangeLabelOrientation(-90);
    }

}
