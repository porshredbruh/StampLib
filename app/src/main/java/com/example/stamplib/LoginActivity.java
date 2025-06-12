package com.example.stamplib;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stamplib.models.FriendRelation;
import com.example.stamplib.models.UserStamp;
import com.example.stamplib.network.ApiClient;
import com.example.stamplib.network.ApiService;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button loginBtn = findViewById(R.id.loginSubmit);

        loginBtn.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.loginUser(email, password);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    int userId = data.get("user_id").getAsInt();
                    String nickname = data.get("nicname").getAsString();
                    String unicCode = data.get("unic_code").getAsString();

                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("logged_in", true)
                            .putInt("user_id", userId)
                            .putString("nickname", nickname)
                            .putString("unic_code", unicCode)
                            .putString("email", email)
                            .apply();

                    new Thread(() -> syncUserData(userId)).start();

                    Toast.makeText(LoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Неверный email или пароль", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncUserData(int userId) {
        ApiService syncApi = ApiClient.getClient().create(ApiService.class);
        DatabaseHelper dbHelper = new DatabaseHelper(LoginActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            dbHelper.clearFriendsTable();
            Call<List<UserStamp>> stampCall = syncApi.getUserStamps(userId);
            Response<List<UserStamp>> stampResponse = stampCall.execute();

            if (stampResponse.isSuccessful() && stampResponse.body() != null) {
                db.delete("user_stamps", "user_id = ?", new String[]{String.valueOf(userId)});
                String insertSQL = "INSERT INTO user_stamps (id, user_id, stamp_id, added_at, note, rating, condition) VALUES (?, ?, ?, ?, ?, ?, ?)";
                SQLiteStatement stmt = db.compileStatement(insertSQL);

                for (UserStamp stamp : stampResponse.body()) {
                    stmt.clearBindings();
                    stmt.bindLong(1, stamp.getId());
                    stmt.bindLong(2, stamp.getUserId());
                    stmt.bindLong(3, stamp.getStampId());
                    stmt.bindString(4, stamp.getAddedAt() != null ? stamp.getAddedAt() : "");
                    stmt.bindString(5, stamp.getNote() != null ? stamp.getNote() : "");
                    stmt.bindLong(6, stamp.getRating());
                    stmt.bindString(7, stamp.getCondition() != null ? stamp.getCondition() : "");
                    stmt.executeInsert();
                }
            }

            Call<List<FriendRelation>> friendCall = syncApi.getFriends(userId);
            Response<List<FriendRelation>> friendResponse = friendCall.execute();

            if (friendResponse.isSuccessful() && friendResponse.body() != null) {
                db.delete("friends", "user_id = ? OR friend_id = ?", new String[]{String.valueOf(userId), String.valueOf(userId)});

                String insertFriendSQL = "INSERT OR REPLACE INTO friends (user_id, friend_id, is_confirmed, created_at, unic_code, nickname) VALUES (?, ?, ?, ?, ?, ?)";
                SQLiteStatement friendStmt = db.compileStatement(insertFriendSQL);

                for (FriendRelation fr : friendResponse.body()) {
                    int otherId = (fr.user_id == userId) ? fr.friend_id : fr.user_id;

                    friendStmt.clearBindings();
                    friendStmt.bindLong(1, userId);
                    friendStmt.bindLong(2, otherId);
                    friendStmt.bindLong(3, fr.is_confirmed ? 1 : 0);
                    friendStmt.bindString(4, fr.created_at != null ? fr.created_at : "");
                    friendStmt.bindString(5, fr.unic_code != null ? fr.unic_code : "");
                    friendStmt.bindString(6, fr.nickname != null ? fr.nickname : "");
                    friendStmt.executeInsert();
                }
            }

            db.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
