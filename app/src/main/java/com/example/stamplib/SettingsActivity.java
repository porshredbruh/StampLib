package com.example.stamplib;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        getSupportActionBar().hide();
        setContentView(R.layout.activity_settings);

        Button ruButton = findViewById(R.id.ru_bt);
        Button engButton = findViewById(R.id.eng_bt);
        Button updateDbButton = findViewById(R.id.updateDbBtn);

        ruButton.setOnClickListener(v -> {
            switchLanguage("ru");
        });

        engButton.setOnClickListener(v -> {
            switchLanguage("en");
        });

        updateDbButton.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("data_loaded", false).apply();

            new DataImporter(this);
            new ArticleImporter(this);
            Toast.makeText(this, "Обновление базы запущено", Toast.LENGTH_SHORT).show();
        });
    }

    private void switchLanguage(String langCode) {
        LocaleHelper.setLocale(this, langCode);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
    }
}
