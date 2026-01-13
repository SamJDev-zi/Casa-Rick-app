package com.casarick.app.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.casarick.app.model.Branch;
import com.casarick.app.model.Category;
import com.casarick.app.model.Customer;
import com.casarick.app.model.Inventory;
import com.casarick.app.model.Product;
import com.casarick.app.model.Sale;
import com.casarick.app.model.User;

public class SaleService {
    private final String URL_API = "http://72.62.105.222:8080/api/sales";
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Sale> getAllSales() {
    List<Sale> sales = new ArrayList<>();

    try {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_API))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JSONArray array = new JSONArray(response.body());
            
            for (int i = 0; i < array.length(); i++) {
                sales.add(mapToSale(array.getJSONObject(i)));
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return sales;
}

    // POST: Realizar una venta
    public boolean createSale(Long customerId, Long userId, Long branchId, Long inventoryId, int quantity, Double discount, String desc) {
        JSONObject json = new JSONObject()
                .put("customerId", customerId)
                .put("userId", userId)
                .put("branchId", branchId)
                .put("InventoryId", inventoryId) // El backend espera "InventoryId" con I mayúscula según SaleRequestDTO
                .put("stock", quantity)
                .put("saleDiscount", discount)
                .put("description", desc);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 201;
        } catch (Exception e) { return false; }
    }

    // GET: Ventas por rango de fecha
    public List<Sale> getByRange(LocalDateTime start, LocalDateTime end) {
        List<Sale> sales = new ArrayList<>();
        String url = URL_API + "/by-range?start=" + start + "&end=" + end;

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONArray array = new JSONArray(response.body());
                for (int i = 0; i < array.length(); i++) {
                    sales.add(mapToSale(array.getJSONObject(i)));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sales;
    }

    private Sale mapToSale(JSONObject json) {
    Sale sale = new Sale();
    try {
        sale.setId(json.optLong("id", 0L));
        sale.setSaleTotal(json.optDouble("salePrice", json.optDouble("saleTotal", 0.0)));
        sale.setStock(json.optInt("stock", 0));
        sale.setSaleDiscount(json.optDouble("saleDiscount", 0.0)); 
        sale.setDescription(json.optString("description", "Venta de Inventario"));

        if (json.has("createdAt") && !json.isNull("createdAt")) {
            try {
                String dateStr = json.getString("createdAt");
                sale.setCreatedAt(LocalDateTime.parse(dateStr, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME).minusHours(4));
            } catch (Exception e) {
                sale.setCreatedAt(LocalDateTime.now());
            }
        } else {
            System.err.println("Campo 'createdAt' no encontrado, usando fecha actual");
            sale.setCreatedAt(LocalDateTime.now());
        }
        Product p = new Product();
        if (json.has("inventoryDTO") && !json.isNull("inventoryDTO")) {
            JSONObject invJson = json.getJSONObject("inventoryDTO");
            if (invJson.has("product") && !invJson.isNull("product")) {
                JSONObject pJson = invJson.getJSONObject("product");
                p.setId(pJson.optLong("id", 0L));
                p.setName(pJson.optString("name", "Producto sin nombre"));
                p.setBarCodeNumber(pJson.optString("barCodeNumber", ""));
                
                // También procesar categoría si existe
                if (pJson.has("categoryDTO") && !pJson.isNull("categoryDTO")) {
                    JSONObject catJson = pJson.getJSONObject("categoryDTO");
                    p.setCategory(new Category(catJson.optLong("id", 0L), catJson.optString("name", "")));
                }
            }
        } else if (json.has("productDTO") && !json.isNull("productDTO")) {
            // Fallback: por si acaso también viene como productDTO
            JSONObject pJson = json.getJSONObject("productDTO");
            p.setName(pJson.optString("name", "Producto sin nombre"));
        } else {
            p.setName("N/A");
        }

        Inventory inv = new Inventory();
        inv.setProduct(p);
        sale.setInventoryDTO(inv);

        // PROCESAMIENTO DE SUCURSAL
        if (json.has("branchDTO") && !json.isNull("branchDTO")) {
            JSONObject bJson = json.getJSONObject("branchDTO");
            Branch branch = new Branch();
            branch.setId(bJson.optLong("id", 0L));
            branch.setName(bJson.optString("name", "Sucursal"));
            sale.setBranchDTO(branch);
        }

        // PROCESAMIENTO DE USUARIO
        User user = new User();
        if (json.has("userDTO") && !json.isNull("userDTO")) {
            JSONObject uJson = json.getJSONObject("userDTO");
            user.setId(uJson.optLong("id", 0L));
            user.setName(uJson.optString("name", "Vendedor"));
            user.setLastName(uJson.optString("lastName", ""));
        } else {
            user.setName("Admin"); // Valor por defecto
        }
        sale.setUserDTO(user);

        // PROCESAMIENTO DE CLIENTE
        if (json.has("customerDTO") && !json.isNull("customerDTO")) {
            JSONObject cJson = json.getJSONObject("customerDTO");
            Customer customer = new Customer();
            customer.setId(cJson.optLong("id", 0L));
            customer.setName(cJson.optString("name", ""));
            customer.setLastName(cJson.optString("lastName", ""));
            customer.setPhoneNumber(cJson.optString("phoneNumber", ""));
            sale.setCustomerDTO(customer);
        }

    } catch (Exception e) {
        System.err.println("Error general en mapeo: " + e.getMessage());
        e.printStackTrace();
    }
    return sale;
}
}