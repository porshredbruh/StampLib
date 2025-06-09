package com.example.stamplib;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.stamplib.models.RemoteSeries;
import com.example.stamplib.models.RemoteStamp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DataImporter {
    private static final String API_URL = "https://YOUR_URL.com/export_series";
    private final DatabaseHelper dbHelper;
    private final Context context;

    public DataImporter(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.close();
            fetchAndStoreData();
        } catch (Exception e) {
            Log.e("DataImporter", "Ошибка при инициализации базы", e);
            showToast("Ошибка инициализации базы");
        }
    }

    public void fetchAndStoreData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<RemoteSeries> seriesList = fetchRemoteSeries();

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.beginTransaction();

                for (RemoteSeries series : seriesList) {
                    db.execSQL("INSERT OR REPLACE INTO series (id, title, year, image_path) VALUES (?, ?, ?, ?)",
                            new Object[]{series.id, series.title, series.year, series.image_path});

                    if (series.stamps != null) {
                        for (RemoteStamp stamp : series.stamps) {
                            db.execSQL("INSERT OR REPLACE INTO stamps (id, series_id, period, name, year, print_type, " +
                                            "perforation_type, perforation_value, paper, watermark, image_path) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                                    new Object[]{
                                            stamp.id, series.id, stamp.period, stamp.name, stamp.year,
                                            stamp.print_type, stamp.perforation_type, stamp.perforation_value,
                                            stamp.paper, stamp.watermark, stamp.image_path
                                    });
                        }
                    }
                }

                db.setTransactionSuccessful();
                db.endTransaction();
                db.close();

                SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("data_loaded", true).apply();

                Log.d("DataImporter", "Импорт завершён: " + seriesList.size() + " серий");
                showToast("Импорт завершён: " + seriesList.size() + " серий");

            } catch (Exception e) {
                Log.e("DataImporter", "Ошибка при импорте данных: " + e.getMessage(), e);
                showToast("Ошибка при импорте данных");
            }
        });
    }

    private List<RemoteSeries> fetchRemoteSeries() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(API_URL).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Ошибка загрузки: " + response);

            ResponseBody responseBody = response.body();
            if (responseBody == null) throw new IOException("Пустое тело ответа от сервера");

            String jsonData = responseBody.string();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<RemoteSeries>>(){}.getType();
            Log.d("DataImporter", "Получено JSON: " + jsonData);
            return gson.fromJson(jsonData, listType);
        }
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }
}
