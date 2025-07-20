package com.example.osmonlinesabjimandi.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.osmonlinesabjimandi.Model.ProductModel;
import com.example.osmonlinesabjimandi.Model.CategoryModel; // Correctly imported
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private DatabaseReference productsRef;
    private DatabaseReference categoriesRef;

    public ProductRepository() {
        productsRef = FirebaseDatabase.getInstance().getReference("Items");
        categoriesRef = FirebaseDatabase.getInstance().getReference("Categories");
    }

    // 🔍 Search for products by name
    public MutableLiveData<List<ProductModel>> searchProducts(String query) {
        MutableLiveData<List<ProductModel>> searchResultsLiveData = new MutableLiveData<>();

        if (query == null || query.trim().isEmpty()) {
            searchResultsLiveData.setValue(new ArrayList<>());
            return searchResultsLiveData;
        }

        Query searchQuery = productsRef.orderByChild("name")
                .startAt(query)
                .endAt(query + "\uf8ff");

        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ProductModel> productsList = new ArrayList<>();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    ProductModel product = productSnapshot.getValue(ProductModel.class);
                    if (product != null) {
                        productsList.add(product);
                    }
                }
                searchResultsLiveData.setValue(productsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                searchResultsLiveData.setValue(null);
            }
        });

        return searchResultsLiveData;
    }

    // 📦 Fetch categories from Firebase
    public MutableLiveData<List<CategoryModel>> getCategories() {
        MutableLiveData<List<CategoryModel>> categoriesLiveData = new MutableLiveData<>();

        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryModel> categoryList = new ArrayList<>(); // Corrected from Category1Model to CategoryModel
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    CategoryModel category = categorySnapshot.getValue(CategoryModel.class); // Corrected
                    if (category != null) {
                        categoryList.add(category);
                    }
                }
                categoriesLiveData.setValue(categoryList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                categoriesLiveData.setValue(null);
            }
        });

        return categoriesLiveData;
    }
}