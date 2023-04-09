package com.prog3.mailclient.controller;

import com.prog3.common.model.Email;
import com.prog3.mailclient.Connection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the list of emails in the inbox.
 */
public class ControllerListEmail implements Initializable {
    @FXML
    private ListView<Email> lst_email;
    private Connection connection = null;
    private ControllerHome controllerHome;
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    /**
     * Initializes the controller class.
     *
     * @param url            the URL to the FXML document
     * @param resourceBundle the resources used by the FXML document
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lst_email.setOnMouseClicked(this::showSelectedEmail);
        lst_email.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            lst_email.refresh();
        });
    }

    /**
     * Set the reference to the ControllerHome instance.
     * @param controllerHome the ControllerHome instance
     */
    public void setControllerHome(ControllerHome controllerHome) {
        this.controllerHome = controllerHome;
        controllerHome.setSelectedIncomingEmail(true);
    }

    /**
     * Sets the connection to the email server.
     * @param connection the connection to the email server
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Returns the list view of emails.
     * @return the list view of emails
     */
    public ListView<Email> getLst_email(){
        return lst_email;
    }

    /**
     * Method to show the selected mail in the view
     * @param mouseEvent
     */
    protected void showSelectedEmail(MouseEvent mouseEvent) {
        controllerHome.setSelectedNewEmail(false);
        controllerHome.setSelectedIncomingEmail(false);
        Email email = lst_email.getSelectionModel().getSelectedItem();
        lst_email.getSelectionModel().clearSelection();
        updateDetailView(email);
    }

    /**
     * Updates the detail view with the selected email.
     * @param email the selected email
     */
    protected void updateDetailView(Email email) {
        controllerHome.onEmailClick(email);
        lst_email.setVisible(false);
    }

    /**
     * Handles the click on the "Incoming Emails" button.
     * Starts the email checker to update the list of received emails.
     */
    public void onButtonIncomingEmailClick() {
        lst_email.setVisible(true);
        controllerHome.setSelectedNewEmail(false);
        controllerHome.setSelectedIncomingEmail(true);
        EmailChecker checker = new EmailChecker(connection, this);
        executor.scheduleAtFixedRate(checker, 0, 2, TimeUnit.SECONDS);
        connection.getAccount().setReceivedEmails(FXCollections.observableArrayList(connection.getAccount().getListReceivedEmails()));
        Collections.sort(connection.getAccount().receivedEmails, Comparator.comparing(Email::getDate).reversed());
        lst_email.itemsProperty().bind(connection.getAccount().receivedEmailsProperty());
        lst_email.setCellFactory(param -> new EmailCell());
        lst_email.refresh();
    }
}
