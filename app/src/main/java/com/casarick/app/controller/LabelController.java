package com.casarick.app.controller;

import com.casarick.app.model.Inventory;
import com.casarick.app.service.InventoryService;
import com.casarick.app.util.LabelPrinter;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.*;
import java.util.ArrayList;
import java.util.List;

public class LabelController {

    @FXML private Button btnToday;
    @FXML private Button btnPrint;
    @FXML private GridPane labelGrid;
    @FXML
    private Button buttonBack;

    private final InventoryService inventoryService = new InventoryService();
    // Esta lista guardará los inventarios únicos para mostrarlos en la UI
    private List<Inventory> displayList = new ArrayList<>();

    @FXML
    public void initialize() {
        btnToday.setOnAction(e -> loadTodayInventories());
        btnPrint.setOnAction(e -> printLabels());
    }

    private void loadTodayInventories() {
        labelGrid.setHgap(10); // Espacio horizontal entre tarjetas
        labelGrid.setVgap(10); // Espacio vertical entre tarjetas
        labelGrid.setPadding(new javafx.geometry.Insets(10)); // Margen interno

        labelGrid.getChildren().clear();
        displayList.clear();

        Long branchId = SessionManager.getInstance().getCurrentBranch().getId();
        // Llamada a tu API ya corregida
        List<Inventory> todayList = inventoryService.getInventoriesByCreated(branchId);

        int column = 0;
        int row = 0;

        for (Inventory inv : todayList) {
            displayList.add(inv);

            // UI: Creamos solo UNA tarjeta visual por inventario
            VBox card = createLabelCard(inv);
            labelGrid.add(card, column, row);

            column++;
            if (column == 2) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createLabelCard(Inventory inv) {
        VBox card = new VBox(1); // Reducimos el espaciado entre elementos a 2
        // Alineación a la IZQUIERDA (Center_Left o Top_Left)
        card.setAlignment(javafx.geometry.Pos.TOP_LEFT);

        // Estilos de la tarjeta: ancho fijo de 200px para que no sea tan larga
        card.setMinWidth(200);
        card.setMaxWidth(200);
        card.setMinHeight(100);
        card.setMaxHeight(100);
        card.setStyle("-fx-border-color: #DCDCDC; -fx-padding: 8; -fx-background-color: white; " +
                "-fx-border-radius: 3; -fx-background-radius: 3;");

        // 1. IDs (Categoría - Producto)
        Label idLabel = new Label("ID: " + inv.getProduct().getCategory().getId() + "-" + inv.getProduct().getId());
        idLabel.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #666666;");



        // 3. Simulación de Código de Barras (Más pequeño)
        Label barcodeSim = new Label(inv.getProduct().getBarCodeNumber());
        barcodeSim.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 10; -fx-text-fill: #333333;");

        // 2. Nombre y Categoría (Con wrapText para que no alargue la tarjeta)
        Label nameAndCategory = new Label(inv.getProduct().getName() + " " + inv.getProduct().getCategory().getName());
        nameAndCategory.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #000000;");

        // 4. Precio
        Label priceLabel = new Label("Bs." + inv.getSalePrice());
        priceLabel.setStyle("-fx-font-size: 9; -fx-font-weight: bold; -fx-text-fill: #000000;"); // Color verde éxito

        // 5. Stock / Cantidad
        Label stockLabel = new Label("Copias: " + inv.getStock());
        stockLabel.setStyle("-fx-font-size: 8; -fx-text-fill: #777777; -fx-background-color: #f4f4f4; -fx-padding: 2;");

        card.getChildren().addAll(idLabel,  barcodeSim, nameAndCategory, priceLabel, stockLabel);
        return card;
    }

    private void printLabels() {
        if (displayList.isEmpty()) return;

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

            // Mandamos a imprimir
            JasperPrintManager.printReport(print, true);

        } catch (JRException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void buttonBack() {
        Stage currentStage = (Stage) buttonBack.getScene().getWindow();
        String nextViewFXML = "home-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
}