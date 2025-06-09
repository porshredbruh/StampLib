package com.example.stamplib;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stamplib.adapters.SeriesCursorAdapter;

public class RsfsrActivity extends AppCompatActivity {
    private static final String TAG = "RsfsrActivity";
    private static final int MIN_YEAR = 1918;
    private static final int MAX_YEAR = 1923;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private EditText yearInput;
    private ListView seriesListView;
    private CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsfsr);

        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        getSupportActionBar().hide();

        initViews();
        initDatabase();
        setupListAdapter();
        setupYearInputListener();
        setupSeriesClickListener();
    }

    private void initViews() {
        yearInput = findViewById(R.id.yearInput);
        seriesListView = findViewById(R.id.seriesListView);
    }

    private void initDatabase() {
        try {
            dbHelper = new DatabaseHelper(this);
            database = dbHelper.getWritableDatabase();
            Log.d(TAG, "Database initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Database initialization failed", e);
            finish();
        }
    }

    private void setupListAdapter() {
        adapter = new SeriesCursorAdapter(this, null, 0);
        seriesListView.setAdapter(adapter);
    }

    private void setupYearInputListener() {
        yearInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (database == null || !database.isOpen()) {
                    Log.e(TAG, "Database not available");
                    return;
                }

                new Thread(() -> {
                    final Cursor cursor = getSeriesCursor(s.toString());
                    runOnUiThread(() -> updateAdapter(cursor, s.toString()));
                }).start();
            }
        });
    }

    private void setupSeriesClickListener() {
        seriesListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(RsfsrActivity.this, StampsActivity.class);
            intent.putExtra("series_id", id);
            startActivity(intent);
        });
    }

    private Cursor getSeriesCursor(String yearFilter) {
        try {
            if (yearFilter.length() == 4) {
                int year = Integer.parseInt(yearFilter);

                if (year < MIN_YEAR || year > MAX_YEAR) {
                    runOnUiThread(() -> Toast.makeText(this,
                            "Введите год от " + MIN_YEAR + " до " + MAX_YEAR,
                            Toast.LENGTH_SHORT).show());
                    return null;
                }

                String query = "SELECT id AS _id, title, year, image_path FROM series WHERE year = ? ORDER BY title";
                return database.rawQuery(query, new String[]{yearFilter});
            } else {
                String query = "SELECT id AS _id, title, year, image_path FROM series WHERE year BETWEEN ? AND ? ORDER BY year DESC, title";
                return database.rawQuery(query, new String[]{
                        String.valueOf(MIN_YEAR),
                        String.valueOf(MAX_YEAR)
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка запроса к БД", e);
            return null;
        }
    }

    private void updateAdapter(Cursor newCursor, String yearFilter) {
        if (newCursor == null) return;

        Cursor oldCursor = adapter.swapCursor(newCursor);
        if (oldCursor != null && !oldCursor.isClosed()) oldCursor.close();

        if (newCursor.getCount() == 0 && yearFilter.length() == 4) {
            Toast.makeText(this, "Серий за " + yearFilter + " год не найдено", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null && adapter.getCursor() != null) {
            adapter.getCursor().close();
        }
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
