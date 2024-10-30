package com.example.gestioncontact.models;

public class User {
    private int id;
    private String username;
    private String phone;
    private String password;

    public User(int id, String username, String phone, String password) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.password = password;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
