package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SubscriptionActivity extends AppCompatActivity {

    private TextView tvSelectedPlan;
    private Button btnGetPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscription_activity);

        setupToolbar();
        bindViews();
        displaySelectedPlan();
        setupClickListener();
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
        btnGetPlan = findViewById(R.id.btnGetPlan);
    }

    private void displaySelectedPlan() {
        String selectedPlan = getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("selectedPlan", null);

        if (selectedPlan == null || selectedPlan.isEmpty()) {
            tvSelectedPlan.setText("You don't have any plan");
        } else {
            tvSelectedPlan.setText("Your current plan: " + selectedPlan);
        }
    }

    private void setupClickListener() {
        btnGetPlan.setOnClickListener(v -> {
            // Launch PaymentActivity without preset data; payment activity will handle choices
            launchPayment();
        });
    }

    private void launchPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displaySelectedPlan();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
