package com.prog3.mailclient.controller;

import com.prog3.common.model.CheckNewEmailsRequest;
import com.prog3.common.model.Email;
import com.prog3.common.model.ReceivedEmailRequest;
import com.prog3.mailclient.Connection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class EmailChecker implements Runnable {
    Connection connection;
    ControllerListEmail controllerListEmail;

    /**
     * Constructs a new EmailChecker object with the specified connection and email list controller.
     * @param connection the connection to use for accessing the email inbox
     * @param controllerListEmail the email list controller to update the view when new emails arrive
     */
    public EmailChecker(Connection connection, ControllerListEmail controllerListEmail) {
        this.connection = connection;
        this.controllerListEmail = controllerListEmail;
    }

    /**
     * This method runs on a separate thread and checks for new emails in the user's email account using the provided
     * connection object.
     * If there are new emails, it adds them to the list of received emails and updates the view using the email list
     * controller.
     * It also displays a pop-up alert to notify the user of the new email(s).
     */
    @Override
    public void run() {
        try {
            int lastEmailId = 0;

            // Find the ID of the latest email received
            if (connection.getAccount().getListReceivedEmails().size() >= 1) {
                lastEmailId = connection.getAccount().getListReceivedEmails().get(connection.getAccount().getListReceivedEmails().size() - 1).getID();
            } else {
                lastEmailId = -1;
            }

            // Send a request to the server to check for new emails
            CheckNewEmailsRequest request = new CheckNewEmailsRequest(connection.getAccount().getEmail(), lastEmailId);
            Object response = connection.sendRequest(request);

            if (response instanceof List) {
                // If there are new emails, add them to the list of received emails
                List<Email> emailList = (ArrayList<Email>) response;
                for (int i = 0; i < emailList.size(); i++) {
                    connection.getAccount().getListReceivedEmails().add(emailList.get(i));
                }

                // Send a request to mark the received emails as read
                ReceivedEmailRequest receivedEmailRequest = new ReceivedEmailRequest(connection.getAccount().getEmail());
                try {
                    connection.sendRequest(receivedEmailRequest);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                // Update the list view with the new emails
                connection.getAccount().setReceivedEmails(FXCollections.observableArrayList(connection.getAccount().getListReceivedEmails()));
                Collections.sort(connection.getAccount().receivedEmails, Comparator.comparing(Email::getDate).reversed());
                Platform.runLater(() -> {
                    controllerListEmail.getLst_email().refresh();
                    controllerListEmail.getLst_email().itemsProperty().bind(connection.getAccount().receivedEmails);
                    showNewEmailsAlert();
                });
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        Thread.currentThread().interrupt();
    }

    /**
     * This method shows an alert indicating that the user has received one or more new emails.
     */
    private void showNewEmailsAlert(){
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Incoming email");
        alert.setContentText("You have received one or more new emails.\nPlease check your inbox.");
        // Add an OK button to the alert
        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: linear-gradient(to right, rgb(248, 115, 43), rgb(246, 33, 153)); " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-text-alignment: CENTER;" +
                "-fx-border-width: 0px;" +
                "-fx-border-radius: 0.4em;" +
                "-fx-background-radius: 0.4em;");
        alert.getDialogPane().setStyle("-fx-background-color: white;");
        // Show the alert and wait for the user to close it
        alert.showAndWait();
    }
}
