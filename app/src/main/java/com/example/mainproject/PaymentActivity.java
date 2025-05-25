package com.example.mainproject;

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
    private CheckBox cbConfirm;
    private Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        rgPlans = findViewById(R.id.rgPlans);
        etName = findViewById(R.id.etName);
        etReference = findViewById(R.id.etReference);
        etAmount = findViewById(R.id.etAmount);
        cbConfirm = findViewById(R.id.cbConfirm);
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

            if (plan.isEmpty() || name.isEmpty() || reference.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate amount
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Amount must be greater than zero.", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbConfirm.isChecked()) {
                Toast.makeText(this, "Please confirm your payment.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnPay.setEnabled(false);
            new SendPaymentTask().execute(plan, name, reference, amountStr);
        });
    }

    private class SendPaymentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String plan = params[0];
            String name = params[1];
            String reference = params[2];
            String amount = params[3];

            try {
                URL url = new URL("http://10.0.2.2/payment.php");  // Change if real device
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "plan=" + URLEncoder.encode(plan, "UTF-8") +
                        "&name=" + URLEncoder.encode(name, "UTF-8") +
                        "&reference_no=" + URLEncoder.encode(reference, "UTF-8") +
                        "&amount=" + URLEncoder.encode(amount, "UTF-8");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postData.getBytes());
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                InputStream is = (responseCode >= 200 && responseCode < 400)
                        ? conn.getInputStream() : conn.getErrorStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    responseBuilder.append(line);
                }
                br.close();

                return responseBuilder.toString();

            } catch (Exception e) {
                Log.e("PaymentActivity", "Error sending payment: " + e.getMessage(), e);
                return "error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            btnPay.setEnabled(true);
            Log.d("PaymentActivity", "Response: " + result);

            if ("success".equalsIgnoreCase(result.trim())) {
                Toast.makeText(PaymentActivity.this, "Payment successful!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(PaymentActivity.this, "Payment failed: " + result, Toast.LENGTH_LONG).show();
            }
        }
    }
}
