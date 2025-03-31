package com.wb.rolladenaufab;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SunTimesUpdateService {

    private static final String PREFS_NAME = "rollladen_settings";
    private static final String SUNRISE_TIME_KEY = "sunrise_time";
    private static final String SUNSET_TIME_KEY = "sunset_time";
    private static final String[] DAYS = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};

    private Context context;

    public SunTimesUpdateService(Context context) {
        this.context = context;
    }

    public void updateSunTimes() {
        new Thread(() -> {
            try {
                HttpURLConnection connection = openConnection("https://api.open-meteo.com/v1/forecast?latitude=52.030083&longitude=7.106341&hourly=temperature_2m&daily=sunrise,sunset&timezone=Europe/Berlin");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(result.toString());
                JSONObject daily = jsonObject.getJSONObject("daily");
                JSONArray sunriseArray = daily.getJSONArray("sunrise");
                JSONArray sunsetArray = daily.getJSONArray("sunset");

                String sunrise = sunriseArray.getString(0).replace("T", " T ");
                String sunset = sunsetArray.getString(0).replace("T", " T ");

                saveSunriseSunsetTimes(sunrise, sunset);
                updateTimesBasedOnSunriseSunset(sunrise, sunset);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private HttpURLConnection openConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }

    private void saveSunriseSunsetTimes(String sunrise, String sunset) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SUNRISE_TIME_KEY, sunrise);
        editor.putString(SUNSET_TIME_KEY, sunset);
        editor.apply();
        Log.d("SunTimesUpdateService", "Sunrise und Sunset wurden gespeichert: " + sunrise + ", " + sunset);
    }

    private void updateTimesBasedOnSunriseSunset(String sunrise, String sunset) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Update on times based on sunrise
        for (String day : DAYS) {
            if (sharedPreferences.getBoolean("wohnzl_" + day + "_Sunrise", false)) {
                String[] timeParts = sunrise.split(" ")[2].split(":");
                editor.putString("onHour_" + day, timeParts[0]);
                editor.putString("onMinute_" + day, timeParts[1]);
                editor.putString("onSecond_" + day, "00");
            }
        }

        // Update off times based on sunset
        for (String day : DAYS) {
            if (sharedPreferences.getBoolean("wohnzl_" + day + "_Sunset", false)) {
                String[] timeParts = sunset.split(" ")[2].split(":");
                editor.putString("offHour_" + day, timeParts[0]);
                editor.putString("offMinute_" + day, timeParts[1]);
                editor.putString("offSecond_" + day, "00");
            }
        }

        editor.apply();
        Log.d("SunTimesUpdateService", "On/Off Zeiten basierend auf Sunrise/Sunset wurden aktualisiert");
    }
}