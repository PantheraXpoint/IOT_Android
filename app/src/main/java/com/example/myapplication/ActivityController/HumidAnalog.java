package com.example.myapplication.ActivityController;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.HumidGraphActivity;
import com.example.myapplication.MQTTHelper;
import com.example.myapplication.OnSwipeTouchListener;
import com.example.myapplication.R;

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



    MQTTHelper mqttHelper;
    PieView txtHumi;
    public String humiUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/humidity";

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
//            if (tmp.get(0).equals("Temperature")){
//                ((TextView)findViewById(R.id.txtTemperature)).setText(tmp.get(1) + "°C");
//            }
            if (tmp.get(0).equals("Humidity")){
                ((PieView)findViewById(R.id.pieView)).setInnerText(tmp.get(1) + "%");
                ((PieView)findViewById(R.id.pieView)).setPercentage(Integer.parseInt(tmp.get(1)));
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
        setContentView(R.layout.humid_analog);

        txtHumi = (PieView) findViewById(R.id.pieView);
//        btnLED = findViewById(R.id.btnLED);


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

        (new HumidAnalog.GetLastData(this)).execute(humiUrl);
        startMQTT();
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
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
}