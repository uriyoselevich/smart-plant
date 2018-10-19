package com.uriyoselevich.smartplant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView lightAmountTextView;
    private TextView humidityPercentTextView;
    private TextView temperatureTextView;
    private CardView cardViewNeedsWaterButton;
    private CardView cardViewDoesntNeedWater;
    private DbHelper helper;
    private TextView textViewLastUpdate;

    private TextView loadingText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        lightAmountTextView = findViewById(R.id.lightAmountTextView);
        humidityPercentTextView = findViewById(R.id.humidityPercentTextView);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        cardViewNeedsWaterButton = findViewById(R.id.cardViewNeedsWaterButton);
        cardViewDoesntNeedWater = findViewById(R.id.cardViewDoesntNeedWater);
        textViewLastUpdate = findViewById(R.id.textViewLastUpdate);
        loadingText = findViewById(R.id.loadingText);


        loadingText.setVisibility(View.VISIBLE);


        helper = DbHelper.getInstance(this);
        helper.getCurrentMonthData(currentMonthDataListener);
    }

    //looks for latest row in DB with the "data.state.reported" structure
    DbHelper.OnCurrentMonthDataListener currentMonthDataListener = new DbHelper.OnCurrentMonthDataListener() {
        @Override
        public void onCurrentMonthData(List<Document> documents) {
            if (documents.size() > 0) {
                for (int i = documents.size() - 1; i >= 0; i--) {
                    if (documents.get(i).get("data").asDocument().containsKey("state")) {

                        Document reported = documents.get(i).get("data").asDocument().get("state").asDocument().get("reported").asDocument();

                        refreshUI(reported, Long.parseLong(documents.get(i).get("timestamp").asString()));
                        return;
                    }
                }
            }
        }
    };

    private void refreshUI(Document lastUpdate, long timeInMillis) {
        int light = lastUpdate.get("Light").asInt();
        boolean needsWater = Boolean.valueOf(lastUpdate.get("NeedsWater").asString());
        float temp = lastUpdate.get("Temp").asFloat();
        int humidity = lastUpdate.get("Humidity").asInt();


        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        @SuppressLint("SimpleDateFormat")
        String lastUpdateTime = new SimpleDateFormat("dd/MM/YY HH:mm").format(cal.getTime());

        //Text Display next to sensors value:
        lightAmountTextView.setText(light + " lux");
        humidityPercentTextView.setText(humidity + "%");
        temperatureTextView.setText(temp + " c°");

        if (needsWater) {
            cardViewNeedsWaterButton.setVisibility(View.VISIBLE);
            cardViewDoesntNeedWater.setVisibility(View.GONE);
        } else {
            cardViewNeedsWaterButton.setVisibility(View.GONE);
            cardViewDoesntNeedWater.setVisibility(View.VISIBLE);
        }

        textViewLastUpdate.setText(getString(R.string.last_update) + lastUpdateTime);
        loadingText.setVisibility(View.GONE);

        Toast.makeText(this, "DATA IS UP TO DATE", Toast.LENGTH_LONG).show();
    }

    //region XML ONCLICK

    public void onSensorsClick(View view) {
        startActivity(new Intent(this, ChartsActivity.class));
    }


    public void onRefreshClick(View view) {
        Toast.makeText(this, "REFRESHING...", Toast.LENGTH_LONG).show();
        helper.getCurrentMonthData(currentMonthDataListener);
    }

    //endregion


}
