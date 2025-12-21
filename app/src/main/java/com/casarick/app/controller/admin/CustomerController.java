package com.casarick.app.controller.admin;

import com.casarick.app.controller.CreateCustomerController;
import com.casarick.app.model.Customer;
import com.casarick.app.service.CustomerService;
import com.casarick.app.util.SceneSwitcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class CustomerController {

    @FXML private TableView<Customer> tblCustomers;
    @FXML private TableColumn<Customer, Long> colId;
    @FXML private TableColumn<Customer, String> colName, colLastName, colPhone;
    @FXML private Button btnNew, btnEdit, btnBack;

    private final CustomerService customerService = new CustomerService();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadCustomers();

        // Habilitar botón editar solo cuando hay selección
        tblCustomers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnEdit.setDisable(newSelection == null);
        });

    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        tblCustomers.setItems(customerList);
    }

    private void loadCustomers() {
        customerList.setAll(customerService.getAllCustomers());
    }

    @FXML
    public void handleCreateCustomer() {
        Stage currentStage = (Stage) btnNew.getScene().getWindow();
        String nextViewFXML = "crud-customer.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleBack() {
        Stage currentStage = (Stage) btnBack.getScene().getWindow();
        String nextViewFXML = "home-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
}