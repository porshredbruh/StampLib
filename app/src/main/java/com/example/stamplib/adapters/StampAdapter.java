package com.example.stamplib.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.R;
import com.example.stamplib.models.UserStamp;

import java.util.List;

public class StampAdapter extends RecyclerView.Adapter<StampAdapter.StampViewHolder> {

    private final List<UserStamp> stampList;
    private final boolean readonly;

    public StampAdapter(List<UserStamp> stampList, boolean readonly) {
        this.stampList = stampList;
        this.readonly = readonly;
    }

    @NonNull
    @Override
    public StampViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stamp_frend, parent, false);
        return new StampViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StampViewHolder holder, int position) {
        UserStamp stamp = stampList.get(position);
        holder.name.setText("ID марки: " + stamp.getStampId());
        holder.note.setText("Заметка: " + (stamp.getNote() != null ? stamp.getNote() : ""));
        holder.rating.setText("Оценка: " + stamp.getRating());
        holder.condition.setText("Состояние: " + stamp.getCondition());
    }

    @Override
    public int getItemCount() {
        return stampList.size();
    }

    static class StampViewHolder extends RecyclerView.ViewHolder {
        TextView name, note, rating, condition;

        public StampViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.stamp_name);
            note = itemView.findViewById(R.id.stamp_note);
            rating = itemView.findViewById(R.id.stamp_rating);
            condition = itemView.findViewById(R.id.stamp_condition);
        }
    }
}
