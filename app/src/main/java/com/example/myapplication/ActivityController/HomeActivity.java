package com.example.myapplication.ActivityController;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myapplication.HumidGraphActivity;
import com.example.myapplication.LedActivity;
import com.example.myapplication.OnSwipeTouchListener;
import com.example.myapplication.R;
import com.example.myapplication.TempGraphActivity;
import com.google.android.material.navigation.NavigationView;

import static android.content.ContentValues.TAG;

public class HomeActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView navDrawerButton;

    private enum Activity {
        TEMPERATURE,
        HUMIDITY,
        LED,
        ALARM
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        drawerLayout = (DrawerLayout) findViewById(R.id.homeLayout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navDrawerButton = findViewById(R.id.imageMenu);


        LinearLayout tempBtn = findViewById(R.id.tempBtn);
        LinearLayout humidBtn = findViewById(R.id.humidBtn);
        LinearLayout ledBtn = findViewById(R.id.ledBtn);
        LinearLayout alarmBtn = findViewById(R.id.alarmBtn);

        navDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.END);
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
        alarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoActivity(Activity.ALARM);
            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.i(TAG, "onNavigationItemSelected: " + item.getItemId());
                switch (item.getItemId()) {

                    case R.id.livingRoom:
                        startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                        return true;

                    case R.id.bedRoom:
                        Intent intentBed = new Intent(HomeActivity.this, HomeActivity.class);
                        startActivity(intentBed);
                        return true;

                    case R.id.bathRoom:
                        Intent intentBath = new Intent(HomeActivity.this, HomeActivity.class);
                        startActivity(intentBath);
                        return true;

                    case R.id.kitchen:
                        Intent intentKitchen = new Intent(HomeActivity.this, HomeActivity.class);
                        startActivity(intentKitchen);
                        return true;

                    case R.id.Settings:
                        Intent intentSettings = new Intent(HomeActivity.this, HomeActivity.class);
                        startActivity(intentSettings);
                        return true;

                    case R.id.History:
                        Intent intentHistory = new Intent(HomeActivity.this, HomeActivity.class);
                        startActivity(intentHistory);
                        return true;

                    case R.id.Profile:
                        Intent intentProfile = new Intent(HomeActivity.this, HomeActivity.class);
                        startActivity(intentProfile);
                        return true;
                }
                drawerLayout.closeDrawer(Gravity.END);
                Log.i(TAG, "onNavigationItemSelected: nothing clicked");
                return false;
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
            case ALARM:
                intent = new Intent(this, AlarmActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        finish();
    }

}
