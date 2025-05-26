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

    private static final String PHP_URL = "http://10.0.2.2/myapp/payment.php"; // Change to your actual PHP URL

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

            // Disable the button to prevent multiple clicks
            btnPay.setEnabled(false);

            // Call the backend PHP to submit payment
            submitPaymentToServer(plan, name, reference, amount);
        });
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

                // Write POST data
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
                            // We got the inserted payment ID, send result back
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
