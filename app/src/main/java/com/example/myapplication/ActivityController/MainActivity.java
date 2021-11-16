package com.example.myapplication.ActivityController;

import com.example.myapplication.Fragment.FragmentAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.util.Log;

import com.example.myapplication.Fragment.HomeActivity;
import com.example.myapplication.MQTTHelper;
import com.example.myapplication.R;
import com.example.myapplication.WeatherRequest.WeatherRequest;
import com.google.android.material.tabs.TabLayout;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

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


    int waiting_period = 0;
    boolean send_message_again = false;
    MQTTHelper mqttHelper;
    WeatherRequest request = WeatherRequest.getInstance();


    List<MQTTMessage> list = new ArrayList<>();
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

        request.getLastdata(tempUrl);
        request.getLastdata(humiUrl);
        request.getLastdata(ledUrl);

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
    }

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

    public class MQTTMessage{
        public String topic;
        public String mess;
    }
}