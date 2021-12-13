package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.slider.Slider;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
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

import az.plainpie.PieView;

public class ActivityAC extends AppCompatActivity {
    ImageView powerIcon;
    ImageView optionIcon;
    LottieAnimationView acLottie;
    LottieAnimationView bgLottie;
    PieView acAnalog;
    MQTTHelper mqttHelper;
    Slider acSlider;

    List<ActivityAC.MQTTMessage> msgList = new ArrayList<>();

    boolean isOn;
    boolean isMessageSentAgain = false;
    boolean isPopupWindowShown = false;

    int waitingPeriod = 0;

    private final String acToggleURL = "https://io.adafruit.com/api/v2/taunhatquang/feeds/toggleac";
    private final String acToggleFeed = "taunhatquang/feeds/toggleac";
    private final String acFeed = "taunhatquang/feeds/ac";
    private final String acURL = "https://io.adafruit.com/api/v2/taunhatquang/feeds/ac";

// Getting newest data from AdaFruit
    private class GetLastData extends AsyncTask<String, Void, String> {
        String topic = "";
        String last_value = "";
        String URLcontent = "";
        private ProgressDialog dialog;

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));

                String tmp = "";
                while(tmp != null) {
                    tmp = bufferedReader.readLine();
                    URLcontent += tmp;
                }

                JSONObject jsonObject = new JSONObject(URLcontent);
                last_value = jsonObject.getString("last_value");
                topic = jsonObject.getString("name");
            } catch (MalformedURLException e) {

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return topic + " " + last_value;
        }

        @Override
        protected void onPreExecute() {
            dialog.setTitle("Getting newest data ...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            List<String> tmp = Arrays.asList(s.split(" "));
            if (tmp.get(0).equals("toggleAC")) {
                if (tmp.get(1).equals("1")) {
                    isOn = true;
                    acLottie.setSpeed(1);
                    acLottie.playAnimation();
                } else {
                    isOn = false;
                    acLottie.setFrame(0);
                    acLottie.setSpeed(0);
                    acLottie.playAnimation();
                }
            }

            if (isPopupWindowShown && tmp.get(0).equals("AC")) {
                Log.d("GetLastData for AC:", "runs");
                acSlider.setValue(Float.parseFloat(tmp.get(1)));
            }

            if (dialog.isShowing()) dialog.dismiss();
        }

        public GetLastData(Activity act) {
            this.dialog = new ProgressDialog(act);
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    protected class MQTTMessage {
        public String topic;
        public String message;

        public MQTTMessage (String topic, String message) {
            this.topic = topic;
            this.message = message;
        }
    }

// To-do functions when rendering screen
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_activity);

        //        Screen layout
        LinearLayout linearLayout = findViewById(R.id.ac_activity);
        linearLayout.setOnTouchListener(new OnSwipeTouchListener(ActivityAC.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(ActivityAC.this, LedActivity.class);
                startActivity(intent);
                finish();
            }
        });

//      AC Lottie
        acLottie = findViewById(R.id.acLottie);
//        acLottie.setSpeed(0);

//        Power button icon
        powerIcon = (ImageView)findViewById(R.id.powerImg);
        powerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sentValue;

                if (isOn == true){
                    isOn = false;
                    sentValue = "0";

                    acLottie.setFrame(0);
                    acLottie.setSpeed(0);

                }
                else {
                    isOn = true;
                    sentValue = "1";

                    acLottie.setSpeed(1);
                }

                sendDataMQTT(acToggleFeed, sentValue);
                msgList.add(new ActivityAC.MQTTMessage(acToggleFeed, sentValue));
            }
        });

//        Option button icon
        optionIcon = (ImageView)findViewById(R.id.optionImg);
        optionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPopupWindowShown) {
                    isPopupWindowShown = true;
                    Log.d("isPopupWindowShown=", (isPopupWindowShown ? "true" : "false"));
                    onButtonShowPopupWindowClick(v);
                }
                else {
                    isPopupWindowShown = false;
                    Log.d("isPopupWindowShown=", (isPopupWindowShown ? "true" : "false"));
                }

            }
        });

//        AC Slider
        if (isPopupWindowShown) {
            Log.d("isPopupWindowShown=", (isPopupWindowShown ? "true" : "false"));
            acSlider = findViewById(R.id.acSlider);
            acSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                    float value = slider.getValue();
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    float value = slider.getValue();

                    sendDataMQTT(acFeed, String.valueOf(value));

                    acSlider.setValue(Float.parseFloat(String.valueOf(value)));

                    msgList.add(new ActivityAC.MQTTMessage(acFeed, String.valueOf(value)));
                }
            });

            (new ActivityAC.GetLastData(this)).execute(acURL);
        }


//        Pie chart analog
        acAnalog = findViewById(R.id.acByView);
        acAnalog.setMainBackgroundColor(getResources().getColor(R.color.DarkOrchid));
        acAnalog.setInnerBackgroundColor(getResources().getColor(R.color.white));
        acAnalog.setTextColor(getResources().getColor(R.color.black));

//        Update screen
        (new ActivityAC.GetLastData(this)).execute(acToggleURL);


        startMQTT();
    }

//    Send data to AdaFruit
    private void sendDataMQTT(String topic, String value) {
        Log.d("(Topic, value)=", '(' + topic + ',' + value + ')');
        waitingPeriod = 3;
        isMessageSentAgain = false;

        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(true);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);

        try {
            mqttHelper.mqttAndroidClient.publish(topic, msg);
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

//    Read data from AdaFruit
    private void startMQTT() {
        mqttHelper = new MQTTHelper(getApplicationContext(), "123456789");
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d("mqtt", "Connection is successfully");
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.contains(acToggleFeed)) {
                    if (message.toString().equals("1")) {
                        isOn = true;
                        acLottie.setSpeed(1);
                        acLottie.playAnimation();
                        Log.d("AC", "isOn");
                    } else {
                        isOn = false;
                        acLottie.setFrame(0);
                        acLottie.setSpeed(0);
                        acLottie.playAnimation();
                    }
                } else if (isPopupWindowShown && topic.contains(acFeed)) {
                    acSlider.setValue(Float.parseFloat(message.toString()));
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

//    Open pop-up slider
    static final int popupWindowWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
    static final int popupWindowHeight = 250;

    public void onButtonShowPopupWindowClick(View v) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, popupWindowWidth, popupWindowHeight, focusable);
        popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 500);

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isPopupWindowShown = false;
                Log.d("isPopupWindowShown=", (isPopupWindowShown ? "true" : "false"));
                popupWindow.dismiss();
                return true;
            }
        });
    }
}
