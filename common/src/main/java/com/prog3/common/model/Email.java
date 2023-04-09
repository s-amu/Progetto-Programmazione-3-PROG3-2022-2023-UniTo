package com.prog3.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class Email implements Serializable {
    private String sender;
    private List<String> receiver;
    private int ID;
    private String subject;
    private String text;
    private LocalDateTime date;

    public Email() { }

    public Email(String sender, List<String> receiver, int ID, String subject, String text, LocalDateTime date) {
        this.sender = sender;
        this.receiver = receiver;
        this.ID = ID;
        this.subject = subject;
        this.text = text;
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public List<String> getReceiver() {
        return receiver;
    }

    public void setReceiver(List<String> receiver) {
        this.receiver = receiver;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Email{" +
                "sender='" + sender + '\'' +
                "| receiver=" + toStringSemiColumn(receiver) +
                "| ID=" + ID +
                "| subject='" + subject + '\'' +
                "| text='" + text + '\'' +
                "| date=" + date +
                '}';
    }

    private static String toStringSemiColumn(List<String> receiver) {
        StringJoiner joiner = new StringJoiner("; ");
        for (String item : receiver) {
            joiner.add(item);
        }
        String receiverStr = "receiver=[" + joiner.toString() + "]";
        return receiverStr;
    }

    public static Email fromString(String str) {
        String[] parts = str.split("\\{")[1].split("\\}")[0].split("\\| ");
        String sender = parts[0].split("='")[1].split("'")[0];
        String[] receiversArray = parts[1].split("=\\[")[1].split("\\]")[0].split(";");
        List<String> receivers = new ArrayList<>(Arrays.asList(receiversArray));
        int ID = Integer.parseInt(parts[2].split("=")[1]);
        String subject = parts[3].split("='")[1].split("'")[0];
        String text = parts[4].split("='")[1].split("'")[0];
        LocalDateTime date = LocalDateTime.parse(parts[5].split("=")[1]);
        return new Email(sender, receivers, ID, subject, text, date);
    }
}