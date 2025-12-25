package com.casarick.app.service;

import com.casarick.app.model.Role;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class RoleService {
    private final String URL_API = "http://72.62.105.222:8080/api/roles";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray array = new JSONArray(response.body());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    roles.add(new Role(obj.getLong("id"), obj.getString("name")));
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return roles;
    }

    public Role getRoleById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/" + id)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject obj = new JSONObject(response.body());
            return new Role(obj.getLong("id"), obj.getString("name"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public Role createNewRole(Role role) {
        try {
            JSONObject json = new JSONObject().put("name", role.getName());
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject obj = new JSONObject(response.body());
            return new Role(obj.getLong("id"), obj.getString("name"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public Role updateRole(Long id, String name) {
        try {
            // Actualización por URL: /api/roles/{id}/{name}
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id + "/" + name.replace(" ", "%20")))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject obj = new JSONObject(response.body());
            return new Role(obj.getLong("id"), obj.getString("name"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}