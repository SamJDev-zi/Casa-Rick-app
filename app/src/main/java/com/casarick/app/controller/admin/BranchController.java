package com.casarick.app.controller.admin;

import com.casarick.app.model.Branch;
import com.casarick.app.service.BranchService;
import com.casarick.app.util.SceneSwitcher;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class BranchController {

    @FXML private TableView<Branch> tblBranches;
    @FXML private TableColumn<Branch, Long> colId;
    @FXML private TableColumn<Branch, String> colName, colAddress, colPhone;
    @FXML private TableColumn<Branch, Boolean> colStatus;

    @FXML private VBox paneEdit;
    @FXML private TextField txtEditName, txtEditAddress, txtEditPhone;
    @FXML private ComboBox<Boolean> cbEditStatus;
    @FXML private Button btnBack;

    private final BranchService branchService = new BranchService();
    private Branch selectedBranch;

    @FXML
    public void initialize() {
        cbEditStatus.setItems(FXCollections.observableArrayList(true, false));
        setupTableColumns();
        loadBranches();

        // Listener para detectar selección en la tabla
        tblBranches.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedBranch = newVal;
                loadSelectionToEdit(newVal);
            }
        });
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("active"));
    }

    private void loadBranches() {
        List<Branch> list = branchService.getAllBranches();
        tblBranches.setItems(FXCollections.observableArrayList(list));
    }

    private void loadSelectionToEdit(Branch b) {
        paneEdit.setDisable(false);
        // Dejamos los prompts para indicar que se puede cambiar,
        // pero podemos setear el texto actual o dejarlo vacío según prefieras
        txtEditName.setText(b.getName());
        txtEditAddress.setText(b.getAddress());
        txtEditPhone.setText(b.getPhoneNumber());
        cbEditStatus.setValue(b.isActive());
    }

    @FXML
    private void handleUpdateAction() {
        if (selectedBranch == null) return;

        // Lógica de "Opcionales": si está vacío, usar el valor original
        String name = txtEditName.getText().trim().isEmpty() ? selectedBranch.getName() : txtEditName.getText().trim();
        String address = txtEditAddress.getText().trim().isEmpty() ? selectedBranch.getAddress() : txtEditAddress.getText().trim();
        String phone = txtEditPhone.getText().trim().isEmpty() ? selectedBranch.getPhoneNumber() : txtEditPhone.getText().trim();
        boolean status = cbEditStatus.getValue() != null ? cbEditStatus.getValue() : selectedBranch.isActive();

        Branch updatedBranch = new Branch(
                selectedBranch.getId(),
                name,
                address,
                phone,
                status
        );

        try {
            branchService.updateBranch(selectedBranch.getId(), updatedBranch);
            showAlert("Éxito", "Sucursal actualizada correctamente.");
            loadBranches();
            paneEdit.setDisable(true);
        } catch (Exception e) {
            showAlert("Error", "No se pudo actualizar: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateNewBranch() {
        // Aquí puedes abrir el nuevo stage para creación
        // Por ahora, redirigimos a una vista de creación o abrimos un Modal
        SceneSwitcher.switchScene((Stage) btnBack.getScene().getWindow(), "create-branch-view.fxml");
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
}