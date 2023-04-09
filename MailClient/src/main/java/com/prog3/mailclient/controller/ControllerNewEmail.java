package com.prog3.mailclient.controller;

import com.prog3.common.model.Email;
import com.prog3.common.model.SendEmailRequest;
import com.prog3.mailclient.Connection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * This class represents the controller for the "New Email" view in the email client application.
 * It handles user input and sending of new emails (including reply, reply all and forward).
 * The controller interacts with the connection object to send the email to the server.
 * It also performs various checks on the input fields to ensure that they are valid before sending the email.
 */
public class ControllerNewEmail implements Initializable {
    @FXML
    private TextField txt_to;
    @FXML
    private TextField txt_subject;
    @FXML
    private TextArea txt_text;
    @FXML
    private Button btn_send;
    private Connection connection = null;
    private ControllerHome controllerHome;

    public void setConnection(Connection connection) { this.connection = connection; }

    public void setControllerHome(ControllerHome controllerHome){
        this.controllerHome = controllerHome;
    }

    public void setToField(String sender) {
        txt_to.setText(sender);
    }

    public void setSubjectField(String subject) {
        txt_subject.setText(subject);
    }

    public void setTextField(String text) {
        txt_text.setText(text);
    }

    /**
     * Method that checks if the email is correct
     */
    private boolean isEmailCorrect(String emailTo){
        String[] emails = emailTo.split(",\\s");
        boolean correct = true;
        for (String email : emails) {
            if (!Pattern.matches("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$", email.trim())) {
                correct = false;
                break;
            }
        }
        return correct;
    }

    /**
     * Shows a generic alert.
     * @param title the title of the alert
     * @param message the content of the alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle(title);
        alert.setContentText(message);
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
        alert.showAndWait();
    }

    /**
     * Handles the action of the "Send" button, sending a new email with the specified recipients, subject, and content.
     * Performs various checks on the input fields and displays an error message if any of them are not valid.
     * If the input is valid, creates a new Email object and sends it to the server with a SendEmailRequest.
     * Closes the window if the email is sent successfully.
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @FXML
    private void onSendButtonClick() throws IOException, ClassNotFoundException {
        connection.setFlagPopUp(true);
        String emailTo = txt_to.getText();

        boolean correct = isEmailCorrect(emailTo);
        if (txt_to.getText().isEmpty()) {
            connection.setFlagPopUp(false);
            // No recipient specified
            showAlert("Error", "Please specify at least one recipient.");
        } else if (!correct) {
            connection.setFlagPopUp(false);
            // Email format not valid
            showAlert("Error", "The address in the \"To\" field was not recognised.\nPlease make sure that all addresses are properly formed.\n.");
        } else if(txt_subject.getText().isEmpty()){
            connection.setFlagPopUp(false);
            // No subject specified
            showAlert("Error", "Please specify the subject.");
        } else if(txt_text.getText().isEmpty()){
            connection.setFlagPopUp(false);
            // No content specified
            showAlert("Error", "Please specify the text of the email.");
        } else {
            // Passed all the controls
            // Add the email receivers to the receivers arraylist
            ArrayList<String> receivers = new ArrayList<>();
            String[] receiverArray = txt_to.getText().split(",");
            for (String receiver : receiverArray) {
                receivers.add(receiver.trim());
            }
            // Create the new email
            Email email = new Email(connection.getEmail(), receivers, -1, txt_subject.getText(), txt_text.getText(), LocalDateTime.now());
            // Send request
            SendEmailRequest sendEmailRequest = new SendEmailRequest(email);
            Object response = connection.sendRequest(sendEmailRequest);
            if(response != null){
                boolean isSent = (boolean) response;
                if(isSent){
                    // Close the window
                    ((Stage) btn_send.getScene().getWindow()).close();
                }
            }
            controllerHome.onButtonIncomingEmailClick();
            controllerHome.setSelectedIncomingEmail(true);
        }
        connection.setFlagPopUp(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        txt_text.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getText().contains("|")) {
                change.setText("");
            }
            return change;
        }));
    }
}
