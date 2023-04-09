package com.prog3.mailserver;

import com.prog3.mailserver.controller.ControllerPrimary;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.*;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/mail-server-primary-view.fxml"));
        ControllerPrimary controller = new ControllerPrimary();
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Mail Server");
        stage.setScene(scene);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });

        stage.show();

        Server s = new Server();
        s.setControllerPrimary(controller);
        s.activate();
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }
}
