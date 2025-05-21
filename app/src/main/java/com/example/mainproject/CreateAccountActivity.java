package com.example.mainproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.util.HashMap;

public class CreateAccountActivity extends AppCompatActivity {

    EditText etLastName, etFirstName, etMiddleName;
    EditText etAddress, etCity, etUsername, etEmail, etPassword, etConfirmPassword;
    Button btnConfirm;

    private static final String REGISTER_URL = "http://10.0.2.2/myapp/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_main);

        // Initialize views
        etLastName = findViewById(R.id.etLastName);
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        // Collect input
        String lastName = etLastName.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String middleName = etMiddleName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Basic validation
        if (lastName.isEmpty() || firstName.isEmpty() || username.isEmpty()
                || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidUsername(username)) {
            Toast.makeText(this, "Invalid username. Use A or K followed by 8 digits.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare data to send
        HashMap<String, String> data = new HashMap<>();
        data.put("lastname", lastName);
        data.put("firstname", firstName);
        data.put("middlename", middleName);
        data.put("address", address);
        data.put("city", city);
        data.put("username", username);
        data.put("email", email);
        data.put("password", password);

        // Show loading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Send POST request
        HttpRequestHelper.sendPost(REGISTER_URL, data, result -> {
            progressDialog.dismiss();
            Log.d("ServerResponse", result);

            try {
                JSONObject jsonResponse = new JSONObject(result);
                String status = jsonResponse.optString("status", "error");
                String message = jsonResponse.optString("message", "Registration completed.");

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                if ("success".equalsIgnoreCase(status)) {
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                } else {
                    Log.e("RegisterError", "Registration failed: " + message);
                }
            } catch (Exception e) {
                Log.e("JSONParse", "Error parsing response: " + e.getMessage());
                Log.e("JSONParse", "Raw response: " + result);
                Toast.makeText(this, "Invalid server response.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[AKak]\\d{8}$");
    }
}
