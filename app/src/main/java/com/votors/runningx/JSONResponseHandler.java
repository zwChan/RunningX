package com.votors.runningx;
//EarthQuake
/**
 * Created by Jason on 2015/11/26 0026.
 */

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JSONResponseHandler implements
        ResponseHandler<List<GpsRec>> {
    @Override
    public List<GpsRec> handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {
        List<GpsRec> result = new ArrayList<GpsRec>();
        String JSONResponse = new BasicResponseHandler()
                .handleResponse(response);
        try {
            JSONObject object = (JSONObject) new JSONTokener(JSONResponse)
                    .nextValue();
            JSONArray earthquakes = object.getJSONArray("earthquakes");
            for (int i = 0; i < earthquakes.length(); i++) {
                JSONObject tmp = (JSONObject) earthquakes.get(i);
                result.add(new GpsRec(
                        new Date(),
                        tmp.getDouble("lat"),
                        tmp.getDouble("lng"),
                        tmp.getDouble("magnitude")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

}
