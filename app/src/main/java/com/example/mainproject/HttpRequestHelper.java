package com.example.mainproject;

import android.os.AsyncTask;
import java.io.*;
import java.net.*;
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
                    // Create connection to the server
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setConnectTimeout(15000); // Connection timeout (15 seconds)
                    connection.setReadTimeout(15000); // Read timeout (15 seconds)
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Build POST data
                    StringBuilder postData = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                        postData.append('=');
                        postData.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    }

                    // Send data to the server
                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(postData.toString());
                    writer.flush();
                    writer.close();
                    os.close();

                    // Read response from the server
                    int responseCode = connection.getResponseCode();
                    InputStream inputStream = (responseCode == HttpURLConnection.HTTP_OK)
                            ? connection.getInputStream()
                            : connection.getErrorStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    return response.toString(); // Return server response

                } catch (Exception e) {
                    return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}"; // Error response
                } finally {
                    if (connection != null) {
                        connection.disconnect(); // Always disconnect the connection
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {
                callback.onResponse(result); // Pass the result back to the callback
            }
        }.execute(); // Execute AsyncTask
    }
}
