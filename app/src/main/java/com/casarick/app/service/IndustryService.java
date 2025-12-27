package com.casarick.app.service;

import com.casarick.app.model.Industry;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IndustryService {
    private final String URL_API = "http://localhost:8080/api/industries";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Industry> getAllIndustries() {
        List<Industry> list = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray array = new JSONArray(response.body());
                for (int i = 0; i < array.length(); i++) {
                    list.add(new Industry(array.getJSONObject(i).getLong("id"), array.getJSONObject(i).getString("name")));
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return list;
    }

    public Industry getIndustryById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/" + id)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? new Industry(new JSONObject(response.body()).getLong("id"), new JSONObject(response.body()).getString("name")) : null;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public Optional<Industry> getIndustryByName(String name) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API + "/name/" + name.replace(" ", "%20"))).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                return Optional.of(new Industry(json.getLong("id"), json.getString("name")));
            }
        } catch (Exception e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    public Industry createNewIndustry(Industry industry) {
        try {
            JSONObject json = new JSONObject().put("name", industry.getName());
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL_API)).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString())).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new Industry(new JSONObject(response.body()).getLong("id"), new JSONObject(response.body()).getString("name"));
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}