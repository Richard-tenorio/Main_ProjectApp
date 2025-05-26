package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SubscriptionActivity extends AppCompatActivity {

    private static final int PAYMENT_REQUEST_CODE = 1001;
    private TextView tvSelectedPlan;
    private Button btnGetPlan;
    private String username;  // To identify user uniquely

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscription_activity);

        tvSelectedPlan = findViewById(R.id.tvSelectedPlan);
        btnGetPlan = findViewById(R.id.btnGetPlan);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Subscription");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        // Get username from intent extras
        username = getIntent().getStringExtra("username");
        if (username == null || username.trim().isEmpty()) {
            username = "default_user";  // fallback username
        }

        // Always reset any saved plan on login
        resetUserPlanOnLogin();

        // Load plan after reset
        loadUserPlan();

        btnGetPlan.setOnClickListener(v -> {
            Intent intent = new Intent(SubscriptionActivity.this, PaymentActivity.class);
            intent.putExtra("username", username);
            startActivityForResult(intent, PAYMENT_REQUEST_CODE);
        });
    }

    private void resetUserPlanOnLogin() {
        SharedPreferences prefs = getSharedPreferences("subscription_prefs_" + username, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("saved_plan"); // only remove plan, keep other data if needed
        editor.apply();
    }

    private void loadUserPlan() {
        SharedPreferences prefs = getSharedPreferences("subscription_prefs_" + username, MODE_PRIVATE);
        String savedPlan = prefs.getString("saved_plan", null);

        if (savedPlan != null && !savedPlan.isEmpty()) {
            tvSelectedPlan.setText("Your selected plan: " + savedPlan);
        } else {
            tvSelectedPlan.setText("You don't have an active plan yet.");
        }
    }

    public void clearUserPlan() {
        SharedPreferences prefs = getSharedPreferences("subscription_prefs_" + username, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        tvSelectedPlan.setText("You don't have an active plan yet.");
        Toast.makeText(this, "Your subscription plan has been reset.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectedPlan = data.getStringExtra("selected_plan");
            if (selectedPlan != null && !selectedPlan.isEmpty()) {
                tvSelectedPlan.setText("Your selected plan: " + selectedPlan);
                Toast.makeText(this, "Plan updated after payment", Toast.LENGTH_SHORT).show();

                SharedPreferences prefs = getSharedPreferences("subscription_prefs_" + username, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("saved_plan", selectedPlan);
                editor.apply();
            } else {
                tvSelectedPlan.setText("You don't have an active plan yet.");
                Toast.makeText(this, "No plan returned after payment", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
