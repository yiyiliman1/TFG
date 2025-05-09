package com.example.join.menu.chat;

import java.util.Date;

public class Mensaje {
    private String senderId;
    private String text;
    private Date timestamp;

    public Mensaje() {} // Constructor vacío necesario para Firestore

    public Mensaje(String senderId, String text, Date timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSenderId() { return senderId; }
    public String getText() { return text; }
    public Date getTimestamp() { return timestamp; }
}

