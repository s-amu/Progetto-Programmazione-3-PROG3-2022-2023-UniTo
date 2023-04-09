package com.prog3.mailserver.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ControllerPrimary {
    @FXML
    private TextArea txt_log;

    @FXML
    public void printLog(String text){
        txt_log.appendText(text + "\n");
    }
}
