package com.casarick.app.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.casarick.app.model.Category;
import com.casarick.app.model.Industry;
import com.casarick.app.model.Inventory;
import com.casarick.app.model.Product;
import com.casarick.app.model.Type;
import com.casarick.app.service.CategoryService;
import com.casarick.app.service.IndustryService;
import com.casarick.app.service.InventoryService;
import com.casarick.app.service.TypeService;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class InventoryController {

    @FXML
    private Button btnBack;
    @FXML
    private Button registerToday;

    @FXML
    private TableView<Inventory> inventoryTable;
    @FXML
    private TableColumn<Inventory, Long> colId;
    @FXML
    private TableColumn<Inventory, String> colProduct;
    @FXML
    private TableColumn<Inventory, String> colCategory;
    @FXML
    private TableColumn<Inventory, String> colType;
    @FXML
    private TableColumn<Inventory, String> colIndustry;
    @FXML
    private TableColumn<Inventory, Integer> colStock;
    @FXML
    private TableColumn<Inventory, String> number;
    @FXML
    private TableColumn<Inventory, String> codeBar;
    @FXML
    private TableColumn<Inventory, Double> colCost;
    @FXML
    private TableColumn<Inventory, Double> colSale;

    // realizado
    @FXML
    private ComboBox<Category> filterCategory;

    @FXML
    private ComboBox<Type> filterType;

    @FXML
    private ComboBox<Industry> filterIndustry;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnSearch;

    @FXML
    private Button btnClearFilters;

    @FXML
    private Label resultsCountLabel;

    // Servicios
    private final InventoryService inventoryService = new InventoryService();
    private final CategoryService categoryService = new CategoryService();
    private final TypeService typeService = new TypeService();
    private final IndustryService industryService = new IndustryService();
 
    // Listas para filtros
    private ObservableList<Inventory> originalInventoryList;
    private FilteredList<Inventory> filteredInventoryList;

    // Método para actualizar el contador de resultados
    private void updateResultsCount(int count) {
        resultsCountLabel.setText("Mostrando " + count + " resultados");
    }

    @FXML
    public void initialize() {
        configureTableColumns();
        loadComboBoxes();
        loadInventoryData();
        setupFiltering();
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
        colProduct.setCellValueFactory(data
                -> new SimpleStringProperty(data.getValue().getProduct() != null ? data.getValue().getProduct().getName() : "N/A"));

        colCategory.setCellValueFactory(data
                -> new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getCategory() != null
                        ? data.getValue().getProduct().getCategory().getName() : "N/A"));

        colType.setCellValueFactory(data
                -> new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getType() != null
                        ? data.getValue().getProduct().getType().getName() : "N/A"));

        colIndustry.setCellValueFactory(data
                -> new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getIndustry() != null
                        ? data.getValue().getProduct().getIndustry().getName() : "N/A"));

        number.setCellValueFactory(data
                -> new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getBarCodeNumber() != null
                        ? data.getValue().getProduct().getCategory().getId() + "-" + data.getValue().getProduct().getId() : "N/A"));

        codeBar.setCellValueFactory(data
                -> new SimpleStringProperty(data.getValue().getProduct() != null && data.getValue().getProduct().getBarCodeNumber() != null
                        ? data.getValue().getProduct().getBarCodeNumber() : "N/A"));
    }

    private void loadComboBoxes() {
        List<Category> allCategories = categoryService.getAllCategories();
        List<Type> allTypes = typeService.getAllTypes();
        List<Industry> allIndustries = industryService.getAllIndustries();

        filterCategory.getItems().setAll(allCategories);
        filterType.getItems().setAll(allTypes);
        filterIndustry.getItems().setAll(allIndustries);

        filterCategory.getItems().add(0, new Category(-1L, "Todas las categorías"));
        filterType.getItems().add(0, new Type(-1L, "Todos los tipos"));
        filterIndustry.getItems().add(0, new Industry(-1L, "Todas las industrias"));

        filterCategory.getSelectionModel().select(0);
        filterType.getSelectionModel().select(0);
        filterIndustry.getSelectionModel().select(0);
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

        // 4. Guardar la lista original y crear la lista filtrada
        originalInventoryList = FXCollections.observableArrayList(filteredList);
        filteredInventoryList = new FilteredList<>(originalInventoryList, p -> true);

        // 5. Configurar la tabla con la lista filtrada
        SortedList<Inventory> sortedList = new SortedList<>(filteredInventoryList);
        sortedList.comparatorProperty().bind(inventoryTable.comparatorProperty());
        inventoryTable.setItems(sortedList);

        // 6. Actualizar contador
        updateResultsCount(filteredInventoryList.size());
    }

    @FXML
    public void loadCreatedTodayInventories() {
        List<Inventory> inventoryList = inventoryService.getInventoriesByCreated(
                SessionManager.getInstance().getCurrentBranch().getId());

        ObservableList<Inventory> observableList = FXCollections.observableArrayList(inventoryList);
        inventoryTable.setItems(observableList);
    }

    private void setupFiltering() {
        filterCategory.setOnAction(e -> applyFilters());
        filterType.setOnAction(e -> applyFilters());
        filterIndustry.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters();
        });

        btnSearch.setOnAction(e -> applyFilters());
    }

    @FXML
    private void applyFilters() {
        filteredInventoryList.setPredicate(inventory -> {
            Product product = inventory.getProduct();
            if (product == null) {
                return false;
            }

            Category selectedCategory = filterCategory.getValue();
            if (selectedCategory != null && selectedCategory.getId() != -1L) {
                if (product.getCategory() == null
                        || !product.getCategory().getId().equals(selectedCategory.getId())) {
                    return false;
                }
            }

            Type selectedType = filterType.getValue();
            if (selectedType != null && selectedType.getId() != -1L) {
                if (product.getType() == null
                        || !product.getType().getId().equals(selectedType.getId())) {
                    return false;
                }
            }

            Industry selectedIndustry = filterIndustry.getValue();
            if (selectedIndustry != null && selectedIndustry.getId() != -1L) {
                if (product.getIndustry() == null
                        || !product.getIndustry().getId().equals(selectedIndustry.getId())) {
                    return false;
                }
            }

            String searchText = searchField.getText().toLowerCase();
            if (searchText != null && !searchText.isEmpty()) {
                boolean matchesName = product.getName().toLowerCase().contains(searchText);
                boolean matchesBarcode = product.getBarCodeNumber() != null
                        && product.getBarCodeNumber().toLowerCase().contains(searchText);
                boolean matchesCategory = product.getCategory() != null
                        && product.getCategory().getName().toLowerCase().contains(searchText);
                String productNumber = product.getCategory().getId() + "-" + product.getId();
                boolean matchesNumber = productNumber.contains(searchText);
                if (!matchesName && !matchesBarcode && !matchesCategory && !matchesNumber) {
                    return false;
                }
            }

            return true;
        });

        updateResultsCount(filteredInventoryList.size());
    }

    @FXML
    private void handleCategoryFilter() {
        applyFilters();
    }

    @FXML
    private void handleTypeFilter() {
        applyFilters();
    }

    @FXML
    private void handleIndustryFilter() {
        applyFilters();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        filterCategory.getSelectionModel().select(0);
        filterType.getSelectionModel().select(0);
        filterIndustry.getSelectionModel().select(0);
        searchField.clear();

        filteredInventoryList.setPredicate(null);
        updateResultsCount(originalInventoryList.size());
    }

    @FXML
    void handleBack() {
        Stage currentStage = (Stage) btnBack.getScene().getWindow();
        String nextViewFXML = "home-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
}
