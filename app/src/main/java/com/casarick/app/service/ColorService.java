package com.casarick.app.service;

import com.casarick.app.model.Color;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ColorService {
    private final String URL_API = "http://localhost:8080/api/colors";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Color> getAllColors() {
        List<Color> list = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray array = new JSONArray(response.body());
                for (int i = 0; i < array.length(); i++) {
                    list.add(mapJsonToModel(array.getJSONObject(i)));
                }
            }
        } catch (IOException | InterruptedException e) { throw new RuntimeException(e); }
        return list;
    }

    public Color createNewColor(Color color) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", color.getName());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapJsonToModel(new JSONObject(response.body()));
        } catch (IOException | InterruptedException e) { throw new RuntimeException(e); }
    }

    private Color mapJsonToModel(JSONObject json) {
        return new Color(json.getLong("id"), json.getString("name"));
    }
}
