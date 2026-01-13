package com.casarick.app.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.casarick.app.model.Product;
import com.casarick.app.model.Sale;
import com.casarick.app.model.User;
import com.casarick.app.service.SaleService;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class SaleHistoryController {

    @FXML private DatePicker dpStart, dpEnd;
    @FXML private Button btnSearch, btnBack, btnToday;
    @FXML private TableView<Sale> tblSales;
    @FXML private TableColumn<Sale, Long> colId;
    @FXML private TableColumn<Sale, String> colDate, colProduct, colDescription, colUser;
    @FXML private TableColumn<Sale, Integer> colQty;
    @FXML private TableColumn<Sale, Double> colTotal;
    @FXML private Label lblBranch, lblTotalPeriodo;

    private final SaleService saleService = new SaleService();
    private final ObservableList<Sale> saleList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();

        if (SessionManager.getInstance().getCurrentBranch() != null) {
            lblBranch.setText("Sucursal: " + SessionManager.getInstance().getCurrentBranch().getName());
        }

        // Por defecto hoy
        dpStart.setValue(LocalDate.now());
        dpEnd.setValue(LocalDate.now());

        loadSales();

        btnSearch.setOnAction(e -> loadSales());

        btnToday.setOnAction(e -> {
            dpStart.setValue(LocalDate.now());
            dpEnd.setValue(LocalDate.now());
            loadSales();

            if (saleList.isEmpty()) {
                showAlert("Info", "No hay ventas hoy.");
            } else {
                showAlert("Éxito", "Cargadas " + saleList.size() + " ventas.");
            }
        });

        btnBack.setOnAction(e -> handleBack());
    }

    private void setupTable() {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colQty.setCellValueFactory(new PropertyValueFactory<>("stock"));
    colTotal.setCellValueFactory(new PropertyValueFactory<>("saleTotal"));
    colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    colDate.setCellValueFactory(cellData -> {
        LocalDateTime fecha = cellData.getValue().getCreatedAt();
        if (fecha != null) {
            return new SimpleStringProperty(fecha.format(formatter));
        } else {
            return new SimpleStringProperty("Sin fecha");
        }
    });
    
    colProduct.setCellValueFactory(cellData -> {
        Sale sale = cellData.getValue();
        if (sale.getInventoryDTO() != null && 
            sale.getInventoryDTO().getProduct() != null) {
            Product p = sale.getInventoryDTO().getProduct();
            return new SimpleStringProperty(p.getName() != null ? p.getName() : "Sin nombre");
        }
        return new SimpleStringProperty("N/A");
    });

    colUser.setCellValueFactory(cellData -> {
        if (cellData.getValue().getUserDTO() != null) {
            User user = cellData.getValue().getUserDTO();
            String nombre = user.getName() != null ? user.getName() : "";
            String apellido = user.getLastName() != null ? user.getLastName() : "";
            return new SimpleStringProperty(nombre + " " + apellido);
        }
        return new SimpleStringProperty("Admin");
    });

    tblSales.setItems(saleList);
}
    private void loadSales() {
    if (dpStart.getValue() == null || dpEnd.getValue() == null) {
        showAlert("Error", "Debe seleccionar un rango de fechas.");
        return;
    }

    LocalDateTime startRange = dpStart.getValue().atStartOfDay();
    LocalDateTime endRange = dpEnd.getValue().atTime(LocalTime.MAX);

    List<Sale> allSales = saleService.getAllSales();

    if (SessionManager.getInstance().getCurrentBranch() == null) return;
    Long currentBranchId = SessionManager.getInstance().getCurrentBranch().getId();

    List<Sale> filteredSales = allSales.stream()
            .filter(s -> {
                boolean sameBranch = s.getBranchDTO() != null &&
                        s.getBranchDTO().getId().equals(currentBranchId);

                boolean inRange = true;
                if (s.getCreatedAt() != null) {
                    inRange = !s.getCreatedAt().isBefore(startRange) &&
                            !s.getCreatedAt().isAfter(endRange);
                } else {
                    inRange = false; 
                }

                return sameBranch && inRange;
            })
            .toList();

    saleList.setAll(filteredSales);

    double total = filteredSales.stream()
            .mapToDouble(s -> s.getSaleTotal() != null ? s.getSaleTotal() : 0.0)
            .sum();

    lblTotalPeriodo.setText(String.format("$%.2f", total));
}

    private void handleBack() {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "home-view.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}