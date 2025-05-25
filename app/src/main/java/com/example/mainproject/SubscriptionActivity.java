package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
    private Integer userId = null;

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

        // ✅ Check if user_id exists at all
        if (prefs.contains("user_id")) {
            userId = prefs.getInt("user_id", 0);
            fetchUserPlan(userId);
        } else {
            tvSelectedPlan.setText("You don't have an active plan yet.");
        }

        btnGetPlan.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(SubscriptionActivity.this, PaymentActivity.class);
            startActivityForResult(paymentIntent, PAYMENT_REQUEST_CODE);
        });
    }

    private void fetchUserPlan(int userId) {
        new AsyncTask<Integer, Void, String>() {
            @Override
            protected String doInBackground(Integer... params) {
                int uid = params[0];
                try {
                    String urlStr = "http://10.0.2.2/myapp/subscription.php?user_id=" + URLEncoder.encode(String.valueOf(uid), "UTF-8");
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();
                        return sb.toString().trim();
                    } else {
                        return "error: HTTP code " + responseCode;
                    }
                } catch (Exception e) {
                    return "error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d("SubscriptionActivity", "Plan result: " + result);

                if (result.startsWith("error:")) {
                    tvSelectedPlan.setText("Failed to fetch plan");
                    Toast.makeText(SubscriptionActivity.this, result, Toast.LENGTH_LONG).show();
                } else if (result.equalsIgnoreCase("none") || result.isEmpty()) {
                    tvSelectedPlan.setText("No active plan found");
                } else {
                    tvSelectedPlan.setText("Your current plan: " + result);
                }
            }
        }.execute(userId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int returnedUserId = data.getIntExtra("user_id", 0);

            if (returnedUserId > 0) {
                userId = returnedUserId;

                // ✅ Save new user_id after payment
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("user_id", userId);
                editor.apply();

                fetchUserPlan(userId);
                Toast.makeText(this, "Plan updated after payment", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No user ID returned after payment", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
