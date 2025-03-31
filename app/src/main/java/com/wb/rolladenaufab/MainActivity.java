package com.wb.rolladenaufab;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

    private static final String PREFS_NAME = "rollladen_settings";
    private static final String SUNRISE_TIME_KEY = "sunrise_time";
    private static final String SUNSET_TIME_KEY = "sunset_time";

    private TextView sunriseTextView;
    private TextView sunsetTextView;
    private ExecutorService executorService;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bildschirm-Ausrichtung auf Hochformat festlegen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_color)); // Setzen Sie die Hintergrundfarbe der Toolbar

        sunriseTextView = findViewById(R.id.sunriseTextView);
        sunsetTextView = findViewById(R.id.sunsetTextView);

        executorService = Executors.newSingleThreadExecutor();
        handler = new Handler(Looper.getMainLooper());

        // Laden der gespeicherten Zeiten
        loadSunriseSunsetTimes();

        // Aktualisieren der Sonnenaufgangs- und Sonnenuntergangszeiten beim Start
        SunTimesUpdateService sunTimesUpdateService = new SunTimesUpdateService(this);
        sunTimesUpdateService.updateSunTimes();

        // Planen der täglichen Aktualisierung um Mitternacht
        scheduleDailyUpdate();
    } // ENDE onCreate

    @Override
    protected void onResume() {
        super.onResume();
        fetchSunriseSunset();
    }

    private void fetchSunriseSunset() {
        executorService.execute(() -> {
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

                handler.post(() -> {
                    sunriseTextView.setText("Sunrise: " + sunrise);
                    sunsetTextView.setText("Sunset: " + sunset);
                    checkAlarmTimes(sunrise, sunset);

                    // Speichern der Zeiten in SharedPreferences
                    saveSunriseSunsetTimes(sunrise, sunset);
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

            // Aktuelle Zeit setzen
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
            Date currentDate = calendar.getTime();

            Log.d("CheckAlarmTimes", "Current Date: " + currentDate + " TimeZone: " + calendar.getTimeZone().getID());
            Log.d("CheckAlarmTimes", "Sunrise Date: " + sunriseDate);
            Log.d("CheckAlarmTimes", "Sunset Date: " + sunsetDate);

            if (currentDate.after(sunriseDate)) {
                Log.d("CheckAlarmTimes", "Current Date ist nach Sunrise -> sunriseTextView wird rot");
                sunriseTextView.setTextColor(Color.RED);
            } else {
                Log.d("CheckAlarmTimes", "Current Date ist vor Sunrise -> sunriseTextView bleibt unverändert");
            }

            if (currentDate.after(sunsetDate)) {
                Log.d("CheckAlarmTimes", "Current Date ist nach Sunset -> sunsetTextView wird rot");
                sunsetTextView.setTextColor(Color.RED);
            } else {
                Log.d("CheckAlarmTimes", "Current Date ist vor Sunset -> sunsetTextView bleibt unverändert");
            }

            // Beide TextViews nach 00:00 wieder grau setzen
            Calendar midnightCalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"));
            midnightCalendar.set(Calendar.HOUR_OF_DAY, 0);
            midnightCalendar.set(Calendar.MINUTE, 0);
            midnightCalendar.set(Calendar.SECOND, 0);
            midnightCalendar.set(Calendar.MILLISECOND, 0);
            Date midnight = midnightCalendar.getTime();

            if (currentDate.after(midnight) && currentDate.before(sunriseDate)) {
                sunriseTextView.setTextColor(Color.GRAY);
                sunsetTextView.setTextColor(Color.GRAY);
            }

        } catch (Exception e) {
            Log.e("CheckAlarmTimes", "Fehler beim Parsen der Zeiten", e);
        }
    }

    private void scheduleDailyUpdate() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //Intent intent = new Intent(this, UpdateReceiver.class);
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);

        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
        //AlarmManager.INTERVAL_DAY, pendingIntent);
    } // ENDE scheduleDailyUpdate

    // Speichern der Sunrise- und Sunset-Zeiten in SharedPreferences
    private void saveSunriseSunsetTimes(String sunrise, String sunset) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SUNRISE_TIME_KEY, sunrise);
        editor.putString(SUNSET_TIME_KEY, sunset);
        Log.d("Sunrise Sunset speichern", "Sunrise und Sunset wurden gespeichert");
        editor.apply();
    }

    // Laden der Sunrise- und Sunset-Zeiten aus SharedPreferences
    private void loadSunriseSunsetTimes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String sunrise = sharedPreferences.getString(SUNRISE_TIME_KEY, "N/A");
        String sunset = sharedPreferences.getString(SUNSET_TIME_KEY, "N/A");

        sunriseTextView.setText("Sunrise: " + sunrise);
        sunsetTextView.setText("Sunset: " + sunset);
    }

    // Menüoptionen in der Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    } // ENDE onCreateOptionsMenu

    // Optionen im Menü
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back || id == android.R.id.home) {
            // Hier kannst du die Aktion für den Zurück-Pfeil behandeln
            onBackPressed(); // Beispiel: Gehe zur vorherigen Aktivität
            return true;
        }
        if (id == R.id.info_settings) {
            Intent intent = new Intent(this, SettingInfoActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.wohnzimmerlinks_settings) {
            Intent intent = new Intent(this, SettingWohnzLinksActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.wohnzimmerrechts_settings) {
            Intent intent = new Intent(this, SettingWohnzRechtsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.schlafzimmerlinks_settings) {
            Intent intent = new Intent(this, SettingSchlafzLinksActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.schlafzimmerrechts_settings) {
            Intent intent = new Intent(this, SettingSchlafzRechtsActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.badezimmer_settings) {
            Intent intent = new Intent(this, SettingBadezActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    } // ENDE onOptionsItemSelected

}