
package com.example.stamplib.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.R;
import com.example.stamplib.models.FriendRelation;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    public interface OnFriendRequestAction {
        void onAccept(FriendRelation request);
        void onReject(FriendRelation request);
    }

    private final List<FriendRelation> requests;
    private final Context context;
    private final int currentUserId;
    private final boolean isOutgoing;
    private final OnFriendRequestAction listener;

    public FriendRequestAdapter(List<FriendRelation> requests, Context context, int currentUserId, boolean isOutgoing, OnFriendRequestAction listener) {
        this.requests = new ArrayList<>(requests);
        this.context = context;
        this.currentUserId = currentUserId;
        this.isOutgoing = isOutgoing;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestAdapter.ViewHolder holder, int position) {
        FriendRelation request = requests.get(position);
        String name = request.nickname != null ? request.nickname : "Пользователь";
        String code = "#" + (request.unic_code != null ? request.unic_code : "------");

        holder.nameText.setText(name);
        holder.codeText.setText(code);

        if (isOutgoing) {
            holder.acceptBtn.setVisibility(View.GONE);
            holder.rejectBtn.setVisibility(View.VISIBLE);
            holder.rejectBtn.setOnClickListener(v -> {
                if (listener != null) listener.onReject(request);
            });
        } else {
            holder.acceptBtn.setVisibility(View.VISIBLE);
            holder.rejectBtn.setVisibility(View.VISIBLE);
            holder.acceptBtn.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(request);
            });
            holder.rejectBtn.setOnClickListener(v -> {
                if (listener != null) listener.onReject(request);
            });
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateData(List<FriendRelation> newList) {
        requests.clear();
        requests.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, codeText;
        ImageButton acceptBtn, rejectBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.requestName);
            codeText = itemView.findViewById(R.id.requestCode);
            acceptBtn = itemView.findViewById(R.id.acceptButton);
            rejectBtn = itemView.findViewById(R.id.rejectButton);
        }
    }
}
