package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


import android.app.Activity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private LineChart mChart;

    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi;
    ToggleButton btnLED;
    boolean isChecked = false;
//
//
    String tempUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/temperature";
    String humiUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/humidity";
    String ledUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/bbc-led";
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
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
        btnLED = findViewById(R.id.btnLED);






        mChart = (LineChart) findViewById(R.id.linechart);
//        mChart.setOnChartGestureListener(MainActivity.this);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);

        ArrayList<Entry> yValues = new ArrayList<>();

        yValues.add(new Entry(0,60f));
        LineDataSet set1 = new LineDataSet(yValues,"Data Set 1: ");

        set1.setFillAlpha(110);
        set1.setColor(Color.RED);
        set1.setLineWidth(3f);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData (dataSets);

        mChart.setData(data);












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