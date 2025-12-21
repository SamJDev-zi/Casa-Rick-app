package com.casarick.app.controller;

import com.casarick.app.model.*;
import com.casarick.app.service.*;
import com.casarick.app.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservationController {

    @FXML private Label lblUser, lblBranch, lblSelectedCustomer, lblSelectedProduct;
    @FXML private TextField txtSearchCustomer, txtSearchProduct, txtQuantity, txtDeposit, txtTotalPrice, txtBalance;
    @FXML private TextArea txtDescription;
    @FXML private DatePicker dpExpiration;
    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableView<Inventory> tblInventory;
    @FXML private TableColumn<Customer, String> colCustName, colCustLast, colCustPhone;
    @FXML private TableColumn<Inventory, String> colInvProd;
    @FXML private TableColumn<Inventory, Integer> colInvStock;
    @FXML private TableColumn<Inventory, Double> colInvPrice;
    @FXML private Button btnSelectCustomer, btnBack;

    private final CustomerService customerService = new CustomerService();
    private final InventoryService inventoryService = new InventoryService();
    private final ReservationService reservationService = new ReservationService();

    private ObservableList<Customer> masterCustomers = FXCollections.observableArrayList();
    private ObservableList<Inventory> masterInventory = FXCollections.observableArrayList();

    private Customer selectedCustomer = null;
    private Inventory selectedInventory = null;

    @FXML
    public void initialize() {
        setupSessionInfo();
        setupTableColumns();
        loadInitialData();
        setupSearchFilters();
        setupAutoCalculations();

        // Evento de selección de producto (Doble clic)
        tblInventory.setRowFactory(tv -> {
            TableRow<Inventory> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    selectProduct(row.getItem());
                }
            });
            return row;
        });

        btnSelectCustomer.setDisable(true);
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> btnSelectCustomer.setDisable(newVal == null));

        btnSelectCustomer.setOnAction(e -> {
            selectedCustomer = tblCustomers.getSelectionModel().getSelectedItem();
            lblSelectedCustomer.setText("Cliente: " + selectedCustomer.getName() + " " + selectedCustomer.getLastName());
            lblSelectedCustomer.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        });
    }

    private void setupSessionInfo() {
        User user = SessionManager.getInstance().getLoggedInUser();
        Branch branch = SessionManager.getInstance().getCurrentBranch();
        lblUser.setText("Usuario: " + (user != null ? user.getName() : "N/A"));
        lblBranch.setText("Sucursal: " + (branch != null ? branch.getName() : "N/A"));
    }

    private void setupTableColumns() {
        colCustName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCustLast.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colCustPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        colInvProd.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        colInvStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colInvPrice.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
    }

    private void selectProduct(Inventory inv) {
        selectedInventory = inv;
        lblSelectedProduct.setText(inv.getProduct().getName() + " ($" + inv.getSalePrice() + ")");
        txtQuantity.setText("1");
        calculateTotals();
    }

    private void setupAutoCalculations() {
        txtQuantity.textProperty().addListener((o, old, newVal) -> calculateTotals());
        txtDeposit.textProperty().addListener((o, old, newVal) -> calculateTotals());
    }

    private void calculateTotals() {
        if (selectedInventory == null) return;
        try {
            int qty = txtQuantity.getText().isEmpty() ? 0 : Integer.parseInt(txtQuantity.getText());
            double deposit = txtDeposit.getText().isEmpty() ? 0.0 : Double.parseDouble(txtDeposit.getText());

            double total = selectedInventory.getSalePrice() * qty;
            double balance = total - deposit;

            txtTotalPrice.setText(String.format("%.2f", total));
            txtBalance.setText(String.format("%.2f", balance));
        } catch (NumberFormatException e) {
            txtBalance.setText("Error");
        }
    }

    @FXML
    private void handleSaveReservation() {
        if (selectedCustomer == null || selectedInventory == null || dpExpiration.getValue() == null) {
            showAlert("Error", "Debe seleccionar cliente, producto y fecha de expiración.");
            return;
        }

        try {
            // 1. Limpiamos y convertimos la cantidad
            int qty = Integer.parseInt(txtQuantity.getText().trim());

            // 2. Limpiamos el depósito (quitamos espacios y cambiamos coma por punto)
            String depositText = txtDeposit.getText().trim().replace(",", ".");
            double deposit = Double.parseDouble(depositText);

            // 3. NO leas del TextField el balance, calcúlalo de nuevo para mayor precisión
            double unitPrice = selectedInventory.getSalePrice();
            double total = unitPrice * qty;
            double balance = total - deposit;

            if (qty > selectedInventory.getStock()) {
                showAlert("Error", "No hay stock suficiente.");
                return;
            }

            Reservation res = new Reservation();

            if (SessionManager.getInstance().getLoggedInUser() != null && SessionManager.getInstance().getCurrentBranch() != null) {
                res.setCustomer(selectedCustomer);
                res.setInventory(selectedInventory);
                res.setUser(SessionManager.getInstance().getLoggedInUser());
                res.setBranch(SessionManager.getInstance().getCurrentBranch());
                res.setStock(qty);
                res.setDeposit(deposit);
                res.setBalance(balance);
                res.setDescription(txtDescription.getText());
                res.setExpiration(LocalDateTime.of(dpExpiration.getValue(), LocalTime.MIDNIGHT));
                res.setStatus("PENDIENTE");
            }


            if (reservationService.createReservation(res)) {
                showAlert("Éxito", "Reserva creada correctamente.");
                handleBackAction();
            } else {
                showAlert("Error", "No se pudo crear la reserva en el servidor.");
            }

        } catch (Exception e) {
            System.out.println(e);
            showAlert("Error", "Revise los montos ingresados.");
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
        FilteredList<Customer> filteredCust = new FilteredList<>(masterCustomers, p -> true);
        txtSearchCustomer.textProperty().addListener((obs, old, newVal) -> {
            filteredCust.setPredicate(c -> newVal == null || newVal.isEmpty() ||
                    c.getName().toLowerCase().contains(newVal.toLowerCase()) ||
                    c.getLastName().toLowerCase().contains(newVal.toLowerCase()));
        });
        tblCustomers.setItems(filteredCust);

        FilteredList<Inventory> filteredInv = new FilteredList<>(masterInventory, p -> true);
        txtSearchProduct.textProperty().addListener((obs, old, newVal) -> {
            filteredInv.setPredicate(i -> newVal == null || newVal.isEmpty() ||
                    i.getProduct().getName().toLowerCase().contains(newVal.toLowerCase()));
        });
        tblInventory.setItems(filteredInv);
    }

    @FXML
    private void handleBackAction() {
        SceneSwitcher.switchScene((Stage) btnBack.getScene().getWindow(), "home-view.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}