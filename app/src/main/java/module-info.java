module com.casarick.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.swing;

    requires java.net.http;
    requires java.desktop;
    requires java.sql; // Añade esta si usas JDBC

    // Estos nombres dependen de cómo esté construido el JAR interno
    requires org.json;
    requires webcam.capture;
    requires jasperreports;

    // IMPORTANTE: Abrir modelos a javafx.base para TableView
    opens com.casarick.app.model to javafx.base, javafx.fxml;

    // Abrir controladores para que FXML los vea
    opens com.casarick.app.controller to javafx.fxml;
    opens com.casarick.app.controller.admin to javafx.fxml;
    opens com.casarick.app to javafx.fxml;

    exports com.casarick.app;
    exports com.casarick.app.controller;
    exports com.casarick.app.controller.admin;
    exports com.casarick.app.model;
}