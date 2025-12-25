package com.casarick.app.service;

import com.casarick.app.model.Category;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryService {
    private final String URL_API = "http://72.62.105.222:8080/api/categories";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
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

    public Category getCategoryById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/" + id)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? mapJsonToModel(new JSONObject(response.body())) : null;
        } catch (IOException | InterruptedException e) { throw new RuntimeException(e); }
    }

    public Optional<Category> getCategoryByName(String name) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/" + name.replace(" ", "%20"))).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && !response.body().equals("null")) {
                return Optional.of(mapJsonToModel(new JSONObject(response.body())));
            }
        } catch (IOException | InterruptedException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    public Category createNewCategory(Category category) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", category.getName());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapJsonToModel(new JSONObject(response.body()));
        } catch (IOException | InterruptedException e) { throw new RuntimeException(e); }
    }

    private Category mapJsonToModel(JSONObject json) {
        return new Category(json.getLong("id"), json.getString("name"));
    }
}