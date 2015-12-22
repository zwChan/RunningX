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
    Spinner spinner_lengthUnit;
    Spinner spinner_mapType;
    Spinner spinner_minDistance;

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
        text_mapType = (TextView) rootView.findViewById(R.id.MAP_TYPE);
        text_minDistance = (TextView) rootView.findViewById(R.id.MIN_DISTANCE);
        spinner_lengthUnit = (Spinner) rootView.findViewById(R.id.LENGTH_UNIT_VALUE);
        spinner_mapType = (Spinner) rootView.findViewById(R.id.MAP_TYPE_VALUE);
        spinner_minDistance = (Spinner) rootView.findViewById(R.id.MIN_DISTANCE_VALUE);
        spinner_lengthUnit.setOnItemSelectedListener(this);
        spinner_mapType.setOnItemSelectedListener(this);
        spinner_minDistance.setOnItemSelectedListener(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter_lengthUnit = ArrayAdapter.createFromResource(context,
                R.array.LENGTH_UNIT_LIST, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter_lengthUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner_lengthUnit.setAdapter(adapter_lengthUnit);
        int pos = adapter_lengthUnit.getPosition(Conf.LENGTH_UNIT);
        if (pos>=0)spinner_lengthUnit.setSelection(pos);

        ArrayAdapter<CharSequence> adapter_mapType = ArrayAdapter.createFromResource(context,
                R.array.MAP_TYPE_LIST, android.R.layout.simple_spinner_item);
        adapter_mapType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_mapType.setAdapter(adapter_mapType);
        pos = adapter_mapType.getPosition(Conf.LENGTH_UNIT);
        if (pos>=0)spinner_mapType.setSelection(pos);

        ArrayAdapter<CharSequence> adapter_minDistance = ArrayAdapter.createFromResource(context,
                R.array.MIN_DISTANCE_LIST, android.R.layout.simple_spinner_item);
        adapter_minDistance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_minDistance.setAdapter(adapter_minDistance);
        pos = adapter_minDistance.getPosition(String.valueOf(Conf.MIN_DISTANCE));
        if (pos>=0)spinner_minDistance.setSelection(pos);

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
            default:
                Log.i(TAG, "spinner view not found. ." + view.getId());
        }
        Conf.save(context);

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}
