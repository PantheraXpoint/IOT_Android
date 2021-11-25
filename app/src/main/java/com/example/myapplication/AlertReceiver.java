package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.ActivityController.AlarmActivity;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;

public class AlertReceiver extends BroadcastReceiver {
    int waiting_period = 0;
    boolean send_message_again = false;
    MQTTHelper mqttHelper;
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(1, nb.build());

        Log.d("Previous","signal");

//        mqttHelper = new MQTTHelper(context,"123456789");
//        MySingleton.getInstance(context).
        sendDataMQTT("taunhatquang/feeds/bbc-led","0");

        Log.d("Previous","signal");

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
}
