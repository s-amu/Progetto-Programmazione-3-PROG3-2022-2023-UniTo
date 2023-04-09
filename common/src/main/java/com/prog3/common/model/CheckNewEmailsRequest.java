package com.prog3.common.model;

import java.io.Serializable;

public class CheckNewEmailsRequest implements Serializable {
    private final String email;
    private final int lastEmailId;

    public CheckNewEmailsRequest(String email, int lastEmailId) {
        this.email = email;
        this.lastEmailId = lastEmailId;
    }

    public String getEmail() {
        return email;
    }

    public int getLastEmailId() {
        return lastEmailId;
    }
}
