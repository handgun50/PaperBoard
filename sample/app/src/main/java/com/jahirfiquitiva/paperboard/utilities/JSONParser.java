package com.jahirfiquitiva.paperboard.utilities;

import android.util.Log;

import org.apache.http.client.fluent.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class JSONParser {

    public static JSONObject getJSONfromURL(String url) {
        final String result;
        try {
            result = Request.Get(url).execute().returnContent().asString();
        } catch (IOException e) {
            Log.e("JSONParser", "Error making request to " + url + ": " + e.toString());
            return null;
        }

        JSONObject json = null;
        try {
            json = new JSONObject(result);
        } catch (JSONException e) {
            Log.e("JSONParser", "Error parsing data " + e.toString());
        }

        return json;
    }
}
