package com.wb.rolladenaufab;


import android.content.SharedPreferences;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateSunrisesetTimeHelperWohnzL {
    private static final String KEY_SUNRISE_TIME = "sunrise_time";
    private static final String KEY_SUNSET_TIME = "sunset_time";
    private static final String[] DAYS = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateTimeFormat;

    public UpdateSunrisesetTimeHelperWohnzL() {
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
    }

    public void updateSunriseTime(SharedPreferences sharedPreferences, Spinner weekdaySpinner, EditText etSeekBarSunriseValue, EditText etOnHour, EditText etOnMinute, EditText etOnSecond, CheckBox cbSunrise) {
        if (!cbSunrise.isChecked()) {
            return; // Do not update if the checkbox is not checked
        }

        int selectedDayIndex = weekdaySpinner.getSelectedItemPosition();
        if (selectedDayIndex >= 0 && selectedDayIndex < DAYS.length) {
            String selectedDay = DAYS[selectedDayIndex];
            String sunriseTime = sharedPreferences.getString(KEY_SUNRISE_TIME, "2025-03-27T06:40");
            int sunriseOffset = Integer.parseInt(etSeekBarSunriseValue.getText().toString());

            Log.d("UpdateSunrisesetTimeHelperWohnzL", "Sunrise time: " + sunriseTime);
            Log.d("UpdateSunrisesetTimeHelperWohnzL", "Sunrise offset: " + sunriseOffset);

            try {
                Date sunriseDate = dateTimeFormat.parse(sunriseTime.replace(" T ", "T"));
                if (sunriseDate != null) {
                    long sunriseMillis = sunriseDate.getTime() + sunriseOffset * 60 * 1000;
                    Date newTime = new Date(sunriseMillis);

                    String[] timeParts = timeFormat.format(newTime).split(":");
                    if (timeParts.length >= 2) {
                        etOnHour.setText(timeParts[0]);
                        etOnMinute.setText(timeParts[1]);
                        etOnSecond.setText("00");

                        Log.d("UpdateSunrisesetTimeHelperWohnzL", "New time: " + timeFormat.format(newTime));
                    }
                }
            } catch (ParseException e) {
                Log.e("UpdateSunrisesetTimeHelperWohnzL", "ParseException: " + e.getMessage());
            }
        }
    }

    public void updateSunsetTime(SharedPreferences sharedPreferences, Spinner weekdaySpinner, EditText etSeekBarSunsetValue, EditText etOffHour, EditText etOffMinute, EditText etOffSecond, CheckBox cbSunset) {
        if (!cbSunset.isChecked()) {
            return; // Do not update if the checkbox is not checked
        }

        int selectedDayIndex = weekdaySpinner.getSelectedItemPosition();
        if (selectedDayIndex >= 0 && selectedDayIndex < DAYS.length) {
            String selectedDay = DAYS[selectedDayIndex];
            String sunsetTime = sharedPreferences.getString(KEY_SUNSET_TIME, "2025-03-27T18:40");
            int sunsetOffset = Integer.parseInt(etSeekBarSunsetValue.getText().toString());

            Log.d("UpdateSunrisesetTimeHelperWohnzL", "Sunset time: " + sunsetTime);
            Log.d("UpdateSunrisesetTimeHelperWohnzL", "Sunset offset: " + sunsetOffset);

            try {
                Date sunsetDate = dateTimeFormat.parse(sunsetTime.replace(" T ", "T"));
                if (sunsetDate != null) {
                    long sunsetMillis = sunsetDate.getTime() + sunsetOffset * 60 * 1000;
                    Date newTime = new Date(sunsetMillis);

                    String[] timeParts = timeFormat.format(newTime).split(":");
                    if (timeParts.length >= 2) {
                        etOffHour.setText(timeParts[0]);
                        etOffMinute.setText(timeParts[1]);
                        etOffSecond.setText("00");

                        Log.d("UpdateSunrisesetTimeHelperWohnzL", "New time: " + timeFormat.format(newTime));
                    }
                }
            } catch (ParseException e) {
                Log.e("UpdateSunrisesetTimeHelperWohnzL", "ParseException: " + e.getMessage());
            }
        }
    }
}