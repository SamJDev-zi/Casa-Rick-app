module com.casarick.app {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.casarick.app to javafx.fxml;
    exports com.casarick.app;
}