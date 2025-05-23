package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    TextView tvPlanName;
    EditText etName, etReference, etAmount;
    CheckBox cbConfirm;
    Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Link UI components
        tvPlanName = findViewById(R.id.tvPlanName);
        etName = findViewById(R.id.etName);
        etReference = findViewById(R.id.etReference);
        etAmount = findViewById(R.id.etAmount);
        cbConfirm = findViewById(R.id.cbConfirm);
        btnPay = findViewById(R.id.btnPay);

        // Get data from intent
        Intent intent = getIntent();
        String planName = intent.getStringExtra("plan");
        String amount = intent.getStringExtra("amount");

        // Display plan and amount
        tvPlanName.setText("Plan: " + (planName != null ? planName : ""));
        etAmount.setText(amount != null ? amount : "");

        btnPay.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String reference = etReference.getText().toString().trim();

            if (name.isEmpty() || reference.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!cbConfirm.isChecked()) {
                Toast.makeText(this, "Please confirm the payment", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Payment Submitted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
