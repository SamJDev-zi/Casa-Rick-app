package com.casarick.app.controller;

import com.casarick.app.model.Permission;
import com.casarick.app.model.User;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class HomeController {
    @FXML
    private Label nameUser;
    @FXML
    private Label roleUser;

    @FXML
    private Button logoutButton;

    @FXML
    private Button newSale;
    @FXML
    private Button newReservation;
    @FXML
    private Button printTicket;
    @FXML
    private Button registerProduct;
    @FXML
    private Button searchProduct;
    @FXML
    private Button inventory;
    @FXML
    private Button register;
    @FXML
    private Button editSale;
    @FXML
    private Button editReservation;

    @FXML
    private Button crudBranches;
    @FXML
    private Button crudUsers;
    @FXML
    private Button crudCustomers;

    @FXML
    private Pane adminPane;

    private void applySecurityConstraints(User user) {
        String roleName = user.getRole().getName().toLowerCase();

        // 1. Si es Administrador, tiene acceso total, no ocultamos nada.
        if (roleName.equals("administrador")) {
            return;
        }

        // 2. Si es Empleado, ocultamos el panel administrativo por defecto
        adminPane.setVisible(false);
        adminPane.setManaged(false); // managed(false) quita el espacio que ocupa el panel

        // 3. Verificamos permisos específicos para los botones de la cuadrícula
        // Extraemos los nombres de los permisos en una lista simple de Strings
        List<String> userPermissions = user.getPermissionList().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());

        // Relación Permiso -> Botón
        setupButtonPermission(newSale, "permiso_venta", userPermissions);
        setupButtonPermission(registerProduct, "permiso_registrar_producto", userPermissions);
        setupButtonPermission(register, "permiso_caja", userPermissions);
        setupButtonPermission(searchProduct, "permiso_inventario", userPermissions); // Según tu lista
        setupButtonPermission(printTicket, "permiso_impresion", userPermissions);
        setupButtonPermission(inventory, "permiso_inventario", userPermissions);
        setupButtonPermission(editSale, "permiso_editar_venta", userPermissions);
        setupButtonPermission(editReservation, "permiso_editar_reserva", userPermissions);
        setupButtonPermission(newReservation, "permiso_reserva", userPermissions);
    }

    /**
     * Oculta un botón si el usuario no tiene el permiso necesario.
     */
    private void setupButtonPermission(Button button, String permissionRequired, List<String> userPerms) {
        boolean hasPermission = userPerms.contains(permissionRequired);
        button.setVisible(hasPermission);
        button.setManaged(hasPermission);
    }

    @FXML
    public void initialize() {
        nameUser.setText(SessionManager.getInstance().getLoggedInUser().getName());
        roleUser.setText(SessionManager.getInstance().getLoggedInUser().getRole().getName());

        applySecurityConstraints(SessionManager.getInstance().getLoggedInUser());
    }

    @FXML
    public void handleLogoutAction() {
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        String nextViewFXML = "login-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);

        SessionManager.getInstance().startSession(null, null);
    }

    @FXML
    public void handleSaleAction() {
        Stage currentStage = (Stage) newSale.getScene().getWindow();
        String nextViewFXML = "sale-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleReservationAction() {
        Stage currentStage = (Stage) newReservation.getScene().getWindow();
        String nextViewFXML = "reservation-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handlePrintTicketAction() {
        Stage currentStage = (Stage) printTicket.getScene().getWindow();
        String nextViewFXML = "label-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleRegisterProductAction() {
        Stage currentStage = (Stage) registerProduct.getScene().getWindow();
        String nextViewFXML = "product-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleSearchProductAction() {
        Stage currentStage = (Stage) searchProduct.getScene().getWindow();
        String nextViewFXML = "search-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleInventoryAction() {
        Stage currentStage = (Stage) inventory.getScene().getWindow();
        String nextViewFXML = "inventory-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);

    }

    @FXML
    public void handleRegisterAction() {
        Stage currentStage = (Stage) register.getScene().getWindow();
        String nextViewFXML = "sale-history-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleEditSaleAction() {

    }

    @FXML
    public void handleEditReservationAction() {
        Stage currentStage = (Stage) editReservation.getScene().getWindow();
        String nextViewFXML = "edit-reservation-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleCrudBranchesAction() {
        Stage currentStage = (Stage) crudBranches.getScene().getWindow();
        String nextViewFXML = "branch-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleCrudUsersAction() {
        Stage currentStage = (Stage) crudUsers.getScene().getWindow();
        String nextViewFXML = "user-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleCrudCustomersAction() {
        Stage currentStage = (Stage) crudCustomers.getScene().getWindow();
        String nextViewFXML = "customer-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
}
