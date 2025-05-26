package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class PaymentActivity extends AppCompatActivity {

    private RadioGroup rgPlans;
    private EditText etName, etReference, etAmount;
    private Button btnPay;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final String PHP_URL = "http://10.0.2.2/myapp/payment.php"; // Optional

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        rgPlans = findViewById(R.id.rgPlans);
        etName = findViewById(R.id.etName);
        etReference = findViewById(R.id.etReference);
        etAmount = findViewById(R.id.etAmount);
        btnPay = findViewById(R.id.btnPay);

        btnPay.setOnClickListener(v -> {
            int selectedId = rgPlans.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a plan.", Toast.LENGTH_SHORT).show();
                return;
            }

            String plan = ((RadioButton) findViewById(selectedId)).getText().toString().trim();
            String name = etName.getText().toString().trim();
            String reference = etReference.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();

            if (name.isEmpty() || reference.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert name to Sentence Case
            name = toSentenceCase(name);

            // Validate reference_no: digits only
            if (!reference.matches("\\d+")) {
                Toast.makeText(this, "Reference number must contain digits only.", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Amount must be greater than zero.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount format.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate amount matches selected plan
            if (!isValidAmount(plan, amount)) {
                Toast.makeText(this, "Amount for " + plan + " plan must be exactly " + (int) amountForPlan(plan) + ".", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent multiple clicks
            btnPay.setEnabled(false);

            // OPTIONAL: Send to backend
            submitPaymentToServer(plan, name, reference, amount);
        });
    }

    private boolean isValidAmount(String plan, double amount) {
        double expected = amountForPlan(plan);
        return amount == expected;
    }

    private double amountForPlan(String plan) {
        switch (plan.toLowerCase()) {
            case "basic": return 200;
            case "premium": return 750;
            case "sponsor": return 375;
            default: return 0;
        }
    }

    private void submitPaymentToServer(String plan, String name, String reference, double amount) {
        new Thread(() -> {
            try {
                String postData = "plan=" + URLEncoder.encode(plan, "UTF-8") +
                        "&name=" + URLEncoder.encode(name, "UTF-8") +
                        "&reference_no=" + URLEncoder.encode(reference, "UTF-8") +
                        "&amount=" + URLEncoder.encode(String.valueOf(amount), "UTF-8");

                URL url = new URL(PHP_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();

                    String response = responseBuilder.toString().trim();

                    mainHandler.post(() -> {
                        btnPay.setEnabled(true);
                        if (response.startsWith("error:")) {
                            Toast.makeText(PaymentActivity.this, "Server error: " + response, Toast.LENGTH_LONG).show();
                        } else {
                            int paymentId;
                            try {
                                paymentId = Integer.parseInt(response);
                            } catch (NumberFormatException e) {
                                Toast.makeText(PaymentActivity.this, "Invalid server response.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            Toast.makeText(PaymentActivity.this, "Payment successful!", Toast.LENGTH_SHORT).show();

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("selected_plan", plan);
                            resultIntent.putExtra("payments_id", paymentId);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                        btnPay.setEnabled(true);
                        Toast.makeText(PaymentActivity.this, "Failed to connect to server. HTTP code: " + responseCode, Toast.LENGTH_LONG).show();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                mainHandler.post(() -> {
                    btnPay.setEnabled(true);
                    Toast.makeText(PaymentActivity.this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String toSentenceCase(String input) {
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
