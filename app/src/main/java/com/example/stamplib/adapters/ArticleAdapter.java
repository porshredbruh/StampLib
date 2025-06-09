package com.example.stamplib.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.ArticleDetailsActivity;
import com.example.stamplib.LocaleHelper;
import com.example.stamplib.R;
import com.example.stamplib.models.Article;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private final List<Article> articles;
    private final Context context;

    public ArticleAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);

        String language = LocaleHelper.getAppLanguage(context);
        String title = "ru".equals(language) ? article.title_ru : article.title_en;
        String content = "ru".equals(language) ? article.content_ru : article.content_en;

        holder.titleText.setText(title);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArticleDetailsActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("content", content);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.articleTitle);
        }
    }
}
