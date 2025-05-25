package com.example.mainproject;

import android.os.AsyncTask;
import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class PaymentHttpHelper {

    public interface Callback {
        void onResponse(String response);
    }

    @SuppressWarnings("deprecation")
    public static void sendPayment(String requestUrl, Map<String, String> postData, Callback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection conn = null;
                BufferedReader reader = null;

                try {
                    // Build POST data
                    StringBuilder postDataString = new StringBuilder();
                    for (Map.Entry<String, String> entry : postData.entrySet()) {
                        if (postDataString.length() != 0) postDataString.append('&');
                        postDataString.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                        postDataString.append('=');
                        postDataString.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    }

                    // Setup connection
                    URL url = new URL(requestUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Write POST data
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(postDataString.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    // Read response
                    int responseCode = conn.getResponseCode();
                    InputStream is = (responseCode >= 200 && responseCode < 400)
                            ? conn.getInputStream()
                            : conn.getErrorStream();

                    reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    return response.toString();

                } catch (Exception e) {
                    Log.e("PaymentHttpHelper", "Error: " + e.getMessage(), e);
                    return "error: " + e.getMessage();
                } finally {
                    try {
                        if (reader != null) reader.close();
                    } catch (IOException ignored) {}
                    if (conn != null) conn.disconnect();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (callback != null) {
                    callback.onResponse(result);
                }
            }
        }.execute();
    }
}
