package com.casarick.app.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.casarick.app.model.Inventory;
import com.casarick.app.service.InventoryService;
import com.casarick.app.util.LabelPrinter;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;

public class LabelController {

    @FXML private Button btnToday;
    @FXML private Button btnPrint;
    @FXML private Button btnSearch;
    @FXML private GridPane labelGrid;
    @FXML private DatePicker dpStart, dpEnd;
    @FXML private Spinner<Integer> spStart, spEnd;
    @FXML private Button buttonBack;

    private final InventoryService inventoryService = new InventoryService();
    private List<Inventory> displayList = new ArrayList<>();

    @FXML
    public void initialize() {
        initializeSpinners();
        
        btnToday.setOnAction(e -> loadTodayInventories());
        btnSearch.setOnAction(e -> loadInventoriesByRange());
        btnPrint.setOnAction(e -> printLabels());
        
        dpStart.setValue(LocalDate.now());
        dpEnd.setValue(LocalDate.now());
        
        loadTodayInventories();
    }

    private void initializeSpinners() {
        SpinnerValueFactory<Integer> startFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 0);
        SpinnerValueFactory<Integer> endFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 23);
        
        spStart.setValueFactory(startFactory);
        spEnd.setValueFactory(endFactory);
        spStart.setEditable(true);
        spEnd.setEditable(true);
        spStart.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        spEnd.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
    }

    private void loadTodayInventories() {
        dpStart.setValue(LocalDate.now());
        dpEnd.setValue(LocalDate.now());
        spStart.getValueFactory().setValue(0);
        spEnd.getValueFactory().setValue(23);
        
        loadInventoriesByRange();
    }

    private void loadInventoriesByRange() {
        if (dpStart.getValue() == null || dpEnd.getValue() == null) {
            showAlert("Error", "Por favor seleccione ambas fechas.");
            return;
        }
        if (dpStart.getValue().isAfter(dpEnd.getValue())) {
            showAlert("Error", "La fecha de inicio no puede ser mayor que la fecha de fin.");
            return;
        }
        int startHour = spStart.getValue();
        int endHour = spEnd.getValue();
        if (startHour < 0 || startHour > 23 || endHour < 0 || endHour > 23) {
            showAlert("Error", "Las horas deben estar entre 0 y 23.");
            return;
        }
        if (dpStart.getValue().isEqual(dpEnd.getValue()) && startHour > endHour) {
            showAlert("Error", "En el mismo día, la hora de inicio no puede ser mayor que la hora de fin.");
            return;
        }
        LocalDateTime startDateTime = dpStart.getValue().atTime(startHour, 0);
        LocalDateTime endDateTime = dpEnd.getValue().atTime(endHour, 59);
        labelGrid.getChildren().clear();
        labelGrid.setHgap(10);
        labelGrid.setVgap(10);
        labelGrid.setPadding(new javafx.geometry.Insets(10));
        
        displayList.clear();
        
        Long branchId = SessionManager.getInstance().getCurrentBranch().getId();
        
        List<Inventory> allInventories = inventoryService.getAll();
        
        int column = 0;
        int row = 0;
        int count = 0;
        
        for (Inventory inv : allInventories) {
            if (inv.getBranch() != null && inv.getBranch().getId().equals(branchId)) {
                if (inv.getUpdated() != null) {
                    LocalDateTime invDateTime = inv.getUpdated();
                    
                    boolean isInRange = !invDateTime.isBefore(startDateTime) && 
                                       !invDateTime.isAfter(endDateTime);
                    
                    if (isInRange) {
                        displayList.add(inv);
                        VBox card = createLabelCard(inv);
                        labelGrid.add(card, column, row);
                        
                        column++;
                        if (column == 2) {
                            column = 0;
                            row++;
                        }
                        count++;
                    }
                } else if (inv.getCreated() != null) {
                    LocalDateTime invDateTime = inv.getCreated();
                    boolean isInRange = !invDateTime.isBefore(startDateTime) && 
                                       !invDateTime.isAfter(endDateTime);
                    
                    if (isInRange) {
                        displayList.add(inv);
                        VBox card = createLabelCard(inv);
                        labelGrid.add(card, column, row);
                        
                        column++;
                        if (column == 2) {
                            column = 0;
                            row++;
                        }
                        count++;
                    }
                }
            }
        }
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String startDateStr = startDateTime.format(dateFormatter);
        String endDateStr = endDateTime.format(dateFormatter);
        String startTimeStr = startDateTime.format(timeFormatter);
        String endTimeStr = endDateTime.format(timeFormatter);
        if (count == 0) {
            showAlert("Información", "No se encontraron inventarios para el rango seleccionado.\n" +
                                   "Sucursal: " + SessionManager.getInstance().getCurrentBranch().getName() + "\n" +
                                   "Desde: " + startDateStr + " " + startTimeStr + "\n" +
                                   "Hasta: " + endDateStr + " " + endTimeStr);
        } else {
            showAlert("Éxito", "Se encontraron " + count + " inventarios.\n" +
                             "Sucursal: " + SessionManager.getInstance().getCurrentBranch().getName() + "\n" +
                             "Desde: " + startDateStr + " " + startTimeStr + "\n" +
                             "Hasta: " + endDateStr + " " + endTimeStr);
        }
    }

    private VBox createLabelCard(Inventory inv) {
        VBox card = new VBox(1);
        card.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        card.setMinWidth(200);
        card.setMaxWidth(200);
        card.setMinHeight(100);
        card.setMaxHeight(100);
        card.setStyle("-fx-border-color: #DCDCDC; -fx-padding: 8; -fx-background-color: white; " +
                "-fx-border-radius: 3; -fx-background-radius: 3;");

        // 1. IDs (Categoría - Producto)
        Label idLabel = new Label("ID: " + inv.getProduct().getCategory().getId() + "-" + inv.getProduct().getId());
        idLabel.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #666666;");

        // 2. Fecha de actualización o creación
        if (inv.getUpdated() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
            Label dateLabel = new Label("Actualizado: " + inv.getUpdated().format(formatter));
            dateLabel.setStyle("-fx-font-size: 8; -fx-text-fill: #777777;");
            card.getChildren().add(dateLabel);
        } else if (inv.getCreated() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
            Label dateLabel = new Label("Creado: " + inv.getCreated().format(formatter));
            dateLabel.setStyle("-fx-font-size: 8; -fx-text-fill: #777777;");
            card.getChildren().add(dateLabel);
        }

        // 3. Simulación de Código de Barras
        Label barcodeSim = new Label(inv.getProduct().getBarCodeNumber());
        barcodeSim.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 10; -fx-text-fill: #333333;");

        // 4. Nombre y Categoría
        String productName = inv.getProduct().getName() != null ? inv.getProduct().getName() : "Sin nombre";
        String categoryName = inv.getProduct().getCategory() != null ? inv.getProduct().getCategory().getName() : "Sin categoría";
        Label nameAndCategory = new Label(productName + " " + categoryName);
        nameAndCategory.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #000000;");

        // 5. Precio
        Label priceLabel = new Label("Bs." + inv.getSalePrice());
        priceLabel.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #000000;");

        // 6. Stock / Cantidad
        Label stockLabel = new Label("Copias: " + inv.getStock());
        stockLabel.setStyle("-fx-font-size: 8; -fx-text-fill: #777777; -fx-background-color: #f4f4f4; -fx-padding: 2;");

        // 7. Sucursal (opcional)
        if (inv.getBranch() != null) {
            Label branchLabel = new Label("Suc: " + inv.getBranch().getName());
            branchLabel.setStyle("-fx-font-size: 7; -fx-text-fill: #888888;");
            card.getChildren().add(branchLabel);
        }

        // Agregar todos los elementos
        card.getChildren().addAll(idLabel, barcodeSim, nameAndCategory, priceLabel, stockLabel);
        return card;
    }

    private void printLabels() {
        if (displayList.isEmpty()) {
            showAlert("Advertencia", "No hay etiquetas para imprimir.");
            return;
        }

        try {
            // Creamos una nueva lista temporal para "inflar" los datos
            List<Inventory> listaParaImprimir = new ArrayList<>();

            // CÁLCULO DE ETIQUETAS:
            // Una hoja Oficio es larga. Caben aprox 13 a 14 filas de 2cm.
            // 6 columnas x 13 filas = 78 etiquetas.
            // Vamos a poner 72 para dejar margen y asegurar que no se corte nada.
            int copiasPorHoja = 72;

            for (Inventory inv : displayList) {
                // AQUÍ ESTÁ LA CLAVE:
                // Repetimos el MISMO producto 72 veces en la lista
                for (int i = 0; i < copiasPorHoja; i++) {
                    listaParaImprimir.add(inv);
                }
            }

            // Enviamos la lista "inflada" (con cientos de registros) al reporte
            LabelPrinter dataSource = new LabelPrinter(listaParaImprimir);

            java.io.InputStream reportStream = getClass().getResourceAsStream("/reports/etiquetas_casa_rick.jasper");

            if (reportStream == null) {
                System.out.println("Error: No se encuentra el archivo .jasper");
                return;
            }

            JasperPrint print = JasperFillManager.fillReport(reportStream, null, dataSource);

            JasperPrint print = JasperFillManager.fillReport(report, null, dataSource);
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Impresión");
            alert.setHeaderText("Preparando para imprimir");
            alert.setContentText("Se imprimirán " + displayList.size() + " etiquetas.\n¿Desea continuar?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response.getButtonData().isDefaultButton()) {
                    try {
                        JasperPrintManager.printReport(print, true);
                        showAlert("Éxito", "Impresión enviada a la impresora.");
                    } catch (JRException e) {
                        e.printStackTrace();
                        showAlert("Error", "Error al imprimir: " + e.getMessage());
                    }
                }
            });

        } catch (JRException e) {
            e.printStackTrace();
            showAlert("Error", "Error al generar la impresión: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Error inesperado: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void buttonBack() {
        Stage currentStage = (Stage) buttonBack.getScene().getWindow();
        String nextViewFXML = "home-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
}