package com.casarick.app.service;

import com.casarick.app.model.Type;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TypeService {
    private final String URL_API = "http://72.62.105.222:8080/api/types";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Type> getAllTypes() {
        List<Type> list = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray array = new JSONArray(response.body());
            for (int i = 0; i < array.length(); i++) {
                list.add(new Type(array.getJSONObject(i).getLong("id"), array.getJSONObject(i).getString("name")));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return list;
    }

    public Type getTypeById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/" + id)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            return new Type(json.getLong("id"), json.getString("name"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public Optional<Type> getByName(String name) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/name/" + name.replace(" ", "%20"))).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                return Optional.of(new Type(json.getLong("id"), json.getString("name")));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    public Type createNewType(Type type) {
        try {
            JSONObject json = new JSONObject().put("name", type.getName());
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API)).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString())).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Type(new JSONObject(response.body()).getLong("id"), new JSONObject(response.body()).getString("name"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}