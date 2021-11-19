package com.example.myapplication.Fragment;


import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.myapplication.MQTTHelper;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MySingleton;
import com.example.myapplication.R;

public class HomeActivity extends Fragment {
//    private static final String TAG = "MainActivity";


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    //    private MQTTHelper helper = new MQTTHelper(getContext(),"123456789");
    TextView txtTemp, txtHumi;
    ToggleButton btnLED;
    boolean isChecked = false;
    //

    public static HomeActivity newInstance(String param1, String param2) {
        HomeActivity fragment = new HomeActivity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment1, container, false);
        txtTemp = rootView.findViewById(R.id.txtTemperature);
        txtHumi = rootView.findViewById(R.id.txtHumidity);
        btnLED = rootView.findViewById(R.id.btnLED);




//        btnLED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck){
////                btnLED.setVisibility(View.INVISIBLE);
//                if(isCheck == true){
//                    Log.d("mqtt","Button is checked");
//                    helper.sendDataMQTT("taunhatquang/feeds/bbc-led","1");
//                }
//                else{
//                    Log.d("mqtt","Button is unchecked");
//                    helper.sendDataMQTT("taunhatquang/feeds/bbc-led","0");
//                }
//            }
//        });
//
        return rootView;
    }



}