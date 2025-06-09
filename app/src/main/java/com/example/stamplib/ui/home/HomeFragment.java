package com.example.stamplib.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.stamplib.ArticlesActivity;
import com.example.stamplib.LocaleHelper;
import com.example.stamplib.ProcessingActivity;
import com.example.stamplib.SettingsActivity;
import com.example.stamplib.databinding.FragmentHomeBinding;

import java.io.File;
import java.io.IOException;

public class HomeFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private FragmentHomeBinding binding;
    private Uri photoURI;
    private File photoFile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        String language = LocaleHelper.getAppLanguage(getActivity());
        LocaleHelper.setLocale(getActivity(), language);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.settings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        binding.find.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                try {
                    photoFile = createImageFile();
                    photoURI = FileProvider.getUriForFile(
                            requireContext(),
                            "com.example.stamplib.fileprovider",
                            photoFile
                    );
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Ошибка создания файла", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Камера недоступна", Toast.LENGTH_SHORT).show();
            }
        });

        binding.lect.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ArticlesActivity.class);
            startActivity(intent);
        });

        return root;
    }

    private File createImageFile() throws IOException {
        String fileName = "stamp_" + System.currentTimeMillis();
        File storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (photoFile != null && photoFile.exists()) {
                openProcessingActivity(photoFile);
            } else {
                Toast.makeText(getContext(), "Файл не найден", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openProcessingActivity(File file) {
        Intent intent = new Intent(getContext(), ProcessingActivity.class);
        intent.putExtra("photo_path", file.getAbsolutePath());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
