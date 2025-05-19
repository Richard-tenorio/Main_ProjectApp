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
    private static final String PREF_USER = "UserInfo";
    private static final String PREF_THEME = "AppTheme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load saved theme before setting content view
        SharedPreferences themePrefs = getSharedPreferences(PREF_THEME, MODE_PRIVATE);
        boolean isDark = themePrefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_settings);

        // Initialize UI components
        tvFullName = findViewById(R.id.tvFullName);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // Get SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_USER, MODE_PRIVATE);

        // Get full name from intent or shared prefs
        String fullName = getIntent().getStringExtra("fullName");
        if (fullName != null) {
            tvFullName.setText("Full Name: " + fullName);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("fullName", fullName);
            editor.apply();
        } else {
            String savedName = sharedPreferences.getString("fullName", "Unknown");
            tvFullName.setText("Full Name: " + savedName);
        }

        // Profile button logic
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(accountSettingsActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Logout button logic
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(accountSettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Dark mode switch logic
        switchDarkMode.setChecked(isDark);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            SharedPreferences.Editor themeEditor = getSharedPreferences(PREF_THEME, MODE_PRIVATE).edit();
            themeEditor.putBoolean("dark_mode", isChecked);
            themeEditor.apply();

            recreate();
        });
    }
}
