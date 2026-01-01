package com.casarick.app;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppController extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppController.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        String cssPath = getClass().getResource("/css/stylecss.css").toExternalForm();

        scene.getStylesheets().add(cssPath);
        stage.setTitle("Casa Rick");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}