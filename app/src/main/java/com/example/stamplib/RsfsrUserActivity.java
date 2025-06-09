package com.example.stamplib;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.adapters.SeriesCardAdapter;
import com.example.stamplib.models.SeriesCard;

import java.util.ArrayList;
import java.util.List;

public class RsfsrUserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SeriesCardAdapter adapter;
    private final List<SeriesCard> seriesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsfsr_user);

        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.seriesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        loadSeriesFromLocalDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSeriesFromLocalDatabase();
    }

    private void loadSeriesFromLocalDatabase() {
        seriesList.clear();
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor seriesCursor = db.rawQuery("SELECT id, title FROM series WHERE year >= 1917", null);

        if (seriesCursor.moveToFirst()) {
            do {
                int seriesId = seriesCursor.getInt(0);
                String title = seriesCursor.getString(1);

                Cursor imageCursor = db.rawQuery(
                        "SELECT image_path FROM stamps WHERE series_id = ? LIMIT 1",
                        new String[]{String.valueOf(seriesId)});
                String imagePath = imageCursor.moveToFirst() ? imageCursor.getString(0) : null;
                imageCursor.close();

                Cursor totalCursor = db.rawQuery(
                        "SELECT COUNT(*) FROM stamps WHERE series_id = ?",
                        new String[]{String.valueOf(seriesId)});
                int total = totalCursor.moveToFirst() ? totalCursor.getInt(0) : 0;
                totalCursor.close();

                Cursor collectedCursor = db.rawQuery(
                        "SELECT COUNT(*) FROM user_stamps us " +
                                "JOIN stamps s ON us.stamp_id = s.id " +
                                "WHERE us.user_id = ? AND s.series_id = ?",
                        new String[]{String.valueOf(userId), String.valueOf(seriesId)});
                int collected = collectedCursor.moveToFirst() ? collectedCursor.getInt(0) : 0;
                collectedCursor.close();

                String progress = collected + "/" + total;

                if (imagePath != null && !imagePath.startsWith("http")) {
                    imagePath = "https://YOUR_URL.com" + imagePath;
                } else if (imagePath == null || imagePath.isEmpty()) {
                    imagePath = "android.resource://" + getPackageName() + "/drawable/stamp_placeholder";
                }

                seriesList.add(new SeriesCard(seriesId, title, imagePath, progress));
            } while (seriesCursor.moveToNext());
        }
        seriesCursor.close();

        if (adapter == null) {
            adapter = new SeriesCardAdapter(this, seriesList);
            adapter.setOnItemClickListener(series -> {
                Intent intent = new Intent(RsfsrUserActivity.this, StampsUserActivity.class);
                intent.putExtra("series_id", series.getSeriesId());
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
