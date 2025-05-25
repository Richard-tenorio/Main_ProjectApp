package com.example.mainproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

            // Validate and format amount as double with 2 decimals
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

            String amountFormatted = String.format("%.2f", amount);

            btnPay.setEnabled(false);
            new SendPaymentTask().execute(plan, name, reference, amountFormatted);
        });
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

    private class SendPaymentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String plan = params[0];
            String name = params[1];
            String reference = params[2];
            String amount = params[3];

            try {
                URL url = new URL("http://10.0.2.2/myapp/payment.php"); // Localhost on Android emulator
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                String postData = "plan=" + URLEncoder.encode(plan, "UTF-8") +
                        "&name=" + URLEncoder.encode(name, "UTF-8") +
                        "&reference_no=" + URLEncoder.encode(reference, "UTF-8") +
                        "&amount=" + URLEncoder.encode(amount, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                br.close();
                return response.toString().trim();

            } catch (Exception e) {
                return "error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            btnPay.setEnabled(true);
            result = result.trim();
            Log.d("PaymentResult", "Server response: '" + result + "'");

            if ("success".equalsIgnoreCase(result)) {
                Toast.makeText(PaymentActivity.this, "Payment successful!", Toast.LENGTH_LONG).show();
                // Go back to SubscriptionActivity
                Intent intent = new Intent(PaymentActivity.this, SubscriptionActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(PaymentActivity.this, "Payment failed: " + result, Toast.LENGTH_LONG).show();
            }
        }
    }
}
