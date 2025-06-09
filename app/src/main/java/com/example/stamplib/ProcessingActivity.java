package com.example.stamplib;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stamplib.models.AnalyzeResponse;
import com.example.stamplib.network.ApiClient;
import com.example.stamplib.network.ApiService;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProcessingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView progressText;
    private Handler handler = new Handler();
    private int progress = 0;
    private String photoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String language = LocaleHelper.getAppLanguage(this);
        LocaleHelper.setLocale(this, language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing);

        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);

        getSupportActionBar().hide();

        photoPath = getIntent().getStringExtra("photo_path");

        simulateProgress();
        uploadPhoto(new File(photoPath));
    }

    private void simulateProgress() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progress < 90) {
                    progress += 5;
                    progressBar.setProgress(progress);
                    progressText.setText(progress + "%");
                    handler.postDelayed(this, 150);
                }
            }
        }, 150);
    }

    private void uploadPhoto(File file) {
        ApiService apiService = ApiClient.getService();
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Call<AnalyzeResponse> call = apiService.uploadStampPhoto(body);
        call.enqueue(new Callback<AnalyzeResponse>() {
            @Override
            public void onResponse(Call<AnalyzeResponse> call, Response<AnalyzeResponse> response) {
                handler.removeCallbacksAndMessages(null);
                progressBar.setProgress(100);
                progressText.setText("100%");

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ProcessingActivity.this, "Ошибка обработки на сервере", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                AnalyzeResponse result = response.body();

                Intent intent = new Intent(ProcessingActivity.this, ResultActivity.class);
                intent.putExtra("photo_path", result.getCropped_path());
                intent.putExtra("recognition_results", result.getResults().toArray(new String[0]));
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<AnalyzeResponse> call, Throwable t) {
                handler.removeCallbacksAndMessages(null);
                Toast.makeText(ProcessingActivity.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}
