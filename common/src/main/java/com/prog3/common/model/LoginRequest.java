package com.prog3.common.model;

import java.io.Serializable;

public class LoginRequest implements Serializable {
    private String email; // email of the login

    public LoginRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}