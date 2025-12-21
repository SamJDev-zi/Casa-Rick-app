package com.casarick.app.controller.admin;

import com.casarick.app.model.*;
import com.casarick.app.service.*;
import com.casarick.app.util.SceneSwitcher;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserController {

    @FXML private TableView<User> tblUsers;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colName, colLastName, colPhone, colRole;

    @FXML private TextField txtName, txtLastName, txtPhone;
    @FXML private PasswordField txtPass;
    @FXML private ComboBox<Role> cbRole;
    @FXML private FlowPane flowPermissions;
    @FXML private Label lblPass, lblActionTitle;
    @FXML private Button btnBack, btnSave;

    private final UserService userService = new UserService();
    private final RoleService roleService = new RoleService();
    private final PermissionService permissionService = new PermissionService();

    private Map<Permission, CheckBox> permissionCheckBoxMap = new HashMap<>();
    private User selectedUser = null;

    @FXML
    public void initialize() {
        setupTable();
        loadInitialData();

        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                loadUserToEdit(newVal);
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colRole.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole() != null ? cellData.getValue().getRole().getName() : "Sin Rol"));
    }

    private void loadInitialData() {
        tblUsers.setItems(FXCollections.observableArrayList(userService.getAllUsers()));
        cbRole.setItems(FXCollections.observableArrayList(roleService.getAllRoles()));

        // Cargar permisos dinámicamente como CheckBoxes
        flowPermissions.getChildren().clear();
        permissionCheckBoxMap.clear();
        List<Permission> allPerms = permissionService.getAllPermissions();
        for (Permission p : allPerms) {
            CheckBox cb = new CheckBox(p.getName().replace("permiso_", "").replace("_", " "));
            permissionCheckBoxMap.put(p, cb);
            flowPermissions.getChildren().add(cb);
        }
    }

    private void loadUserToEdit(User u) {
        lblActionTitle.setText("EDITANDO USUARIO: " + u.getName());
        txtName.setText(u.getName());
        txtLastName.setText(u.getLastName());
        txtPhone.setText(u.getPhoneNumber());
        cbRole.setValue(u.getRole());

        // Bloquear contraseña en edición
        txtPass.setDisable(true);
        txtPass.setText("********");
        lblPass.setOpacity(0.5);

        // Marcar permisos del usuario
        permissionCheckBoxMap.values().forEach(cb -> cb.setSelected(false));
        for (Permission userPerm : u.getPermissionList()) {
            // Buscamos el permiso en el mapa por ID (comparación manual para seguridad)
            permissionCheckBoxMap.forEach((perm, cb) -> {
                if (perm.getId().equals(userPerm.getId())) cb.setSelected(true);
            });
        }
    }

    @FXML
    private void handlePrepareCreate() {
        selectedUser = null;
        lblActionTitle.setText("CREAR NUEVO USUARIO");
        txtName.clear();
        txtLastName.clear();
        txtPhone.clear();
        txtPass.clear();
        txtPass.setDisable(false);
        lblPass.setOpacity(1.0);
        cbRole.getSelectionModel().clearSelection();
        permissionCheckBoxMap.values().forEach(cb -> cb.setSelected(false));
        tblUsers.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSave() {
        if (txtName.getText().isEmpty() || cbRole.getValue() == null) {
            showAlert("Error", "Nombre y Rol son obligatorios.");
            return;
        }

        // Construir lista de permisos seleccionados
        List<Permission> selectedPerms = new ArrayList<>();
        permissionCheckBoxMap.forEach((perm, cb) -> {
            if (cb.isSelected()) selectedPerms.add(perm);
        });

        User user = (selectedUser == null) ? new User() : selectedUser;
        user.setName(txtName.getText());
        user.setLastName(txtLastName.getText());
        user.setPhoneNumber(txtPhone.getText());
        user.setRole(cbRole.getValue());
        user.setPermissionList(selectedPerms);

        try {
            if (selectedUser == null) {
                // CREAR
                userService.createNewUser(user, txtPass.getText());
                showAlert("Éxito", "Usuario creado correctamente.");
            } else {
                // EDITAR (Enviamos null o string vacío en password según tu API,
                // pero como en edición no se cambia, el UserService usará el actual si el backend lo permite)
                userService.updateUser(selectedUser.getId(), user, "");
                showAlert("Éxito", "Usuario actualizado.");
            }
            loadInitialData();
            handlePrepareCreate();
        } catch (Exception e) {
            showAlert("Error", "Error al procesar: " + e.getMessage());
        }
    }

    @FXML private void handleBack() { SceneSwitcher.switchScene((Stage) btnBack.getScene().getWindow(), "home-view.fxml"); }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}