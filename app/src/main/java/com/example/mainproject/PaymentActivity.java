package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

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

            // Validate and format amount
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

            // Here we assume the payment is successful (no PHP interaction)
            Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show();

            // Send selected plan back to SubscriptionActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_plan", plan);
            setResult(RESULT_OK, resultIntent);
            finish();
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
}
