package com.prog3.mailclient.controller;

import com.prog3.common.model.LoginRequest;
import com.prog3.common.model.MailBox;
import com.prog3.mailclient.Connection;
import com.prog3.mailclient.Main;
import javafx.beans.binding.ObjectExpression;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.regex.Pattern;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.application.Application.launch;

public class ControllerLogin implements Initializable {
    @FXML
    private ImageView btn_close;
    @FXML
    private TextField txt_email;
    @FXML
    private Label lbl_err_email;
    @FXML
    private Button btn_login;
    private final String IP = "127.0.0.1";
    private final int port = 4445;
    private ControllerHome controllerHome;
    private ControllerListEmail controllerListEmail;
    Connection connection = null;

    /**
     * Initializes the controller class.
     *
     * @param url            the URL to the FXML document
     * @param resourceBundle the resources used by the FXML document
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { }

    public void setControllerListEmail(ControllerListEmail controllerListEmail) {
        this.controllerListEmail = controllerListEmail;
    }

    /**
     * This method is used to close the login window.
     */
    @FXML
    protected void onCloseButtonClick() {
        ((Stage) btn_close.getScene().getWindow()).close();
    }

    /**
     * Handles the action of the "Login" button. Check to see if the email has been entered and if the email is
     * syntactically correct.
     * Furthermore, if the email entered is correct, the connection takes place and the home view is opened.
     */
    @FXML
    protected void onLoginButtonClick() {
        boolean regex = Pattern.matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$", txt_email.getText().trim());
        // Check if the email is syntactically correct
        if (!regex) {
            if (txt_email.getText().trim().isEmpty()) {
                lbl_err_email.setText("Please enter your email");
            }
            else  {
                lbl_err_email.setText("Enter a valid email address");
            }
        } else {
            try {
                // Initialize the connection with server
                if (connection == null) {
                    try {
                        connection = new Connection(IP, port);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                connection.setFlagPopUp(false);
                String email = txt_email.getText();
                LoginRequest loginRequest = new LoginRequest(email);

                // Send the request to the server
                Object response = connection.sendRequest(loginRequest);
                MailBox account = (MailBox) response;
                connection.setAccount(account);

                // If the response is the mailbox matches the inserted email
                if (account != null && account.getEmail().equals(email)) {
                    // Opening home view
                    FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/mail-client-home-view.fxml"));
                    Parent scene = (Parent) fxmlLoader.load();
                    controllerHome = fxmlLoader.getController();
                    controllerHome.setConnection(connection);
                    controllerHome.setControllerLogin(this);
                    controllerListEmail.setControllerHome(controllerHome);
                    controllerListEmail.setConnection(connection);
                    controllerListEmail.onButtonIncomingEmailClick();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(scene,800,520));
                    controllerHome.setStage(stage);
                    stage.show();
                    // Closing login view
                    ((Stage) btn_login.getScene().getWindow()).close();
                } else {
                    lbl_err_email.setText("This email does not exist");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
