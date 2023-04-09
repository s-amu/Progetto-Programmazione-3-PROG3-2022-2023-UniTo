package com.prog3.common.model;

import java.io.Serializable;

public class DeleteEmailRequest implements Serializable {
    private int idEmail; // id of the email to delete
    private String email;

    public DeleteEmailRequest(int idEmail, String email) {
        this.idEmail = idEmail;
        this.email = email;
    }

    public int getIdEmail() {
        return idEmail;
    }

    public String getEmail() {
        return email;
    }
}