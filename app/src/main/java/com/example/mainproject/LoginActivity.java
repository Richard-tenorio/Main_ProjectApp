package com.example.mainproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText editUsername, editPassword;
    Button btnLogin, btnCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnLogin.setOnClickListener(view -> {
            String username = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (isValidUsername(username)) {
                HashMap<String, String> loginData = new HashMap<>();
                loginData.put("username", username);
                loginData.put("password", password);

                String url = "http://10.0.2.2/myapp/login.php";

                HttpRequestHelper.sendPost(url, loginData, result -> {
                    try {
                        Log.d("SERVER_RESPONSE", result); // Log full server response

                        if (result != null && !result.isEmpty()) {
                            JSONObject response = new JSONObject(result);

                            if (response.has("status") && response.getString("status").equalsIgnoreCase("success")) {
                                Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish(); // Close login screen
                            } else {
                                String message = response.has("message") ? response.getString("message") : "Login failed.";
                                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "No response from server", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e("LoginError", "Error parsing JSON: " + e.getMessage());
                        Log.e("LoginError", "Raw response: " + result); // 💡 This helps debugging malformed JSON
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "Invalid username format", Toast.LENGTH_SHORT).show();
            }
        });

        btnCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });
    }

    private boolean isValidUsername(String username) {
        if (username.length() != 9) return false;
        char firstChar = Character.toUpperCase(username.charAt(0));
        if (firstChar != 'A' && firstChar != 'K') return false;
        for (int i = 1; i < 9; i++) {
            if (!Character.isDigit(username.charAt(i))) return false;
        }
        return true;
    }
}
