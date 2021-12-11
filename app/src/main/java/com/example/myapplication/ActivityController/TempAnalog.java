package com.example.myapplication.ActivityController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.myapplication.HumidGraphActivity;
import com.example.myapplication.LedActivity;
import com.example.myapplication.MQTTHelper;
import com.example.myapplication.OnSwipeTouchListener;
import com.example.myapplication.R;
import com.example.myapplication.TempGraphActivity;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.slider.Slider;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

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

public class TempAnalog extends AppCompatActivity {

    private static final String TAG = "TempAnalog";

    int waiting_period = 0;
    boolean send_message_again = false;
    List<TempAnalog.MQTTMessage> list = new ArrayList<>();
    MQTTHelper mqttHelper;
    PieView txtTemp;
    Switch waterHose;
    ShimmerTextView temp;
    Shimmer shimmer = new Shimmer();
    public String tempUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/temperature";
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
//            dialog.setTitle("Updating last data ...");
//            dialog.show();
        }

        @Override
        protected void onPostExecute(String temp) {
            List<String> tmp = Arrays.asList(temp.split(" "));
            if (tmp.get(0).equals("Temperature")){
                ((PieView)findViewById(R.id.pieViewTemp)).setPercentage(Integer.parseInt(tmp.get(1)));
                ((PieView)findViewById(R.id.pieViewTemp)).setInnerText(tmp.get(1) + "°C");
            }
            if (tmp.get(0).equals("Watering")){
                if (tmp.get(1).equals("1")) {
                    ((Switch) findViewById(R.id.wateringTemp)).setChecked(true);
                }
                else {
                    ((Switch) findViewById(R.id.wateringTemp)).setChecked(false);
                }
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
        setContentView(R.layout.temp_analog);

        txtTemp = findViewById(R.id.pieViewTemp);
        waterHose = findViewById(R.id.wateringTemp);

        txtTemp.setPercentageBackgroundColor(getResources().getColor(R.color.teal_200));


        ConstraintLayout constraintLayout = findViewById(R.id.tempAnalog);
        constraintLayout.setOnTouchListener(new OnSwipeTouchListener(TempAnalog.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(TempAnalog.this, HomeActivity.class);
                startActivity(intent);
//                overridePendingTransition(R.anim.slide_out_right,R.anim.slide_in_left);
                finish();
            }
            public void onSwipeLeft() {
                Intent intent = new Intent(TempAnalog.this, TempGraphActivity.class);
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
                    waterHose.setChecked(true);
                    ((Switch) findViewById(R.id.wateringTemp)).setChecked(true);
                    list.add(new TempAnalog.MQTTMessage("taunhatquang/feeds/watering","1"));
                }
                else{
                    Log.d("mqtt","Button is unchecked");
                    sendDataMQTT("taunhatquang/feeds/watering","0");
                    waterHose.setChecked(false);
                    ((Switch) findViewById(R.id.wateringTemp)).setChecked(false);
                    list.add(new TempAnalog.MQTTMessage("taunhatquang/feeds/watering","0"));
                }
            }
        });


        (new TempAnalog.GetLastData(this)).execute(tempUrl);
        (new TempAnalog.GetLastData(this)).execute(hoseUrl);

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
                if (topic.contains("taunhatquang/feeds/temperature")){
                    ((PieView)findViewById(R.id.pieViewTemp)).setPercentage(Integer.parseInt(message.toString()));
                    txtTemp.setInnerText(message.toString()+"°C");
                }
                if (topic.contains("taunhatquang/feeds/watering")){
                    if (message.toString().equals("1")){
                        waterHose.setChecked(true);
                        ((Switch) findViewById(R.id.wateringTemp)).setChecked(true);
                    }
                    else {
                        waterHose.setChecked(false);
                        ((Switch) findViewById(R.id.wateringTemp)).setChecked(false);
                    }
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