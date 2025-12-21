package com.casarick.app.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneSwitcher {
    public static void switchScene(Stage stage, String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneSwitcher.class.getResource("/com/casarick/app/" + fxmlFileName)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la escena: " + fxmlFileName);
        }
    }
}