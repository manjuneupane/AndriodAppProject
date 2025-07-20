// In ProductModel.java

package com.example.osmonlinesabjimandi.Model;

import com.google.firebase.database.PropertyName;
import java.io.Serializable;

// This class now perfectly matches your "Items" data structure in the JSON file.
public class ProductModel implements Serializable {

    @PropertyName("CategoryId")
    private Long CategoryId;

    @PropertyName("Description")
    private String Description;

    @PropertyName("Id")
    private Long Id;

    @PropertyName("ImagePath")
    private String ImagePath;

    @PropertyName("LocationId")
    private Long LocationId;

    @PropertyName("Price")
    private Double Price;

    @PropertyName("Star")
    private Double Star;

    @PropertyName("Title")
    private String Title;

    // Default constructor is required for Firebase
    public ProductModel() {}

    public ProductModel(Long id, String title, Double price, String imagePath) { // Changed id to Long
        this.Id = id;
        this.Title = title;
        this.Price = price;
        this.ImagePath = imagePath;
        // Initialize other fields to null or default if not provided
        this.CategoryId = null;
        this.Description = null;
        this.LocationId = null;
        this.Star = null;
    }


    // Getters and Setters
    public Long getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(Long categoryId) {
        CategoryId = categoryId;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public Long getLocationId() {
        return LocationId;
    }

    public void setLocationId(Long locationId) {
        LocationId = locationId;
    }

    public Double getPrice() {
        return Price;
    }

    public void setPrice(Double price) {
        Price = price;
    }

    public Double getStar() {
        return Star;
    }

    public void setStar(Double star) {
        Star = star;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }




}