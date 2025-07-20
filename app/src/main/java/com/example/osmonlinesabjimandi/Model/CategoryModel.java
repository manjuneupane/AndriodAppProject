package com.example.osmonlinesabjimandi.Model;

import com.google.firebase.database.PropertyName;

public class CategoryModel {

    // Change 'String' to 'Long' for the id field
    @PropertyName("Id")
    private Long id; // Changed from String to Long
    @PropertyName("Name")
    private String name;
    @PropertyName("ImagePath")
    private String imageUrl;

    public CategoryModel() {
        // Required empty public constructor for Firebase Realtime Database
    }

    public CategoryModel(Long id, String name, String imageUrl) { // Changed String id to Long id
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    @PropertyName("Id")
    public Long getId() { return id; } // Changed return type to Long
    @PropertyName("Id")
    public void setId(Long id) { this.id = id; } // Changed parameter type to Long

    @PropertyName("Name")
    public String getName() { return name; }
    @PropertyName("Name")
    public void setName(String name) { this.name = name; }

    @PropertyName("ImagePath")
    public String getImageUrl() { return imageUrl; }
    @PropertyName("ImagePath")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}