package com.example.sobadguys;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonNumber;
import org.json.JSONException;

public class HttpParse {

    private static final String API_URL = "https://api.callfilter.app/apis/";
    private static final String API_KEY = "OlsiNTA0MjA4Mjk4MSJdiEFWIO69QP";
    private static final String MODE = "1";

    public String getDataFromJsonString(String jsonString) throws JSONException, IOException {
        Map<String, String> map = new HashMap<>();

        try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
            JsonObject jsonObject = reader.readObject();
            for (String key : jsonObject.keySet()) {
                JsonValue value = jsonObject.get(key);
                if (value instanceof JsonNumber) {
                    map.put(key, value.toString());
                }
                else if (value instanceof JsonString) {
                    map.put(key, ((JsonString)value).getString());
                }
                else {
                    throw new IllegalArgumentException("Неверный тип Json значения: " + value.getClass().getName());
                }
            }
        }
        return Objects.requireNonNull(map.getOrDefault("cat", ""));
    }

    public String executeGetRequest(String phoneNumber) throws IOException {
        URL url = new URL(API_URL + API_KEY + "/" + MODE + "/" + phoneNumber);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            int responseCode = urlConnection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                try (InputStream in = new BufferedInputStream(urlConnection.getInputStream())) {
                    String result = readStream(in);
                    return getDataFromJsonString(result);
                }
                catch (JSONException e) {
                    return "Json не распарсиля";
                }
            }
            else {
                return "Ошибка: HTTP error code: " + responseCode;
            }
        }
        catch (IOException e) {
            return "Ошибка: " + e.getMessage();
        }
        finally {
            urlConnection.disconnect();
        }
    }
    private String readStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString().trim();
    }
}
