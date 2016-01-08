package com.votors.runningx;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TimelineFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    Context context = null;
    TextView text_dist = null;
    TextView text_speed = null;
    TextView text_time = null;
    Spinner button_spinner;

    public final static String EXTRA_MESSAGE = "com.votors.runningx.MESSAGE";
    public final static String EXTRA_GpsRec = "com.votors.runningx.GpsRec";
    private final String TAG = "RecordButton";

    Record record;
    ArrayList<Date> recordsDate = null;
    ArrayList<GpsRec> locations = null;

    public TimelineFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        context = getActivity();
        View rootView = inflater.inflate(R.layout.record_button, container, false);
        record = new Record(context);

        // Get a reference to the Press Me Button
        final Button button_map = (Button) rootView.findViewById(R.id.button_map);
        final Button button_del = (Button) rootView.findViewById(R.id.button_delete);
        final Button button_chart = (Button) rootView.findViewById(R.id.button_chart);
        button_spinner = (Spinner) rootView.findViewById(R.id.button_spinner);
        text_dist = (TextView) rootView.findViewById(R.id.button_distance);
        text_speed = (TextView) rootView.findViewById(R.id.button_speed);
        text_time = (TextView) rootView.findViewById(R.id.button_time);
        button_spinner.setOnItemSelectedListener(this);

        recordsDate = Record.getRecords(context);
        SimpleDateFormat sdf = new SimpleDateFormat () ;
        ArrayList<String> datestr = new ArrayList<>();
        datestr.add(context.getResources().getString(R.string.select_prompt));
        for (Date date: recordsDate) {
            datestr.add(sdf.format(date));
        }
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter =new  ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, datestr);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        button_spinner.setAdapter(adapter);

        button_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = button_spinner.getSelectedItemPosition();
                if (pos > 0) {
                    Log.i(TAG, "delete a record." + pos);
                    Record.delete(context, recordsDate.get(pos - 1));
                    initSpinner();
                } else {
                    Toast.makeText(context, context.getString(R.string.select_no), Toast.LENGTH_SHORT).show();
                }

            }
        });

        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locations != null) {
                    button_map.setText(getResources().getString(R.string.map));
                    Intent intent = Conf.getMapIntent();
                    intent.putExtra(EXTRA_MESSAGE, locations);
                    Log.i(TAG, "MAP onclick..");
                    startActivity(intent);
                }else{
                    Toast.makeText(context, context.getString(R.string.select_no), Toast.LENGTH_SHORT).show();
                }

            }
        });

        button_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locations != null) {
                    button_chart.setText(getResources().getString(R.string.chart));
                    Log.i(TAG, "chart onclick..");
                    final Intent intent_chart = new Intent(context, ChartActivity.class);
                    intent_chart.putExtra(EXTRA_MESSAGE, locations);
                    startActivity(intent_chart);
                }else{
                    Toast.makeText(context, context.getString(R.string.select_no), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    void initSpinner() {
        recordsDate = Record.getRecords(context);
        SimpleDateFormat sdf = new SimpleDateFormat () ;
        ArrayList<String> datestr = new ArrayList<>();
        datestr.add(context.getString(R.string.select_prompt));
        for (Date date: recordsDate) {
            datestr.add(sdf.format(date));
        }
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter =new  ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_item, datestr);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        button_spinner.setAdapter(adapter);
        button_spinner.setSelection(0);
        button_spinner.invalidate();
        locations = null;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.i(TAG, "spinner select id " + id);
        if (id>0) {
            record.read(recordsDate.get((int)id-1));
            record.usedTime /=1000; //millisecond to second
            locations = record.gpsRecs;
            final String timeStr = String.format("%d:%02d:%02d", record.usedTime / 3600, record.usedTime % 3600 / 60, record.usedTime % 3600 % 60);
            text_time.setText(timeStr);
            text_speed.setText(String.format("%s %s", Conf.getSpeedString(record.distance/record.usedTime), Conf.getSpeedUnit()));
            text_dist.setText(String.format("%.2f %s", Conf.getDistance(record.distance), Conf.getDistanceUnit()));

        } else {
            locations = null;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        locations = null;
    }
}
