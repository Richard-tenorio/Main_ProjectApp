package com.example.mainproject;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestHelper {

    public interface Callback {
        void onResponse(String result);
    }

    public static void sendPost(String urlString, HashMap<String, String> params, Callback callback) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Build POST data string
                    StringBuilder postData = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                        postData.append('=');
                        postData.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    }

                    // Write POST data
                    try (OutputStream os = connection.getOutputStream();
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                        writer.write(postData.toString());
                        writer.flush();
                    }

                    // Get response stream
                    int responseCode = connection.getResponseCode();
                    InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK)
                            ? connection.getInputStream()
                            : connection.getErrorStream();

                    // Read server response
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    return response.toString();

                } catch (Exception e) {
                    return "{\"status\":\"error\",\"message\":\"" + e.getMessage().replace("\"", "'") + "\"}";
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onResponse(result);
                    }
                });
            }
        }.execute();
    }
}
