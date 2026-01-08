package com.casarick.app.controller;

import com.casarick.app.model.*;
import com.casarick.app.service.*;
import com.casarick.app.util.BarcodeGenerator;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;
import com.casarick.app.util.WebcamCaptureTask;
import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ProductController {

    @FXML
    private Button buttonBack;

    @FXML private TextField product,
            stockField,
            costPriceField,
            salePriceField,
            barCodeField;
    @FXML private ComboBox<Category> categoryMenu;
    @FXML private ComboBox<Type> typeMenu;
    @FXML private ComboBox<Industry> industryMenu;
    @FXML private ComboBox<Color> colorMenu;
    @FXML private ComboBox<String> sizes;
    @FXML private ImageView liveView, capturedImageView;
    @FXML private Label photoPathLabel;

    // Services
    private final ProductService productService = new ProductService();
    private final InventoryService inventoryService = new InventoryService();
    private final CategoryService categoryService = new CategoryService();
    private final TypeService typeService = new TypeService();
    private final IndustryService industryService = new IndustryService();
    private final ColorService colorService = new ColorService();

    // Camera Utils
    private Webcam webcam;
    private WebcamCaptureTask captureTask;
    private BufferedImage lastCapturedBuffer;

    @FXML
    public void initialize() {
        loadComboBoxes();
        barCodeField.setText(BarcodeGenerator.generateUniqueBarcode());
    }

    private void loadComboBoxes() {
        List<String> sizeList = new ArrayList<>();

        sizeList.add("S");
        sizeList.add("M");
        sizeList.add("L");
        sizeList.add("XS");
        sizeList.add("XM");
        sizeList.add("XL");

        categoryMenu.getItems().setAll(categoryService.getAllCategories());
        typeMenu.getItems().setAll(typeService.getAllTypes());
        industryMenu.getItems().setAll(industryService.getAllIndustries());
        colorMenu.getItems().setAll(colorService.getAllColors());
        sizes.getItems().setAll(sizeList);
    }

    // --- LÓGICA DE CÁMARA ---

    @FXML
    void handleTurnOnCamera(ActionEvent event) {
        // 1. Obtener todas las cámaras detectadas
        List<Webcam> webcams = Webcam.getWebcams();

        // Imprimir en consola para que veas cuáles detecta tu PC
        System.out.println("Cámaras detectadas:");
        for (int i = 0; i < webcams.size(); i++) {
            System.out.println(i + ": " + webcams.get(i).getName());
        }

        // 2. Intentar buscar específicamente la que diga "DroidCam"
        // y que NO sea la que muestra el logo de error (suele ser la Source 3 o la 2)
        webcam = null;
        for (Webcam w : webcams) {
            if (w.getName().contains("DroidCam Source 3")) { // Prueba con Source 3 o Source 2
                webcam = w;
                break;
            }
        }

        // 3. Si no encuentra una específica, usa la última de la lista (DroidCam suele aparecer al final)
        if (webcam == null && !webcams.isEmpty()) {
            webcam = webcams.get(webcams.size() - 1);
        }

        if (webcam != null) {
            // Configuración de tamaño
            webcam.setViewSize(webcam.getViewSizes()[0]);

            if (!webcam.isOpen()) {
                webcam.open();
            }

            captureTask = new WebcamCaptureTask(liveView, webcam);
            Thread thread = new Thread(captureTask);
            thread.setDaemon(true);
            thread.start();
        } else {
            System.out.println("No se encontró ninguna cámara de DroidCam activa.");
        }
    }

    @FXML
    void handleTurnOffCamera(ActionEvent event) {
        if (captureTask != null) {
            captureTask.stopCamera();
        }
    }

    @FXML
    void handleTakePhoto(ActionEvent event) {
        if (webcam != null && webcam.isOpen()) {
            lastCapturedBuffer = webcam.getImage();
            if (lastCapturedBuffer != null) {
                Image image = SwingFXUtils.toFXImage(lastCapturedBuffer, null);
                capturedImageView.setImage(image);
            }
        }
    }

    @FXML
    void handleSavePhoto(ActionEvent event) {
        if (lastCapturedBuffer != null) {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Guardar Foto del Producto");

            // Filtros para que solo guarde imágenes
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Imagen PNG", "*.png"),
                    new javafx.stage.FileChooser.ExtensionFilter("Imagen JPG", "*.jpg")
            );

            // Nombre sugerido inicial
            fileChooser.setInitialFileName("prod_" + product.getText().replace(" ", "_") + "_" + System.currentTimeMillis() + ".png");

            // Abrir la ventana de guardado
            File file = fileChooser.showSaveDialog(product.getScene().getWindow());


            if (file != null) {
                photoPathLabel.setText(file.getAbsolutePath());
                try {
                    // Determinar el formato según la extensión elegida
                    String format = file.getName().endsWith(".jpg") ? "JPG" : "PNG";

                    ImageIO.write(lastCapturedBuffer, format, file);

                    // Guardamos la ruta absoluta en el Label y en una variable si fuera necesario
                    photoPathLabel.setText(file.getAbsolutePath());

                    showSuccess("Foto guardada exitosamente en: " + file.getName());
                } catch (Exception e) {
                    showError("Error al escribir el archivo: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            showError("Primero debes 'Tomar Foto' antes de guardar.");
        }
    }

    // --- LÓGICA DE CREACIÓN (FLUJO DOBLE) ---

    @FXML
    void handleCreateProduct(ActionEvent event) {
        if (!isFormValid()) {
            return;
        }

        try {
            Product product = new Product();




            product.setCategory(categoryMenu.getValue());
            product.setType(typeMenu.getValue());
            product.setIndustry(industryMenu.getValue());
            product.setColor(colorMenu.getValue());
            product.setSize(sizes.getValue());
            product.setBarCodeNumber(barCodeField.getText());
            product.setPhotoUrl(photoPathLabel.getText());

            String productName = categoryMenu.getValue().getName() + " " +
                    typeMenu.getValue().getName() + " " +
                    industryMenu.getValue();

            product.setName(productName);

            this.product.setText(productName);

            // 2. Guardar Producto en API y obtener el objeto con ID
            Product savedProduct = productService.createNewProduct(product);

            if (savedProduct != null && savedProduct.getId() != null) {

                Branch branch = SessionManager.getInstance().getCurrentBranch();

                inventoryService.create(savedProduct.getId(),
                        SessionManager.getInstance().getCurrentBranch().getId(),
                        Double.parseDouble(costPriceField.getText()),
                        Double.parseDouble(salePriceField.getText()),
                        Integer.parseInt(stockField.getText()));

                showSuccess("Producto e Inventario creados con éxito.");
                clearForm();
            }
        } catch (Exception e) {
            showError("Error al guardar: " + e.getMessage());
        }
    }

    // Métodos de apoyo
    private void clearForm() {
        stockField.clear();
        costPriceField.clear();
        salePriceField.clear();
        barCodeField.setText(BarcodeGenerator.generateUniqueBarcode());
        capturedImageView.setImage(null);
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.show();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.show();
    }

    @FXML
    public void handleBackAction() {
        handleTurnOffCamera(null);

        Stage currentStage = (Stage) buttonBack.getScene().getWindow();
        String nextViewFXML = "home-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
    @FXML void handleAddCategory(ActionEvent event) {
        String name = showInputDialog("Nueva Categoría", "Ingrese el nombre de la categoría:");
        if (name != null && !name.trim().isEmpty()) {
            Category newCat = new Category();
            newCat.setName(name);
            // Llamamos al servicio (Asumiendo que devuelve el objeto creado o null)
            Category saved = categoryService.createNewCategory(newCat);
            if (saved != null) {
                categoryMenu.getItems().add(saved);
                categoryMenu.setValue(saved); // Seleccionarlo automáticamente
            }
        }
    }
    @FXML void handleAddType(ActionEvent event) {
        String name = showInputDialog("Nuevo Tipo", "Ingrese el nombre del tipo de producto:");
        if (name != null && !name.trim().isEmpty()) {
            Type newType = new Type();
            newType.setName(name);
            Type saved = typeService.createNewType(newType);
            if (saved != null) {
                typeMenu.getItems().add(saved);
                typeMenu.setValue(saved);
            }
        }
    }

    @FXML void handleAddIndustry(ActionEvent event) {
        String name = showInputDialog("Nueva Industria", "Ingrese el nombre de la industria/marca:");
        if (name != null && !name.trim().isEmpty()) {
            Industry newInd = new Industry();
            newInd.setName(name);
            Industry saved = industryService.createNewIndustry(newInd);
            if (saved != null) {
                industryMenu.getItems().add(saved);
                industryMenu.setValue(saved);
            }
        }
    }

    @FXML void handleAddColor(ActionEvent event) {
        String name = showInputDialog("Nuevo Color", "Ingrese el nombre del Color:");
        if (name != null && !name.trim().isEmpty()) {
            Color newColor = new Color();
            newColor.setName(name);
            Color saved = colorService.createNewColor(newColor);
            if (saved != null) {
                colorMenu.getItems().add(saved);
                colorMenu.setValue(saved);
            }
        }
    }

    private String showInputDialog(String title, String header) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("Nombre:");

        // Mostramos y esperamos el resultado
        return dialog.showAndWait().orElse(null);
    }

    private boolean isFormValid() {
        String errorMsg = "";

        // Validar TextFields
        if (stockField.getText().trim().isEmpty()) errorMsg += "- Stock inicial\n";
        if (costPriceField.getText().trim().isEmpty()) errorMsg += "- Precio Costo\n";
        if (salePriceField.getText().trim().isEmpty()) errorMsg += "- Precio Venta\n";

        // Validar ComboBoxes
        if (categoryMenu.getValue() == null) errorMsg += "- Categoría\n";
        if (typeMenu.getValue() == null) errorMsg += "- Tipo\n";
        if (industryMenu.getValue() == null) errorMsg += "- Industria/Marca\n";
        if (colorMenu.getValue() == null) errorMsg += "- Color\n";
        if (sizes.getValue().trim().isEmpty()) errorMsg += "- Talla/Tamaño\n";

        // Validar que se haya guardado la foto
        // Comprobamos si el label está vacío o tiene el texto por defecto
        if (photoPathLabel.getText() == null || photoPathLabel.getText().trim().isEmpty() || photoPathLabel.getText().equals("Ruta de la foto...")) {
            errorMsg += "- Fotografía del producto (Debe tomar y GUARDAR la foto)\n";
        }

        if (!errorMsg.isEmpty()) {
            showError("Por favor, complete los siguientes campos obligatorios:\n" + errorMsg);
            return false;
        }

        return true;
    }
}