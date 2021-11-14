package com.example.myapplication.ActivityController;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.myapplication.MQTTHelper;
import com.example.myapplication.MySingleton;
import com.example.myapplication.R;
import com.example.myapplication.WeatherDataService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


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

import static java.lang.Integer.parseInt;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";



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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
        btnLED = findViewById(R.id.btnLED);




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

//    private boolean isRunning = false;
//    private LineChart chart;
//    private Thread thread;
//    String tempUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/temperature";
//    int tempAPI = 0;
//    int tem = 1;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_main);
//
//        chart = findViewById(R.id.lineChart);
//        initChart();
//
//        Button btStop,btStart,btReset;
//        btStart = findViewById(R.id.button_RunData);
//        btStop = findViewById(R.id.button_Stop);
//        btReset = findViewById(R.id.button_Reset);
//
//        btStart.setOnClickListener(v->{
//            startRun();
//        });
//
//        btStop.setOnClickListener(v->{
//            isRunning = false;
//        });
//
//        btReset.setOnClickListener(v->{
//            chart.clear();
//            tempAPI = 0;
//            tem = 1;
//            initChart();
//        });
//    }
//
//    private void startRun(){
//        if (isRunning)return;
//        if (thread != null) thread.interrupt();
//        isRunning = true;
//
//        Runnable runnable  = ()->{
//            WeatherDataService weatherDataService = new WeatherDataService(MainActivity.this);
//            weatherDataService.getLastdata(tempUrl, new WeatherDataService.VolleyResponseListener() {
//                @Override
//                public void onError(String message) {
//                    Toast.makeText(MainActivity.this, "Something wrong!!", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onResponse(String temp) {
//                    if(parseInt(temp) != tempAPI) {
//                        Toast.makeText(MainActivity.this, "Return temp: " + temp, Toast.LENGTH_SHORT).show();
//                        tempAPI = parseInt(temp);
//                    }
//                }
//            });
////            addData((int)(Math.random()*(60-40+1))+35);
//            if(tem != tempAPI) {
//                addData(tempAPI);
//            }
//            tem = tempAPI;
//        };
//
//        thread =  new Thread(()->{
//            while (isRunning) {
//                runOnUiThread(runnable);
//                if (!isRunning)break;
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
//    }
//
//    private void initChart(){
//        chart.getDescription().setEnabled(false);
//        chart.setTouchEnabled(false);
//        chart.setDragEnabled(false);
//
//        LineData data = new LineData();
//        data.setValueTextColor(Color.BLACK);
//        chart.setData(data);
//
//        Legend l =  chart.getLegend();
//        l.setForm(Legend.LegendForm.LINE);
//        l.setTextColor(Color.BLACK);
//
//        XAxis x =  chart.getXAxis();
//        x.setTextColor(Color.BLACK);
//        x.setDrawGridLines(true);
//        x.setPosition(XAxis.XAxisPosition.BOTTOM);
//        x.setLabelCount(5,true);
//        x.setValueFormatter(new ValueFormatter() {
//            @Override
//            public String getFormattedValue(float value) {
//                return "No. " + Math.round(value);
//            }
//        });
//        //
//        YAxis y = chart.getAxisLeft();
//        y.setTextColor(Color.BLACK);
//        y.setDrawGridLines(true);
//        y.setAxisMaximum(100);
//        y.setAxisMinimum(0);
//        chart.getAxisRight().setEnabled(false);
//        chart.setVisibleXRange(0,50);
//    }
//
//    private void addData(float inputData){
//        LineData data =  chart.getData();
//        ILineDataSet set = data.getDataSetByIndex(0);
//        if (set == null){
//            set = createSet();
//            data.addDataSet(set);
//        }
//        data.addEntry(new Entry(set.getEntryCount(),inputData),0);
//        //
//        data.notifyDataChanged();
//        chart.notifyDataSetChanged();
//        chart.setVisibleXRange(0,10);//Range Value(ox)
//        chart.moveViewToX(data.getEntryCount());
//    }
//
//    private LineDataSet createSet() {
//        LineDataSet set = new LineDataSet(null, "lINE data");
//        set.setAxisDependency(YAxis.AxisDependency.LEFT);
//        set.setColor(Color.GRAY);
//        set.setLineWidth(2);
//        set.setDrawCircles(true);
//        set.setFillColor(Color.RED);
//        set.setFillAlpha(50);
//        set.setDrawFilled(true);
//        set.setValueTextColor(Color.BLACK);
//        set.setDrawValues(false);
//        return set;
//    }

}