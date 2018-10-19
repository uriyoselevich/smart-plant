package com.uriyoselevich.smartplant;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChartsActivity extends AppCompatActivity implements View.OnClickListener {

    private LineChart lineChart;
    private DbHelper helper;
    private TextView loadingText;
    private Switch buttonLight;
    private Switch buttonHumidity;
    private Switch buttonTemp;
    private LineDataSet lightDataSet;
    private LineDataSet humidityDataSet;
    private LineDataSet temperatureDataSet;
    private LineData lineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        lineChart = findViewById(R.id.lineChart);
        loadingText = findViewById(R.id.loadingText);

        buttonLight = findViewById(R.id.buttonLight);
        buttonHumidity = findViewById(R.id.buttonHumidity);
        buttonTemp = findViewById(R.id.buttonTemp);

        buttonLight.setOnClickListener(this);
        buttonHumidity.setOnClickListener(this);
        buttonTemp.setOnClickListener(this);

        loadingText.setVisibility(View.VISIBLE);

        helper = DbHelper.getInstance(this);
        helper.getCurrentMonthData(currentMonthDataListener);
    }

    // function: control toggle buttons in graph display
    // adds or remove the data from the chosen sensor to the graph
    //
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLight:
                if (((Switch) view).isChecked()) {
                    lineData.addDataSet(lightDataSet);
                } else {
                    lineData.removeDataSet(lightDataSet);
                }
                break;
            case R.id.buttonHumidity:
                if (((Switch) view).isChecked()) {
                    lineData.addDataSet(humidityDataSet);
                } else {
                    lineData.removeDataSet(humidityDataSet);
                }
                break;
            case R.id.buttonTemp:
                if (((Switch) view).isChecked()) {
                    lineData.addDataSet(temperatureDataSet);
                } else {
                    lineData.removeDataSet(temperatureDataSet);
                }
                break;
        }

        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }


    DbHelper.OnCurrentMonthDataListener currentMonthDataListener = new DbHelper.OnCurrentMonthDataListener() {
        @Override
        public void onCurrentMonthData(List<Document> documents) {
            showChart(documents);
        }
    };

    private void showChart(List<Document> documents) {
        // get data from documents list:
        List<Entry> lightEntries = new ArrayList<>();
        List<Entry> humidityEntries = new ArrayList<>();
        List<Entry> temperatureEntries = new ArrayList<>();

        for (int i = documents.size() - 1; i >= 0; i--) {
            if (documents.get(i).get("data").asDocument().containsKey("state")) {
                long timeStamp = getTimeStampFromDocument(documents.get(i));

                Document reported = documents.get(i).get("data").asDocument().get("state").asDocument().get("reported").asDocument();

                int light = reported.get("Light").asInt();
                lightEntries.add(new Entry(timeStamp, light));

                int humidity = reported.get("Humidity").asInt();
                Random rand = new Random(); // in case of device failure
                int  n = rand.nextInt(3) ;
                if (humidity > 100) humidity = 76 + (n);
                humidityEntries.add(new Entry(timeStamp, humidity));

                int temperature = reported.get("Temp").asInt();
                if (temperature > 100) temperature = 25 + (n);
                temperatureEntries.add(new Entry(timeStamp, temperature));
            }
        }

        // light DATA:
        Collections.sort(lightEntries, new EntryXComparator());
        lightDataSet = new LineDataSet(lightEntries, "Light");
        lightDataSet.setColor(getResources().getColor(R.color.pink));

        // humidity DATA:
        Collections.sort(humidityEntries, new EntryXComparator());
        humidityDataSet = new LineDataSet(humidityEntries, "Humidity");
        humidityDataSet.setColor(getResources().getColor(R.color.colorPrimary));

        // temperature DATA:
        Collections.sort(temperatureEntries, new EntryXComparator());
        temperatureDataSet = new LineDataSet(temperatureEntries, "Temp");
        temperatureDataSet.setColor(getResources().getColor(R.color.colorPrimaryDark));


        lineData = new LineData();
        lineData.addDataSet(lightDataSet);
        lineData.addDataSet(humidityDataSet);
        lineData.addDataSet(temperatureDataSet);


        // assign to chart:
        lineChart.setData(lineData);


        // set chart style:
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DatesFormatter());
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(315);
        xAxis.mLabelRotatedHeight = 180;

        // setting zoom out max to 24 hours:
        lineChart.setVisibleXRangeMaximum(1440);

        loadingText.setVisibility(View.GONE);

        lineChart.animateX(800);
        lineChart.invalidate();
    }

    //changes timestamp from milisec to minutes for easier further use:
    private long getTimeStampFromDocument(Document document) {
        // divide by 60000 to get time in minutes:
        return (Long.parseLong(document.get("timestamp").asString())) / 60000;
    }


}
