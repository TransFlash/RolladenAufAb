package com.wb.rolladenaufab;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SettingSchlafzLinksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_schlafzlinks);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Zeigt den Zurück-Pfeil
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Geht zurück zur vorherigen Aktivität
            }
        });

        TextView textView = findViewById(R.id.schlafzlinksTextView);
        textView.setText("Rollladen Schlafzimmer Links");
    }
}