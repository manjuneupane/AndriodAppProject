package com.example.osmonlinesabjimandi.Activity;

// REMOVE THIS INCORRECT IMPORT:
// import static android.os.Build.VERSION_CODES.R; // <-- DELETE THIS LINE

import android.content.Intent; // You might use this later for navigation, keeping it.
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.osmonlinesabjimandi.R; // Correct import for your project's resources
import com.bumptech.glide.Glide; // <-- ADD THIS IMPORT for Glide

// If you plan to use Firestore:
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue; // Needed for FieldValue.serverTimestamp()

// If you plan to use Firebase Storage for images:
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth; // Assuming you'll use FirebaseAuth, though not directly in this snippet's logic for user.

import java.util.HashMap;
import java.util.Map;
import java.util.UUID; // For unique image filenames

public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "AddItemActivity";

    private ImageView itemImageView;
    private EditText itemNameEditText;
    private EditText itemDescriptionEditText;
    private EditText itemPriceEditText;
    private EditText itemUnitEditText;
    private Spinner categorySpinner;
    private Button addItemButton;

    private Uri selectedImageUri; // To store the URI of the selected image

    // Firebase instances
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    // private FirebaseAuth mAuth; // If you need FirebaseAuth for anything specific in AddItemActivity

    // ActivityResultLauncher for picking images
    private ActivityResultLauncher<String> pickImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // CORRECTED: Use R.layout.your_layout_name
        setContentView(R.layout.activity_add__item);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        // mAuth = FirebaseAuth.getInstance(); // Uncomment if you need FirebaseAuth here

        // Initialize views
        ImageView backButton = findViewById(R.id.backButton); // This ID is from your XML
        itemImageView = findViewById(R.id.itemImageView);
        itemNameEditText = findViewById(R.id.itemNameEditText);
        itemDescriptionEditText = findViewById(R.id.itemDescriptionEditText);
        itemPriceEditText = findViewById(R.id.itemPriceEditText);
        itemUnitEditText = findViewById(R.id.itemUnitEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        addItemButton = findViewById(R.id.addItemButton);

        // Set up back button
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed(); // Go back to the previous activity
                }
            });
        }

        // Setup image picker launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        // Glide is now imported correctly
                        Glide.with(AddItemActivity.this).load(selectedImageUri).into(itemImageView);
                    }
                }
        );

        // Set up image view click listener to pick image
        itemImageView.setOnClickListener(v -> pickImage());

        // Setup Spinner Adapter (replace with real categories later)
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.product_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);


        // Set up Add Item button click listener
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadItem();
            }
        });
    }

    private void pickImage() {
        // Launch image picker
        pickImageLauncher.launch("image/*");
    }

    private void uploadItem() {
        String name = itemNameEditText.getText().toString().trim();
        String description = itemDescriptionEditText.getText().toString().trim();
        String priceStr = itemPriceEditText.getText().toString().trim();
        String unit = itemUnitEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();

        // Basic validation
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image for the item.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.isEmpty()) {
            itemNameEditText.setError("Item Name is required.");
            itemNameEditText.requestFocus();
            return;
        }
        if (priceStr.isEmpty()) {
            itemPriceEditText.setError("Price is required.");
            itemPriceEditText.requestFocus();
            return;
        }
        if (unit.isEmpty()) {
            itemUnitEditText.setError("Unit is required (e.g., KG, Piece).");
            itemUnitEditText.requestFocus();
            return;
        }
        if (category.equals("Select Category")) { // Assuming your first spinner item is a placeholder
            Toast.makeText(this, "Please select a category.", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        // Show loading or disable button
        addItemButton.setEnabled(false);
        addItemButton.setText("Uploading...");
        Toast.makeText(this, "Uploading item...", Toast.LENGTH_LONG).show();


        // 1. Upload image to Firebase Storage
        StorageReference imageRef = storageRef.child("product_images/" + UUID.randomUUID().toString() + ".jpg");
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get image URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // 2. Save item details to Firestore
                        saveItemToFirestore(name, description, price, unit, category, imageUrl);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(AddItemActivity.this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        addItemButton.setEnabled(true);
                        addItemButton.setText("Add Item");
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddItemActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    addItemButton.setEnabled(true);
                    addItemButton.setText("Add Item");
                });
    }

    private void saveItemToFirestore(String name, String description, double price, String unit, String category, String imageUrl) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("name", name);
        itemData.put("description", description);
        itemData.put("price", price);
        itemData.put("unit", unit);
        itemData.put("category", category); // Consider using category ID if applicable
        itemData.put("imageUrl", imageUrl);
        itemData.put("timestamp", FieldValue.serverTimestamp()); // Correct FieldValue import
        // You might also add seller ID, stock quantity, etc.
        itemData.put("isFeatured", false); // Default to false, can be changed later

        db.collection("products") // Your Firestore collection name for products
                .add(itemData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddItemActivity.this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                    addItemButton.setEnabled(true);
                    addItemButton.setText("Add Item");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddItemActivity.this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    addItemButton.setEnabled(true);
                    addItemButton.setText("Add Item");
                });
    }

    private void clearForm() {
        itemImageView.setImageResource(R.drawable.placeholder_image);
        selectedImageUri = null;
        itemNameEditText.setText("");
        itemDescriptionEditText.setText("");
        itemPriceEditText.setText("");
        itemUnitEditText.setText("");
        categorySpinner.setSelection(0); // Reset spinner to first item
    }
}