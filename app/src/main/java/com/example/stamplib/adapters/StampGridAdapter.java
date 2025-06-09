package com.example.stamplib.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stamplib.R;
import com.example.stamplib.StampDetailsActivity;


public class StampGridAdapter extends RecyclerView.Adapter<StampGridAdapter.ViewHolder> {

    private final Context context;
    private Cursor cursor;

    public StampGridAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public StampGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stamp_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StampGridAdapter.ViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
            long stampId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));

            holder.nameText.setText(name);
            Glide.with(context)
                    .load("https://YOUR_URL.com" + imagePath)
                    .placeholder(R.drawable.placeholder)
                    .into(holder.imageView);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, StampDetailsActivity.class);
                intent.putExtra("stamp_id", stampId);
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView nameText;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.stampImage);
            nameText = view.findViewById(R.id.stampName);
        }
    }
}
