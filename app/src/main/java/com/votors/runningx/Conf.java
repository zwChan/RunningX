package com.votors.runningx;

import android.content.Context;
import android.content.Intent;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Jason on 2015/12/22 0022.
 */
public class Conf {

    public static String LENGTH_UNIT = "International unit";
    public static String MAP_TYPE = "Google Map";

    public static int MIN_DISTANCE = 5;
    public static int INTERVAL_LOCATION = 5;  //second
    public static int INTERVAL_LOCATION_FAST = 2; //second
    public static int LOCATION_ACCURACY = 30;
    public static int SPEED_AVG = 5;

    public static String TAG = "Conf";

    static boolean existConfFile(Context context) {
        File dir = context.getDir(".", 0);
        String filename = String.format(dir.getAbsolutePath() + "/conf.json");
        return new File(filename).exists();
    }

    static public void  save(Context context) {
        File dir = context.getDir(".", 0);
        String filename = String.format(dir.getAbsolutePath() + "/conf.json");
        try {
            FileOutputStream fout = new FileOutputStream(filename);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(fout, "UTF-8"));
            writer.setIndent("  ");
            writer.beginObject();

            // write common information
            writer.name("LENGTH_UNIT").value(LENGTH_UNIT);
            writer.name("MAP_TYPE").value(MAP_TYPE);
            writer.name("MIN_DISTANCE").value(MIN_DISTANCE);
            writer.name("INTERVAL_LOCATION").value(INTERVAL_LOCATION);
            writer.name("INTERVAL_LOCATION_FAST").value(INTERVAL_LOCATION_FAST);
            writer.name("LOCATION_ACCURACY").value(LOCATION_ACCURACY);
            writer.name("SPEED_AVG").value(SPEED_AVG);

            writer.endObject();
            writer.close();
            fout.close();
        }catch (Exception e) {
            Log.i(TAG, "save conf to file fail.");
        }
    }

    static public void read(Context context) {
        File dir = context.getDir(".", 0);
        String filename = String.format(dir.getAbsolutePath() + "/conf.json");
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

}
