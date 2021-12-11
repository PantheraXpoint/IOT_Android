package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.myapplication.ActivityController.HomeActivity;

public class ActivityAC extends AppCompatActivity {
    ImageView img_icon;
    LottieAnimationView lottie;
    boolean isOn = false;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_activity);

        img_icon = (ImageView)findViewById(R.id.acImg);
        lottie = findViewById(R.id.acLottie);
        lottie.setSpeed(0);

        img_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOn == true){
                    lottie.setFrame(0);
                    lottie.setSpeed(0);
                    isOn = false;
                }
                else{
                    lottie.setSpeed(1);
                    lottie.playAnimation();;
                    isOn = true;
                }
            }
        });

        LinearLayout linearLayout = findViewById(R.id.ac_activity);
        linearLayout.setOnTouchListener(new OnSwipeTouchListener(ActivityAC.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(ActivityAC.this, LedActivity.class);
                startActivity(intent);
//                overridePendingTransition(R.anim.slide_out_right,R.anim.slide_in_left);
                finish();
            }
        });
    }
}
