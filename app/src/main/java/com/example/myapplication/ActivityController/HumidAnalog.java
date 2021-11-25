package com.example.myapplication.ActivityController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.myapplication.HumidGraphActivity;
import com.example.myapplication.MQTTHelper;
import com.example.myapplication.OnSwipeTouchListener;
import com.example.myapplication.R;
import com.google.android.material.slider.Slider;

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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import az.plainpie.PieView;

public class HumidAnalog extends AppCompatActivity {

    private static final String TAG = "HumidAnalog";

    int waiting_period = 0;
    boolean send_message_again = false;
    List<HumidAnalog.MQTTMessage> list = new ArrayList<>();

    MQTTHelper mqttHelper;
    PieView txtHumi;
    Switch waterHose;
    Slider airConditioner;
    public String humiUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/humidity";
    public String acUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/ac";
    public String hoseUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/watering";

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

            if (tmp.get(0).equals("Humidity")){
                ((PieView)findViewById(R.id.pieView)).setInnerText(tmp.get(1) + "%");
                ((PieView)findViewById(R.id.pieView)).setPercentage(Integer.parseInt(tmp.get(1)));
            }
            if (tmp.get(0).equals("Watering")){
                if (tmp.get(1).equals("1")) {
                    ((Switch) findViewById(R.id.watering)).setChecked(true);
                }
                else {
                    ((Switch) findViewById(R.id.watering)).setChecked(false);
                }
            }
            if (tmp.get(0).equals("AC")){
                ((Slider)findViewById(R.id.slider)).setValue(Float.parseFloat(tmp.get(1)));
            }
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
        setContentView(R.layout.humid_analog);

        txtHumi = (PieView) findViewById(R.id.pieView);
        waterHose = findViewById(R.id.watering);
        airConditioner = findViewById(R.id.slider);


        LinearLayout linearLayout = findViewById(R.id.humidAnalog);
        linearLayout.setOnTouchListener(new OnSwipeTouchListener(HumidAnalog.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(HumidAnalog.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_right,R.anim.slide_in_left);
                finish();
            }
            public void onSwipeLeft() {
                Intent intent = new Intent(HumidAnalog.this, HumidGraphActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
            }
        });
        waterHose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck){
//                btnLED.setVisibility(View.INVISIBLE);
                if(isCheck == true){
                    Log.d("mqtt","Button is checked");

                    sendDataMQTT("taunhatquang/feeds/watering","1");
                    ((Switch) findViewById(R.id.watering)).setChecked(true);
                    list.add(new HumidAnalog.MQTTMessage("taunhatquang/feeds/watering","1"));
                }
                else{
                    Log.d("mqtt","Button is unchecked");
                    sendDataMQTT("taunhatquang/feeds/watering","0");
                    ((Switch) findViewById(R.id.watering)).setChecked(false);
                    list.add(new HumidAnalog.MQTTMessage("taunhatquang/feeds/watering","0"));
                }
            }
        });

        airConditioner.addOnSliderTouchListener( new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                float values = slider.getValue();
                Log.d("SliderPreviousValue", String.valueOf(values));
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                float values = slider.getValue();
                Log.d("SliderAfterValue", String.valueOf(values));
                sendDataMQTT("taunhatquang/feeds/ac",String.valueOf(values));
                ((Slider)findViewById(R.id.slider)).setValue(values);
                list.add(new HumidAnalog.MQTTMessage("taunhatquang/feeds/ac",String.valueOf(values)));
            }
        });
        (new HumidAnalog.GetLastData(this)).execute(humiUrl);
        (new HumidAnalog.GetLastData(this)).execute(acUrl);
        (new HumidAnalog.GetLastData(this)).execute(hoseUrl);
        startMQTT();
    }
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
                if (topic.contains("taunhatquang/feeds/humidity")){
                    Log.d("humidity", message.toString());
                    txtHumi.setInnerText(message.toString()+"%");
                    ((PieView)findViewById(R.id.pieView)).setPercentage(Integer.parseInt(message.toString()));
                }
                if (topic.contains("taunhatquang/feeds/watering")){
                    if (message.toString().equals("1")){
                        waterHose.setChecked(true);
                        ((Switch) findViewById(R.id.watering)).setChecked(true);
                    }
                    else {
                        waterHose.setChecked(false);
                        ((Switch) findViewById(R.id.watering)).setChecked(false);
                    }
                }
                if (topic.contains("taunhatquang/feeds/ac")){
                    airConditioner.setValue(Float.parseFloat(message.toString()));
                    ((Slider)findViewById(R.id.slider)).setValue(Float.parseFloat(message.toString()));
                }
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