package com.prog3.common.model;

import java.io.Serializable;

public class SendEmailRequest implements Serializable {
    private Email email;

    public SendEmailRequest(Email email) {
        this.email = email;
    }

    public Email getEmail() {
        return email;
    }
}
