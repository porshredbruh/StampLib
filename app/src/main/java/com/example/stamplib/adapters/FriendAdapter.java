package com.example.stamplib.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.R;
import com.example.stamplib.models.Friend;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final List<Friend> friends;
    private final OnFriendClickListener clickListener;

    public interface OnFriendClickListener {
        void onFriendClick(Friend friend);
    }

    public FriendAdapter(List<Friend> friends, OnFriendClickListener clickListener) {
        this.friends = friends;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);
        holder.nameText.setText(friend.getName());
        holder.codeText.setText(friend.getCode());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFriendClick(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public void updateData(List<Friend> newFriends) {
        friends.clear();
        friends.addAll(newFriends);
        notifyDataSetChanged();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, codeText;

        public FriendViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.friendName);
            codeText = itemView.findViewById(R.id.friendCode);
        }
    }
}
