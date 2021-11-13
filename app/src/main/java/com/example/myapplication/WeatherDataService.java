package com.example.myapplication;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataService {
    String data;
    Context context;

    public WeatherDataService(Context context){
        this.context = context;
    }

    public interface VolleyResponseListener{
        void onError(String message);
        void onResponse(String temp);
    }

    public void getLastdata(String url, VolleyResponseListener volleyResponseListener){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                data = "";
                try {
//                    JSONObject dataInfor = response.getJSONObject(0);
//                    data = dataInfor.getString("last_value");
                    data = response.getString("last_value");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                volleyResponseListener.onResponse(data);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError("Something wrong!");
            }
        }
        );
        MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }
}