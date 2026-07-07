module com.example.pharmacy_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires java.desktop;

    opens com.example.pharmacy_management_system to javafx.fxml;
    exports com.example.pharmacy_management_system;

    exports com.example.pharmacy_management_system.controllers;
    opens com.example.pharmacy_management_system.controllers to javafx.fxml;

    exports com.example.pharmacy_management_system.models;
    opens com.example.pharmacy_management_system.models to javafx.fxml;

    exports com.example.pharmacy_management_system.utility;
    opens com.example.pharmacy_management_system.utility to javafx.fxml;
}
