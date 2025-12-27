package com.casarick.app.controller;

import com.casarick.app.model.Inventory;
import com.casarick.app.service.InventoryService;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryController {

    @FXML
    private Button btnBack;
    @FXML
    private Button registerToday;

    @FXML private TableView<Inventory> inventoryTable;
    @FXML private TableColumn<Inventory, Long> colId;
    @FXML private TableColumn<Inventory, String> colProduct;
    @FXML private TableColumn<Inventory, String> colCategory;
    @FXML private TableColumn<Inventory, String> colType;
    @FXML private TableColumn<Inventory, String> colIndustry;
    @FXML private TableColumn<Inventory, Integer> colStock;
    @FXML private TableColumn<Inventory, String> number;
    @FXML private TableColumn<Inventory, String> codeBar;
    @FXML private TableColumn<Inventory, Double> colCost;
    @FXML private TableColumn<Inventory, Double> colSale;

    private final InventoryService inventoryService = new InventoryService();

    @FXML
    public void initialize() {
        configureTableColumns();
        loadInventoryData();
    }

    private void configureTableColumns() {
        // Mapeo de columnas simples
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colStock.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStock()));
        colCost.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCostPrice()));
        colSale.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSalePrice()));

        //number.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSalePrice()));
        //codeBar.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().ge));

        // Mapeo de columnas anidadas (Navegando por el objeto Product)
        colProduct.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct() != null ? data.getValue().getProduct().getName() : "N/A"));

        colCategory.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getCategory() != null
                        ? data.getValue().getProduct().getCategory().getName() : "N/A"));

        colType.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getType() != null
                        ? data.getValue().getProduct().getType().getName() : "N/A"));

        colIndustry.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getIndustry() != null
                        ? data.getValue().getProduct().getIndustry().getName() : "N/A"));

        number.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getBarCodeNumber() != null
                        ? data.getValue().getProduct().getCategory().getId() + "-" + data.getValue().getProduct().getId() : "N/A"));

        codeBar.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getBarCodeNumber() != null
                        ? data.getValue().getProduct().getBarCodeNumber() : "N/A"));
    }

    @FXML
    private void loadInventoryData() {
        // 1. Obtener todos los inventarios con stock > 0 desde la API
        List<Inventory> allWithStock = inventoryService.getWithStock();

        // 2. Obtener ID de la sucursal actual del SessionManager
        Long currentBranchId = SessionManager.getInstance().getCurrentBranch().getId();

        // 3. Filtrar por sucursal logueada
        List<Inventory> filteredList = allWithStock.stream()
                .filter(inv -> inv.getBranch() != null && inv.getBranch().getId().equals(currentBranchId))
                .collect(Collectors.toList());

        // 4. Cargar en la tabla
        ObservableList<Inventory> observableList = FXCollections.observableArrayList(filteredList);
        inventoryTable.setItems(observableList);
    }

    @FXML
    public void loadCreatedTodayInventories() {
        List<Inventory> inventoryList = inventoryService.getInventoriesByCreated(
                SessionManager.getInstance().getCurrentBranch().getId());

        ObservableList<Inventory> observableList = FXCollections.observableArrayList(inventoryList);
        inventoryTable.setItems(observableList);
    }

    @FXML
    void handleBack() {
        Stage currentStage = (Stage) btnBack.getScene().getWindow();
        String nextViewFXML = "home-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
}