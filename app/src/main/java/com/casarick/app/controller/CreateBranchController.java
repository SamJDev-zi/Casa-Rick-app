package com.casarick.app.controller;

import com.casarick.app.model.Branch;
import com.casarick.app.service.BranchService;
import com.casarick.app.util.SceneSwitcher;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;

public class CreateBranchController {
    @FXML
    private TextField name;
    @FXML
    private TextField address;
    @FXML
    private TextField phone;

    @FXML
    private Button create;
    @FXML
    private Button cancel;

    @FXML
    public void initialize() {
        BooleanBinding isVoid = name.textProperty().isEmpty()
                .or(address.textProperty().isEmpty())
                .or(phone.textProperty().isEmpty());
        create.disableProperty().bind(isVoid);
    }

    @FXML
    public void handleCreateBranch() {
        String name = this.name.getText();
        String address = this.address.getText();
        String phone = this.phone.getText();

        BranchService service = new BranchService();

        Branch branch = new Branch();

        branch.setName(name);
        branch.setAddress(address);
        branch.setPhoneNumber(phone);
        branch.setActive(true);

        service.createNewBranch(branch);

        Stage currentStage = (Stage) create.getScene().getWindow();
        String nextViewFXML = "branch-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }

    @FXML
    public void handleCancelAction() {
        Stage currentStage = (Stage) cancel.getScene().getWindow();
        String nextViewFXML = "branch-view.fxml";
        SceneSwitcher.switchScene(currentStage, nextViewFXML);
    }
}
