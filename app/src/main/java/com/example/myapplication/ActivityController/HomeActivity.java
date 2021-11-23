package com.example.myapplication.ActivityController;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.HumidGraphActivity;
import com.example.myapplication.LedActivity;
import com.example.myapplication.OnSwipeTouchListener;
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

        RelativeLayout relativeLayout = findViewById(R.id.homeLayout);
        relativeLayout.setOnTouchListener(new OnSwipeTouchListener(HomeActivity.this) {
            public void onSwipeRight() {

            }
            public void onSwipeLeft() {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                finish();
            }
        });

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
                intent = new Intent(this, TempAnalog.class);
                break;
            case HUMIDITY:
                intent = new Intent(this, HumidAnalog.class);
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