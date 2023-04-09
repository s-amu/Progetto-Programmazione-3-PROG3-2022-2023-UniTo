module com.progetto.prog3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.prog3.common;


    opens com.prog3.mailserver to javafx.fxml;
    exports com.prog3.mailserver;
    exports com.prog3.mailserver.controller;
    opens com.prog3.mailserver.controller to javafx.fxml;
}