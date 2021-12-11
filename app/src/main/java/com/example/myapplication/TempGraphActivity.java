package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myapplication.ActivityController.HomeActivity;
import com.example.myapplication.ActivityController.MainActivity;
import com.example.myapplication.ActivityController.TempAnalog;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import static java.lang.Integer.parseInt;

public class TempGraphActivity extends AppCompatActivity {

    private boolean isRunning = false;
    private LineChart chart;
    private Thread thread;
    String tempUrl = "https://io.adafruit.com/api/v2/taunhatquang/feeds/temperature";
    int tempAPI = 0;
    int tem = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.temp_graph);


        chart = findViewById(R.id.lineChart);
        initChart();

        Button btStop,btStart,btReset;
        btStart = findViewById(R.id.button_RunData);
        btStop = findViewById(R.id.button_Stop);
        btReset = findViewById(R.id.button_Reset);

        LinearLayout linearLayout = findViewById(R.id.tempGraph);
        linearLayout.setOnTouchListener(new OnSwipeTouchListener(TempGraphActivity.this) {
            public void onSwipeRight() {
                Intent intent = new Intent(TempGraphActivity.this, TempAnalog.class);
                startActivity(intent);
//                overridePendingTransition(R.anim.slide_out_right,R.anim.slide_in_left);
                finish();
            }
        });

        btStart.setOnClickListener(v->{
            startRun();
        });

        btStop.setOnClickListener(v->{
            isRunning = false;
        });

        btReset.setOnClickListener(v->{
            chart.clear();
            tempAPI = 0;
            tem = 1;
            initChart();
        });
    }

    private void startRun(){
        if (isRunning)return;
        if (thread != null) thread.interrupt();
        isRunning = true;

        Runnable runnable  = ()->{
            WeatherDataService weatherDataService = new WeatherDataService(TempGraphActivity.this);
            weatherDataService.getLastdata(tempUrl, new WeatherDataService.VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    Toast.makeText(TempGraphActivity.this, "Something wrong!!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(String temp) {
                    if(parseInt(temp) != tempAPI) {
                        Toast.makeText(TempGraphActivity.this, "Return temp: " + temp, Toast.LENGTH_SHORT).show();
                        tempAPI = parseInt(temp);
                    }
                }
            });
//            addData((int)(Math.random()*(60-40+1))+35);
            if(tem != tempAPI) {
                addData(tempAPI);
            }
            tem = tempAPI;
        };

        thread =  new Thread(()->{
            while (isRunning) {
                runOnUiThread(runnable);
                if (!isRunning)break;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void initChart(){
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);
        chart.setData(data);

        Legend l =  chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.BLACK);

        XAxis x =  chart.getXAxis();
        x.setTextColor(Color.BLACK);
        x.setDrawGridLines(true);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setLabelCount(5,true);
        x.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "No. " + Math.round(value);
            }
        });
        //
        YAxis y = chart.getAxisLeft();
        y.setTextColor(Color.BLACK);
        y.setDrawGridLines(true);
        y.setAxisMaximum(100);
        y.setAxisMinimum(0);
        chart.getAxisRight().setEnabled(false);
        chart.setVisibleXRange(0,50);
    }

    private void addData(float inputData){
        LineData data =  chart.getData();
        ILineDataSet set = data.getDataSetByIndex(0);
        if (set == null){
            set = createSet();
            data.addDataSet(set);
        }
        data.addEntry(new Entry(set.getEntryCount(),inputData),0);
        //
        data.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.setVisibleXRange(0,10);//Range Value(ox)
        chart.moveViewToX(data.getEntryCount());
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "lINE data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.GRAY);
        set.setLineWidth(2);
        set.setDrawCircles(true);
        set.setFillColor(Color.RED);
        set.setFillAlpha(50);
        set.setDrawFilled(true);
        set.setValueTextColor(Color.BLACK);
        set.setDrawValues(false);
        return set;
    }
}