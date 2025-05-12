package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class accountSettingsActivity extends AppCompatActivity {

    TextView tvFullName;
    Button btnProfile, btnLogout;
    Switch switchDarkMode;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_settings);

        // Initialize UI components
        tvFullName = findViewById(R.id.tvFullName);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Get SharedPreferences
        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

        // Get full name from intent
        String fullName = getIntent().getStringExtra("fullName");
        if (fullName != null) {
            tvFullName.setText("Full Name: " + fullName);

            // Save fullName into SharedPreferences for ProfileActivity
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("fullName", fullName);
            editor.apply();
        } else {
            tvFullName.setText("Full Name: Unknown");
        }

        // Profile button logic
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(accountSettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Logout button logic
        btnLogout.setOnClickListener(v -> {
            // Clear SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Navigate back to LoginActivity and clear back stack
            Intent intent = new Intent(accountSettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Dark mode toggle
        switchDarkMode.setChecked(isDarkModeEnabled());
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
        });
    }

    private boolean isDarkModeEnabled() {
        int mode = AppCompatDelegate.getDefaultNightMode();
        return mode == AppCompatDelegate.MODE_NIGHT_YES;
    }
}
