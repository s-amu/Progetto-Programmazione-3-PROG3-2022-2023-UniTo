package com.prog3.mailclient.controller;

import com.prog3.common.model.Email;
import com.prog3.mailclient.Connection;
import com.prog3.mailclient.Main;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControllerHome implements Initializable {
    @FXML
    public StackPane content;
    @FXML
    private ToggleButton btn_new_email;
    @FXML
    private ToggleButton btn_incoming_email;
    @FXML
    private Label lbl_title;
    @FXML
    private Label lbl_welcome;
    public ControllerListEmail controllerListEmail;
    public ControllerEmail controllerEmail;
    private ControllerLogin controllerLogin;
    private ControllerNewEmail controllerNewEmail;
    private Stage stage;
    Connection connection = null;

    /**
     * Initializes the controller class.
     *
     * @param url            the URL to the FXML document
     * @param resourceBundle the resources used by the FXML document
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            FXMLLoader listLoader = new FXMLLoader(Main.class.getResource("view/mail-client-list-email-view.fxml"));
            Parent root = listLoader.load();
            controllerListEmail = listLoader.getController();
            content.getChildren().add(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method for setting connection
     * @param connection the connection to server
     */
    public void setConnection(Connection connection) {
        lbl_welcome.setText("Welcome, " + connection.getName() + "!");
        this.connection = connection;
    }

    /**
     * Set the reference to the Stage instance.
     *
     * @param stage the Stage instance
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                try {
                    connection.sendRequest(connection.getEmail());
                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
                Platform.exit();
                System.exit(0);
            }
        });
    }

    /**
     * Set the reference to the ControllerLogin instance to initialize the ListView (in login)
     *
     * @param controllerLogin the ControllerLogin instance
     */
    public void setControllerLogin(ControllerLogin controllerLogin) {
        this.controllerLogin = controllerLogin;
        controllerLogin.setControllerListEmail(controllerListEmail);
    }

    /**
     * Handles the action of the "New email" button.
     * When this button is pressed, the new email composition view is opened.
     */
    @FXML
    protected void onButtonNewEmailClick() {
        btn_incoming_email.setSelected(false);
        btn_new_email.setSelected(true);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/mail-client-new-email-view.fxml"));
            Parent scene = (Parent) fxmlLoader.load();
            controllerNewEmail = fxmlLoader.getController();
            controllerNewEmail.setConnection(this.connection);
            controllerNewEmail.setControllerHome(this);
            Stage stage = new Stage();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    setSelectedIncomingEmail(true);
                    setSelectedNewEmail(false);
                    stage.close();
                }
            });
            stage.setScene(new Scene(scene));
            stage.initModality(Modality.APPLICATION_MODAL); // Imposta la finestra di dialogo come modale
            stage.initOwner(this.stage); // Imposta lo Stage principale come proprietario della finestra di dialogo
            stage.showAndWait(); // Mostra la finestra di dialogo e attendi la sua chiusura
        } catch (Exception e) {
            e.printStackTrace();
        }
        btn_new_email.setSelected(false);
    }

    /**
     * Handles the action of the "Incoming email" button.
     * The home is updated by checking if there are new emails present.
     */
    @FXML
    protected void onButtonIncomingEmailClick() {
        controllerListEmail.onButtonIncomingEmailClick();
        if (controllerEmail != null) {
            controllerEmail.onButtonIncomingEmailClick();
        }
        btn_incoming_email.setSelected(true);
        btn_new_email.setSelected(false);
    }

    public void setSelectedIncomingEmail(boolean value){
        btn_incoming_email.setSelected(value);
    }

    public void setSelectedNewEmail(boolean value){
        btn_new_email.setSelected(value);
    }

    /**
     * Handles the button action of an email.
     * The view for viewing the email is opened.
     *
     * @param email the selected email
     */
    protected void onEmailClick(Email email) {
        btn_incoming_email.setSelected(false);
        btn_new_email.setSelected(false);
        try {
            FXMLLoader listLoader = new FXMLLoader(Main.class.getResource("view/mail-client-email-view.fxml"));
            Parent root = listLoader.load();
            controllerEmail = listLoader.getController();
            controllerEmail.setControllerHome(this);
            content.getChildren().add(root);
            controllerEmail.setContent(email);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
