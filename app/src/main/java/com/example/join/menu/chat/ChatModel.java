package com.example.join.menu.chat;

public class ChatModel {
    private String userId;
    private String username;
    private String lastMessage;

    public ChatModel(String userId, String username, String lastMessage) {
        this.userId = userId;
        this.username = username;
        this.lastMessage = lastMessage;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
