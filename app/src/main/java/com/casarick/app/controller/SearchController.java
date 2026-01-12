package com.casarick.app.controller;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.casarick.app.model.Branch;
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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class SearchController {

    @FXML private TextField searchNumber; // Este actuará como tu campo de búsqueda general
    @FXML private ComboBox<Category> categoryMenu;
    @FXML private ComboBox<Type> typeMenu;
    @FXML private ComboBox<Industry> industryMenu;

    @FXML private GridPane gridCards;

    @FXML private Button backButton;
    @FXML private Button clearButton;
    @FXML private Label resultsCountLabel; // Opcional, si deseas mostrar "X resultados encontrados"

    // Servicios
    private final InventoryService inventoryService = new InventoryService();
    private final CategoryService categoryService = new CategoryService();
    private final TypeService typeService = new TypeService();
    private final IndustryService industryService = new IndustryService();

    // Listas para el manejo de datos en memoria
    private ObservableList<Inventory> masterData = FXCollections.observableArrayList();
    private FilteredList<Inventory> filteredData;

    @FXML
    public void initialize() {
        // 1. Cargar Combos
        loadComboBoxes();

        // 2. Cargar Datos del Inventario (Filtrado por Sucursal)
        loadInventoryData();

        // 3. Configurar Listeners para búsqueda dinámica
        setupFiltering();
    }

    private void loadComboBoxes() {
        categoryMenu.getItems().add(new Category(-1L, "Todas")); // Opción por defecto
        categoryMenu.getItems().addAll(categoryService.getAllCategories());

        typeMenu.getItems().add(new Type(-1L, "Todos"));
        typeMenu.getItems().addAll(typeService.getAllTypes());

        industryMenu.getItems().add(new Industry(-1L, "Todas"));
        industryMenu.getItems().addAll(industryService.getAllIndustries());

        // Seleccionar opción por defecto
        categoryMenu.getSelectionModel().selectFirst();
        typeMenu.getSelectionModel().selectFirst();
        industryMenu.getSelectionModel().selectFirst();
    }

    private void loadInventoryData() {
        // Obtenemos la sucursal actual
        Branch currentBranch = SessionManager.getInstance().getCurrentBranch();
        if (currentBranch == null) return;

        // Traemos todo el stock
        List<Inventory> allStock = inventoryService.getWithStock();

        // Filtramos por la sucursal de la sesión
        List<Inventory> branchStock = allStock.stream()
                .filter(inv -> inv.getBranch() != null && inv.getBranch().getId().equals(currentBranch.getId()))
                .collect(Collectors.toList());

        // Inicializamos las listas
        masterData.setAll(branchStock);
        filteredData = new FilteredList<>(masterData, p -> true);

        // Renderizamos inicialmente
        renderGrid();
    }

    private void setupFiltering() {
        // Listener para el texto
        searchNumber.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Listeners para los combos
        categoryMenu.setOnAction(e -> applyFilters());
        typeMenu.setOnAction(e -> applyFilters());
        industryMenu.setOnAction(e -> applyFilters());

        // Botón limpiar
        clearButton.setOnAction(e -> clearFilters());
    }

    private void applyFilters() {
        filteredData.setPredicate(inv -> {
            Product p = inv.getProduct();
            if (p == null) return false;

            // 1. Filtro Texto (Nombre, Codigo, ID compuesto)
            String searchText = searchNumber.getText().toLowerCase().trim();
            if (!searchText.isEmpty()) {
                String fullName = p.getName().toLowerCase();
                String barcode = (p.getBarCodeNumber() != null) ? p.getBarCodeNumber().toLowerCase() : "";
                String composedId = p.getCategory().getId() + "-" + p.getId();

                // Si no coincide con ninguno, retorna falso
                if (!fullName.contains(searchText) && !barcode.contains(searchText) && !composedId.contains(searchText)) {
                    return false;
                }
            }

            // 2. Filtro Categoría
            Category selCat = categoryMenu.getValue();
            if (selCat != null && selCat.getId() != -1L) {
                if (p.getCategory() == null || !p.getCategory().getId().equals(selCat.getId())) return false;
            }

            // 3. Filtro Tipo
            Type selType = typeMenu.getValue();
            if (selType != null && selType.getId() != -1L) {
                if (p.getType() == null || !p.getType().getId().equals(selType.getId())) return false;
            }

            // 4. Filtro Industria
            Industry selInd = industryMenu.getValue();
            if (selInd != null && selInd.getId() != -1L) {
                if (p.getIndustry() == null || !p.getIndustry().getId().equals(selInd.getId())) return false;
            }

            return true; // Pasó todos los filtros
        });

        // Una vez filtrado, redibujamos el Grid
        renderGrid();
    }

    private void renderGrid() {
        gridCards.getChildren().clear(); // Limpiar grid anterior

        int column = 0;
        int row = 0;

        for (Inventory inv : filteredData) {
            // Creamos la tarjeta visual
            VBox card = createProductCard(inv);

            // Añadimos al grid en la posición calculada
            gridCards.add(card, column, row);

            column++;
            // Tu GridPane tiene 3 columnas (0, 1, 2)
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    /**
     * Crea una tarjeta VERTICAL diseñada para un Grid de 3 columnas
     */
    private VBox createProductCard(Inventory inv) {
        Product p = inv.getProduct();

        // --- CONTENEDOR PRINCIPAL ---
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.TOP_CENTER);
        // Estilo CSS inline para sombra y bordes (similar a tu HBox anterior)
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-border-radius: 10; " +
                "-fx-border-color: #ecf0f1; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Definimos un tamaño fijo o preferido para que todas sean iguales
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefWidth(Control.USE_COMPUTED_SIZE);

        // --- 1. IMAGEN ---
        ImageView imageView = new ImageView();
        String path = p.getPhotoUrl();

        // Carga de imagen segura (Tu lógica anterior)
        if (path != null && !path.trim().isEmpty()) {
            try {
                File file = new File(path);
                if (file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    imageView.setImage(img);
                } else {
                    // Puedes poner una imagen por defecto aquí
                }
            } catch (Exception e) {
                System.err.println("Error img: " + e.getMessage());
            }
        }
        imageView.setFitHeight(150); // Altura fija para uniformidad
        imageView.setFitWidth(200);
        imageView.setPreserveRatio(true);

        // --- 2. NOMBRE ---
        Label nameLbl = new Label(p.getName() != null ? p.getName().toUpperCase() : "SIN NOMBRE");
        nameLbl.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLbl.setWrapText(true);
        nameLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // --- 3. DETALLES (Categoría | Tipo) ---
        String detailsText = (p.getCategory() != null ? p.getCategory().getName() : "-") + " | " +
                (p.getType() != null ? p.getType().getName() : "-");
        Label detailsLbl = new Label(detailsText);
        detailsLbl.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        // --- 4. TALLA Y COLOR ---
        Label metaLbl = new Label("Talla: " + (p.getSize() != null ? p.getSize() : "U") +
                "  •  Color: " + (p.getColor() != null ? p.getColor().getName() : "-"));
        metaLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");

        // --- 5. PRECIO Y STOCK (Footer de la tarjeta) ---
        HBox footer = new HBox(20);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(10, 0, 0, 0));

        // Stock
        VBox stockBox = createMiniMetric("STOCK", String.valueOf(inv.getStock()), "#27ae60");
        // Precio
        VBox priceBox = createMiniMetric("PRECIO", String.format("Bs. %.2f", inv.getSalePrice()), "#2980b9");

        footer.getChildren().addAll(stockBox, priceBox);

        // Agregar todo a la tarjeta
        card.getChildren().addAll(imageView, nameLbl, detailsLbl, metaLbl, new Separator(), footer);

        return card;
    }

    // Helper pequeño para las métricas del footer
    private VBox createMiniMetric(String title, String value, String color) {
        VBox vb = new VBox(2);
        vb.setAlignment(Pos.CENTER);
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 9px; -fx-text-fill: #95a5a6;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        vb.getChildren().addAll(t, v);
        return vb;
    }

    @FXML
    private void clearFilters() {
        searchNumber.clear();
        categoryMenu.getSelectionModel().selectFirst();
        typeMenu.getSelectionModel().selectFirst();
        industryMenu.getSelectionModel().selectFirst();
        applyFilters();
    }

    @FXML
    private void handleSearchByNumber() {
        applyFilters();
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        SceneSwitcher.switchScene(stage, "home-view.fxml");
    }
}