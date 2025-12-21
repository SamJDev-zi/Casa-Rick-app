package com.casarick.app.service;

import com.casarick.app.model.*;
import com.casarick.app.util.BarcodeGenerator;
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

public class ProductService {
    private final String URL_API = "http://localhost:8080/api/products";
    private final HttpClient client = HttpClient.newHttpClient();

    // 1. OBTENER TODOS LOS PRODUCTOS (GET)
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray jsonArray = new JSONArray(response.body());
                for (int i = 0; i < jsonArray.length(); i++) {
                    productList.add(mapJsonToProduct(jsonArray.getJSONObject(i)));
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return productList;
    }

    // 2. OBTENER PRODUCTO POR ID Y CATEGORÍA (GET /{id}/{catId})
    public Optional<Product> getProductByIdAndCategory(Long id, Long categoryId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id + "/" + categoryId))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Optional.of(mapJsonToProduct(new JSONObject(response.body())));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    // 3. OBTENER PRODUCTO POR NOMBRE (GET /{name})
    public Optional<Product> getProductByName(String name) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + name.replace(" ", "%20")))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Optional.of(mapJsonToProduct(new JSONObject(response.body())));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    // 4. CREAR NUEVO PRODUCTO (POST)
    public Product createNewProduct(Product product) {
        try {
            // Generar código de barras si no tiene uno
            if (product.getBarCodeNumber() == null || product.getBarCodeNumber().isEmpty()) {
                product.setBarCodeNumber(BarcodeGenerator.generateUniqueBarcode());
            }

            JSONObject jsonRequest = mapProductToRequestJson(product);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return mapJsonToProduct(new JSONObject(response.body()));
            } else {
                throw new RuntimeException("Error al crear producto: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 5. ACTUALIZAR PRODUCTO (PUT /{id})
    public Product updateProduct(Long id, Product product) {
        try {
            JSONObject jsonRequest = mapProductToRequestJson(product);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonRequest.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapJsonToProduct(new JSONObject(response.body()));
            } else {
                throw new RuntimeException("Error al actualizar producto: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 6. ELIMINAR PRODUCTO (DELETE /{id})
    public void deleteProduct(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 204 && response.statusCode() != 200) {
                throw new RuntimeException("Error al eliminar producto");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // --- MÉTODOS DE MAPEO ---

    // Mapea el ResponseDTO de la API al modelo Product de tu App
    private Product mapJsonToProduct(JSONObject json) {
        Product p = new Product();
        p.setId(json.getLong("id"));
        p.setName(json.getString("name"));
        p.setColor(json.optString("color", ""));
        p.setSize(json.optString("size", ""));
        p.setPhotoUrl(json.getString("photoUrl"));
        p.setBarCodeNumber(json.optString("barCodeNumber", ""));

        // Mapeo de objetos anidados (Category, Type, Industry)
        if (json.has("categoryDTO")) {
            JSONObject cat = json.getJSONObject("categoryDTO");
            p.setCategory(new Category(cat.getLong("id"), cat.getString("name")));
        }
        if (json.has("typeDTO")) {
            JSONObject type = json.getJSONObject("typeDTO");
            p.setType(new Type(type.getLong("id"), type.getString("name")));
        }
        if (json.has("industryDTO")) {
            JSONObject ind = json.getJSONObject("industryDTO");
            p.setIndustry(new Industry(ind.getLong("id"), ind.getString("name")));
        }
        return p;
    }

    // Mapea el modelo Product al RequestDTO que espera la API (solo IDs)
    private JSONObject mapProductToRequestJson(Product p) {
        JSONObject json = new JSONObject();
        json.put("id", p.getId());
        json.put("name", p.getName());
        json.put("color", p.getColor());
        json.put("size", p.getSize());
        json.put("photoUrl", p.getPhotoUrl());
        json.put("barCodeNumber", p.getBarCodeNumber());

        // La API espera IDs planos según ProductRequestDTO.java
        json.put("categoryId", p.getCategory() != null ? p.getCategory().getId() : null);
        json.put("typeId", p.getType() != null ? p.getType().getId() : null);
        json.put("industryId", p.getIndustry() != null ? p.getIndustry().getId() : null);

        return json;
    }
}