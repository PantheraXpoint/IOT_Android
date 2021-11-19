package com.example.myapplication.WeatherRequest;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherRequest {


    private static String BBC_TEMP;
    private static String BBC_HUMI;
    private static boolean BBC_LED;


    public  String getBbcTemp() {
        return BBC_TEMP;
    }

    public  void setBbcTemp(String bbcTemp) {
        BBC_TEMP = bbcTemp;
    }

    public  String getBbcHumi() {
        return BBC_HUMI;
    }

    public  void setBbcHumi(String bbcHumi) {
        BBC_HUMI = bbcHumi;
    }

    public  boolean getBbcLed() {
        return BBC_LED;
    }

    public  void setBbcLed(boolean bbcLed) {
        BBC_LED = bbcLed;
    }

    public  WeatherRequest getRequest() {
        return request;
    }

    public static void setRequest(WeatherRequest request) {
        WeatherRequest.request = request;
    }
    private static WeatherRequest request;

    public static   WeatherRequest getInstance() {
        if (request == null)
        {
            request = new WeatherRequest();
        }
        return request;
    }


    public JsonObjectRequest getLastdata(String url, Response.Listener<JSONObject> response, Response.ErrorListener errorListener){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, response, errorListener);
        return jsonObjectRequest;
    }
}
