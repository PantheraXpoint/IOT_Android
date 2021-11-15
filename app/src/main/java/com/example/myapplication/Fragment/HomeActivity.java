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

    private AppCompatActivity app;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi;
    ToggleButton btnLED;
    boolean isChecked = false;
    //
//
    String tempUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/temperature";
    String humiUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/humidity";
    String ledUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/bbc-led";

    public HomeActivity() {
        // Required empty public constructor
    }

    public static HomeActivity newInstance(String param1, String param2) {
        HomeActivity fragment = new HomeActivity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    protected void getLastdata(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getString("name").equals("BBC_TEMP")){
                        txtTemp.setText(response.getString("last_value")+"°C");
//                        circleTemp.setProgress(Integer.parseInt(response.getString("last_value").toString()));
                    }
                    if(response.getString("name").equals("BBC_HUMI")){
                        txtHumi.setText(response.getString("last_value")+"%");
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
            }
        }
        );
        MySingleton.getInstance(this.app).addToRequestQueue(jsonObjectRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        txtTemp = app.findViewById(R.id.txtTemperature);
        txtHumi = app.findViewById(R.id.txtHumidity);
        btnLED = app.findViewById(R.id.btnLED);




        btnLED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck){
//                btnLED.setVisibility(View.INVISIBLE);
                if(isCheck == true){
                    Log.d("mqtt","Button is checked");
                    sendDataMQTT("taunhatquang/feeds/bbc-led","1");
                }
                else{
                    Log.d("mqtt","Button is unchecked");
                    sendDataMQTT("taunhatquang/feeds/bbc-led","0");
                }
            }
        });
//

        getLastdata(tempUrl);
        getLastdata(humiUrl);
        getLastdata(ledUrl);

        setupScheduler();
        startMQTT();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment1, container, false);
    }

    int waiting_period = 0;
    boolean send_message_again = false;
    List<MQTTMessage> list = new ArrayList<>();

    private void  setupScheduler(){
        Timer aTimer = new Timer();
        TimerTask scheduler = new TimerTask() {
            @Override
            public void run() {
                Log.d("mqtt","Timer is executed");

//                btnLED.setVisibility(View.VISIBLE);
                if(waiting_period > 0){
                    waiting_period--;
                    if(waiting_period == 0){
                        send_message_again = true;
                    }
                }
                if(send_message_again == true){
                    sendDataMQTT(list.get(0).topic,list.get(0).mess);
                }
            }
        };
        aTimer.schedule(scheduler,0,5000);
    }
    //
    private  void sendDataMQTT(String topic, String value){
        waiting_period = 3;
        send_message_again = false;
//        MQTTMessage aMessage = new MQTTMessage();
//        aMessage.topic = topic; aMessage.mess = value;
//        list.add(aMessage);

        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);



        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try{
            mqttHelper.mqttAndroidClient.publish(topic,msg);
        }catch (Exception e){

        }
    }
    private void startMQTT(){
        mqttHelper = new MQTTHelper(app.getApplicationContext(),"123456789");
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("mqtt","Connection is successful");
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.contains("taunhatquang/feeds/temperature")){
                    txtTemp.setText(message.toString());
                }
                if (topic.contains("taunhatquang/feeds/humidity")){
                    Log.d("temperature", message.toString());
                    System.out.println("Hello world!!!");
                    txtHumi.setText(message.toString());
                }
//                Log.d("temperature", topic);
                if (topic.contains("taunhatquang/feeds/bbc-led")){
//                    waiting_period = 0;
//                    send_message_again = false;

                    System.out.println("Hello world!!!");
                    if (message.toString().equals("1")){
                        btnLED.setChecked(true);
                    }
                    else {
                        btnLED.setChecked(false);
                    }

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    public class MQTTMessage{
        public String topic;
        public String mess;
    }

}
