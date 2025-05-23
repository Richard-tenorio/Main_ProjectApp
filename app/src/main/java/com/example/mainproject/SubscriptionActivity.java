package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SubscriptionActivity extends AppCompatActivity {

    private TextView tvSelectedPlan;
    private LinearLayout box1, box2, box3;
    private ImageView successImage1, successImage2, successImage3;
    private Button btnGetPlan;

    private String selectedPlanName = null;
    private String selectedPlanAmount = null;

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

        successImage1 = findViewById(R.id.successImage1);
        successImage2 = findViewById(R.id.successImage2);
        successImage3 = findViewById(R.id.successImage3);

        btnGetPlan = findViewById(R.id.btnGetPlan);
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
        box1.setOnClickListener(v -> {
            selectPlan("1 Month Plan", "100");
        });

        box2.setOnClickListener(v -> {
            selectPlan("Sponsored", "0");
        });

        box3.setOnClickListener(v -> {
            selectPlan("Whole Semester Plan", "300");
        });

        btnGetPlan.setOnClickListener(v -> {
            if (selectedPlanName != null && selectedPlanAmount != null) {
                launchPayment(selectedPlanName, selectedPlanAmount);
            }
        });
    }

    private void selectPlan(String plan, String amount) {
        selectedPlanName = plan;
        selectedPlanAmount = amount;

        // Update UI indicators
        successImage1.setVisibility(View.GONE);
        successImage2.setVisibility(View.GONE);
        successImage3.setVisibility(View.GONE);

        if (plan.equals("1 Month Plan")) {
            successImage1.setVisibility(View.VISIBLE);
        } else if (plan.equals("Sponsored")) {
            successImage2.setVisibility(View.VISIBLE);
        } else if (plan.equals("Whole Semester Plan")) {
            successImage3.setVisibility(View.VISIBLE);
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
