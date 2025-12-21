package com.casarick.app.controller.admin;

import com.casarick.app.model.Customer;
import com.casarick.app.service.CustomerService;
import com.casarick.app.util.SceneSwitcher;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CreateCustomerController {
    @FXML
    private Button createCustomer;
    @FXML
    private Button cancelAction;
    @FXML
    private TextField customerName;
    @FXML
    private TextField customerLastName;
    @FXML
    private TextField customerPhone;

    private final CustomerService customerService = new CustomerService();
    private Customer editingCustomer; // Si es null, estamos creando

    // Método para recibir datos desde el controlador principal
    public void setEditData(Customer customer) {
        this.editingCustomer = customer;
        customerName.setText(customer.getName());
        customerLastName.setText(customer.getLastName());
        customerPhone.setText(customer.getPhoneNumber());
        createCustomer.setText("Actualizar");
    }

    @FXML
    public void initialize() {
        BooleanBinding isVoid = customerName.textProperty().isEmpty()
                .or(customerLastName.textProperty().isEmpty())
                .or(customerPhone.textProperty().isEmpty());
        createCustomer.disableProperty().bind(isVoid);
    }

    @FXML
    public void  handleCreateAction() {
        String name = customerName.getText();
        String lastName = customerLastName.getText();
        String phoneNumber = customerPhone.getText();

        Customer customer = new Customer();

        customer.setName(name);
        customer.setLastName(lastName);
        customer.setPhoneNumber(phoneNumber);

        CustomerService service = new CustomerService();
        service.createNewCustomer(customer);

        Stage currentStage = (Stage) createCustomer.getScene().getWindow();
        String nextViewFXML = "customer-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleCancelAction() {
        Stage currentStage = (Stage) cancelAction.getScene().getWindow();
        String nextViewFXML = "customer-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
}
