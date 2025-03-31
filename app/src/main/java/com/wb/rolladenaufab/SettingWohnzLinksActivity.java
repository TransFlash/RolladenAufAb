package com.wb.rolladenaufab;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingWohnzLinksActivity extends AppCompatActivity {
    private EditText ipBlock1, ipBlock2, ipBlock3, ipBlock4;
    private CheckBox cbMonday, cbTuesday, cbWednesday, cbThursday, cbFriday, cbSaturday, cbSunday;
    private CheckBox cbSunrise, cbSunset;
    private EditText etOnHour, etOnMinute, etOnSecond, etOffHour, etOffMinute, etOffSecond;
    private Spinner weekdaySpinner;
    private SeekBar seekBarSunrise, seekBarSunset;
    private TextView tvSeekBarSunriseValue, tvSeekBarSunsetValue;
    private EditText etSeekBarSunriseValue, etSeekBarSunsetValue;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "rollladen_settings";
    private static final String KEY_IP = "ip_addresse_wohnzl";
    private static final String KEY_MONDAY = "wohnzl_Monday_aktiv";
    private static final String KEY_TUESDAY = "wohnzl_Tuesday_aktiv";
    private static final String KEY_WEDNESDAY = "wohnzl_Wednesday_aktiv";
    private static final String KEY_THURSDAY = "wohnzl_Thursday_aktiv";
    private static final String KEY_FRIDAY = "wohnzl_Friday_aktiv";
    private static final String KEY_SATURDAY = "wohnzl_Saturday_aktiv";
    private static final String KEY_SUNDAY = "wohnzl_Sunday_aktiv";

    private static final String KEY_SUNRISE_PREFIX = "wohnzl_seekbar_offset_";
    private static final String KEY_SUNSET_PREFIX = "wohnzl_seekbar_offset_";
    private static final String KEY_SUNRISE_TIME = "sunrise_time";
    private static final String KEY_SUNSET_TIME = "sunset_time";
    private static final String[] DAYS = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};

    private UpdateSunrisesetTimeHelperWohnzL updateSunrisesetTimeHelperWohnzL;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_wohnzlinks);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        updateSunrisesetTimeHelperWohnzL = new UpdateSunrisesetTimeHelperWohnzL();

        // Spinner initialisieren und Adapter setzen
        weekdaySpinner = findViewById(R.id.wohnzl_weekday_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.weekday_array, R.layout.spinner_wohnzlinks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekdaySpinner.setAdapter(adapter);

        ipBlock1 = findViewById(R.id.ip_wohnzl_block_1);
        ipBlock2 = findViewById(R.id.ip_wohnzl_block_2);
        ipBlock3 = findViewById(R.id.ip_wohnzl_block_3);
        ipBlock4 = findViewById(R.id.ip_wohnzl_block_4);
        cbMonday = findViewById(R.id.cb_wohnzl_Monday);
        cbTuesday = findViewById(R.id.cb_wohnzl_Tuesday);
        cbWednesday = findViewById(R.id.cb_wohnzl_Wednesday);
        cbThursday = findViewById(R.id.cb_wohnzl_Thursday);
        cbFriday = findViewById(R.id.cb_wohnzl_Friday);
        cbSaturday = findViewById(R.id.cb_wohnzl_Saturday);
        cbSunday = findViewById(R.id.cb_wohnzl_Sunday);
        cbSunrise = findViewById(R.id.cb_wohnzl_Sunrise);
        cbSunset = findViewById(R.id.cb_wohnzl_Sunset);
        etOnHour = findViewById(R.id.et_onHour);
        etOnMinute = findViewById(R.id.et_onMinute);
        etOnSecond = findViewById(R.id.et_onSecond);
        etOffHour = findViewById(R.id.et_offHour);
        etOffMinute = findViewById(R.id.et_offMinute);
        etOffSecond = findViewById(R.id.et_offSecond);
        seekBarSunrise = findViewById(R.id.seekBar_sunrise);
        seekBarSunset = findViewById(R.id.seekBar_sunset);
        etSeekBarSunriseValue = findViewById(R.id.et_seekbar_sunrise_value);
        etSeekBarSunsetValue = findViewById(R.id.et_seekbar_sunset_value);
        Button saveButton = findViewById(R.id.save_wohnzlinks_button);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loadSettings();

        setupTextWatcher(ipBlock1, ipBlock2);
        setupTextWatcher(ipBlock2, ipBlock3);
        setupTextWatcher(ipBlock3, ipBlock4);
        setupFinalBlockWatcher(ipBlock4);

        setupTimeInputSwitcher(etOnHour, etOnMinute, 23);
        setupTimeInputSwitcher(etOnMinute, etOnSecond, 59);
        setupTimeInputSwitcher(etOnSecond, etOffHour, 59);
        setupTimeInputSwitcher(etOffHour, etOffMinute, 23);
        setupTimeInputSwitcher(etOffMinute, etOffSecond, 59);
        setupTimeInputSwitcher(etOffSecond, null, 59);

        cbSunrise.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                updateSunrisesetTimeHelperWohnzL.updateSunriseTime(sharedPreferences, weekdaySpinner, etSeekBarSunriseValue, etOnHour, etOnMinute, etOnSecond, cbSunrise);
            }
        });

        cbSunset.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                updateSunrisesetTimeHelperWohnzL.updateSunsetTime(sharedPreferences, weekdaySpinner, etSeekBarSunsetValue, etOffHour, etOffMinute, etOffSecond, cbSunset);
            }
        });

        seekBarSunrise.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress - 360; // Convert progress to range -360 to +360
                if (!etSeekBarSunriseValue.hasFocus()) {
                    etSeekBarSunriseValue.setText(String.valueOf(value));
                    updateSunrisesetTimeHelperWohnzL.updateSunriseTime(sharedPreferences, weekdaySpinner, etSeekBarSunriseValue, etOnHour, etOnMinute, etOnSecond, cbSunrise);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        etSeekBarSunriseValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        etSeekBarSunriseValue.addTextChangedListener(new TextWatcher() {
            private String lastValidValue = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastValidValue = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    try {
                        int value = Integer.parseInt(s.toString());
                        if (value >= -360 && value <= 360) {
                            seekBarSunrise.setProgress(value + 360); // Convert value to range 0 to 720
                        } else {
                            etSeekBarSunriseValue.setError("Ungültiger Wert");
                        }
                    } catch (NumberFormatException e) {
                        etSeekBarSunriseValue.setError("Ungültiger Wert");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentValue = s.toString();
                if (!currentValue.equals(lastValidValue)) {
                    lastValidValue = currentValue;
                }
            }
        });

        // SeekBar Listener für Sunset (Analog zu Sunrise)
        seekBarSunset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = progress - 360; // Convert progress to range -360 to +360
                if (!etSeekBarSunsetValue.hasFocus()) {
                    etSeekBarSunsetValue.setText(String.valueOf(value));
                    updateSunrisesetTimeHelperWohnzL.updateSunsetTime(sharedPreferences, weekdaySpinner, etSeekBarSunsetValue, etOffHour, etOffMinute, etOffSecond, cbSunset);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        etSeekBarSunsetValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        etSeekBarSunsetValue.addTextChangedListener(new TextWatcher() {
            private String lastValidValue = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastValidValue = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    try {
                        int value = Integer.parseInt(s.toString());
                        if (value >= -360 && value <= 360) {
                            seekBarSunset.setProgress(value + 360); // Convert value to range 0 to 720
                        } else {
                            etSeekBarSunsetValue.setError("Ungültiger Wert");
                        }
                    } catch (NumberFormatException e) {
                        etSeekBarSunsetValue.setError("Ungültiger Wert");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentValue = s.toString();
                if (!currentValue.equals(lastValidValue)) {
                    lastValidValue = currentValue;
                }
            }
        });

        saveButton.setOnClickListener(v -> {
            saveIPAddress();
            saveCheckBoxStates();
            saveSunriseSunsetStates();
            saveTimes();
        });

        weekdaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadSunriseSunsetStates();
                loadTimes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupTextWatcher(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            private String lastValidValue = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastValidValue = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 3) {
                    try {
                        int value = Integer.parseInt(s.toString());
                        if (value > 255) {
                            current.setText(lastValidValue);
                            current.setSelection(current.getText().length());
                        } else if (next != null) {
                            next.requestFocus();
                        }
                    } catch (NumberFormatException e) {
                        current.setText(lastValidValue);
                        current.setSelection(current.getText().length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupFinalBlockWatcher(EditText lastBlock) {
        lastBlock.addTextChangedListener(new TextWatcher() {
            private String lastValidValue = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                lastValidValue = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 3) {
                    try {
                        int value = Integer.parseInt(s.toString());
                        if (value > 255) {
                            lastBlock.setText(lastValidValue);
                            lastBlock.setSelection(lastBlock.getText().length());
                        }
                    } catch (NumberFormatException e) {
                        lastBlock.setText(lastValidValue);
                        lastBlock.setSelection(lastBlock.getText().length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupTimeInputSwitcher(EditText current, EditText next, int maxValue) {
        current.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    try {
                        int value = Integer.parseInt(s.toString());
                        if (value > maxValue) {
                            current.setError("Ungültiger Wert");
                            current.setText("");
                        } else if (next != null) {
                            next.requestFocus();
                        }
                    } catch (NumberFormatException e) {
                        current.setError("Ungültiger Wert");
                        current.setText("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void saveIPAddress() {
        String ip1 = ipBlock1.getText().toString().trim();
        String ip2 = ipBlock2.getText().toString().trim();
        String ip3 = ipBlock3.getText().toString().trim();
        String ip4 = ipBlock4.getText().toString().trim();

        if (ip1.isEmpty() || ip2.isEmpty() || ip3.isEmpty() || ip4.isEmpty()) {
            Toast.makeText(this, "Ungültige IP-Adresse!", Toast.LENGTH_SHORT).show();
            return;
        }

        String ipAddress = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
        sharedPreferences.edit().putString(KEY_IP, ipAddress).apply();
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.toast_text));

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText("IP-Adresse gespeichert!");

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    private void saveCheckBoxStates() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_MONDAY, cbMonday.isChecked());
        editor.putBoolean(KEY_TUESDAY, cbTuesday.isChecked());
        editor.putBoolean(KEY_WEDNESDAY, cbWednesday.isChecked());
        editor.putBoolean(KEY_THURSDAY, cbThursday.isChecked());
        editor.putBoolean(KEY_FRIDAY, cbFriday.isChecked());
        editor.putBoolean(KEY_SATURDAY, cbSaturday.isChecked());
        editor.putBoolean(KEY_SUNDAY, cbSunday.isChecked());
        editor.apply();
    }

    private void saveSunriseSunsetStates() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int selectedDayIndex = weekdaySpinner.getSelectedItemPosition();
        if (selectedDayIndex >= 0 && selectedDayIndex < DAYS.length) {
            String selectedDay = DAYS[selectedDayIndex];
            editor.putBoolean(KEY_SUNRISE_PREFIX + selectedDay + "_Sunrise", cbSunrise.isChecked());
            editor.putBoolean(KEY_SUNSET_PREFIX + selectedDay + "_Sunset", cbSunset.isChecked());
            int sunriseOffset = Integer.parseInt(etSeekBarSunriseValue.getText().toString());
            editor.putInt(KEY_SUNRISE_PREFIX + selectedDay + "_SunriseOffset", sunriseOffset);
            int sunsetOffset = Integer.parseInt(etSeekBarSunsetValue.getText().toString());
            editor.putInt(KEY_SUNSET_PREFIX + selectedDay + "_SunsetOffset", sunsetOffset);
        }
        editor.apply();
    }

    private void saveTimes() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        int selectedDayIndex = weekdaySpinner.getSelectedItemPosition();
        if (selectedDayIndex >= 0 && selectedDayIndex < DAYS.length) {
            String selectedDay = DAYS[selectedDayIndex];
            editor.putString("onHour_" + selectedDay, etOnHour.getText().toString());
            editor.putString("onMinute_" + selectedDay, etOnMinute.getText().toString());
            editor.putString("onSecond_" + selectedDay, etOnSecond.getText().toString());
            editor.putString("offHour_" + selectedDay, etOffHour.getText().toString());
            editor.putString("offMinute_" + selectedDay, etOffMinute.getText().toString());
            editor.putString("offSecond_" + selectedDay, etOffSecond.getText().toString());
        }
        editor.apply();
    }

    private void loadSettings() {
        loadIPAddress();
        cbMonday.setChecked(sharedPreferences.getBoolean(KEY_MONDAY, false));
        cbTuesday.setChecked(sharedPreferences.getBoolean(KEY_TUESDAY, false));
        cbWednesday.setChecked(sharedPreferences.getBoolean(KEY_WEDNESDAY, false));
        cbThursday.setChecked(sharedPreferences.getBoolean(KEY_THURSDAY, false));
        cbFriday.setChecked(sharedPreferences.getBoolean(KEY_FRIDAY, false));
        cbSaturday.setChecked(sharedPreferences.getBoolean(KEY_SATURDAY, false));
        cbSunday.setChecked(sharedPreferences.getBoolean(KEY_SUNDAY, false));
        loadSunriseSunsetStates();
        loadTimes();
    }

    private void loadIPAddress() {
        String savedIP = sharedPreferences.getString(KEY_IP, "");
        if (!savedIP.isEmpty()) {
            String[] parts = savedIP.split("\\.");
            if (parts.length == 4) {
                ipBlock1.setText(parts[0]);
                ipBlock2.setText(parts[1]);
                ipBlock3.setText(parts[2]);
                ipBlock4.setText(parts[3]);
            }
        }
    }

    private void loadSunriseSunsetStates() {
        int selectedDayIndex = weekdaySpinner.getSelectedItemPosition();
        if (selectedDayIndex >= 0 && selectedDayIndex < DAYS.length) {
            String selectedDay = DAYS[selectedDayIndex];
            boolean isSunriseChecked = sharedPreferences.getBoolean(KEY_SUNRISE_PREFIX + selectedDay + "_Sunrise", false);
            boolean isSunsetChecked = sharedPreferences.getBoolean(KEY_SUNSET_PREFIX + selectedDay + "_Sunset", false);

            cbSunrise.setChecked(isSunriseChecked);
            cbSunset.setChecked(isSunsetChecked);

            int sunriseOffset = sharedPreferences.getInt(KEY_SUNRISE_PREFIX + selectedDay + "_SunriseOffset", 0);
            seekBarSunrise.setProgress(sunriseOffset + 360); // Convert value to range 0 to 720
            etSeekBarSunriseValue.setText(String.valueOf(sunriseOffset));

            int sunsetOffset = sharedPreferences.getInt(KEY_SUNSET_PREFIX + selectedDay + "_SunsetOffset", 0);
            seekBarSunset.setProgress(sunsetOffset + 360); // Convert value to range 0 to 720
            etSeekBarSunsetValue.setText(String.valueOf(sunsetOffset));
        }
    }

    private void loadTimes() {
        int selectedDayIndex = weekdaySpinner.getSelectedItemPosition();
        if (selectedDayIndex >= 0 && selectedDayIndex < DAYS.length) {
            String selectedDay = DAYS[selectedDayIndex];
            etOnHour.setText(sharedPreferences.getString("onHour_" + selectedDay, ""));
            etOnMinute.setText(sharedPreferences.getString("onMinute_" + selectedDay, ""));
            etOnSecond.setText(sharedPreferences.getString("onSecond_" + selectedDay, ""));
            etOffHour.setText(sharedPreferences.getString("offHour_" + selectedDay, ""));
            etOffMinute.setText(sharedPreferences.getString("offMinute_" + selectedDay, ""));
            etOffSecond.setText(sharedPreferences.getString("offSecond_" + selectedDay, ""));
        }
    }
}