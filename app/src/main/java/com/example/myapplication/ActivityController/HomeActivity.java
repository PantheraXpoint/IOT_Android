package com.example.myapplication.ActivityController;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.HumidGraphActivity;
import com.example.myapplication.LedActivity;
import com.example.myapplication.R;
import com.example.myapplication.TempGraphActivity;

public class HomeActivity extends AppCompatActivity {

    private enum Activity {
        TEMPERATURE,
        HUMIDITY,
        LED
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        RelativeLayout tempBtn = findViewById(R.id.tempBtn);
        RelativeLayout humidBtn = findViewById(R.id.humidBtn);
        RelativeLayout ledBtn = findViewById(R.id.ledBtn);

        tempBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(Activity.TEMPERATURE);
            }
        });
        humidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(Activity.HUMIDITY);
            }
        });
        ledBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            gotoActivity(Activity.LED);
        }
    });

    }

    private void gotoActivity(Activity act)
    {
        Intent intent = null;
        switch (act) {
            case TEMPERATURE:
                intent = new Intent(this, TempGraphActivity.class);
                break;
            case HUMIDITY:
                intent = new Intent(this, HumidGraphActivity.class);
                break;
            case LED:
                intent = new Intent(this, LedActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        finish();
    }

}
