package com.casarick.app.controller;

import com.casarick.app.model.Branch;
import com.casarick.app.model.User;
import com.casarick.app.service.BranchService;
import com.casarick.app.service.UserService;
import com.casarick.app.util.SceneSwitcher;
import com.casarick.app.util.SessionManager;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;

public class LoginController {

    @FXML
    private TextField nameUser;
    @FXML
    private TextField lastNameUser;
    @FXML
    private PasswordField passwordUser;
    @FXML
    private Button loginButton;
    @FXML
    private ComboBox<Branch> selectBranch;

    private ObservableList<Branch> branches = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        System.out.println(Arrays.toString(branches.toArray()));
        fillComboBox();
        System.out.println(Arrays.toString(branches.toArray()));
        if (!branches.isEmpty()) {
            selectBranch.getSelectionModel().getSelectedItem();
        } else {
            System.out.println("No hay sucursales cargadas!!");
        }

        BooleanBinding isVoid = nameUser.textProperty().isEmpty()
                .or(lastNameUser.textProperty().isEmpty())
                .or(passwordUser.textProperty().isEmpty())
                .or(selectBranch.valueProperty().isNull());
        loginButton.disableProperty().bind(isVoid);
    }

    @FXML
    public void handleLoginAction() {
        String name = nameUser.getText().toLowerCase();
        String lastName = lastNameUser.getText().toLowerCase();
        String password = passwordUser.getText().toLowerCase();

        UserService userService = new UserService();

        User user = userService.userLogin(name, lastName, password);
        Branch branch = selectBranch.getSelectionModel().getSelectedItem();



        if (user != null) {
            SessionManager.getInstance().startSession(user, branch);
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            String nextViewFXML = "home-view.fxml";
            SceneSwitcher.switchScene(currentStage, nextViewFXML);
        } else {
            System.out.println("Error!!!");
        }

        nameUser.clear();
        lastNameUser.clear();
        passwordUser.clear();
    }

    private void fillComboBox() {
        BranchService service = new BranchService();
        branches.addAll(service.getAllBranches());

        selectBranch.setItems(branches);
    }
}