package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
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


public class MainActivity extends AppCompatActivity {
    MQTTHelper mqttHelper;
    TextView txtTemp, txtHum1;
//    ToggleButton btnLED;
//
//
    String tempUrl = "https://io.adafruit.com/api/v2/RinnnnN/feeds/bbc-temp";
    String tempUrl2 = "https://io.adafruit.com/api/v2/taunhatquang/feeds/humidity";
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
                        txtHum1.setText(response.getString("last_value")+"%");
//                        circleHumid.setProgress(Integer.parseInt(response.getString("last_value").toString()));
                    }
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
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        txtTemp = findViewById(R.id.txtTemperature);
        txtHum1 = findViewById(R.id.txtHumidity);
//        btnLED = findViewById(R.id.btnLED);
//
//
//        txtTemp.setText("40" + "°C");
//        txtHum1.setText("80" + "%");
//
//        btnLED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck){
////                btnLED.setVisibility(View.INVISIBLE);
//                if(isCheck == true){
//                    Log.d("mqtt","Button is checked");
//                    sendDataMQTT("RinnnnN/feeds/bbc-led","1");
//                }
//                else{
//                    Log.d("mqtt","Button is unchecked");
//                    sendDataMQTT("RinnnnN/feeds/bbc-led","0");
//                }
//            }
//        });
//
        getLastdata(tempUrl);
        getLastdata(tempUrl2);


        startMQTT();


    }

//    int waiting_period = 0;
//    boolean send_message_again = false;
//    List<MQTTMessage> list = new ArrayList<>();
//
//    private void  setupScheduler(){
//        Timer aTimer = new Timer();
//        TimerTask scheduler = new TimerTask() {
//            @Override
//            public void run() {
//                Log.d("mqtt","Timer is executed");
//
////                btnLED.setVisibility(View.VISIBLE);
//                if(waiting_period > 0){
//                    waiting_period--;
//                    if(waiting_period == 0){
//                        send_message_again = true;
//                    }
//                }
//                if(send_message_again == true){
//                    sendDataMQTT(list.get(0).topic,list.get(0).mess);
//                }
//            }
//        };
//        aTimer.schedule(scheduler,0,5000);
//    }
//
//    private  void sendDataMQTT(String topic, String vaule){
//        waiting_period = 3;
//        send_message_again = false;
//        MQTTMessage aMessage = new MQTTMessage();
//        aMessage.topic = topic; aMessage.mess = vaule;
//        list.add(aMessage);
//
//        MqttMessage msg = new MqttMessage();
//        msg.setId(1234);
//        msg.setQos(0);
//        msg.setRetained(true);
//
//
//
//        byte[] b = vaule.getBytes(Charset.forName("UTF-8"));
//        msg.setPayload(b);
//
//        try{
//            mqttHelper.mqttAndroidClient.publish(topic,msg);
//        }catch (Exception e){
//
//        }
//    }
    private void startMQTT(){
        mqttHelper = new MQTTHelper(getApplicationContext(),"123456789");
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
                    txtHum1.setText(message.toString());
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