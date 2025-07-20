package com.example.osmonlinesabjimandi.ViewModel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.osmonlinesabjimandi.Model.CategoryModel;
import com.example.osmonlinesabjimandi.Model.ProductModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    private MutableLiveData<List<CategoryModel>> categoriesLiveData;
    private MutableLiveData<List<ProductModel>> productsLiveData;
    private MutableLiveData<List<ProductModel>> searchResultsLiveData;

    private List<ProductModel> allProductsCached;

    private DatabaseReference categoriesRef;
    private DatabaseReference productsRef;

    public MainViewModel() {
        categoriesLiveData = new MutableLiveData<>();
        productsLiveData = new MutableLiveData<>();
        searchResultsLiveData = new MutableLiveData<>();
        allProductsCached = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (database == null) {
            Log.e(TAG, "FirebaseDatabase.getInstance() returned null. Firebase might not be initialized correctly.");
            categoriesLiveData.postValue(new ArrayList<>());
            productsLiveData.postValue(new ArrayList<>());
            searchResultsLiveData.postValue(new ArrayList<>());
            return;
        } else {
            Log.d(TAG, "FirebaseDatabase instance obtained successfully.");
        }

        categoriesRef = database.getReference("Category");
        productsRef = database.getReference("Items");

        // Initial load when ViewModel is created
        loadCategories();
        loadProductsFromDatabase();
    }

    public LiveData<List<CategoryModel>> getCategoriesLiveData() {
        return categoriesLiveData;
    }

    public LiveData<List<ProductModel>> getProductsLiveData() {
        return productsLiveData;
    }

    public LiveData<List<ProductModel>> getSearchResultsLiveData() {
        return searchResultsLiveData;
    }

    /**
     * Public method to trigger a refresh of all data from the database.
     */
    public void refreshData() {
        Log.d(TAG, "refreshData() called. Reloading categories and products.");
        loadCategories();
        loadProductsFromDatabase();
        // Optionally, reset search results if refreshing means showing all products
        searchResultsLiveData.postValue(new ArrayList<>(allProductsCached));
    }


    private void loadCategories() {
//        Log.d(TAG, "Attempting to load categories from Realtime Database path: " + categoriesRef.getPath());

        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener() { // Changed to SingleValueEvent for refresh
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CategoryModel> categories = new ArrayList<>();
                if (!dataSnapshot.exists()) {
//                    Log.d(TAG, "Realtime Database: No data found at path: " + categoriesRef.getPath());
                } else {
                    Log.d(TAG, "Realtime Database: DataSnapshot for categories exists. Children count: " + dataSnapshot.getChildrenCount());
                }

                for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                    try {
                        CategoryModel category = categorySnapshot.getValue(CategoryModel.class);
                        if (category != null) {
                            if (category.getId() == null) {
                                try {
                                    category.setId(Long.parseLong(categorySnapshot.getKey()));
                                } catch (NumberFormatException nfe) {
                                    Log.e(TAG, "Realtime Database: Cannot convert category key '" + categorySnapshot.getKey() + "' to Long for category ID. Using null for ID.", nfe);
                                    category.setId(null);
                                }
                            }
                            Log.d(TAG, "Successfully parsed category: " + category.getName() + " (ID: " + category.getId() + ", Key: " + categorySnapshot.getKey() + ")");
                            categories.add(category);
                        } else {
                            Log.e(TAG, "Realtime Database: Failed to parse CategoryModel from snapshot for key: " + categorySnapshot.getKey() + ". Raw data: " + categorySnapshot.getValue());
                        }
                    } catch (Exception parseException) {
                        Log.e(TAG, "Realtime Database: Error parsing category for key " + categorySnapshot.getKey() + ". Data: " + categorySnapshot.getValue() + ". Error: " + parseException.getMessage(), parseException);
                    }
                }
                categoriesLiveData.postValue(categories);
                Log.d(TAG, "Categories LiveData updated with " + categories.size() + " items from Realtime DB.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Realtime Database: Listen failed for categories. Error: " + databaseError.getMessage(), databaseError.toException());
                categoriesLiveData.postValue(new ArrayList<>());
            }
        });
    }

    /**
     * Loads product data from Firebase Realtime Database.
     * This method listens for changes in the "Items" node and updates the LiveData.
     */
    private void loadProductsFromDatabase() {
//        Log.d(TAG, "Attempting to load products from Realtime Database path: " + productsRef.getPath());

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() { // Changed to SingleValueEvent for refresh
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<ProductModel> products = new ArrayList<>();
                if (!dataSnapshot.exists()) {
//                    Log.d(TAG, "Realtime Database: No data found at path: " + productsRef.getPath());
                } else {
                    Log.d(TAG, "Realtime Database: DataSnapshot for products exists. Children count: " + dataSnapshot.getChildrenCount());
                }

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    try {
                        ProductModel product = productSnapshot.getValue(ProductModel.class);
                        if (product != null) {
                            if (product.getId() == null) {
                                try {
                                    product.setId(Long.parseLong(productSnapshot.getKey()));
                                } catch (NumberFormatException nfe) {
                                    Log.e(TAG, "Realtime Database: Cannot convert product key '" + productSnapshot.getKey() + "' to Long. This might be expected if your 'Items' node is an array in JSON but not a direct list of objects in DB.", nfe);
                                    product.setId(null);
                                }
                            }
                            Log.d(TAG, "Successfully parsed product: " + product.getTitle() + " (ID: " + product.getId() + ", Key: " + productSnapshot.getKey() + ")");
                            products.add(product);
                        } else {
                            Log.e(TAG, "Realtime Database: Failed to parse ProductModel from snapshot for key: " + productSnapshot.getKey() + ". Raw data: " + productSnapshot.getValue());
                        }
                    } catch (Exception parseException) {
                        Log.e(TAG, "Realtime Database: Error parsing product for key " + productSnapshot.getKey() + ". Data: " + productSnapshot.getValue() + ". Error: " + parseException.getMessage(), parseException);
                    }
                }
                productsLiveData.postValue(products);
                allProductsCached.clear();
                allProductsCached.addAll(products);
                searchResultsLiveData.postValue(new ArrayList<>(products));
                Log.d(TAG, "Products LiveData and cache updated with " + products.size() + " items from Realtime DB.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Realtime Database: Listen failed for products. Error: " + databaseError.getMessage(), databaseError.toException());
                productsLiveData.postValue(new ArrayList<>());
                allProductsCached.clear();
                searchResultsLiveData.postValue(new ArrayList<>());
            }
        });
    }

    public void performSearch(String query) {
        if (allProductsCached == null || allProductsCached.isEmpty()) {
            searchResultsLiveData.postValue(new ArrayList<>());
            return;
        }

        if (query.isEmpty()) {
            searchResultsLiveData.postValue(new ArrayList<>(allProductsCached));
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            List<ProductModel> filteredList = allProductsCached.stream()
                    .filter(product -> product.getTitle().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery))
                    .collect(Collectors.toList());
            searchResultsLiveData.postValue(filteredList);
        }
    }

    public void filterProductsByCategory(String categoryId) {
        if (allProductsCached == null || allProductsCached.isEmpty() || categoryId == null || categoryId.isEmpty()) {
            searchResultsLiveData.postValue(new ArrayList<>(allProductsCached));
            return;
        }

        try {
            Long filterCategoryId = Long.parseLong(categoryId);
            List<ProductModel> filteredList = allProductsCached.stream()
                    .filter(product -> product.getCategoryId() != null && product.getCategoryId().equals(filterCategoryId))
                    .collect(Collectors.toList());
            searchResultsLiveData.postValue(filteredList);
            Log.d(TAG, "Filtered products by category " + categoryId + ": " + filteredList.size() + " items.");
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid categoryId format: " + categoryId, e);
            searchResultsLiveData.postValue(new ArrayList<>(allProductsCached));
        }
    }
}
