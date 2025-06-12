package com.example.stamplib.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stamplib.R;
import com.example.stamplib.StampDetailsActivity;

import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {

    private final List<String> items;
    private final String baseUrl;
    private final Context context;

    public ResultAdapter(Context context, List<String> items, String baseUrl) {
        this.context = context;
        this.items = items;
        this.baseUrl = baseUrl;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.resultImageViewItem);
        }
    }

    @NonNull
    @Override
    public ResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imagePath = items.get(position);
        String fullUrl = baseUrl + imagePath;

        Glide.with(context)
                .load(fullUrl)
                .placeholder(R.drawable.stamp_placeholder)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            try {
                String relativePath = imagePath.replace("/static/stamps/", "");

                Intent intent = new Intent(context, StampDetailsActivity.class);
                intent.putExtra("from_search", true);
                intent.putExtra("image_path", relativePath);
                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "Ошибка открытия марки", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
