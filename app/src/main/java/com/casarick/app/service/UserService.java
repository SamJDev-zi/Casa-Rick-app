package com.casarick.app.service;

import com.casarick.app.model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {
    private final String URL_API = "http://72.62.105.222:8080/api/users";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray array = new JSONArray(response.body());
            for (int i = 0; i < array.length(); i++) {
                users.add(mapJsonToUser(array.getJSONObject(i)));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return users;
    }

    public User userLogin(String name, String lastName, String password) {
        try {
            JSONObject loginJson = new JSONObject()
                    .put("name", name)
                    .put("lastName", lastName)
                    .put("password", password);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(loginJson.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapJsonToUser(new JSONObject(response.body()));
            }
            return null; // 401 Unauthorized
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public User createNewUser(User user, String password) {
        try {
            JSONObject jsonRequest = mapUserToRequestJson(user, password);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapJsonToUser(new JSONObject(response.body()));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public User updateUser(Long id, User user, String password) {
        try {
            JSONObject jsonRequest = mapUserToRequestJson(user, password);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return mapJsonToUser(new JSONObject(response.body()));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void deleteUser(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/" + id)).DELETE().build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // --- MÉTODOS DE MAPEO ---

    private User mapJsonToUser(JSONObject json) {
        // Mapeo básico y Role
        Role role = null;
        if (json.has("role") && !json.isNull("role")) {
            JSONObject r = json.getJSONObject("role");
            role = new Role(r.getLong("id"), r.getString("name"));
        }

        User user = new User(
                json.getLong("id"),
                json.getString("name"),
                json.getString("lastName"),
                json.getString("phoneNumber"),
                role
        );

        // Mapeo de lista de Permisos (PermissionDTO -> Permission)
        if (json.has("permissions")) {
            JSONArray perms = json.getJSONArray("permissions");
            for (int i = 0; i < perms.length(); i++) {
                JSONObject p = perms.getJSONObject(i);
                user.getPermissionList().add(new Permission(p.getLong("id"), p.getString("name")));
            }
        }
        return user;
    }

    private JSONObject mapUserToRequestJson(User user, String password) {
        JSONObject json = new JSONObject();
        json.put("name", user.getName());
        json.put("lastName", user.getLastName());
        json.put("phoneNumber", user.getPhoneNumber());
        json.put("password", password);
        json.put("roleId", user.getRole() != null ? user.getRole().getId() : null);

        // Extraer solo los IDs de la lista de permisos para el RequestDTO
        List<Long> permIds = user.getPermissionList().stream()
                .map(Permission::getId)
                .collect(Collectors.toList());
        json.put("permissionsId", new JSONArray(permIds));

        return json;
    }
}