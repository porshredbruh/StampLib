package com.example.stamplib.adapters;
import com.example.stamplib.StampDetailsActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stamplib.DatabaseHelper;
import com.example.stamplib.R;
import com.example.stamplib.Stamp;

import com.example.stamplib.network.ApiClient;
import com.example.stamplib.network.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserStampAdapter extends RecyclerView.Adapter<UserStampAdapter.StampViewHolder> {

    private final Context context;
    private final List<Stamp> stamps;
    private final DatabaseHelper dbHelper;
    private final int userId;

    public UserStampAdapter(Context context, List<Stamp> stamps) {
        this.context = context;
        this.stamps = stamps;
        this.dbHelper = new DatabaseHelper(context);

        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        this.userId = prefs.getInt("user_id", -1);
    }

    @NonNull
    @Override
    public StampViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stamp_with_control, parent, false);
        return new StampViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StampViewHolder holder, int position) {
        Stamp stamp = stamps.get(position);
        boolean isCollected = dbHelper.isStampCollected(userId, stamp.getId());

        holder.nameText.setText(stamp.getName());

        String path = stamp.getImagePath();
        if (path != null && !path.startsWith("http")) {
            path = "https://YOUR_URL.com" + path;
        }

        Glide.with(context)
                .load(path)
                .placeholder(R.drawable.stamp_placeholder)
                .into(holder.imageView);

        holder.container.setBackgroundResource(
                isCollected ? R.drawable.stamp_border_green : R.drawable.stamp_border_red
        );

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StampDetailsActivity.class);
            intent.putExtra("stamp_id", stamp.getId());
            context.startActivity(intent);
        });

        holder.addButton.setOnClickListener(v -> {
            dbHelper.addStampToUser(userId, stamp.getId());
            notifyItemChanged(holder.getAdapterPosition());

            ApiService api = ApiClient.getClient().create(ApiService.class);
            Call<Void> call = api.addUserStamp(userId, stamp.getId(), "", 0, "");
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) { }

                @Override
                public void onFailure(Call<Void> call, Throwable t) { }
            });
        });

        holder.removeButton.setOnClickListener(v -> {
            dbHelper.removeStampFromUser(userId, stamp.getId());
            notifyItemChanged(holder.getAdapterPosition());

            ApiService api = ApiClient.getClient().create(ApiService.class);
            Call<Void> call = api.removeUserStamp(userId, stamp.getId());
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) { }

                @Override
                public void onFailure(Call<Void> call, Throwable t) { }
            });
        });
    }

    @Override
    public int getItemCount() {
        return stamps.size();
    }

    static class StampViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText;
        View container;
        ImageButton addButton, removeButton;

        public StampViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.stampContainer);
            imageView = itemView.findViewById(R.id.stampImage);
            nameText = itemView.findViewById(R.id.stampName);
            addButton = itemView.findViewById(R.id.addButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}
