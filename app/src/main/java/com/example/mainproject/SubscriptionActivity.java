package com.example.mainproject;

import android.content.Intent;
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

        tvSelectedPlan.setText("You don't have an active plan yet.");

        btnGetPlan.setOnClickListener(v -> {
            Intent intent = new Intent(SubscriptionActivity.this, PaymentActivity.class);
            startActivityForResult(intent, PAYMENT_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String selectedPlan = data.getStringExtra("selected_plan");
            if (selectedPlan != null && !selectedPlan.isEmpty()) {
                tvSelectedPlan.setText("Your selected plan: " + selectedPlan);
                Toast.makeText(this, "Plan updated after payment", Toast.LENGTH_SHORT).show();
            } else {
                tvSelectedPlan.setText("No plan selected.");
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
