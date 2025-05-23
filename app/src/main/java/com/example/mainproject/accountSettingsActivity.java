package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class accountSettingsActivity extends AppCompatActivity {

    TextView tvFullName;
    Button btnProfile, btnLogout;

    SharedPreferences sharedPreferences;
    private static final String PREF_USER = "UserInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_settings); // This should be the XML file you shared

        // Initialize UI components
        tvFullName = findViewById(R.id.tvFullName);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

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
    }
}
