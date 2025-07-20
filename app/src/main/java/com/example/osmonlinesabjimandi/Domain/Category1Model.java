package com.example.osmonlinesabjimandi.Domain; // You might want to create a 'Model' package for your data classes

public class Category1Model {
    private String name;
    private String imageUrl; // Assuming you might have an image for each category

    // Default constructor required for Firebase Realtime Database
    public Category1Model() {
    }

    public Category1Model(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters (Crucial for Firebase data mapping)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
