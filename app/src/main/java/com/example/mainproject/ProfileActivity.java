package com.example.mainproject;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ProfileActivity extends AppCompatActivity {

    EditText etLastName, etFirstName, etMiddleName, etAddress, etCity,
            etUsername, etEmail, etPassword, etConfirmPassword;
    Button btnConfirm;

    SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

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

        loadProfileData();

        btnConfirm.setOnClickListener(v -> {
            if (validateInputs()) {
                saveProfileData();
                registerUser();
            }
        });
    }

    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(etLastName.getText()) ||
                TextUtils.isEmpty(etFirstName.getText()) ||
                TextUtils.isEmpty(etAddress.getText()) ||
                TextUtils.isEmpty(etCity.getText()) ||
                TextUtils.isEmpty(etUsername.getText()) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveProfileData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastName", etLastName.getText().toString());
        editor.putString("firstName", etFirstName.getText().toString());
        editor.putString("middleName", etMiddleName.getText().toString());
        editor.putString("address", etAddress.getText().toString());
        editor.putString("city", etCity.getText().toString());
        editor.putString("username", etUsername.getText().toString());
        editor.putString("email", etEmail.getText().toString());
        editor.putString("password", etPassword.getText().toString());
        editor.apply();
    }

    private void loadProfileData() {
        etLastName.setText(sharedPreferences.getString("lastName", ""));
        etFirstName.setText(sharedPreferences.getString("firstName", ""));
        etMiddleName.setText(sharedPreferences.getString("middleName", ""));
        etAddress.setText(sharedPreferences.getString("address", ""));
        etCity.setText(sharedPreferences.getString("city", ""));
        etUsername.setText(sharedPreferences.getString("username", ""));
        etEmail.setText(sharedPreferences.getString("email", ""));
        etPassword.setText(sharedPreferences.getString("password", ""));
        etConfirmPassword.setText(sharedPreferences.getString("password", ""));
    }

    private void registerUser() {
        String lastname = etLastName.getText().toString();
        String firstname = etFirstName.getText().toString();
        String middlename = etMiddleName.getText().toString();
        String address = etAddress.getText().toString();
        String city = etCity.getText().toString();
        String username = etUsername.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection conn = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL("http://10.0.2.2/myapp/register.php"); // for emulator; use actual IP if on device
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String postData = "lastname=" + URLEncoder.encode(lastname, "UTF-8") +
                            "&firstname=" + URLEncoder.encode(firstname, "UTF-8") +
                            "&middlename=" + URLEncoder.encode(middlename, "UTF-8") +
                            "&address=" + URLEncoder.encode(address, "UTF-8") +
                            "&city=" + URLEncoder.encode(city, "UTF-8") +
                            "&username=" + URLEncoder.encode(username, "UTF-8") +
                            "&email=" + URLEncoder.encode(email, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8");

                    OutputStream os = conn.getOutputStream();
                    os.write(postData.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        return result.toString();
                    } else {
                        return "Server error: " + responseCode;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return "Exception: " + e.getMessage();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ignored) {}
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        }.execute();
    }
}
