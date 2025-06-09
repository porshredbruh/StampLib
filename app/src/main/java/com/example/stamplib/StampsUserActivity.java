package com.example.stamplib;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.adapters.UserStampAdapter;

import java.util.ArrayList;
import java.util.List;

public class StampsUserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserStampAdapter adapter;
    private final List<Stamp> stamps = new ArrayList<>();
    private int seriesId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stamps_user);

        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.stampsRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        seriesId = getIntent().getIntExtra("series_id", -1);
        if (seriesId != -1) {
            loadStampsFromSeries();
        }
    }

    private void loadStampsFromSeries() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, series_id, name, year, image_path FROM stamps WHERE series_id = ?",
                new String[]{String.valueOf(seriesId)}
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                int sId = cursor.getInt(1);
                String name = cursor.getString(2);
                int year = cursor.getInt(3);
                String imagePath = cursor.getString(4);

                if (imagePath == null || imagePath.isEmpty()) {
                    imagePath = "android.resource://" + getPackageName() + "/drawable/stamp_placeholder";
                }

                stamps.add(new Stamp(id, sId, name, year, imagePath));
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new UserStampAdapter(this, stamps);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
