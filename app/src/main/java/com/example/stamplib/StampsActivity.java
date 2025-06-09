package com.example.stamplib;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.adapters.StampGridAdapter;

public class StampsActivity extends AppCompatActivity {

    private static final String TAG = "StampsActivity";

    private ListView stampListView;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private SimpleCursorAdapter adapter;
    private TextView seriesTitle;

    private long seriesId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stamps);

        getSupportActionBar().hide();

        RecyclerView recyclerView = findViewById(R.id.stampGrid);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        Intent intent = getIntent();
        seriesId = intent.getLongExtra("series_id", -1);

        if (seriesId == -1) {
            Toast.makeText(this, "Серия не указана", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        Cursor cursor = database.rawQuery(
                "SELECT id AS _id, name, year, image_path FROM stamps WHERE series_id = ? ORDER BY year, name",
                new String[]{String.valueOf(seriesId)}
        );

        seriesTitle = findViewById(R.id.seriesTitle);
        setSeriesTitle();

        StampGridAdapter adapter = new StampGridAdapter(this, cursor);
        recyclerView.setAdapter(adapter);
    }

    private void setupList() {
        try {
            Cursor cursor = database.rawQuery(
                    "SELECT id AS _id, name, year FROM stamps WHERE series_id = ? ORDER BY year, name",
                    new String[]{String.valueOf(seriesId)}
            );

            adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_2,
                    cursor,
                    new String[]{"name", "year"},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    0
            );

            stampListView.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при загрузке марок", e);
            Toast.makeText(this, "Не удалось загрузить марки", Toast.LENGTH_SHORT).show();
        }
    }

    private void setSeriesTitle() {
        Cursor cursor = database.rawQuery(
                "SELECT title, year FROM series WHERE id = ?",
                new String[]{String.valueOf(seriesId)}
        );

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
            seriesTitle.setText(title + " (" + year + ")");
            cursor.close();
        } else {
            seriesTitle.setText("Серия не найдена");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null && adapter.getCursor() != null) {
            adapter.getCursor().close();
        }
        if (database != null) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
