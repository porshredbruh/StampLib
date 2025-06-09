package com.example.stamplib;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stamplib.adapters.FriendAdapter;
import com.example.stamplib.models.Friend;
import com.example.stamplib.viewmodel.FriendsViewModel;

import java.util.ArrayList;
import java.util.List;

public class MyFriendsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private List<Friend> friendList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    private EditText searchInput;
    private ImageButton sendInviteButton, notifyButton;

    private FriendsViewModel viewModel;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);

        getSupportActionBar().hide();

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.friendsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FriendAdapter(friendList, this::openFriendCollection);
        recyclerView.setAdapter(adapter);

        searchInput = findViewById(R.id.searchInput);
        sendInviteButton = findViewById(R.id.sendInviteButton);
        notifyButton = findViewById(R.id.notifyButton);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        viewModel = new ViewModelProvider(this).get(FriendsViewModel.class);

        sendInviteButton.setOnClickListener(v -> {
            String code = searchInput.getText().toString().trim();
            if (code.length() >= 3) {
                viewModel.addFriendByCode(currentUserId, code, success -> {
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "Заявка отправлена", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Ошибка отправки", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            } else {
                Toast.makeText(this, "Введите код (минимум 3 символа)", Toast.LENGTH_SHORT).show();
            }
        });

        notifyButton.setOnClickListener(v ->
                startActivity(new Intent(this, FriendRequestsActivity.class)));

        loadFriendsFromDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFriendsFromDatabase();
    }

    private void loadFriendsFromDatabase() {
        friendList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT friend_id, nickname, unic_code FROM friends", null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String nickname = cursor.getString(1);
                String unicCode = cursor.getString(2);

                if (nickname == null) nickname = "Друг " + id;
                if (unicCode == null) unicCode = String.format("#%06d", id);

                friendList.add(new Friend(nickname, "#" + unicCode, id));
            }

            adapter.notifyDataSetChanged();

        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    private void openFriendCollection(Friend friend) {
        Intent intent = new Intent(this, FriendCollectionActivity.class);
        intent.putExtra("friend_id", friend.getFriendId());
        intent.putExtra("nickname", friend.getName());
        intent.putExtra("unic_code", friend.getCode().replace("#", ""));
        startActivity(intent);
    }
}
