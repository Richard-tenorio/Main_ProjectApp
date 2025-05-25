package com.example.mainproject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class accountSettingsActivity extends AppCompatActivity {

    // Declare views
    @Nullable // Indicate that these might be null initially or if findViewById fails
    private TextView tvFullName;
    @Nullable
    private Button btnProfile;
    @Nullable
    private Button btnLogout;

    @Nullable // SharedPreferences can also fail to initialize in rare edge cases
    private SharedPreferences sharedPreferences;

    // Constants for SharedPreferences and Logging
    private static final String PREF_USER_INFO = "UserInfoPreferences";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String TAG = "AccountSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Attempt to set the content view
        try {
            setContentView(R.layout.account_settings);
        } catch (Exception e) {
            Log.e(TAG, "FATAL: Error setting content view. Check R.layout.account_settings.", e);
            Toast.makeText(this, "Critical error: Cannot load screen.", Toast.LENGTH_LONG).show();
            finish(); // Cannot proceed if layout can't be set
            return;
        }

        // Initialize UI components
        tvFullName = findViewById(R.id.tvFullName);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Critical check: Ensure all necessary views are found
        if (tvFullName == null || btnProfile == null || btnLogout == null) {
            Log.e(TAG, "FATAL: Not all UI components were found. Check XML IDs (tvFullName, btnProfile, btnLogout) in account_settings.xml.");
            Toast.makeText(this, "Error: Screen components missing. Please contact support.", Toast.LENGTH_LONG).show();
            finish(); // Cannot operate without these views
            return;
        }

        // Initialize SharedPreferences
        try {
            sharedPreferences = getSharedPreferences(PREF_USER_INFO, MODE_PRIVATE);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing SharedPreferences.", e);
            Toast.makeText(this, "Error: Could not load user settings.", Toast.LENGTH_SHORT).show();
            // Decide if the activity can continue without SharedPreferences or should finish
            // For this example, we'll allow it to continue but features might be limited
        }

        loadAndDisplayFullName();

        // Setup button listeners
        // btnProfile listener
        btnProfile.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(accountSettingsActivity.this, ProfileActivity.class);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Error: ProfileActivity not found. Is it declared in AndroidManifest.xml?", e);
                Toast.makeText(accountSettingsActivity.this, "Could not open profile: Activity not found.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Error starting ProfileActivity.", e);
                Toast.makeText(accountSettingsActivity.this, "An unexpected error occurred while opening profile.", Toast.LENGTH_SHORT).show();
            }
        });

        // btnLogout listener
        btnLogout.setOnClickListener(v -> {
            performLogout();
        });
    }

    private void loadAndDisplayFullName() {
        if (tvFullName == null) {
            Log.e(TAG, "tvFullName is null in loadAndDisplayFullName. Cannot set text.");
            return; // Guard clause
        }

        String fullNameToDisplay = "Unknown User"; // Default value

        // Try to get name from Intent
        Intent intent = getIntent();
        if (intent != null) {
            String fullNameFromIntent = intent.getStringExtra(KEY_FULL_NAME);
            if (fullNameFromIntent != null && !fullNameFromIntent.isEmpty()) {
                fullNameToDisplay = fullNameFromIntent;
                // Save to SharedPreferences if we got it from Intent and SharedPreferences is available
                if (sharedPreferences != null) {
                    try {
                        sharedPreferences.edit().putString(KEY_FULL_NAME, fullNameFromIntent).apply();
                    } catch (Exception e) {
                        Log.e(TAG, "Error saving fullName to SharedPreferences from Intent.", e);
                    }
                }
            }
        }

        // If not found in Intent or to confirm, try loading from SharedPreferences
        // Condition ensures we try SharedPreferences if intent didn't provide a name,
        // or if the intent itself was null (though less likely if activity started normally).
        if (sharedPreferences != null && (fullNameToDisplay.equals("Unknown User") || intent == null || intent.getStringExtra(KEY_FULL_NAME) == null) ) {
            try {
                String savedName = sharedPreferences.getString(KEY_FULL_NAME, "Unknown User");
                // Only update if SharedPreferences has a more specific name than the default or what might have come from a faulty intent.
                if (!savedName.equals("Unknown User")) {
                    fullNameToDisplay = savedName;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading fullName from SharedPreferences.", e);
            }
        } else if (sharedPreferences == null && fullNameToDisplay.equals("Unknown User")) {
            Log.w(TAG, "SharedPreferences is null. Cannot load saved full name.");
        }
        tvFullName.setText("Full Name: " + fullNameToDisplay);
    }

    private void performLogout() {
        if (sharedPreferences != null) {
            try {
                sharedPreferences.edit().clear().apply();
                Toast.makeText(this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Error clearing SharedPreferences during logout.", e);
                Toast.makeText(this, "Error during logout process.", Toast.LENGTH_SHORT).show();
                // Even if clearing prefs fails, still try to navigate
            }
        } else {
            Log.w(TAG, "SharedPreferences is null in performLogout. Cannot clear session data.");
            Toast.makeText(this, "Logout incomplete: session data not found.", Toast.LENGTH_SHORT).show();
        }

        // Navigate to LoginActivity
        try {
            Intent intent = new Intent(accountSettingsActivity.this, LoginActivity.class);
            // Clear back stack so user cannot navigate back to this activity after logout
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Close this activity
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "FATAL: LoginActivity not found. Is it declared in AndroidManifest.xml?", e);
            Toast.makeText(this, "Critical error: Login screen not found. Please contact support.", Toast.LENGTH_LONG).show();
            // If LoginActivity is missing, the app is in a bad state.
            // Depending on desired behavior, could try to finish more forcefully or just log.
        } catch (Exception e) {
            Log.e(TAG, "Error starting LoginActivity after logout.", e);
            Toast.makeText(this, "Error redirecting to login screen.", Toast.LENGTH_SHORT).show();
            // Still finish this activity
            if (!isFinishing()) {
                finish();
            }
        }
    }
}