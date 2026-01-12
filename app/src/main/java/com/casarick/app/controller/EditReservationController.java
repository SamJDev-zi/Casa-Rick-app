package com.casarick.app.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.casarick.app.model.Branch;
import com.casarick.app.model.Reservation;
import com.casarick.app.service.ReservationService;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EditReservationController {

    @FXML private TableView<Reservation> tblReservations;
    @FXML private TableColumn<Reservation, Long> colId;
    @FXML private TableColumn<Reservation, String> colDesc, colCustomer, colStatus, colExpiration;
    @FXML private TableColumn<Reservation, Integer> colStock;
    @FXML private TableColumn<Reservation, Double> colTotalPrice, colBalance;

    @FXML private VBox paneEdit;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TextField txtBalance;
    @FXML private TextArea txtDescription;
    @FXML private Button btnBack;

    private final ReservationService service = new ReservationService();
    private final ObservableList<Reservation> reservationData = FXCollections.observableArrayList();
    private Reservation selectedReservation;

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("TODAS","PENDIENTE", "CANCELADA"));
        cbStatusFilter.setItems(FXCollections.observableArrayList("TODAS","PENDIENTE", "CANCELADA", "TERMINADA"));
        setupTable();
        loadReservations("TODAS");

        // Listener para seleccionar fila
        tblReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showReservationDetails(newVal);
            }
        });
        filtering();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Columna Calculada: Precio Total (Stock * Precio Venta del Inventario)
        colTotalPrice.setCellValueFactory(cellData -> {
            Reservation r = cellData.getValue();
            double total = r.getStock() * r.getInventory().getSalePrice();
            return new SimpleObjectProperty<>(total);
        });

        // Columna: Nombre Cliente
        colCustomer.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCustomer().getName() + " " + cellData.getValue().getCustomer().getLastName())
        );

        // Columna: Fecha Expiración
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colExpiration.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getExpiration().format(formatter))
        );
    }

    private void loadReservations(String reservation) {
        // 1. Validar que la sucursal de la sesión exista
        Branch currentBranch = SessionManager.getInstance().getCurrentBranch();
        if (currentBranch == null || currentBranch.getId() == null) {
            System.err.println("Error: No hay una sucursal activa en la sesión.");
            return;
        }

        Long currentBranchId = currentBranch.getId();

        // 2. Traer todas y filtrar con seguridad (Null-safe)
        List<Reservation> allReservations = service.getAllReservations();

        List<Reservation> filteredList;
        if("TODAS".equalsIgnoreCase(reservation)){
            filteredList = allReservations.stream()
                .filter(r -> r.getBranch() != null && r.getBranch().getId() != null) // Validación de seguridad
                .filter(r -> r.getBranch().getId().equals(currentBranchId))
                //.filter(r -> "PENDIENTE".equalsIgnoreCase(r.getStatus()))
                .toList();
        }else{
            filteredList = allReservations.stream()
                .filter(r -> r.getBranch() != null && r.getBranch().getId() != null) // Validación de seguridad
                .filter(r -> r.getBranch().getId().equals(currentBranchId))
                .filter(r -> reservation.equalsIgnoreCase(r.getStatus()))
                .toList();

        }
        
        reservationData.setAll(filteredList);
        tblReservations.setItems(reservationData);
    }

    private void showReservationDetails(Reservation res) {
        selectedReservation = res;
        paneEdit.setDisable(false);
        cbStatus.setValue(res.getStatus());
        txtBalance.setText(String.valueOf(res.getBalance()));
        txtDescription.setText(res.getDescription());
    }

    @FXML
    private void handleUpdate() {
        if (selectedReservation == null) return;

        try {
            String newStatus = cbStatus.getValue();
            // Reemplazamos coma por punto para el parseo
            Double newBalance = Double.parseDouble(txtBalance.getText().trim().replace(",", "."));
            String newDesc = txtDescription.getText();

            boolean success = service.updateReservation(
                    selectedReservation.getId(),
                    newStatus,
                    newBalance,
                    selectedReservation.getDeposit(), // Mantenemos el depósito original
                    newDesc,
                    selectedReservation.getStock()
            );

            if (success) {
                showAlert("Éxito", "Reserva actualizada correctamente.");
                loadReservations("TODAS"); // Recargar tabla
                paneEdit.setDisable(true);
                tblReservations.getSelectionModel().clearSelection();
            } else {
                showAlert("Error", "No se pudo actualizar la reserva en el servidor.");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "El formato del saldo no es válido.");
        }
    }

    @FXML
    private void handleBack() {
        SceneSwitcher.switchScene((Stage) btnBack.getScene().getWindow(), "home-view.fxml");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void filtering(){
        cbStatusFilter.setOnAction(e -> applyFilters());
    }
    @FXML
    private void applyFilters() {
        String selectedCategory = cbStatusFilter.getValue();
        loadReservations(selectedCategory);
    }

    @FXML
    private void handleStatusFilter() {
        applyFilters();
    }
}