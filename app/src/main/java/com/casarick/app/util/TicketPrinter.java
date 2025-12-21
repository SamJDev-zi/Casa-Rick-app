package com.casarick.app.util;

import com.casarick.app.model.Customer;
import com.casarick.app.model.Inventory;
import com.casarick.app.model.Sale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketPrinter {

    private static final double CM_TO_PTS = 28.35;// 2 cm

    // CAMBIO 1: Aumentamos el ancho a 4.0 cm (antes 3.0)
    private static final double WIDTH_PTS = 4.0 * CM_TO_PTS;
    private static final double HEIGHT_PTS = 2.3 * CM_TO_PTS; // 2 cm de alto

    // Medidas para Factura (7x10)
    private static final double INVOICE_WIDTH_PTS = 7.0 * CM_TO_PTS;  // 198.45 pts
    private static final double INVOICE_HEIGHT_PTS = 10.0 * CM_TO_PTS; // 283.5 pts

    public static void printFullPageLabels(Inventory inventory) {
        // 1. Obtener impresora predeterminada (Directo)
        Printer printer = Printer.getDefaultPrinter();

        if (printer == null) {
            System.err.println("No se encontró una impresora instalada.");
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob(printer);

        if (job != null) {
            // 2. Configurar el diseño de página (Oficio/Legal)
            PageLayout pageLayout = printer.createPageLayout(Paper.LEGAL,
                    PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);

            // 3. Crear el contenedor (Grid) que llena la hoja
            GridPane grid = createGridOfLabels(inventory, pageLayout);

            // 4. IMPRESIÓN DIRECTA: Enviar a la impresora sin mostrar diálogo de configuración
            boolean success = job.printPage(pageLayout, grid);
            if (success) {
                job.endJob(); // Finaliza y envía a la cola de impresión del OS
            }
        }
    }

    private static GridPane createGridOfLabels(Inventory inv, PageLayout pl) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT);

        // Calcular etiquetas por página
        int cols = (int) (pl.getPrintableWidth() / WIDTH_PTS);
        int rows = (int) (pl.getPrintableHeight() / HEIGHT_PTS);

        grid.setHgap(5);
        grid.setVgap(5);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid.add(createSingleLabelNode(inv), c, r);
            }
        }
        return grid;
    }

    // Dentro de TicketPrinter.java

    public static Node createSingleLabelNode(Inventory inv) {
        VBox box = new VBox(2); // Espacio vertical muy pequeño entre líneas (2px)
        // Ajuste de tamaño para etiqueta estándar (ej. 50mm x 30mm)
        box.setPrefSize(WIDTH_PTS, HEIGHT_PTS);
        box.setAlignment(Pos.TOP_LEFT); // Alineación izquierda como en la foto
        box.setPadding(new Insets(5, 5, 5, 10)); // Margen izquierdo un poco mayor
        box.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0.1;");



        // 2. ID PEQUEÑO (Debajo del código de barras)
        // Formato: "34 T3" -> ID Categoría + " " + ID Producto
        String idText = inv.getProduct().getCategory().getId() + " P" + inv.getProduct().getId();
        Label idLbl = new Label(idText);
        idLbl.setFont(Font.font("Courier New", FontWeight.NORMAL, 8)); // Fuente monoespaciada pequeña

        // 1. CÓDIGO DE BARRAS (Superior)
        // Aquí llamamos al generador. Si usas ZXing, el código cambia ligeramente.
        Pane barcode = BarcodeGenerator.createBarcode(inv.getProduct().getBarCodeNumber(), WIDTH_PTS - 15, 15);

        // 3. DESCRIPCIÓN (Nombre del producto)
        // Formato: "BASICA - SOLERA VARIOS"
        String descText = inv.getProduct().getName().toUpperCase();
        if (descText.length() > 22) descText = descText.substring(0, 22); // Cortar si es muy largo
        Label descLbl = new Label(descText);
        descLbl.setFont(Font.font("Courier New", FontWeight.BOLD, 6)); // Fuente tipo máquina de escribir

        // 4. PRECIO (Grande y abajo)
        // Formato: "Bs38,00" (Sin espacio entre Bs y número, coma decimal)
        String priceText = String.format("Bs%.2f", inv.getSalePrice()).replace(".", ",");
        Label priceLbl = new Label(priceText);
        priceLbl.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 10)); // Letra grande y gruesa

        box.getChildren().addAll(idLbl, barcode, descLbl, priceLbl);
        return box;
    }

    public static void printInvoice(List<Sale> sales) {
        if (sales == null || sales.isEmpty()) return;

        Printer printer = Printer.getDefaultPrinter();
        if (printer == null) return;

        PrinterJob job = PrinterJob.createPrinterJob(printer);
        if (job != null) {
            // Creamos un tamaño de papel personalizado o usamos uno estándar que quepa
            // Para tickets de 7x10 solemos usar Paper.A8 o configuración personalizada
            PageLayout pageLayout = printer.createPageLayout(Paper.A4,
                    PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);

            VBox invoiceNode = createInvoiceNode(sales);

            if (job.printPage(pageLayout, invoiceNode)) {
                job.endJob();
            }
        }
    }

    private static VBox createInvoiceNode(List<Sale> sales) {
        VBox container = new VBox(5);
        container.setPrefSize(INVOICE_WIDTH_PTS, INVOICE_HEIGHT_PTS);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: white;");
        container.setAlignment(Pos.TOP_CENTER);

        // 1. Encabezado: Nombre de la tienda
        Label shopName = new Label("CASA RICK");
        shopName.setFont(Font.font("System", FontWeight.BOLD, 14));

        // 2. Datos del Cliente (tomados de la primera venta)
        Customer customer = sales.get(0).getCustomerDTO();
        String customerFullName = (customer != null) ? customer.getName() + " " + customer.getLastName() : "Público General";
        Label clientInfo = new Label("Cliente: " + customerFullName);
        clientInfo.setFont(Font.font("System", 8));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateInfo = new Label("Fecha: " + sales.get(0).getCreatedAt().format(dtf));
        dateInfo.setFont(Font.font("System", 8));

        container.getChildren().addAll(shopName, new Separator(), clientInfo, dateInfo, new Separator());

        // 3. Listado de Productos (Cuerpo)
        VBox itemsBox = new VBox(2);
        double totalGeneral = 0;

        for (Sale s : sales) {
            String prodName = s.getInventoryDTO().getProduct().getName();
            int qty = s.getStock();
            double totalItem = s.getSaleTotal();
            totalGeneral += totalItem;

            // Formato: Prod x Cant ....... $Total
            HBox row = new HBox();
            Label nameLbl = new Label(qty + " x " + (prodName.length() > 15 ? prodName.substring(0, 15) : prodName));
            nameLbl.setFont(Font.font("System", 7));

            Label priceLbl = new Label(String.format("$%.2f", totalItem));
            priceLbl.setFont(Font.font("System", 7));

            // Espaciador para empujar el precio a la derecha
            Pane spacer = new Pane();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            row.getChildren().addAll(nameLbl, spacer, priceLbl);
            itemsBox.getChildren().add(row);
        }

        container.getChildren().add(itemsBox);

        // 4. Total Final
        container.getChildren().add(new Separator());
        HBox totalRow = new HBox();
        Label totalTxt = new Label("TOTAL:");
        totalTxt.setFont(Font.font("System", FontWeight.BOLD, 10));

        Label totalVal = new Label(String.format("$%.2f", totalGeneral));
        totalVal.setFont(Font.font("System", FontWeight.BOLD, 10));

        Pane spacer2 = new Pane();
        HBox.setHgrow(spacer2, javafx.scene.layout.Priority.ALWAYS);

        totalRow.getChildren().addAll(totalTxt, spacer2, totalVal);
        container.getChildren().add(totalRow);

        // 5. Pie de página
        Label footer = new Label("¡Gracias por su compra!");
        footer.setFont(Font.font("System", 7));
        footer.setPadding(new Insets(10, 0, 0, 0));
        container.getChildren().add(footer);

        return container;
    }
}