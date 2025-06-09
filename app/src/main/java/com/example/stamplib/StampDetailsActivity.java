package com.example.stamplib;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.stamplib.network.ApiClient;
import com.example.stamplib.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StampDetailsActivity extends AppCompatActivity {

    private ImageView stampImage;
    private TextView stampName, stampYear, printType, perforationType, perforationValue, paper, watermark;
    private Button addButton, removeButton;

    private DatabaseHelper dbHelper;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    private long stampId = -1;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stamp_details);

        getSupportActionBar().hide();

        stampImage = findViewById(R.id.stampImage);
        stampName = findViewById(R.id.stampName);
        stampYear = findViewById(R.id.stampYear);
        printType = findViewById(R.id.printType);
        perforationType = findViewById(R.id.perforationType);
        perforationValue = findViewById(R.id.perforationValue);
        paper = findViewById(R.id.paper);
        watermark = findViewById(R.id.watermark);
        addButton = findViewById(R.id.addButton);
        removeButton = findViewById(R.id.removeButton);

        dbHelper = new DatabaseHelper(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);

        boolean fromSearch = getIntent().getBooleanExtra("from_search", false);
        if (fromSearch) {
            String imagePath = getIntent().getStringExtra("image_path");
            if (imagePath != null) {
                loadStampByImagePath(imagePath);
            } else {
                Toast.makeText(this, "Путь к изображению отсутствует", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            stampId = getIntent().getLongExtra("stamp_id", -1L);
            if (stampId == -1) {
                finish();
                return;
            }
            loadStampDetails(stampId);
        }

        if (userId != -1) {
            addButton.setOnClickListener(v -> {
                dbHelper.addStampToUser(userId, stampId);
                apiService.addUserStamp(userId, stampId, "", 0, "").enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override public void onFailure(Call<Void> call, Throwable t) {}
                });
                updateCollectionButtons();
                Toast.makeText(this, "Марка добавлена в коллекцию", Toast.LENGTH_SHORT).show();
            });

            removeButton.setOnClickListener(v -> {
                dbHelper.removeStampFromUser(userId, stampId);
                apiService.removeUserStamp(userId, stampId).enqueue(new Callback<Void>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override public void onFailure(Call<Void> call, Throwable t) {}
                });
                updateCollectionButtons();
                Toast.makeText(this, "Марка удалена из коллекции", Toast.LENGTH_SHORT).show();
            });
        } else {
            addButton.setVisibility(View.GONE);
            removeButton.setVisibility(View.GONE);
        }
    }

    private void updateCollectionButtons() {
        boolean isCollected = dbHelper.isStampCollected(userId, stampId);
        addButton.setVisibility(isCollected ? View.GONE : View.VISIBLE);
        removeButton.setVisibility(isCollected ? View.VISIBLE : View.GONE);
    }

    private void loadStampByImagePath(String fullPath) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] parts = fullPath.split("/");
        int len = parts.length;
        if (len < 2) {
            Toast.makeText(this, "Неверный путь к изображению", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String postfix = parts[len - 2] + "/" + parts[len - 1];
        Cursor cursor = db.rawQuery("SELECT * FROM stamps WHERE image_path LIKE ?", new String[]{"%" + postfix});

        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Марка не найдена: " + postfix, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        stampId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        fillUI(cursor);
        cursor.close();

        if (userId != -1) {
            updateCollectionButtons();
        }
    }

    private void loadStampDetails(long stampId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM stamps WHERE id = ?", new String[]{String.valueOf(stampId)});
        if (!cursor.moveToFirst()) {
            Toast.makeText(this, "Марка не найдена", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fillUI(cursor);
        cursor.close();
        if (userId != -1) {
            updateCollectionButtons();
        }
    }

    private void fillUI(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        int year = cursor.getInt(cursor.getColumnIndexOrThrow("year"));
        String print = cursor.getString(cursor.getColumnIndexOrThrow("print_type"));
        String perfType = cursor.getString(cursor.getColumnIndexOrThrow("perforation_type"));
        String perfValue = cursor.getString(cursor.getColumnIndexOrThrow("perforation_value"));
        String paperType = cursor.getString(cursor.getColumnIndexOrThrow("paper"));
        String watermarkStr = cursor.getString(cursor.getColumnIndexOrThrow("watermark"));
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));

        stampName.setText(name);
        stampYear.setText(String.valueOf(year));
        printType.setText(print);
        perforationType.setText(perfType);
        perforationValue.setText(perfValue);
        paper.setText(paperType);
        watermark.setText(watermarkStr);

        if (imagePath != null && !imagePath.startsWith("http")) {
            imagePath = "https://YOUR_URL.com/" + imagePath;
        }

        Glide.with(this)
                .load(imagePath)
                .placeholder(R.drawable.stamp_placeholder)
                .into(stampImage);
    }
}
