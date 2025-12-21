package com.casarick.app.controller;

import com.casarick.app.model.*;
import com.casarick.app.service.*;
import com.casarick.app.util.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SaleController {

    @FXML private Label lblUser, lblBranch, lblTotal, lblSubtotal, lblSelectedCustomer;
    @FXML private TextField txtSearchCustomer, txtSearchProduct;
    @FXML private TextField txtGlobalDescription; // Nueva descripción global
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableView<Inventory> tblInventory;
    @FXML private TableView<Sale> tblCart;

    @FXML private Button btnCompleteSale, btnNewCustomer, btnBack, btnSelectCustomer;

    // Columnas
    @FXML private TableColumn<Customer, String> colCustName, colCustLast, colCustPhone;
    @FXML private TableColumn<Inventory, String> colInvProd;
    @FXML private TableColumn<Inventory, Integer> colInvStock;
    @FXML private TableColumn<Inventory, Double> colInvPrice;

    // Columnas Carrito Actualizadas
    @FXML private TableColumn<Sale, String> colCartProd;
    @FXML private TableColumn<Sale, Integer> colCartQty;
    @FXML private TableColumn<Sale, Double> colCartDiscount; // Nueva
    @FXML private TableColumn<Sale, Double> colCartTotal;

    private final InventoryService inventoryService = new InventoryService();
    private final CustomerService customerService = new CustomerService();
    private final SaleService saleService = new SaleService();

    private ObservableList<Inventory> masterInventory = FXCollections.observableArrayList();
    private ObservableList<Customer> masterCustomers = FXCollections.observableArrayList();
    private ObservableList<Sale> cartItems = FXCollections.observableArrayList();

    private Customer selectedCustomer = null;

    @FXML
    public void initialize() {
        setupSessionInfo();
        setupTableColumns();
        loadInitialData();
        setupEvents();
        setupSearchFilters();

        btnSelectCustomer.setDisable(true); // Desactivado al inicio
        txtGlobalDescription.setText("Venta POS"); // Valor por defecto
    }

    private void setupSessionInfo() {
        User user = SessionManager.getInstance().getLoggedInUser();
        Branch branch = SessionManager.getInstance().getCurrentBranch();
        if (user != null) lblUser.setText("Usuario: " + user.getName());
        if (branch != null) lblBranch.setText("Sucursal: " + branch.getName());
    }

    private void setupTableColumns() {
        colCustName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCustLast.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colCustPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        colInvProd.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        colInvStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colInvPrice.setCellValueFactory(new PropertyValueFactory<>("salePrice"));

        // Configuración Carrito
        colCartProd.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInventoryDTO().getProduct().getName()));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCartDiscount.setCellValueFactory(new PropertyValueFactory<>("saleDiscount"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("saleTotal"));

        tblCart.setItems(cartItems);
    }

    /*private void setupEvents() {
        // Lógica de selección de cliente (Botón)
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnSelectCustomer.setDisable(newVal == null);
        });

        btnSelectCustomer.setOnAction(e -> {
            selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();
            lblSelectedCustomer.setText("Cliente: " + selectedCustomer.getName() + " " + selectedCustomer.getLastName());
            lblSelectedCustomer.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        });

        // Doble clic en inventario (Solo pide descuento)
        tblInventory.setRowFactory(tv -> {
            TableRow<Inventory> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    promptDiscountOnly(row.getItem());
                }
            });
            return row;
        });

        btnCompleteSale.setOnAction(e -> handleProcessSale());
        btnBack.setOnAction(e -> SceneSwitcher.switchScene((Stage) btnBack.getScene().getWindow(), "home-view.fxml"));
    }*/

    private void promptDetails(Inventory inv) {
        // 1. Pedir Descuento
        TextInputDialog discDialog = new TextInputDialog("0.0");
        discDialog.setTitle("Descuento");
        discDialog.setHeaderText("Producto: " + inv.getProduct().getName());
        discDialog.setContentText("Monto de descuento TOTAL para esta línea ($):");

        Optional<String> discResult = discDialog.showAndWait();
        if (discResult.isEmpty()) return;

        // 2. Pedir Cantidad
        TextInputDialog qtyDialog = new TextInputDialog("1");
        qtyDialog.setTitle("Cantidad");
        qtyDialog.setHeaderText("Stock disponible: " + inv.getStock());
        qtyDialog.setContentText("¿Cuántas unidades desea llevar?:");

        Optional<String> qtyResult = qtyDialog.showAndWait();
        if (qtyResult.isEmpty()) return;

        try {
            double totalDiscount = Double.parseDouble(discResult.get());
            int quantity = Integer.parseInt(qtyResult.get());

            // Subtotal para validar el descuento
            double subtotalLine = inv.getSalePrice() * quantity;

            // VALIDACIÓN CORREGIDA
            if (totalDiscount < 0 || totalDiscount >= subtotalLine) {
                showAlert("Error", "El descuento no puede ser mayor o igual al subtotal de la línea ($" + subtotalLine + ").");
                return;
            }
            if (quantity <= 0 || quantity > inv.getStock()) {
                showAlert("Error", "Cantidad no válida o insuficiente stock.");
                return;
            }

            addToCart(inv, totalDiscount, quantity);

        } catch (NumberFormatException e) {
            showAlert("Error", "Por favor, ingrese valores numéricos válidos.");
        }
    }

    private void addToCart(Inventory inv, double totalDiscount, int quantity) {
        Sale item = new Sale();
        item.setInventoryDTO(inv);
        item.setStock(quantity);

        // Guardamos el precio unitario base
        item.setSaleAmount(inv.getSalePrice());

        // Guardamos el descuento total para este ítem
        item.setSaleDiscount(totalDiscount);

        // CÁLCULO CORREGIDO:
        // (Precio Unitario * Cantidad) - Descuento Global del ítem
        double subtotalSinDescuento = inv.getSalePrice() * quantity;
        double totalConDescuento = subtotalSinDescuento - totalDiscount;

        item.setSaleTotal(totalConDescuento);

        cartItems.add(item);
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = cartItems.stream().mapToDouble(Sale::getSaleAmount).sum();
        double total = cartItems.stream().mapToDouble(Sale::getSaleTotal).sum();

        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblTotal.setText(String.format("$%.2f", total));
    }

    @FXML
    private void handleProcessSale() {
        if (selectedCustomer == null) {
            showAlert("Error", "Debe seleccionar un cliente usando el botón 'Seleccionar'.");
            return;
        }
        if (cartItems.isEmpty()) {
            showAlert("Error", "El carrito está vacío.");
            return;
        }

        String globalDesc = txtGlobalDescription.getText();
        List<Sale> confirmedSales = new ArrayList<>();

        for (Sale item : cartItems) {
            Inventory inv = item.getInventoryDTO();
            // Asignamos la descripción global aquí
            item.setDescription(globalDesc);

            boolean success = saleService.createSale(
                    selectedCustomer.getId(),
                    SessionManager.getInstance().getLoggedInUser().getId(),
                    SessionManager.getInstance().getCurrentBranch().getId(),
                    inv.getId(),
                    item.getStock(),
                    item.getSaleDiscount(),
                    globalDesc
            );

            if (success) {
                item.setInventoryDTO(inv);
                item.setCustomerDTO(selectedCustomer);
                item.setCreatedAt(LocalDateTime.now());
                confirmedSales.add(item);
            }
        }

        if (!confirmedSales.isEmpty()) {
            TicketPrinter.printInvoice(confirmedSales);
            showAlert("Éxito", "Venta realizada correctamente.");
            cartItems.clear();
            loadInitialData();
            updateTotals();
        }
    }

    private void loadInitialData() {
        Branch currentBranch = SessionManager.getInstance().getCurrentBranch();
        masterInventory.setAll(inventoryService.getWithStock().stream()
                .filter(i -> i.getBranch().getId().equals(currentBranch.getId()))
                .toList());
        tblInventory.setItems(masterInventory);
        masterCustomers.setAll(customerService.getAllCustomers());
        tblCustomers.setItems(masterCustomers);
    }

    private void setupSearchFilters() {
        FilteredList<Inventory> filteredData = new FilteredList<>(masterInventory, p -> true);
        txtSearchProduct.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(inv -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lower = newValue.toLowerCase();
                return inv.getProduct().getName().toLowerCase().contains(lower);
            });
        });
        tblInventory.setItems(filteredData);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupEvents() {
        // Lógica de selección de cliente (Botón) con confirmación
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            btnSelectCustomer.setDisable(newVal == null);
        });

        btnSelectCustomer.setOnAction(e -> {
            selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                lblSelectedCustomer.setText("Cliente: " + selectedCustomer.getName() + " " + selectedCustomer.getLastName());
                lblSelectedCustomer.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

                // Mensaje de éxito al asignar
                showAlert("Cliente Asignado", "Se ha vinculado a: " +
                        selectedCustomer.getName().toUpperCase() + " para esta venta.");
            }
        });

        // Doble clic en inventario (Pide descuento y luego cantidad)
        tblInventory.setRowFactory(tv -> {
            TableRow<Inventory> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    promptDetails(row.getItem());
                }
            });
            return row;
        });

        // ... (resto de eventos igual)
    }

    @FXML
    private void handleCreateNewCustomer() {
        SceneSwitcher.switchScene((Stage) btnNewCustomer.getScene().getWindow(), "create-customer-view.fxml");
    }

    @FXML
    private void handleBackAction() {
        Stage currentStage = (Stage) btnBack.getScene().getWindow();
        String nextViewFXML = "home-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
}