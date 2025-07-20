package com.example.osmonlinesabjimandi.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.osmonlinesabjimandi.Model.CartItem;
import com.example.osmonlinesabjimandi.Model.ProductModel; // Make sure your ProductModel is correctly imported
import com.example.osmonlinesabjimandi.R;
import com.example.osmonlinesabjimandi.Utils.CartManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale; // For price formatting

public class ProductDetailActivity extends AppCompatActivity {

    // UI Elements
    private ImageView productImageView;
    private TextView productNameTextView;
    private TextView productDescriptionTextView;
    private TextView productPriceTextView;
    private TextView productUnit; // Assuming you have a TextView for unit
    private TextView quantityTextView;
    private Button incrementBtn, decrementBtn;
    private Button addToCartBtn;
    private ProgressBar progressBar; // To show loading state

    // Firebase
    private FirebaseFirestore db;

    // Product Data (will be loaded from Firebase)
    private String productId;
    private ProductModel currentProduct; // Holds the fetched product data
    private int selectedQuantity = 1; // Default quantity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_product); // Make sure you have this layout

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        productImageView = findViewById(R.id.productImageView);
        productNameTextView = findViewById(R.id.productName);
//        productDescriptionTextView = findViewById(R.id.productDescription);
        productPriceTextView = findViewById(R.id.productPrice);
//        productUnitTextView = findViewById(R.id.productUnitTextView); // Assume you have this ID
//        quantityTextView = findViewById(R.id.quantityTextView);
//        incrementBtn = findViewById(R.id.incrementBtn);
//        decrementBtn = findViewById(R.id.decrementBtn);
        addToCartBtn = findViewById(R.id.addToCartButton);
//        progressBar = findViewById(R.id.progressBar); // Assume you have this ID

        // Get product ID from Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("productId")) {
            productId = intent.getStringExtra("productId");
            fetchProductDetails(productId);
        } else {
            Toast.makeText(this, "Product ID not found.", Toast.LENGTH_LONG).show();
            finish(); // Close activity if no product ID
        }

        // Set up quantity buttons
        incrementBtn.setOnClickListener(v -> {
            selectedQuantity++;
            quantityTextView.setText(String.valueOf(selectedQuantity));
        });

        decrementBtn.setOnClickListener(v -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                quantityTextView.setText(String.valueOf(selectedQuantity));
            }
        });

        // Set up Add to Cart button listener
        addToCartBtn.setOnClickListener(v -> {
            if (currentProduct != null) {
                CartItem item = new CartItem(currentProduct, selectedQuantity);
                CartManager.getInstance().addItem(item);
                Toast.makeText(ProductDetailActivity.this, currentProduct.getTitle() + " added to Cart!", Toast.LENGTH_SHORT).show();
//                Toast.makeText(ProductDetailActivity.this, currentProduct.getName() + " added to Cart!", Toast.LENGTH_SHORT).show();
                // Optionally navigate to cart or show a confirmation dialog
            } else {
                Toast.makeText(ProductDetailActivity.this, "Product data not loaded yet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProductDetails(String pId) {
        progressBar.setVisibility(View.VISIBLE);
        // Hide other UI elements until data is loaded
        // For example:
        // findViewById(R.id.detail_content_layout).setVisibility(View.GONE);

        db.collection("products").document(pId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        progressBar.setVisibility(View.GONE);
                        // findViewById(R.id.detail_content_layout).setVisibility(View.VISIBLE); // Show content

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Convert Firestore document to ProductModel object
                                // Ensure your ProductModel has a no-argument constructor if you use .toObject()
                                // Or manually map fields
                                currentProduct = document.toObject(ProductModel.class);

                                if (currentProduct != null) {
                                    // Set data to UI
                                    productNameTextView.setText(currentProduct.getTitle());
                                    productDescriptionTextView.setText(currentProduct.getDescription()); // Assuming description field exists
                                    productPriceTextView.setText(String.format(Locale.getDefault(), "₹ %.2f", currentProduct.getPrice()));
                                    // productUnit.setText("/ " + currentProduct.getUnit()); // Remove this line// Display unit

                                    // Load image using Glide
                                    if (currentProduct.getImagePath() != null && !currentProduct.getImagePath().isEmpty()) {
                                        Glide.with(ProductDetailActivity.this)
                                                .load(currentProduct.getImagePath())
                                                .placeholder(R.drawable.placeholder_image) // Your placeholder drawable
                                                .error(R.drawable.error_image)         // Your error drawable
                                                .into(productImageView);
                                    } else {
                                        productImageView.setImageResource(R.drawable.placeholder_image);
                                    }
                                } else {
                                    Toast.makeText(ProductDetailActivity.this, "Failed to parse product data.", Toast.LENGTH_LONG).show();
                                    finish();
                                }

                            } else {
                                Toast.makeText(ProductDetailActivity.this, "Product not found.", Toast.LENGTH_LONG).show();
                                finish(); // Close activity if product not found
                            }
                        } else {
                            Toast.makeText(ProductDetailActivity.this, "Error loading product: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            finish(); // Close activity on error
                        }
                    }
                });
    }
}