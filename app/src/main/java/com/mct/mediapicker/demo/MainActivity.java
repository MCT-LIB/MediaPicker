package com.mct.mediapicker.demo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mct.mediapicker.M3ThemeStrategy;
import com.mct.mediapicker.MediaPicker;
import com.mct.mediapicker.MediaPickerOption;
import com.mct.mediapicker.demo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView((binding = ActivityMainBinding.inflate(getLayoutInflater())).getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnStart.setOnClickListener(v -> {
            MediaPicker.pick(this, createOption());
        });
    }

    private MediaPickerOption createOption() {
        MediaPickerOption.Builder builder = new MediaPickerOption.Builder();
        // @formatter:off

        // type
        if (binding.rbImage.isChecked()) builder.image();
        if (binding.rbVideo.isChecked()) builder.video();
        if (binding.rbAll.isChecked()) builder.all();

        // mode
        if (binding.rbSingle.isChecked()) builder.single(uri -> {});
        if (binding.rbMulti.isChecked()) builder.multi(uris -> {});
        if (binding.rbMultiExact.isChecked()) builder.multiExact(uris -> {}, 5);
        if (binding.rbMultiRange.isChecked()) builder.multiRange(uris -> {}, 1, 10);

        // theme
        if (binding.rbDefault.isChecked()) builder.themeStrategy(M3ThemeStrategy.DEFAULT);
        if (binding.rbInherit.isChecked()) builder.themeStrategy(M3ThemeStrategy.INHERIT);
        if (binding.rbDynamic.isChecked()) builder.themeStrategy(M3ThemeStrategy.DYNAMIC);
        if (binding.rbInheritOrDynamic.isChecked()) builder.themeStrategy(M3ThemeStrategy.INHERIT_OR_DYNAMIC);
        if (binding.rbDynamicOrInherit.isChecked()) builder.themeStrategy(M3ThemeStrategy.DYNAMIC_OR_INHERIT);

        // @formatter:on
        return builder.build();
    }
}