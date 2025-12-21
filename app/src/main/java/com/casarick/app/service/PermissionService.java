package com.casarick.app.service;

import com.casarick.app.model.Permission;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PermissionService {
    private final String URL_API = "http://localhost:8080/api/permissions";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Permission> getAllPermissions() {
        List<Permission> list = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray array = new JSONArray(response.body());
            for (int i = 0; i < array.length(); i++) {
                list.add(new Permission(array.getJSONObject(i).getLong("id"), array.getJSONObject(i).getString("name")));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return list;
    }

    public Permission getPermissionById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/" + id)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            return new Permission(json.getLong("id"), json.getString("name"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public Optional<Permission> getPermissionByName(String name) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/" + name.replace(" ", "%20"))).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                return Optional.of(new Permission(json.getLong("id"), json.getString("name")));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    public Permission createNewPermission(Permission permission) {
        try {
            JSONObject json = new JSONObject().put("name", permission.getName());
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API)).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString())).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Permission(new JSONObject(response.body()).getLong("id"), new JSONObject(response.body()).getString("name"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public Permission updatePermission(Long id, String name) {
        try {
            // El controlador espera /{id}/{name}
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id + "/" + name.replace(" ", "%20")))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            return new Permission(json.getLong("id"), json.getString("name"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}