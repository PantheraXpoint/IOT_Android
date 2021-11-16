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
    private static String BBC_LED;

    private static WeatherRequest request;

    public static   WeatherRequest getInstance() {
        if (request == null)
        {
            request = new WeatherRequest();
        }
        return request;
    }


    public void getLastdata(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getString("name").equals("BBC_TEMP")){
//                        txtTemp.setText(response.getString("last_value")+"°C");
                        BBC_TEMP = response.getString("last_value");
//                        circleTemp.setProgress(Integer.parseInt(response.getString("last_value").toString()));
                    }
                    if(response.getString("name").equals("BBC_HUMI")){
                        BBC_HUMI = response.getString("last_value");
//                        txtHumi.setText(response.getString("last_value")+"%");
//                        circleHumid.setProgress(Integer.parseInt(response.getString("last_value").toString()));
                    }
//                    if(response.getString("name").equals("BBC_LED")){
//                        btnLED.setChecked(true);

                    //}
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        );
    }
}
