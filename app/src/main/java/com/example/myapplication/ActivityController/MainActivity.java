package com.example.myapplication.ActivityController;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.example.myapplication.MQTTHelper;
import com.example.myapplication.MySingleton;
import com.example.myapplication.OnSwipeTouchListener;
import com.example.myapplication.R;
import com.example.myapplication.TempGraphActivity;
import com.example.myapplication.WeatherRequest.WeatherRequest;
import com.google.android.material.tabs.TabLayout;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";

    int waiting_period = 0;
    boolean send_message_again = false;
    List<MQTTMessage> list = new ArrayList<>();


    MQTTHelper mqttHelper;
    TextView txtTemp, txtHumi;
//    ToggleButton btnLED;
    boolean isChecked = false;
//
//
    public String tempUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/temperature";
    public String humiUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/humidity";
    public String ledUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/bbc-led";


//        protected void getLastdata(String url){
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
//                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    if(response.getString("name").equals("BBC_TEMP")){
//                        txtTemp.setText(response.getString("last_value")+"°C");
////                        circleTemp.setProgress(Integer.parseInt(response.getString("last_value").toString()));
//                    }
//                    if(response.getString("name").equals("BBC_HUMI")){
//                        txtHumi.setText(response.getString("last_value")+"%");
////                        circleHumid.setProgress(Integer.parseInt(response.getString("last_value").toString()));
//                    }
////                    if(response.getString("name").equals("BBC_LED")){
////                        btnLED.setChecked(true);
//
//                //}
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//            }
//        }
//        );
//        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
//    }

    private class GetLastData extends AsyncTask<String,Void,String>
    {
        String topic = "";
        String last_value ="";
        String URLcontent = "";
        private ProgressDialog dialog;

        public GetLastData(Activity activity) {
            this.dialog = new ProgressDialog(activity);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String tmp = "";
                while (tmp != null) {
                    tmp = bufferedReader.readLine();
                    URLcontent += tmp;
                }
                JSONObject jsonObject = new JSONObject(URLcontent);
                last_value = jsonObject.getString("last_value");
                topic = jsonObject.getString("name");
//                JSONObject rates = (JSONObject) jsonObject.get("rates");
//                Iterator<String> keys = rates.keys();
//                Double VND_rate = rates.getDouble("VND");
//                String key="";
//                while(keys.hasNext()) {
//                    key = keys.next();
//                    if (key.equals("VND") || key.equals(base_rate))
//                        continue;
//                    Double rate = VND_rate / ((Double) rates.get(key));
//                    MyCurrency newCur = new MyCurrency(key, rate, "", 0);
//                    res.add(newCur);
//                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return topic + " " + last_value;
        }

        @Override
        protected void onPreExecute() {
            dialog.setTitle("Updating last data ...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(String temp) {
            List<String> tmp = Arrays.asList(temp.split(" "));
            if (tmp.get(0).equals("Temperature")){
                ((TextView)findViewById(R.id.txtTemperature)).setText(tmp.get(1) + "°C");
            }
            if (tmp.get(0).equals("Humidity")){
                ((TextView)findViewById(R.id.txtHumidity)).setText(tmp.get(1) + "%");
            }
//            if (tmp.get(0).equals("LED")){
//                if (tmp.get(1).equals("1"))
//                    ((ToggleButton)findViewById(R.id.btnLED)).setChecked(true);
//                else
//                    ((ToggleButton)findViewById(R.id.btnLED)).setChecked(false);
//            }
            if (dialog.isShowing())
                dialog.dismiss();
        }
    }
    @Override
    public void finish() {
        super.finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        txtTemp = findViewById(R.id.txtTemperature);
        txtHumi = findViewById(R.id.txtHumidity);
//        btnLED = findViewById(R.id.btnLED);



        LinearLayout linearLayout = findViewById(R.id.mainActivity);
        linearLayout.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            public void onSwipeRight() {

            }
            public void onSwipeLeft() {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
            }
        });





//        btnLED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck){
////                btnLED.setVisibility(View.INVISIBLE);
//                if(isCheck == true){
//                    Log.d("mqtt","Button is checked");
//                    sendDataMQTT("taunhatquang/feeds/bbc-led","1");
//                    list.add(new MQTTMessage("taunhatquang/feeds/bbc-led","1"));
//                }
//                else{
//                    Log.d("mqtt","Button is unchecked");
//                    sendDataMQTT("taunhatquang/feeds/bbc-led","0");
//                    list.add(new MQTTMessage("taunhatquang/feeds/bbc-led","0"));
//                }
//            }
//        });
//
        (new GetLastData(this)).execute(humiUrl);
        (new GetLastData(this)).execute(tempUrl);
//        (new GetLastData(this)).execute(ledUrl);
//        setupScheduler();
        startMQTT();


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
                    send_message_again = true;
                    if(waiting_period == 0){
                        send_message_again = false;
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
                    txtTemp.setText(message.toString()+"°C");
                }
                if (topic.contains("taunhatquang/feeds/humidity")){
                    Log.d("humidity", message.toString());
                    txtHumi.setText(message.toString()+"%");
                }
//                Log.d("temperature", topic);
//                if (topic.contains("taunhatquang/feeds/bbc-led")){
////                    waiting_period = 0;
////                    send_message_again = false;
//
////                    System.out.println("Hello world!!!");
//                    if (message.toString().equals("1")){
//                        btnLED.setChecked(true);
//                    }
//                    else {
//                        btnLED.setChecked(false);
//                    }
//
//                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    protected class MQTTMessage{
        public String topic;
        public String mess;
        public MQTTMessage (String topic, String mess){
            this.topic = topic;
            this.mess = mess;
        }
    }
}