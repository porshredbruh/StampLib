package com.example.stamplib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stamplib.network.ApiClient;
import com.example.stamplib.network.ApiService;
import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountSettingsActivity extends AppCompatActivity {

    private EditText nicknameInput;
    private Button saveNicknameBtn, resetPasswordBtn, logoutBtn;
    private SharedPreferences prefs;
    private String savedEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        getSupportActionBar().hide();

        prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        savedEmail = prefs.getString("email", "");
        String savedNickname = prefs.getString("nickname", "");

        nicknameInput = findViewById(R.id.nicknameInput);
        saveNicknameBtn = findViewById(R.id.saveNicknameBtn);
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        nicknameInput.setText(savedNickname);

        saveNicknameBtn.setOnClickListener(v -> {
            String newNick = nicknameInput.getText().toString().trim();
            if (!newNick.isEmpty()) {
                updateNickname(savedEmail, newNick);
            } else {
                Toast.makeText(this, "Ник не может быть пустым", Toast.LENGTH_SHORT).show();
            }
        });

        resetPasswordBtn.setOnClickListener(v -> showResetPasswordDialog());

        logoutBtn.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Intent intent = new Intent(AccountSettingsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateNickname(String email, String newNick) {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("new_nicname", newNick);

        ApiService api = ApiClient.getService();
        Call<Void> call = api.updateNickname(body);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    prefs.edit().putString("nickname", newNick).apply();
                    Toast.makeText(AccountSettingsActivity.this, "Ник обновлён", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AccountSettingsActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(AccountSettingsActivity.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResetPasswordDialog() {
        EditText input = new EditText(this);
        input.setHint("Новый пароль");

        new AlertDialog.Builder(this)
                .setTitle("Сброс пароля")
                .setView(input)
                .setPositiveButton("Сбросить", (dialog, which) -> {
                    String newPass = input.getText().toString().trim();
                    if (newPass.length() >= 6) {
                        resetPassword(savedEmail, newPass);
                    } else {
                        Toast.makeText(this, "Минимум 6 символов", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void resetPassword(String email, String newPassword) {
        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("new_password", newPassword);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("https://YOUR_URL.com/reset_password")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(AccountSettingsActivity.this, "Ошибка подключения", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(AccountSettingsActivity.this, "Пароль сброшен", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AccountSettingsActivity.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка формирования запроса", Toast.LENGTH_SHORT).show();
        }
    }
}
