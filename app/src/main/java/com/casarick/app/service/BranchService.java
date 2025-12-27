package com.casarick.app.service;

import com.casarick.app.model.Branch;
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

public class BranchService {
    private final String URL_API = "http://localhost:8080/api/branches";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Branch> getAllBranches() {
        List<Branch> branchList = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                for (int i = 0; i < jsonArray.length(); i++) {
                    branchList.add(mapJsonToBranch(jsonArray.getJSONObject(i)));
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return branchList;
    }

    public Optional<Branch> getBranchByName(String name) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + name.replace(" ", "%20")))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && !response.body().equals("null")) {
                return Optional.of(mapJsonToBranch(new JSONObject(response.body())));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Branch createNewBranch(Branch branch) {
        try {
            JSONObject jsonBody = mapBranchToJson(branch);
            // Eliminamos el ID para que la API lo genere (o lo mandamos nulo)
            jsonBody.remove("id");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return mapJsonToBranch(new JSONObject(response.body()));
            } else {
                throw new RuntimeException("Error al crear sucursal: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Branch updateBranch(Long id, Branch branch) {
        try {
            JSONObject jsonBody = mapBranchToJson(branch);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapJsonToBranch(new JSONObject(response.body()));
            } else {
                throw new RuntimeException("Error al actualizar sucursal: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteBranch(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .header("Content-Type", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204 && response.statusCode() != 200) {
                throw new RuntimeException("Error al eliminar sucursal: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Branch mapJsonToBranch(JSONObject json) {
        return new Branch(
                json.getLong("id"),
                json.getString("name"),
                json.getString("address"),
                json.getString("phoneNumber"),
                json.optBoolean("isActive")
        );
    }

    private JSONObject mapBranchToJson(Branch branch) {
        JSONObject json = new JSONObject();
        json.put("id", branch.getId());
        json.put("name", branch.getName());
        json.put("address", branch.getAddress());
        json.put("phoneNumber", branch.getPhoneNumber());
        json.put("isActive", branch.isActive());
        return json;
    }
}