package com.example.stamplib;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.adapters.ResultAdapter;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    private ImageView imageView;
    private RecyclerView recyclerView;
    private ResultAdapter adapter;

    private static final String BASE_URL = "https://YOUR_URL.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imageView = findViewById(R.id.resultImageView);
        recyclerView = findViewById(R.id.resultRecyclerView);

        getSupportActionBar().hide();

        Intent intent = getIntent();
        String photoPath = intent.getStringExtra("photo_path");
        String[] resultsArray = intent.getStringArrayExtra("recognition_results");

        if (photoPath != null && !photoPath.isEmpty()) {
            Picasso.get()
                    .load(BASE_URL + photoPath)
                    .into(imageView);
        }

        List<String> results = resultsArray != null ? Arrays.asList(resultsArray) : Arrays.asList();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new ResultAdapter(this, results, BASE_URL);
        recyclerView.setAdapter(adapter);
    }
}
