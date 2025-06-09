package com.example.stamplib;

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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput, nicknameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().hide();

        nicknameInput = findViewById(R.id.nicname);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button registerBtn = findViewById(R.id.registerSubmit);

        registerBtn.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String nickname = nicknameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (nickname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<JsonObject> call = apiService.registerUser(nickname, email, password);

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject data = response.body();

                    int userId = data.get("user_id").getAsInt();
                    String returnedNickname = data.get("nicname").getAsString();
                    String unicCode = data.get("unic_code").getAsString();

                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("logged_in", true)
                            .putInt("user_id", userId)
                            .putString("nickname", returnedNickname)
                            .putString("unic_code", unicCode)
                            .putString("email", email)
                            .apply();

                    Toast.makeText(RegisterActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Пользователь уже существует", Toast.LENGTH_SHORT).show();
                    Log.w("REGISTER", "Ошибка регистрации: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(RegisterActivity.this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
                Log.e("REGISTER", "Ошибка подключения", t);
            }
        });
    }
}
