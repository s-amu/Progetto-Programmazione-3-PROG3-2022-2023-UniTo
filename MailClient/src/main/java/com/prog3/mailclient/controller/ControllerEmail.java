package com.prog3.mailclient.controller;

import com.prog3.common.model.DeleteEmailRequest;
import com.prog3.common.model.Email;
import com.prog3.mailclient.Main;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ControllerEmail implements Initializable {
    @FXML
    private BorderPane borderpane;
    @FXML
    private Label lbl_sender;
    @FXML
    private Label lbl_receiver;
    @FXML
    private Label lbl_subject;
    @FXML
    private TextArea txt_content;
    private ControllerHome controllerHome;
    private Email email;

    /**
     * Initializes the controller class.
     *
     * @param url            the URL to the FXML document
     * @param resourceBundle the resources used by the FXML document
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) { }

    /**
     * Handles the click on the "Delete" button.
     * The request is sent to the server which sends it back the updated email list and then updates the listview
     * and returns to the home.
     */
    @FXML
    protected void onButtonCancelEmailClick(){
        controllerHome.connection.setFlagPopUp(true);
        controllerHome.setSelectedIncomingEmail(true);
        try{
            DeleteEmailRequest request = new DeleteEmailRequest(email.getID(),controllerHome.connection.getEmail());
            Object response = controllerHome.connection.sendRequest(request);
            boolean result;
            if(response!=null)
                result = (boolean) response;
            else
                result = false;
            if (result) {
                controllerHome.connection.getAccount().getListReceivedEmails().remove(email);
                controllerHome.connection.getAccount().receivedEmails = new SimpleListProperty(FXCollections.observableArrayList(controllerHome.connection.getAccount().getListReceivedEmails()));
                controllerHome.controllerListEmail.getLst_email().itemsProperty().bind(controllerHome.connection.getAccount().receivedEmails);
                onButtonIncomingEmailClick();
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        controllerHome.connection.setFlagPopUp(false);
    }

    /**
     * Handles the click on the "Forward" button.
     * The view for writing a new email is opened, but there is only the option to enter the recipient of the message.
     */
    @FXML
    protected void onButtonForwardEmailClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/mail-client-new-email-view.fxml"));
            Parent scene = (Parent) fxmlLoader.load();

            // Set the "To" field to the sender of the email being replied to
            ControllerNewEmail newEmailController = fxmlLoader.getController();
            newEmailController.setConnection(controllerHome.connection);
            newEmailController.setControllerHome(controllerHome);
            newEmailController.setSubjectField(email.getSubject());
            newEmailController.setTextField("Forwarded message: " + email.getText());

            Stage stage = new Stage();
            stage.setScene(new Scene(scene));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        controllerHome.connection.setFlagPopUp(false);
    }

    /**
     * Handles the click on the "Reply" button.
     * The view for writing a new email opens, but you can only enter the subject and text of the message.
     */
    @FXML
    protected void onButtonReplyEmailClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/mail-client-new-email-view.fxml"));
            Parent scene = (Parent) fxmlLoader.load();

            // Set the To field to the sender of the email being replied to
            ControllerNewEmail newEmailController = fxmlLoader.getController();
            newEmailController.setConnection(controllerHome.connection);
            newEmailController.setControllerHome(controllerHome);

            newEmailController.setToField(email.getSender());
            newEmailController.setSubjectField("Re: " + email.getSubject());

            Stage stage = new Stage();
            stage.setScene(new Scene(scene));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        controllerHome.connection.setFlagPopUp(false);
    }

    /**
     * Handles the click on the "Reply All" button.
     * The view for writing a new email opens, but you can only enter the subject and text of the message.
     */
    @FXML
    protected void onButtonReplyAllEmailClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view/mail-client-new-email-view.fxml"));
            Parent scene = (Parent) fxmlLoader.load();

            // Set the To field to the sender of the email being replied to
            ControllerNewEmail newEmailController = fxmlLoader.getController();
            newEmailController.setConnection(controllerHome.connection);
            newEmailController.setControllerHome(controllerHome);

            String to = email.getSender();
            for(String r : email.getReceiver()){
                r = r.replaceAll("\\s+", "");
                if(!r.equals(controllerHome.connection.getAccount().getEmail())){
                    to += ", " + r;
                }
            }
            newEmailController.setToField(to);
            newEmailController.setSubjectField("Re: " + email.getSubject());

            Stage stage = new Stage();
            stage.setScene(new Scene(scene));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        controllerHome.connection.setFlagPopUp(false);
    }

    /**
     * Handles the action of the "Incoming email" button.
     * The home is updated by checking if there are new emails present.
     */
    public void onButtonIncomingEmailClick() {
        controllerHome.connection.getAccount().receivedEmails = new SimpleListProperty(FXCollections.observableArrayList(controllerHome.connection.getAccount().getListReceivedEmails()));
        controllerHome.controllerListEmail.onButtonIncomingEmailClick();
        borderpane.setVisible(false);
        controllerHome.connection.setFlagPopUp(false);
    }

    /**
     * Method for setting email content
     *
     * @param email
     */
    public void setContent(Email email) {
        this.email = email;
        lbl_sender.setText(email.getSender());
        List<String> myList = email.getReceiver();
        String receivers = String.join(", ", myList);
        lbl_receiver.setText(receivers);
        lbl_subject.setText(email.getSubject());
        txt_content.setText(email.getText());
    }

    /**
     * Set the reference to the ControllerHome instance.
     *
     * @param controllerHome the ControllerHome instance
     */
    public void setControllerHome(ControllerHome controllerHome){
        this.controllerHome = controllerHome;
    }
}