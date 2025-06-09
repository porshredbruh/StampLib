package com.example.stamplib;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stamplib.models.FriendStats;
import com.example.stamplib.network.ApiClient;
import com.example.stamplib.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendCollectionActivity extends AppCompatActivity {

    private int friendId;
    private String nickname;
    private String unicCode;

    private TextView nicknameText;
    private TextView userCodeText;
    private View collectionButton;
    private View friendsButton;
    private View settingsBtn;
    private View loginLayout;
    private View collectionLayout;

    private TextView stampsCollectedText;
    private TextView seriesCollectedText;
    private TextView friendsCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notifications);

        getSupportActionBar().hide();

        nicknameText = findViewById(R.id.nicknameText);
        userCodeText = findViewById(R.id.userCodeText);
        collectionButton = findViewById(R.id.collectionButton);
        friendsButton = findViewById(R.id.friendsButton);
        settingsBtn = findViewById(R.id.settingsBtn);
        loginLayout = findViewById(R.id.loginLayout);
        collectionLayout = findViewById(R.id.collectionLayout);

        stampsCollectedText = findViewById(R.id.stampsCollectedText);
        seriesCollectedText = findViewById(R.id.seriesCollectedText);
        friendsCountText = findViewById(R.id.friendsCountText);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("friend_id")) {
            friendId = intent.getIntExtra("friend_id", -1);
            nickname = intent.getStringExtra("nickname");
            unicCode = intent.getStringExtra("unic_code");
        } else {
            finish();
            return;
        }

        nicknameText.setText(nickname);
        userCodeText.setText("#" + unicCode);

        settingsBtn.setVisibility(View.GONE);
        collectionButton.setVisibility(View.GONE);
        friendsButton.setVisibility(View.GONE);
        loginLayout.setVisibility(View.GONE);
        collectionLayout.setVisibility(View.VISIBLE);

        loadFriendStats();
    }

    private void loadFriendStats() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        Call<FriendStats> call = api.getUserStats(friendId);

        call.enqueue(new Callback<FriendStats>() {
            @Override
            public void onResponse(Call<FriendStats> call, Response<FriendStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FriendStats stats = response.body();
                    stampsCollectedText.setText("Марок собрано: " + stats.stamps_count);
                    seriesCollectedText.setText("Серий: " + stats.series_count);
                    friendsCountText.setText("Друзей: " + stats.friends_count);
                } else {
                    Toast.makeText(FriendCollectionActivity.this, "Ошибка загрузки статистики", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FriendStats> call, Throwable t) {
                Toast.makeText(FriendCollectionActivity.this, "Ошибка подключения к серверу", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
