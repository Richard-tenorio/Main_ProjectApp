package com.example.mainproject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class accountSettingsActivity extends AppCompatActivity {

    @Nullable
    private Button btnProfile;
    @Nullable
    private Button btnLogout;

    @Nullable
    private SharedPreferences sharedPreferences;

    private static final String PREF_USER_INFO = "UserInfoPreferences";
    private static final String TAG = "AccountSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.account_settings);
        } catch (Exception e) {
            Log.e(TAG, "FATAL: Error setting content view. Check R.layout.account_settings.", e);
            Toast.makeText(this, "Critical error: Cannot load screen.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        if (btnProfile == null || btnLogout == null) {
            Log.e(TAG, "FATAL: Buttons not found. Check XML IDs (btnProfile, btnLogout).");
            Toast.makeText(this, "Error: Screen components missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            sharedPreferences = getSharedPreferences(PREF_USER_INFO, MODE_PRIVATE);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SharedPreferences.", e);
            Toast.makeText(this, "Error: Could not load user settings.", Toast.LENGTH_SHORT).show();
        }

        btnProfile.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(accountSettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "ProfileActivity not found.", e);
                Toast.makeText(accountSettingsActivity.this, "Profile screen not found.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Error starting ProfileActivity.", e);
                Toast.makeText(accountSettingsActivity.this, "Error opening profile.", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            performLogout();
        });
    }

    private void performLogout() {
        if (sharedPreferences != null) {
            try {
                sharedPreferences.edit().clear().apply();
                Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing SharedPreferences.", e);
                Toast.makeText(this, "Error during logout.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "SharedPreferences is null. Cannot clear session data.");
            Toast.makeText(this, "Logout incomplete: session not found.", Toast.LENGTH_SHORT).show();
        }

        try {
            Intent intent = new Intent(accountSettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "LoginActivity not found.", e);
            Toast.makeText(this, "Login screen not found.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error starting LoginActivity.", e);
            Toast.makeText(this, "Error returning to login screen.", Toast.LENGTH_SHORT).show();
            if (!isFinishing()) {
                finish();
            }
        }
    }
}
