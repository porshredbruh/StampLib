package com.example.stamplib;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.widget.Toast;

import com.example.stamplib.network.ApiClient;
import com.example.stamplib.network.ApiService;
import com.example.stamplib.models.Article;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleImporter {

    private final Context context;
    private final SQLiteDatabase db;

    public ArticleImporter(Context context) {
        this.context = context;
        this.db = new DatabaseHelper(context).getWritableDatabase();
        fetchAndStoreArticles();
    }

    private void fetchAndStoreArticles() {
        ApiService api = ApiClient.getService();
        Call<List<Article>> call = api.getArticles();

        call.enqueue(new Callback<List<Article>>() {
            @Override
            public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    db.beginTransaction();
                    try {
                        db.delete("articles", null, null);
                        String sql = "INSERT INTO articles (id, title_ru, content_ru, title_en, content_en, published_at) VALUES (?, ?, ?, ?, ?, ?)";
                        SQLiteStatement stmt = db.compileStatement(sql);

                        for (Article article : response.body()) {
                            stmt.bindLong(1, article.id);
                            stmt.bindString(2, article.title_ru);
                            stmt.bindString(3, article.content_ru);
                            stmt.bindString(4, article.title_en);
                            stmt.bindString(5, article.content_en);
                            stmt.bindString(6, article.published_at);
                            stmt.executeInsert();
                        }

                        db.setTransactionSuccessful();
                        Toast.makeText(context, "Статьи успешно синхронизированы", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("ArticleSync", "Ошибка при вставке статей", e);
                        Toast.makeText(context, "Ошибка при сохранении статей", Toast.LENGTH_SHORT).show();
                    } finally {
                        db.endTransaction();
                    }
                } else {
                    Toast.makeText(context, "Не удалось получить статьи", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Article>> call, Throwable t) {
                Log.e("ArticleSync", "Ошибка сети", t);
                Toast.makeText(context, "Ошибка при загрузке статей", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
