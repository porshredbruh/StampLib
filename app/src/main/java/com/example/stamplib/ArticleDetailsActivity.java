package com.example.stamplib;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ArticleDetailsActivity extends AppCompatActivity {

    private TextView titleView, contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_details);

        getSupportActionBar().hide();

        titleView = findViewById(R.id.detailArticleTitle);
        contentView = findViewById(R.id.detailArticleContent);

        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");

        if (title == null || content == null) {
            Toast.makeText(this, "Ошибка загрузки статьи", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        titleView.setText(title);
        contentView.setText(content);
    }
}
