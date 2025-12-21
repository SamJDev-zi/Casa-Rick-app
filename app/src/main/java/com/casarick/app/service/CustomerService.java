package com.casarick.app.service;

import com.casarick.app.model.Customer;
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

public class CustomerService {
    private final String URL_API = "http://localhost:8080/api/customers";
    private final HttpClient client = HttpClient.newHttpClient();

    // 1. OBTENER TODOS LOS CLIENTES (GET)
    public List<Customer> getAllCustomers() {
        List<Customer> customerList = new ArrayList<>();
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
                    customerList.add(mapJsonToCustomer(jsonArray.getJSONObject(i)));
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return customerList;
    }

    // 2. OBTENER CLIENTE POR ID (GET /{id})
    public Customer getCustomerById(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapJsonToCustomer(new JSONObject(response.body()));
            } else {
                throw new RuntimeException("Cliente no encontrado con ID: " + id);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 3. OBTENER CLIENTE POR NOMBRE (GET /by-name/{name})
    public Optional<Customer> getCustomerByName(String name) {
        try {
            // Reemplazamos espacios por %20 para la URL
            String encodedName = name.replace(" ", "%20");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/by-name/" + encodedName))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 && !response.body().equals("null")) {
                return Optional.of(mapJsonToCustomer(new JSONObject(response.body())));
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    // 4. CREAR NUEVO CLIENTE (POST)
    public Customer createNewCustomer(Customer customer) {
        try {
            JSONObject jsonBody = mapCustomerToJson(customer);
            jsonBody.remove("id"); // Dejamos que la API genere el ID

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                return mapJsonToCustomer(new JSONObject(response.body()));
            } else {
                throw new RuntimeException("Error al crear cliente: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 5. ACTUALIZAR CLIENTE (PUT /{id})
    public Customer updateCustomer(Long id, Customer customer) {
        try {
            JSONObject jsonBody = mapCustomerToJson(customer);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return mapJsonToCustomer(new JSONObject(response.body()));
            } else {
                throw new RuntimeException("Error al actualizar cliente: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // 6. ELIMINAR CLIENTE (DELETE /{id})
    public void deleteCustomer(Long id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_API + "/" + id))
                    .header("Content-Type", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204 && response.statusCode() != 200) {
                throw new RuntimeException("Error al eliminar cliente: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // --- MÉTODOS DE MAPEO ---

    private Customer mapJsonToCustomer(JSONObject json) {
        return new Customer(
                json.getLong("id"),
                json.getString("name"),
                json.getString("lastName"),
                json.getString("phoneNumber")
        );
    }

    private JSONObject mapCustomerToJson(Customer customer) {
        JSONObject json = new JSONObject();
        json.put("id", customer.getId());
        json.put("name", customer.getName());
        json.put("lastName", customer.getLastName());
        json.put("phoneNumber", customer.getPhoneNumber());
        return json;
    }
}