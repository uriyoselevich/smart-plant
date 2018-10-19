package com.uriyoselevich.smartplant;

import android.annotation.SuppressLint;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatesFormatter implements IAxisValueFormatter {
    private Calendar cal = Calendar.getInstance();
//    private int lastPresentedDay = -1;

    @SuppressLint("SimpleDateFormat")
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        //return timestamp to milisec:
        cal.setTimeInMillis((long) value * 60000);
//        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        //sets the view of hour and date for the graph:
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm  dd/MM/yy");
//        if (cal.get(Calendar.DAY_OF_MONTH) != lastPresentedDay) {
//            lastPresentedDay = cal.get(Calendar.DAY_OF_MONTH);
            return dateFormat.format(cal.getTime());
//        } else {
//            return timeFormat.format(cal.getTime());
//        }
    }
}
