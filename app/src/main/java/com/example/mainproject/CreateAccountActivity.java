package com.example.mainproject;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.HashMap;

public class CreateAccountActivity extends AppCompatActivity {

    EditText etLastName, etFirstName, etMiddleName;
    EditText etAddress, etCity, etUsername, etEmail, etPassword, etConfirmPassword;
    EditText etBirthdate;
    Button btnConfirm;
    CheckBox cbConfirmInfo;

    private static final String REGISTER_URL = "http://10.0.2.2/myapp/register.php";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_main);

        etLastName = findViewById(R.id.etLastName);
        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etBirthdate = findViewById(R.id.etBirthdate);
        btnConfirm = findViewById(R.id.btnConfirm);
        cbConfirmInfo = findViewById(R.id.cbConfirmInfo);

        btnConfirm.setVisibility(View.GONE); // Hide button initially

        cbConfirmInfo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnConfirm.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        etBirthdate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CreateAccountActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear);
                        etBirthdate.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });

        btnConfirm.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        if (!cbConfirmInfo.isChecked()) {
            Toast.makeText(this, "Please confirm the information by checking the box.", Toast.LENGTH_SHORT).show();
            return;
        }

        String lastName = etLastName.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String middleName = etMiddleName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String birthdateInput = etBirthdate.getText().toString().trim();

        if (!lastName.matches("^[a-zA-Z]+$") || !firstName.matches("^[a-zA-Z]+$") || !middleName.matches("^[a-zA-Z]*$")) {
            etLastName.setError("Invalid characters in name");
            etFirstName.setError("Invalid characters in name");
            etMiddleName.setError("Invalid characters in name");
            Toast.makeText(this, "Names must not contain numbers or special characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.endsWith("@umak.edu.ph")) {
            etEmail.setError("Email must end with '@umak.edu.ph'");
            Toast.makeText(this, "Email must end with '@umak.edu.ph'.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() <= 8) {
            etPassword.setError("Password must be more than 8 characters");
            Toast.makeText(this, "Password must be more than 8 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lastName.isEmpty() || firstName.isEmpty() || username.isEmpty()
                || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || address.isEmpty() || city.isEmpty() || birthdateInput.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            Toast.makeText(this, "Invalid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidUsername(username)) {
            etUsername.setError("Username must start with A or K followed by 8 digits");
            Toast.makeText(this, "Username must start with A or K followed by 8 digits.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] dateParts = birthdateInput.split("/");
        if (dateParts.length != 3) {
            etBirthdate.setError("Invalid date format");
            Toast.makeText(this, "Invalid birthdate format.", Toast.LENGTH_SHORT).show();
            return;
        }

        int month = Integer.parseInt(dateParts[0]) - 1;
        int day = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        if (!isValidBirthdate(year, month, day)) {
            etBirthdate.setError("Birthdate cannot be in the future");
            Toast.makeText(this, "Birthdate cannot be in the future.", Toast.LENGTH_SHORT).show();
            return;
        }

        String birthdate = String.format("%04d-%02d-%02d", year, month + 1, day);

        HashMap<String, String> data = new HashMap<>();
        data.put("lastname", lastName);
        data.put("firstname", firstName);
        data.put("middlename", middleName);
        data.put("address", address);
        data.put("city", city);
        data.put("username", username);
        data.put("email", email);
        data.put("password", password);
        data.put("birthdate", birthdate);

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Registering...");
            progressDialog.setCancelable(false);
        }

        if (!isFinishing() && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        HttpRequestHelper.sendPost(REGISTER_URL, data, result -> runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing() && !isFinishing()) {
                progressDialog.dismiss();
            }

            Log.d("ServerResponse", result);

            try {
                JSONObject jsonResponse = new JSONObject(result);
                String status = jsonResponse.optString("status", "error");
                String message = jsonResponse.optString("message", "Unknown error occurred.");

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();

                if ("success".equalsIgnoreCase(status)) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("RegisterError", "Registration failed: " + message);
                }
            } catch (Exception e) {
                Log.e("JSONParse", "Error parsing response: ", e);
                Toast.makeText(this, "Invalid server response.", Toast.LENGTH_LONG).show();
            }
        }));
    }

    private boolean isValidUsername(String username) {
        return username.matches("^[AKak]\\d{8}$");
    }

    private boolean isValidBirthdate(int year, int month, int day) {
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);
        Calendar today = Calendar.getInstance();
        return !selectedDate.after(today);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
