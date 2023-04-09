package com.prog3.common.model;

import java.io.Serializable;

public class ReceivedEmailRequest implements Serializable {
    private String email;

    public ReceivedEmailRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

}