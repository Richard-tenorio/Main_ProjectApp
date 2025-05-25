package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ProfileActivity extends AppCompatActivity {

    TextView tvLastName, tvFirstName, tvMiddleName, tvAddress, tvCity,
            tvUsername, tvEmail, tvBirthdate;
    Button btnConfirm;

    SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Ensure this matches the XML file name

        // Initialize TextViews
        tvLastName = findViewById(R.id.tvLastName);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvMiddleName = findViewById(R.id.tvMiddleName);
        tvAddress = findViewById(R.id.tvAddress);
        tvCity = findViewById(R.id.tvCity);
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvBirthdate = findViewById(R.id.tvBirthdate);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Get username from SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");

        if (!TextUtils.isEmpty(username)) {
            fetchProfile(username);
        } else {
            Toast.makeText(this, "No username found", Toast.LENGTH_SHORT).show();
        }

        btnConfirm.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });
    }

    private void fetchProfile(String username) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection conn = null;
                BufferedReader reader = null;

                try {
                    String baseUrl = "http://10.0.2.2/myapp/profile.php";
                    String encodedUsername = URLEncoder.encode(username, "UTF-8");
                    URL url = new URL(baseUrl + "?username=" + encodedUsername);

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

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
                        return "{\"error\":\"Server error: " + responseCode + "\"}";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "{\"error\":\"Exception: " + e.getMessage() + "\"}";
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
                try {
                    JSONObject json = new JSONObject(result);

                    if (json.has("error")) {
                        Toast.makeText(ProfileActivity.this, json.getString("error"), Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Set the data to TextViews
                    tvLastName.setText(json.optString("lastname", ""));
                    tvFirstName.setText(json.optString("firstname", ""));
                    tvMiddleName.setText(json.optString("middlename", ""));
                    tvAddress.setText(json.optString("address", ""));
                    tvCity.setText(json.optString("city", ""));
                    tvUsername.setText(json.optString("username", ""));
                    tvEmail.setText(json.optString("email", ""));
                    tvBirthdate.setText(json.optString("birthdate", ""));

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ProfileActivity.this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}