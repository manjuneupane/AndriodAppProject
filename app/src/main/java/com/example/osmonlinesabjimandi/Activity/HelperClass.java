package com.example.osmonlinesabjimandi.Activity;


public class HelperClass {

    String name, email, username, password;

    // Default constructor required for Firebase Realtime Database
    public HelperClass() {
    }

    // Parameterized constructor to set the user data
    public HelperClass(String name, String email, String username, String password) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password; // Note: Storing password here is generally not recommended as Firebase Auth handles it securely.
    }

    // You should also add getter and setter methods for each field
    // Getters are crucial for Firebase to properly retrieve data

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}