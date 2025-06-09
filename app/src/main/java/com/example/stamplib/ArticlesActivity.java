package com.example.stamplib;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.adapters.ArticleAdapter;
import com.example.stamplib.models.Article;

import java.util.ArrayList;
import java.util.List;

public class ArticlesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArticleAdapter adapter;
    private final List<Article> articleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);

        getSupportActionBar().hide();

        recyclerView = findViewById(R.id.articleRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ArticleAdapter(this, articleList);
        recyclerView.setAdapter(adapter);

        loadArticlesFromDatabase(language);
    }

    private void loadArticlesFromDatabase(String lang) {
        articleList.clear();

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM articles ORDER BY published_at DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Article a = new Article();
                a.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                a.published_at = cursor.getString(cursor.getColumnIndexOrThrow("published_at"));

                if ("ru".equals(lang)) {
                    a.title_ru = cursor.getString(cursor.getColumnIndexOrThrow("title_ru"));
                    a.content_ru = cursor.getString(cursor.getColumnIndexOrThrow("content_ru"));
                } else {
                    a.title_en = cursor.getString(cursor.getColumnIndexOrThrow("title_en"));
                    a.content_en = cursor.getString(cursor.getColumnIndexOrThrow("content_en"));
                }

                articleList.add(a);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        adapter.notifyDataSetChanged();
    }
}
