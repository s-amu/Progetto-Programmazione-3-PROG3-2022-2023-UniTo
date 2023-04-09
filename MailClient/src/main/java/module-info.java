module com.progetto.prog3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.prog3.common;
    requires java.desktop;

    opens com.prog3.mailclient to javafx.fxml;
    exports com.prog3.mailclient;


    exports com.prog3.mailclient.controller;
    opens com.prog3.mailclient.controller to javafx.fxml;
}