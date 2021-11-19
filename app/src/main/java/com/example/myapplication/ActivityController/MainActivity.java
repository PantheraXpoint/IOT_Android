package com.example.myapplication.ActivityController;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.Fragment.FragmentAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.Fragment.HomeActivity;
import com.example.myapplication.MQTTHelper;
import com.example.myapplication.MySingleton;
import com.example.myapplication.R;
import com.example.myapplication.WeatherRequest.WeatherRequest;
import com.google.android.material.tabs.TabLayout;

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


    TabLayout tabLayout;
    ViewPager2 pager2;
    FragmentAdapter adapter;
    String tempUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/temperature";
    String humiUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/humidity";
    String ledUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/bbc-led";


    int waiting_period = 1;
    boolean send_message_again = false;
    MQTTHelper mqttHelper;
    WeatherRequest request = WeatherRequest.getInstance();


    List<MQTTMessage> list = new ArrayList<>();

    void getLastData(String url) {
        JsonObjectRequest rq = request.getLastdata(url,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getString("name").equals("BBC_TEMP")){
//                        txtTemp.setText(response.getString("last_value")+"°C");
                        request.setBbcTemp("last_value");
//                        circleTemp.setProgress(Integer.parseInt(response.getString("last_value").toString()));
                    }
                    if(response.getString("name").equals("BBC_HUMI")){
//                                BBC_HUMI = response.getString("last_value");
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
        });
        MySingleton.getInstance(this).addToRequestQueue(rq);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tab_layout);
        pager2 = findViewById(R.id.view_pager2);

        FragmentManager fm = getSupportFragmentManager();
        adapter = new FragmentAdapter(fm, getLifecycle());
        pager2.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Graph View"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        pager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        getLastData(tempUrl);
        getLastData(humiUrl);
        getLastData(ledUrl);

        setupScheduler();
        startMQTT();
    }



    private void startMQTT() {
        mqttHelper = new MQTTHelper(getApplicationContext(), "123456789");
        mqttHelper.startMQTT(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("mqtt", "Connection is successful");
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.contains("taunhatquang/feeds/temperature")) {
                    request.setBbcTemp(message.toString());
                }
                if (topic.contains("taunhatquang/feeds/humidity")) {
                    Log.d("temperature", message.toString());
                    System.out.println("Hello world!!!");
                    request.setBbcHumi(message.toString());
//                    txtHumi.setText(message.toString());
                }
//                Log.d("temperature", topic);
                if (topic.contains("taunhatquang/feeds/bbc-led")) {
//                    waiting_period = 0;
//                    send_message_again = false;

                    System.out.println("Hello world!!!");
                    if (message.toString().equals("1")) {
                        request.setBbcLed(true);
//                        btnLED.setChecked(true);
                    } else {
                        request.setBbcLed(false);
//                        btnLED.setChecked(false);
                    }

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    private void sendDataMQTT(String topic, String value){

        waiting_period = 3;
        send_message_again = false;

        mqttHelper.sendDataMQTT(topic,value);
        Log.d("Topic: " + topic.toString(),"Value" + value.toString());
    }

    private void  setupScheduler(){
        Toast.makeText(this, "begin scheduler", Toast.LENGTH_LONG).show();
        Timer aTimer = new Timer();
        TimerTask scheduler = new TimerTask() {
            @Override
            public void run() {
                Log.d("mqtt","Timer is executed");

//                btnLED.setVisibility(View.VISIBLE);
                if(waiting_period > 0){
                    waiting_period--;
                    Log.d("time",String.valueOf(waiting_period));
                    if(waiting_period <= 0){
                        send_message_again = true;
                    }
                }
                if(send_message_again == true){
//                    Toast.makeText(getBaseContext(), "period = 0", Toast.LENGTH_LONG).show();
                    if (list.size() == 0){
                        waiting_period = 10;
                        send_message_again = false;
                    }
                    else{
                        sendDataMQTT(list.get(0).topic,list.get(0).mess);
                    }
                }
            }
        };
        aTimer.schedule(scheduler,0,5000);
        Toast.makeText(this, "reset scheduler", Toast.LENGTH_LONG).show();
    }
    //

    public class MQTTMessage{
        public String topic;
        public String mess;
    }
}