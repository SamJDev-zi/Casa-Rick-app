package com.casarick.app.controller;

import com.casarick.app.model.Inventory;
import com.casarick.app.model.Product;
import com.casarick.app.service.InventoryService;
import com.casarick.app.service.ProductService;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class SearchController {

    @FXML private TextField searchByName, searchByNumber;
    @FXML private Button buttonBack;
    @FXML private Pane viewProduct;

    private final ProductService productService = new ProductService();
    private final InventoryService inventoryService = new InventoryService();

    @FXML
    public void initialize() {
        // Configurar búsquedas al presionar Enter
        searchByName.setOnAction(e -> handleSearchByName());
        searchByNumber.setOnAction(e -> handleSearchByNumber());

        // Configurar botón volver
        buttonBack.setOnAction(e -> handleBack());
    }

    private void handleSearchByName() {
        String name = searchByName.getText().trim();
        if (!name.isEmpty()) {
            processResult(productService.getProductByName(name));
        }
    }

    private void handleSearchByNumber() {
        String input = searchByNumber.getText().trim();
        if (input.contains("-")) {
            try {
                String[] parts = input.split("-");
                Long catId = Long.parseLong(parts[0]);
                Long prodId = Long.parseLong(parts[1]);
                processResult(productService.getProductByIdAndCategory(prodId, catId));
            } catch (Exception e) {
                showError("Formato inválido. Use: CATEGORIA-PRODUCTO (ej. 1-1)");
            }
        }
    }

    private void processResult(Optional<Product> productOpt) {
        viewProduct.getChildren().clear();

        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            // Validar que exista una sesión de sucursal
            if (SessionManager.getInstance().getCurrentBranch() == null) {
                showError("Error: No se ha detectado una sucursal activa en la sesión.");
                return;
            }

            Long branchId = SessionManager.getInstance().getCurrentBranch().getId();

            // Buscar el inventario que coincida con este producto y esta sucursal
            Optional<Inventory> invOpt = inventoryService.getWithStock().stream()
                    .filter(inv -> inv.getProduct() != null &&
                            inv.getProduct().getId().equals(product.getId()) &&
                            inv.getBranch() != null &&
                            inv.getBranch().getId().equals(branchId))
                    .findFirst();

            if (invOpt.isPresent()) {
                displayProductCard(invOpt.get());
            } else {
                showError("Producto '" + product.getName() + "' encontrado, pero no tiene inventario en esta sucursal.");
            }
        } else {
            showError("No se encontró ningún producto con los datos proporcionados.");
        }
    }

    private void displayProductCard(Inventory inv) {
        Product p = inv.getProduct();
        if (p == null) return;

        // Contenedor de la Tarjeta
        HBox card = new HBox(30);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 15, 0, 0, 0);");
        card.setPrefSize(820, 320);
        card.setLayoutX(40);
        card.setLayoutY(30);

        // --- SECCIÓN IZQUIERDA: IMAGEN ---
        ImageView imageView = new ImageView();
        String path = p.getPhotoUrl();

        if (path != null && !path.trim().isEmpty()) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    // Usamos toURI para manejar correctamente rutas de Windows con espacios y caracteres especiales
                    Image img = new Image(file.toURI().toString());
                    System.out.println(path);
                    imageView.setImage(img);
                } else {
                    System.err.println("Imagen no encontrada físicamente en: " + path);
                }
            } catch (Exception e) {
                System.err.println("Error al cargar la imagen: " + e.getMessage());
            }
        }

        imageView.setFitWidth(260);
        imageView.setFitHeight(260);
        imageView.setPreserveRatio(true);

        // --- SECCIÓN DERECHA: DATOS ---
        VBox info = new VBox(12);
        info.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(p.getName() != null ? p.getName().toUpperCase() : "PRODUCTO DESCONOCIDO");
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 26));

        String catName = (p.getCategory() != null) ? p.getCategory().getName() : "Sin Categoría";
        String typeName = (p.getType() != null) ? p.getType().getName() : "Sin Tipo";
        String indName = (p.getIndustry() != null) ? p.getIndustry().getName() : "Sin Industria";

        Label subDetails = new Label(catName + " | " + typeName + " | " + indName);
        subDetails.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 15px;");

        Label colorSizeLbl = new Label(String.format("🎨 Color: %s   📏 Talla: %s",
                (p.getColor() == null ? "N/A" : p.getColor()),
                (p.getSize() == null ? "N/A" : p.getSize())));
        colorSizeLbl.setFont(Font.font("System", 17));

        // Contenedor de Stock y Precio
        HBox metrics = new HBox(35);
        metrics.setPadding(new Insets(15, 0, 0, 0));

        VBox stockBox = createMetricVBox("EXISTENCIAS", String.valueOf(inv.getStock()), "#27ae60");
        VBox priceBox = createMetricVBox("PRECIO PÚBLICO", String.format("$%.2f", inv.getSalePrice()), "#2980b9");

        metrics.getChildren().addAll(stockBox, priceBox);
        info.getChildren().addAll(nameLbl, subDetails, colorSizeLbl, metrics);

        card.getChildren().addAll(imageView, info);
        viewProduct.getChildren().add(card);
    }

    private VBox createMetricVBox(String title, String value, String hexColor) {
        VBox vBox = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #95a5a6; -fx-font-weight: bold;");

        Label valueLbl = new Label(value);
        valueLbl.setFont(Font.font("System", FontWeight.BLACK, 28));
        valueLbl.setStyle("-fx-text-fill: " + hexColor + ";");

        vBox.getChildren().addAll(titleLbl, valueLbl);
        return vBox;
    }

    private void handleBack() {
        Stage stage = (Stage) buttonBack.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "home-view.fxml");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Resultado de Búsqueda");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}