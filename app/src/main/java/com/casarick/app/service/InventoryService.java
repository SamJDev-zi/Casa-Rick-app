package com.casarick.app.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.casarick.app.model.Branch;
import com.casarick.app.model.Category;
import com.casarick.app.model.Industry;
import com.casarick.app.model.Inventory;
import com.casarick.app.model.Product;
import com.casarick.app.model.Type;

public class InventoryService {
    private final String URL_API = "http://72.62.105.222:8080/api/inventories";
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
            System.out.println("Fetching URL: " + url); // Debug
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Response Code: " + response.statusCode()); // Debug
            System.out.println("Response Body: " + response.body()); // Debug
            
            if (response.statusCode() == 200) {
                JSONArray array = new JSONArray(response.body());
                System.out.println("Number of items in JSON: " + array.length()); // Debug
                for (int i = 0; i < array.length(); i++) {
                    list.add(mapToInventory(array.getJSONObject(i)));
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
            System.err.println("Error in fetchList: " + e.getMessage());
        }
        return list;
    }

    private Inventory mapToInventory(JSONObject json) {
        Inventory inv = new Inventory();
        try {
            System.out.println("Mapping JSON: " + json.toString()); // Debug
            
            inv.setId(json.optLong("id", 0L));
            inv.setStock(json.optInt("stock", 0));
            inv.setCostPrice(json.optDouble("costPrice", 0.0));
            inv.setSalePrice(json.optDouble("salePrice", 0.0));

            // Mapear fechas: updated y created
            if (json.has("updated") && !json.isNull("updated")) {
                String updatedStr = json.getString("updated");
                System.out.println("Found 'updated' field: " + updatedStr); // Debug
                try {
                    // Intentar parsear como ISO_LOCAL_DATE_TIME (ej: "2025-12-29T21:16:41")
                    LocalDateTime updated = LocalDateTime.parse(updatedStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    inv.setUpdated(updated);
                    System.out.println("Successfully parsed 'updated': " + updated); // Debug
                } catch (Exception e) {
                    System.err.println("Error parsing 'updated': " + updatedStr + " - " + e.getMessage());
                    // Intentar otros formatos si el primero falla
                    try {
                        LocalDateTime updated = LocalDateTime.parse(updatedStr);
                        inv.setUpdated(updated);
                    } catch (Exception e2) {
                        System.err.println("Failed all parsing attempts for 'updated'");
                    }
                }
            } else {
                System.out.println("No 'updated' field found in JSON"); // Debug
            }

            if (json.has("created") && !json.isNull("created")) {
                String createdStr = json.getString("created");
                System.out.println("Found 'created' field: " + createdStr); // Debug
                try {
                    LocalDateTime created = LocalDateTime.parse(createdStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    inv.setCreated(created);
                    System.out.println("Successfully parsed 'created': " + created); // Debug
                } catch (Exception e) {
                    System.err.println("Error parsing 'created': " + createdStr + " - " + e.getMessage());
                    try {
                        LocalDateTime created = LocalDateTime.parse(createdStr);
                        inv.setCreated(created);
                    } catch (Exception e2) {
                        System.err.println("Failed all parsing attempts for 'created'");
                    }
                }
            } else {
                System.out.println("No 'created' field found in JSON"); // Debug
            }

            // 1. Mapear el Producto (Objeto anidado)
            if (json.has("productDTO") && !json.isNull("productDTO")) {
                JSONObject pJson = json.getJSONObject("productDTO");
                Product p = new Product();
                p.setId(pJson.optLong("id", 0L));
                p.setName(pJson.optString("name", "Sin nombre"));
                p.setSize(pJson.optString("size", ""));
                p.setBarCodeNumber(pJson.optString("barCodeNumber", ""));

                // Mapear Categoría dentro del Producto
                if (pJson.has("categoryDTO") && !pJson.isNull("categoryDTO")) {
                    JSONObject cJson = pJson.getJSONObject("categoryDTO");
                    p.setCategory(new Category(cJson.optLong("id", 0L), cJson.optString("name", "")));
                }

                // Mapear Tipo dentro del Producto
                if (pJson.has("typeDTO") && !pJson.isNull("typeDTO")) {
                    JSONObject tJson = pJson.getJSONObject("typeDTO");
                    p.setType(new Type(tJson.optLong("id", 0L), tJson.optString("name", "")));
                }

                // Mapear Industria dentro del Producto
                if (pJson.has("industryDTO") && !pJson.isNull("industryDTO")) {
                    JSONObject iJson = pJson.getJSONObject("industryDTO");
                    p.setIndustry(new Industry(iJson.optLong("id", 0L), iJson.optString("name", "")));
                }

                inv.setProduct(p);
                System.out.println("Product mapped: " + p.getName()); // Debug
            } else {
                System.out.println("No productDTO found"); // Debug
            }

            // 2. Mapear la Sucursal (Branch)
            if (json.has("branchDTO") && !json.isNull("branchDTO")) {
                JSONObject bJson = json.getJSONObject("branchDTO");
                Branch b = new Branch();
                b.setId(bJson.optLong("id", 0L));
                b.setName(bJson.optString("name", "Sin nombre"));
                b.setAddress(bJson.optString("address", ""));
                inv.setBranch(b);
                System.out.println("Branch mapped: " + b.getName()); // Debug
            } else {
                System.out.println("No branchDTO found"); // Debug
            }

        } catch (Exception e) {
            System.err.println("Error in mapToInventory: " + e.getMessage());
            e.printStackTrace();
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
        } catch (Exception e) { 
            e.printStackTrace();
            return false; 
        }
    }
    
    // Método adicional: Obtener inventarios por rango de fecha y sucursal
    public List<Inventory> getInventoriesByDateRange(Long branchId, LocalDateTime start, LocalDateTime end) {
        // Formatear las fechas para la URL
        String startStr = start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String endStr = end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        String url = String.format("%s/by-range?branchId=%d&start=%s&end=%s", 
            URL_API, branchId, startStr, endStr);
        
        System.out.println("Fetching by date range: " + url); // Debug
        
        return fetchList(url);
    }
    
    // Método adicional: Obtener inventarios por sucursal
    public List<Inventory> getInventoriesByBranch(Long branchId) {
        List<Inventory> allInventories = getAll();
        List<Inventory> filtered = new ArrayList<>();
        
        for (Inventory inv : allInventories) {
            if (inv.getBranch() != null && inv.getBranch().getId().equals(branchId)) {
                filtered.add(inv);
            }
        }
        
        System.out.println("Filtered by branch " + branchId + ": " + filtered.size() + " items"); // Debug
        return filtered;
    }
}