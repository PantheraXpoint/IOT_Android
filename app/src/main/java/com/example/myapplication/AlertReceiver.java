package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.ActivityController.AlarmActivity;
import com.example.myapplication.Observer.Observables;
import com.example.myapplication.Observer.RepositoryObserver;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class AlertReceiver extends BroadcastReceiver  implements Observables {
    int waiting_period = 0;
    boolean send_message_again = false;
    private static AlertReceiver INSTANCE = null;

    private static ArrayList<RepositoryObserver> mObservers;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(1, nb.build());
        notifyObservers();
        Log.d("Previous","signal");


        Log.d("Previous","signal");

    }

    @Override
    public void registerObserver(RepositoryObserver repositoryObserver) {
        if (!mObservers.contains(repositoryObserver))
        {
            mObservers.add(repositoryObserver);
        }
    }

    public static AlertReceiver getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new AlertReceiver() {
            };
        }
        return INSTANCE;
    }

    @Override
    public void removeObserver(RepositoryObserver repositoryObserver) {
        if (mObservers.contains(repositoryObserver))
        {
            mObservers.remove(repositoryObserver);
        }
    }

    @Override
    public void notifyObservers() {
        for (RepositoryObserver observer: mObservers){
            observer.onAlarmSent();
        }
    }
}
