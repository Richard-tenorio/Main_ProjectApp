package com.example.mainproject;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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

        // Initialize views
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
            String amountStr = etAmount.getText().toString().trim();
            boolean isConfirmed = cbConfirm.isChecked();

            if (name.isEmpty() || reference.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isConfirmed) {
                Toast.makeText(this, "Please confirm the payment.", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount entered.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent double clicks
            btnPay.setEnabled(false);

            // Show processing toast
            Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();

            handler.postDelayed(() -> {
                // Get selected plan text
                RadioButton selectedRadioButton = findViewById(selectedId);
                String selectedPlan = selectedRadioButton.getText().toString();

                // Save selected plan to SharedPreferences
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().putString("selectedPlan", selectedPlan).apply();

                // Save data to database
                DBHelper dbHelper = new DBHelper(PaymentActivity.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Fixed: Use backticks to avoid conflict with reserved keywords like "plan"
                String insertQuery = "INSERT INTO payments (`plan`, `name`, `reference_no`, `amount`, `confirmed`) VALUES (?, ?, ?, ?, ?)";
                db.execSQL(insertQuery, new Object[]{
                        selectedPlan,
                        name,
                        reference,
                        amount,
                        isConfirmed ? 1 : 0
                });

                Toast.makeText(PaymentActivity.this, "Payment successful!", Toast.LENGTH_LONG).show();
                finish();
            }, 2000);
        });
    }
}
