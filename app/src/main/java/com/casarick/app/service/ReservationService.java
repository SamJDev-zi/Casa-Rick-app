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
import com.casarick.app.model.Customer;
import com.casarick.app.model.Inventory;
import com.casarick.app.model.Product;
import com.casarick.app.model.Reservation;

public class ReservationService {
    private final String URL_API = "http://72.62.105.222:8080/api/reservations";
    private final HttpClient client = HttpClient.newHttpClient();

    // 1. GET: Obtener todas las reservas
    public List<Reservation> getAllReservations() {
        return fetchListFromApi(URL_API);
    }

    // 2. GET: Obtener solo reservas pendientes (/pending)
    public List<Reservation> getReservationsPending() {
        return fetchListFromApi(URL_API + "/pending");
    }

    // 3. GET: Obtener reserva por ID (/{id})
    public Reservation getReservationById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapToReservation(new JSONObject(response.body()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 4. POST: Crear nueva reserva
    public boolean createReservation(Reservation reservation) {
        try {
            JSONObject json = new JSONObject()
                    .put("description", reservation.getDescription())
                    .put("deposit", reservation.getDeposit())
                    .put("balance", reservation.getBalance())
                    .put("stock", reservation.getStock())
                    .put("expiration", reservation.getExpiration() != null ? reservation.getExpiration().toString() : null)
                    .put("customerId", reservation.getCustomer().getId())
                    .put("userId", reservation.getUser().getId())
                    .put("branchId", reservation.getBranch().getId())
                    .put("inventoryId", reservation.getInventory().getId());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. PUT: Actualizar reserva (Estado y Balance)
    public boolean updateReservation(Long id, String status, Double balance, Double deposit, String description, int stock) {
        try {
            JSONObject json = new JSONObject()
                    .put("status", status)
                    .put("balance", balance)
                    .put("deposit", deposit)
                    .put("description", description)
                    .put("stock", stock);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Métodos Auxiliares ---

    private List<Reservation> fetchListFromApi(String url) {
        List<Reservation> reservations = new ArrayList<>();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONArray array = new JSONArray(response.body());
                for (int i = 0; i < array.length(); i++) {
                    reservations.add(mapToReservation(array.getJSONObject(i)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reservations;
    }

    private Reservation mapToReservation(JSONObject json) {
        Reservation res = new Reservation();
        try {
            res.setId(json.optLong("id"));
            res.setDescription(json.optString("description", ""));
            res.setDeposit(json.optDouble("deposit", 0.0));
            res.setBalance(json.optDouble("balance", 0.0));
            res.setStatus(json.optString("status", "PENDIENTE"));
            res.setStock(json.optInt("stock", 0));

            // Mapeo de fechas (Usa optString para evitar errores si vienen nulas)
            if (!json.isNull("created")) res.setCreated(LocalDateTime.parse(json.getString("created")));
            if (!json.isNull("expiration")) res.setExpiration(LocalDateTime.parse(json.getString("expiration")));

            // IMPORTANTE: Tu JSON usa "customerDTO", no "customer"
            if (!json.isNull("customerDTO")) {
                JSONObject cJson = json.getJSONObject("customerDTO");
                Customer c = new Customer();
                c.setId(cJson.getLong("id"));
                c.setName(cJson.getString("name"));
                c.setLastName(cJson.getString("lastName"));
                c.setPhoneNumber(cJson.getString("phoneNumber"));
                res.setCustomer(c); // Aquí lo asignas a tu modelo
            }

            // Tu JSON usa "branchDTO"
            if (!json.isNull("branchDTO")) {
                JSONObject bJson = json.getJSONObject("branchDTO");
                Branch b = new Branch();
                b.setId(bJson.getLong("id"));
                b.setName(bJson.getString("name"));
                res.setBranch(b);
            }

            // Tu JSON usa "inventoryDTO"
            if (!json.isNull("inventoryDTO")) {
                JSONObject iJson = json.getJSONObject("inventoryDTO");
                Inventory inv = new Inventory();
                inv.setId(iJson.getLong("id"));
                inv.setSalePrice(iJson.optDouble("salePrice", 0.0)); // Necesario para el cálculo del precio total

                if (!iJson.isNull("productDTO")) {
                    JSONObject pJson = iJson.getJSONObject("productDTO");
                    Product p = new Product();
                    p.setName(pJson.getString("name"));
                    inv.setProduct(p);
                }
                res.setInventory(inv);
            }

        } catch (Exception e) {
            System.err.println("Error detallado en mapeo: " + e.getMessage());
            e.printStackTrace();
        }
        return res;
    }
}