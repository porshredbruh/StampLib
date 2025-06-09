package com.example.stamplib.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stamplib.R;
import com.example.stamplib.models.SeriesCard;

import java.util.List;

public class SeriesCardAdapter extends RecyclerView.Adapter<SeriesCardAdapter.SeriesViewHolder> {

    private final Context context;
    private final List<SeriesCard> seriesList;
    private OnItemClickListener listener;

    public SeriesCardAdapter(Context context, List<SeriesCard> seriesList) {
        this.context = context;
        this.seriesList = seriesList;
    }

    public interface OnItemClickListener {
        void onItemClick(SeriesCard series);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_series_card, parent, false);
        return new SeriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeriesViewHolder holder, int position) {
        SeriesCard series = seriesList.get(position);

        holder.seriesName.setText(series.getTitle());
        holder.seriesProgress.setText(series.getProgress());

        Glide.with(context)
                .load(series.getImagePath())
                .placeholder(R.drawable.stamp_placeholder)
                .into(holder.seriesImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(series);
        });
    }

    @Override
    public int getItemCount() {
        return seriesList.size();
    }

    public static class SeriesViewHolder extends RecyclerView.ViewHolder {
        ImageView seriesImage;
        TextView seriesName;
        TextView seriesProgress;

        public SeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            seriesImage = itemView.findViewById(R.id.stampImage);
            seriesName = itemView.findViewById(R.id.stampName);
            seriesProgress = itemView.findViewById(R.id.seriesProgress);
        }
    }
}
