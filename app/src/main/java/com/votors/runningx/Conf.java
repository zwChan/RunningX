package com.votors.runningx;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Jason on 2015/12/22 0022.
 */
public class Conf {
    static Context context;

    public static String LENGTH_UNIT = "International unit";
    public static String MAP_TYPE = "Google Map";
    public static String SPEED_TYPE = "Pace";

    public static int MIN_DISTANCE = 3;
    public static int INTERVAL_LOCATION = 10;  //second
    public static int INTERVAL_LOCATION_FAST = 1; //second
    public static int LOCATION_ACCURACY = 50;
    public static int SPEED_AVG = 5;
    public static boolean GPS_ONLY = true;
    public static float ACCELERATE_FACTOR = 0.5f;

    // no stored
    public static final int INVALID_ALT = -999;
    public static final float MARK_DISTANCE = 1000;

    public static String TAG = "Conf";

    static public void init(Context context) {
        if (Conf.context == null) {
            Conf.context = context;
        }
        LENGTH_UNIT = context.getString(R.string.international);

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS)
        {
            MAP_TYPE = context.getString(R.string.gmap);
        } else {
            MAP_TYPE = context.getString(R.string.amap);
        }
        read();
    }

    static boolean existConfFile() {
        String filename = String.format(getRootDir() + "/conf.json");
        return new File(filename).exists();
    }

    static public void  save() {
        String filename = String.format(getRootDir() + "/conf.json");
        try {
            FileOutputStream fout = new FileOutputStream(filename);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fout, "UTF-8"));
            writer.setIndent("  ");
            writer.beginObject();

            // write common information
            writer.name("LENGTH_UNIT").value(LENGTH_UNIT);
            writer.name("MAP_TYPE").value(MAP_TYPE);
            writer.name("SPEED_TYPE").value(SPEED_TYPE);
            writer.name("MIN_DISTANCE").value(MIN_DISTANCE);
            writer.name("INTERVAL_LOCATION").value(INTERVAL_LOCATION);
            writer.name("INTERVAL_LOCATION_FAST").value(INTERVAL_LOCATION_FAST);
            writer.name("LOCATION_ACCURACY").value(LOCATION_ACCURACY);
            writer.name("SPEED_AVG").value(SPEED_AVG);
            writer.name("GPS_ONLY").value(GPS_ONLY);
            writer.name("ACCELERATE_FACTOR").value(ACCELERATE_FACTOR);

            writer.endObject();
            writer.close();
            fout.close();
        }catch (Exception e) {
            Log.i(TAG, "save conf to file fail.");
        }
    }

    static public void read() {
        String filename = String.format(getRootDir() + "/conf.json");
        try {
            FileInputStream fin = new FileInputStream(filename);
            JsonReader reader = new JsonReader(new InputStreamReader(fin, "UTF-8"));

            reader.beginObject();

            // write common information
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("LENGTH_UNIT")) {
                    LENGTH_UNIT = reader.nextString();
                } else if (name.equals("MAP_TYPE")) {
                    MAP_TYPE = reader.nextString();
                } else if (name.equals("SPEED_TYPE")) {
                    SPEED_TYPE = reader.nextString();
                } else if (name.equals("MIN_DISTANCE")) {
                    MIN_DISTANCE = reader.nextInt();
                } else if (name.equals("INTERVAL_LOCATION")) {
                    INTERVAL_LOCATION = reader.nextInt();
                } else if (name.equals("INTERVAL_LOCATION_FAST")) {
                    INTERVAL_LOCATION_FAST = reader.nextInt();
                } else if (name.equals("LOCATION_ACCURACY")) {
                    LOCATION_ACCURACY = reader.nextInt();
                } else if (name.equals("SPEED_AVG")) {
                    SPEED_AVG = reader.nextInt();
                } else if (name.equals("GPS_ONLY")) {
                    GPS_ONLY = reader.nextBoolean();
                } else if (name.equals("GPS_ONLY")) {
                    ACCELERATE_FACTOR = (float)reader.nextDouble();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();

            reader.close();
            fin.close();
        }catch (Exception e) {
            Log.i(TAG,"read conf from file fail.");
        }
    }

    public static String getDistanceUnit() {
        if (LENGTH_UNIT.equals(context.getResources().getString(R.string.international))) {
            return "Km";
        }else{
            return "Mile";
        }
    }
    public static String getAltitudeUnit() {
        if (LENGTH_UNIT.equals(context.getResources().getString(R.string.international))) {
            return "m";
        }else{
            return "feet";
        }
    }
    public static String getSpeedUnit() {
        if (LENGTH_UNIT.equals(context.getResources().getString(R.string.international))) {
            if (SPEED_TYPE.equals(context.getResources().getString(R.string.pace))) {
                return "Min/Km";
            }else{
                return "Km/H";
            }
        }else{
            if (SPEED_TYPE.equals(context.getResources().getString(R.string.pace))) {
                return "Min/Mi";
            }else{
                return "Mi/H";
            }
        }
    }

    public static float getDistance(float distance) {
        if (LENGTH_UNIT.equals(context.getResources().getString(R.string.international))) {
            return distance/1000f;
        }else{
            return distance/1609.34f;
        }
    }
    // feet or m
    public static float getAltitude(float alt) {
        if (LENGTH_UNIT.equals(context.getResources().getString(R.string.international))) {
            return alt;
        }else{
            return alt * 3.28084f;
        }
    }
    public static float getSpeed(float speed, float avgSpeed) {
        if (speed < 0.000001) return 0;
        if (LENGTH_UNIT.equals(context.getResources().getString(R.string.international))) {
            if (SPEED_TYPE.equals(context.getResources().getString(R.string.pace))) {
                if (speed<avgSpeed/2)speed = avgSpeed/2;//avoid too big pace
                return 1000/(60*speed);
            }else{
                return (speed/1000)*3600;
            }
        }else{
            if (SPEED_TYPE.equals(context.getResources().getString(R.string.pace))) {
                if (speed<avgSpeed/2)speed = avgSpeed/2;//avoid too big pace
                return 1609.34f/(60*speed);
            }else{
                return (speed/1609.34f)*3600;
            }
        }
    }

    public static String getSpeedString(float speed) {
        float sp = getSpeed(speed,0);
        if (SPEED_TYPE.equals(context.getResources().getString(R.string.pace))) {
            return String.format("%02d:%02d", (int)Math.floor(sp), Math.round(60 * (sp - Math.floor(sp))));
        } else {
            return String.format("%.02f", sp);
        }
    }

    public static Intent getMapIntent() {

        if (MAP_TYPE.equals(context.getResources().getString(R.string.gmap))) {
            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) != ConnectionResult.SUCCESS) {
                Toast.makeText(context, context.getString(R.string.gmap_no), Toast.LENGTH_LONG).show();
            } else {
                return new Intent(context, MapActivity.class);
            }
        }
        return new Intent(context, MapActivity_gd.class);
    }

    public static String getRootDir() {
        File dir = Environment.getExternalStorageDirectory();
        String rootDir = null;
        if (dir == null) {
            dir = context.getDir(".", 0);
            rootDir = dir.getAbsolutePath();
        } else {
            rootDir = dir.getAbsolutePath() +"/"+ context.getResources().getString(R.string.app_name);
        }
        dir = new File(rootDir);
        if (dir.mkdir()) {
            new File(rootDir+"/records").mkdir();
        }
        return rootDir;
    }

    public static float getMarkDistance(float totalDistance) {
        float ret = 0;
        if (LENGTH_UNIT.equals(context.getResources().getString(R.string.international))) {
            ret = 1000;
        }else{
            ret = 1609.34f;
        }
        if (totalDistance < 1700) ret /= 10;
        return ret;
    }
}
