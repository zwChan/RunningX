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

public class ConfFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    Context context = null;
    View rootView;
    TextView text_warn = null;
    TextView text_lengthUnit = null;
    TextView text_mapType = null;
    TextView text_minDistance = null;
    TextView text_minGpsInterval = null;
    TextView text_minAccuracy = null;
    TextView text_speedAvg = null;
    Spinner spinner_lengthUnit;
    Spinner spinner_mapType;
    Spinner spinner_minDistance;
    Spinner spinner_minGpsInterval;
    Spinner spinner_minAccuracy;
    Spinner spinner_speedAvg;

    public final static String EXTRA_MESSAGE = "com.votors.Conf.MESSAGE";
    private final String TAG = "ConfFragment";


    public ConfFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        rootView = inflater.inflate(R.layout.fragment_conf, container, false);

        Conf.read(context);

        // Get a reference to the Press Me Button
        text_warn = (TextView) rootView.findViewById(R.id.conf_changed_warning);
        text_warn.setVisibility(View.INVISIBLE);

        text_lengthUnit = (TextView) rootView.findViewById(R.id.LENGTH_UNIT);
        spinner_lengthUnit = (Spinner) rootView.findViewById(R.id.LENGTH_UNIT_VALUE);
        spinner_lengthUnit.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter_lengthUnit = ArrayAdapter.createFromResource(context,
                R.array.LENGTH_UNIT_LIST, android.R.layout.simple_spinner_item);
        adapter_lengthUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_lengthUnit.setAdapter(adapter_lengthUnit);
        int pos = adapter_lengthUnit.getPosition(Conf.LENGTH_UNIT);
        if (pos>=0)spinner_lengthUnit.setSelection(pos);

        text_mapType = (TextView) rootView.findViewById(R.id.MAP_TYPE);
        spinner_mapType = (Spinner) rootView.findViewById(R.id.MAP_TYPE_VALUE);
        spinner_mapType.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter_mapType = ArrayAdapter.createFromResource(context,
                R.array.MAP_TYPE_LIST, android.R.layout.simple_spinner_item);
        adapter_mapType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mapType.setAdapter(adapter_mapType);
        pos = adapter_mapType.getPosition(Conf.LENGTH_UNIT);
        if (pos>=0)spinner_mapType.setSelection(pos);

        text_minDistance = (TextView) rootView.findViewById(R.id.MIN_DISTANCE);
        spinner_minDistance = (Spinner) rootView.findViewById(R.id.MIN_DISTANCE_VALUE);
        spinner_minDistance.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter_minDistance = ArrayAdapter.createFromResource(context,
                R.array.MIN_DISTANCE_LIST, android.R.layout.simple_spinner_item);
        adapter_minDistance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_minDistance.setAdapter(adapter_minDistance);
        pos = adapter_minDistance.getPosition(String.valueOf(Conf.MIN_DISTANCE));
        if (pos>=0)spinner_minDistance.setSelection(pos);

        text_minGpsInterval = (TextView) rootView.findViewById(R.id.INTERVAL_LOCATION);
        spinner_minGpsInterval = (Spinner) rootView.findViewById(R.id.INTERVAL_LOCATION_VALUE);
        spinner_minGpsInterval.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter_minGpsInterval = ArrayAdapter.createFromResource(context,
                R.array.INTERVAL_LOCATION_LIST, android.R.layout.simple_spinner_item);
        adapter_minGpsInterval.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_minGpsInterval.setAdapter(adapter_minGpsInterval);
        pos = adapter_minGpsInterval.getPosition(String.valueOf(Conf.INTERVAL_LOCATION));
        if (pos>=0)spinner_minGpsInterval.setSelection(pos);

        text_minAccuracy = (TextView) rootView.findViewById(R.id.LOCATION_ACCURACY);
        spinner_minAccuracy = (Spinner) rootView.findViewById(R.id.LOCATION_ACCURACY_VALUE);
        spinner_minAccuracy.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter_minAccuracy = ArrayAdapter.createFromResource(context,
                R.array.LOCATION_ACCURACY_LIST, android.R.layout.simple_spinner_item);
        adapter_minAccuracy.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_minAccuracy.setAdapter(adapter_minAccuracy);
        pos = adapter_minAccuracy.getPosition(String.valueOf(Conf.LOCATION_ACCURACY));
        if (pos>=0)spinner_minAccuracy.setSelection(pos);

        text_speedAvg = (TextView) rootView.findViewById(R.id.SPEED_AVG);
        spinner_speedAvg = (Spinner) rootView.findViewById(R.id.SPEED_AVG_VALUE);
        spinner_speedAvg.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter_spendAvg = ArrayAdapter.createFromResource(context,
                R.array.SPEED_AVG_LIST, android.R.layout.simple_spinner_item);
        adapter_spendAvg.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_speedAvg.setAdapter(adapter_spendAvg);
        pos = adapter_spendAvg.getPosition(String.valueOf(Conf.SPEED_AVG));
        if (pos>=0)spinner_speedAvg.setSelection(pos);


        return rootView;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        switch (parent.getId())
        {
            case R.id.LENGTH_UNIT_VALUE:
                if (!Conf.LENGTH_UNIT.equals(parent.getItemAtPosition(pos))) {
                    text_warn.setVisibility(View.VISIBLE);
                    Conf.LENGTH_UNIT = (String) parent.getItemAtPosition(pos);
                    Log.i(TAG, "length unit changed." + Conf.LENGTH_UNIT);
                }
                break;
            case R.id.MAP_TYPE_VALUE:
                if (!Conf.MAP_TYPE.equals(parent.getItemAtPosition(pos))) {
                    Conf.MAP_TYPE = (String) parent.getItemAtPosition(pos);
                    Log.i(TAG, "map type changed." + Conf.MAP_TYPE);
                    text_warn.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.MIN_DISTANCE_VALUE:
                if (!String.valueOf(Conf.MIN_DISTANCE).equals(parent.getItemAtPosition(pos))) {
                    Conf.MIN_DISTANCE = Integer.parseInt((String) parent.getItemAtPosition(pos));
                    Log.i(TAG, "min distance changed." + Conf.MIN_DISTANCE);
                    text_warn.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.INTERVAL_LOCATION_VALUE:
                if (!String.valueOf(Conf.INTERVAL_LOCATION).equals(parent.getItemAtPosition(pos))) {
                    Conf.INTERVAL_LOCATION = Integer.parseInt((String) parent.getItemAtPosition(pos));
                    Log.i(TAG, "min distance changed." + Conf.INTERVAL_LOCATION);
                    text_warn.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.LOCATION_ACCURACY_VALUE:
                if (!String.valueOf(Conf.LOCATION_ACCURACY).equals(parent.getItemAtPosition(pos))) {
                    Conf.LOCATION_ACCURACY = Integer.parseInt((String) parent.getItemAtPosition(pos));
                    Log.i(TAG, "min distance changed." + Conf.LOCATION_ACCURACY);
                    text_warn.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.SPEED_AVG_VALUE:
                if (!String.valueOf(Conf.SPEED_AVG).equals(parent.getItemAtPosition(pos))) {
                    Conf.SPEED_AVG = Integer.parseInt((String) parent.getItemAtPosition(pos));
                    Log.i(TAG, "min distance changed." + Conf.SPEED_AVG);
                    text_warn.setVisibility(View.VISIBLE);
                }
                break;
            default:
                Log.i(TAG, "spinner view not found. ." + view.getId());
        }
        Conf.save(context);

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
