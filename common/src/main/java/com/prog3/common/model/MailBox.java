package com.prog3.common.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MailBox implements Serializable {
    private String name;
    private String email;
    private List<Email> listReceivedEmail = new ArrayList<>();
    public transient ListProperty<Email> receivedEmails;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Email> getListReceivedEmails() {
        return listReceivedEmail;
    }

    public void setListReceivedEmail(List<Email> listReceivedEmail) {
        this.listReceivedEmail = listReceivedEmail;
    }

    public final ListProperty<Email> receivedEmailsProperty() {
        return this.receivedEmails;
    }

    public void setReceivedEmails(ObservableList<Email> receivedEmails) {
        this.receivedEmails = new SimpleListProperty<>();
        this.receivedEmails.set(receivedEmails);
    }

    public MailBox() {
        this.receivedEmails = new SimpleListProperty<>(FXCollections.observableArrayList());
    }
}
