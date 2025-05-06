package com.example.join.menu.chat;

public class UserModel {
    private String id;
    private String username;

    public UserModel() {}

    public UserModel(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
