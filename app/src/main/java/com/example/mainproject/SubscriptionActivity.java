package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SubscriptionActivity extends AppCompatActivity {

    private TextView tvSelectedPlan;
    private Button box1, box2, box3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscription_activity);

        setupToolbar();
        bindViews();
        displaySelectedPlan();
        setupClickListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Subscription");
        }
    }

    private void bindViews() {
        tvSelectedPlan = findViewById(R.id.tvSelectedPlan);
        box1 = findViewById(R.id.box1);
        box2 = findViewById(R.id.box2);
        box3 = findViewById(R.id.box3);
    }

    private void displaySelectedPlan() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String selectedPlan = prefs.getString("selectedPlan", null);

        if (selectedPlan == null || selectedPlan.isEmpty()) {
            tvSelectedPlan.setText("You don't have any plan");
        } else {
            tvSelectedPlan.setText("Your current plan: " + selectedPlan);
        }
    }

    private void setupClickListeners() {
        box1.setOnClickListener(v -> launchPayment("1 Month Plan", "100"));
        box2.setOnClickListener(v -> launchPayment("Sponsored", "0"));
        box3.setOnClickListener(v -> launchPayment("Whole Semester Plan", "300"));
    }

    private void launchPayment(String plan, String amount) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("planName", plan);
        intent.putExtra("amount", amount);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displaySelectedPlan();  // Refresh the selected plan when returning
    }

    // Handle toolbar back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
