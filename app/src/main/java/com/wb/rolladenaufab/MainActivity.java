package com.wb.rolladenaufab;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private Button sunriseAlarmButton;
    private Button sunsetAlarmButton;
    private ExecutorService executorService;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sunriseTextView = findViewById(R.id.sunriseTextView);
        sunsetTextView = findViewById(R.id.sunsetTextView);
        sunriseAlarmButton = findViewById(R.id.sunriseAlarmButton);
        sunsetAlarmButton = findViewById(R.id.sunsetAlarmButton);

        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        scheduleDailyUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSunriseSunset();
    }

    private void fetchSunriseSunset() {
        executorService.execute(() -> {
            try {
                HttpURLConnection connection = openConnection("https://api.open-meteo.com/v1/forecast?latitude=52.030083&longitude=7.106341&hourly=temperature_2m&daily=sunrise,sunset&timezone=Europe%2FBerlin&forecast_days=1");

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

                handler.post(() -> {
                    sunriseTextView.setText("Sunrise: " + sunrise);
                    sunsetTextView.setText("Sunset: " + sunset);
                    checkAlarmTimes(sunrise, sunset);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private HttpURLConnection openConnection(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }

    private void checkAlarmTimes(String sunrise, String sunset) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd 'T' HH:mm", Locale.getDefault());
        originalFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));

        try {
            Date sunriseDate = originalFormat.parse(sunrise);
            Date sunsetDate = originalFormat.parse(sunset);

            // Feste Testzeit setzen (Zeitzone explizit setzen)
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
            calendar.set(Calendar.YEAR, 2025);
            calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 13);
            calendar.set(Calendar.HOUR_OF_DAY, 16);
            calendar.set(Calendar.MINUTE, 55);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date currentDate = calendar.getTime();

            Log.d("CheckAlarmTimes", "Current Date: " + currentDate + " TimeZone: " + calendar.getTimeZone().getID());
            Log.d("CheckAlarmTimes", "Sunrise Date: " + sunriseDate);
            Log.d("CheckAlarmTimes", "Sunset Date: " + sunsetDate);

            if (currentDate.after(sunriseDate)) {
                Log.d("CheckAlarmTimes", "Current Date ist nach Sunrise -> Button wird rot");
                sunriseAlarmButton.setBackgroundColor(Color.RED);
            } else {
                Log.d("CheckAlarmTimes", "Current Date ist vor Sunrise -> Button bleibt unverändert");
            }

            if (currentDate.after(sunsetDate)) {
                Log.d("CheckAlarmTimes", "Current Date ist nach Sunset -> Button wird rot");
                sunsetAlarmButton.setBackgroundColor(Color.RED);
            } else {
                Log.d("CheckAlarmTimes", "Current Date ist vor Sunset -> Button bleibt unverändert");
            }

        } catch (Exception e) {
            Log.e("CheckAlarmTimes", "Fehler beim Parsen der Zeiten", e);
        }
    }


    public void onSunriseAlarmClick(View view) {
        toggleButtonColor(sunriseAlarmButton);
    }

    public void onSunsetAlarmClick(View view) {
        toggleButtonColor(sunsetAlarmButton);
    }

    private void toggleButtonColor(Button button) {
        Drawable background = button.getBackground();
        if (background instanceof ColorDrawable) {
            int currentColor = ((ColorDrawable) background).getColor();
            if (currentColor == Color.RED) {
                button.setBackgroundColor(Color.GREEN);
            } else {
                button.setBackgroundColor(Color.RED);
            }
        }
    }

    private void scheduleDailyUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, UpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}