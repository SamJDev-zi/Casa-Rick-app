package com.casarick.app.service;

import com.casarick.app.model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private final String URL_API = "http://localhost:8080/api/inventories";
    private final HttpClient client = HttpClient.newHttpClient();

    // GET: Obtener todos los inventarios
    public List<Inventory> getAll() {
        return fetchList(URL_API);
    }

    // GET: Solo productos con existencias (usado en Ventas)
    public List<Inventory> getWithStock() {
        return fetchList(URL_API + "/stock");
    }

    public List<Inventory> getInventoriesByCreated(Long id) {
        // Enviamos solo la fecha yYYY-MM-DD
        String dateStr = LocalDate.now().atStartOfDay().toString();

        return fetchList(URL_API + "/by-created/"+ dateStr + "/" + id);
    }

    // POST: Crear nuevo registro de inventario
    public boolean create(Long productId, Long branchId, Double cost, Double sale, int stock) {
        JSONObject json = new JSONObject()
                .put("productId", productId)
                .put("branchId", branchId)
                .put("costPrice", cost)
                .put("salePrice", sale)
                .put("stock", stock);

        return sendRequest("POST", URL_API, json);
    }

    private List<Inventory> fetchList(String url) {
        List<Inventory> list = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray array = new JSONArray(response.body());
                for (int i = 0; i < array.length(); i++) {
                    list.add(mapToInventory(array.getJSONObject(i)));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private Inventory mapToInventory(JSONObject json) {
        Inventory inv = new Inventory();
        inv.setId(json.getLong("id"));
        inv.setStock(json.getInt("stock"));
        inv.setCostPrice(json.getDouble("costPrice"));
        inv.setSalePrice(json.getDouble("salePrice"));

        // 1. Mapear el Producto (Objeto anidado)
        if (json.has("productDTO") && !json.isNull("productDTO")) {
            JSONObject pJson = json.getJSONObject("productDTO");
            Product p = new Product();
            p.setId(pJson.getLong("id"));
            p.setName(pJson.getString("name"));
            p.setSize(pJson.optString("size", ""));
            p.setBarCodeNumber(pJson.optString("barCodeNumber", ""));

            // Mapear Categoría dentro del Producto
            if (pJson.has("categoryDTO") && !pJson.isNull("categoryDTO")) {
                JSONObject cJson = pJson.getJSONObject("categoryDTO");
                p.setCategory(new Category(cJson.getLong("id"), cJson.getString("name")));
            }

            // Mapear Tipo dentro del Producto
            if (pJson.has("typeDTO") && !pJson.isNull("typeDTO")) {
                JSONObject tJson = pJson.getJSONObject("typeDTO");
                p.setType(new Type(tJson.getLong("id"), tJson.getString("name")));
            }

            // Mapear Industria dentro del Producto
            if (pJson.has("industryDTO") && !pJson.isNull("industryDTO")) {
                JSONObject iJson = pJson.getJSONObject("industryDTO");
                p.setIndustry(new Industry(iJson.getLong("id"), iJson.getString("name")));
            }

            inv.setProduct(p);
        }

        // 2. Mapear la Sucursal (Branch)
        if (json.has("branchDTO") && !json.isNull("branchDTO")) {
            JSONObject bJson = json.getJSONObject("branchDTO");
            Branch b = new Branch();
            b.setId(bJson.getLong("id"));
            b.setName(bJson.getString("name"));
            b.setAddress(bJson.optString("address", ""));
            inv.setBranch(b);
        }

        return inv;
    }

    private boolean sendRequest(String method, String url, JSONObject body) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() < 300;
        } catch (Exception e) { return false; }
    }
}