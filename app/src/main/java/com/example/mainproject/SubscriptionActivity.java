package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SubscriptionActivity extends AppCompatActivity {

    private static final int PAYMENT_REQUEST_CODE = 1001;

    private TextView tvSelectedPlan;
    private Button btnGetPlan;
    private Integer paymentsId = null;

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

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        if (prefs.contains("payments_id")) {
            paymentsId = prefs.getInt("payments_id", 0);
            fetchUserPlan(paymentsId);
        } else {
            tvSelectedPlan.setText("You don't have an active plan yet.");
        }

        btnGetPlan.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(SubscriptionActivity.this, PaymentActivity.class);
            startActivityForResult(paymentIntent, PAYMENT_REQUEST_CODE);
        });
    }

    private void fetchUserPlan(int payments_id) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            try {
                String urlStr = "http://10.0.2.2/myapp/subcription.php?payments_id=" + URLEncoder.encode(String.valueOf(payments_id), "UTF-8");
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    String result = sb.toString().trim();

                    mainHandler.post(() -> {
                        if (result.startsWith("error:")) {
                            tvSelectedPlan.setText("Failed to fetch plan");
                            Toast.makeText(SubscriptionActivity.this, result, Toast.LENGTH_LONG).show();
                        } else if (result.equalsIgnoreCase("none") || result.isEmpty()) {
                            tvSelectedPlan.setText("No active plan found");
                        } else {
                            tvSelectedPlan.setText("Your current plan: " + result);
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                        tvSelectedPlan.setText("Failed to connect (HTTP " + responseCode + ")");
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    tvSelectedPlan.setText("Error: " + e.getMessage());
                    Toast.makeText(SubscriptionActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int returnedPaymentsId = data.getIntExtra("payments_id", 0);

            if (returnedPaymentsId > 0) {
                paymentsId = returnedPaymentsId;

                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("payments_id", paymentsId);
                editor.apply();

                fetchUserPlan(paymentsId);
                Toast.makeText(this, "Plan updated after payment", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No payment ID returned after payment", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
