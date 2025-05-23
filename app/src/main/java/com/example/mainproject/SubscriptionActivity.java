package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SubscriptionActivity extends AppCompatActivity {

    private TextView tvSelectedPlan;
    private ImageView successImage1, successImage2, successImage3;
    private LinearLayout box1, box2, box3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscription_activity);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Subscription");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Bind UI elements
        tvSelectedPlan = findViewById(R.id.tvSelectedPlan);
        successImage1 = findViewById(R.id.successImage1);
        successImage2 = findViewById(R.id.successImage2);
        successImage3 = findViewById(R.id.successImage3);
        box1 = findViewById(R.id.box1);
        box2 = findViewById(R.id.box2);
        box3 = findViewById(R.id.box3);

        // Hide success images initially
        successImage1.setVisibility(View.GONE);
        successImage2.setVisibility(View.GONE);
        successImage3.setVisibility(View.GONE);

        // Simulated selected plan from database/local storage
        String selectedPlan = getSelectedPlanFromData();

        if (selectedPlan == null || selectedPlan.isEmpty()) {
            tvSelectedPlan.setText("You don't have any plan");
        } else {
            tvSelectedPlan.setText("Your current plan: " + selectedPlan);
            switch (selectedPlan) {
                case "1 Month Plan":
                    successImage1.setVisibility(View.VISIBLE);
                    break;
                case "Sponsored":
                    successImage2.setVisibility(View.VISIBLE);
                    break;
                case "Whole Semester Plan":
                    successImage3.setVisibility(View.VISIBLE);
                    break;
            }
        }

        // Handle clicks on plans
        box1.setOnClickListener(v -> launchPayment("1 Month Plan", "100"));
        box2.setOnClickListener(v -> launchPayment("Sponsored", "0"));
        box3.setOnClickListener(v -> launchPayment("Whole Semester Plan", "300"));
    }

    // Launch PaymentActivity with plan data
    private void launchPayment(String plan, String amount) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("plan", plan);
        intent.putExtra("amount", amount);
        startActivity(intent);
    }

    // Simulated selected plan method (replace with actual logic)
    private String getSelectedPlanFromData() {
        return "1 Month Plan"; // Replace this with actual logic or value from SharedPreferences/Database
    }

    // Handle back button in toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // go back when toolbar back button is clicked
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
