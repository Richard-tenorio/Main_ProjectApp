package com.example.mainproject;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    private RadioGroup rgPlans;
    private EditText etName, etReference, etAmount;
    private CheckBox cbConfirm;
    private Button btnPay;
    private Handler handler = new Handler();  // For delay

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

            String name = etName.getText().toString().trim();
            String reference = etReference.getText().toString().trim();
            String amount = etAmount.getText().toString().trim();
            boolean isConfirmed = cbConfirm.isChecked();

            if (name.isEmpty() || reference.isEmpty() || amount.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isConfirmed) {
                Toast.makeText(this, "Please confirm the payment.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show processing toast
            Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();

            // Disable button to prevent double clicks
            btnPay.setEnabled(false);

            // Simulate delay (e.g., 2 seconds) before showing success
            handler.postDelayed(() -> {
                Toast.makeText(PaymentActivity.this, "Payment successful!", Toast.LENGTH_LONG).show();

                // Save the selected plan
                RadioButton selectedRadioButton = findViewById(selectedId);
                String selectedPlan = selectedRadioButton.getText().toString();

                getSharedPreferences("AppPrefs", MODE_PRIVATE)
                        .edit()
                        .putString("selectedPlan", selectedPlan)
                        .apply();

                // Close activity after showing success message
                finish();
            }, 2000); // 2000 milliseconds = 2 seconds
        });
    }
}
