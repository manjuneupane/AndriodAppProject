package com.example.osmonlinesabjimandi.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView; // Corrected type for top-right cartButton
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.osmonlinesabjimandi.Adapter.CategoryAdapter;
import com.example.osmonlinesabjimandi.Adapter.ProductAdapter;
import com.example.osmonlinesabjimandi.Model.CartItem;
import com.example.osmonlinesabjimandi.Model.CategoryModel;
import com.example.osmonlinesabjimandi.Model.ProductModel;
import com.example.osmonlinesabjimandi.R;
import com.example.osmonlinesabjimandi.Utils.CartManager;
import com.example.osmonlinesabjimandi.ViewModel.MainViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * The main screen of the application. Displays categories, featured products,
 * and provides navigation and search functionality.
 *
 * Implements ProductAdapter.OnAddToCartClickListener to handle clicks on the "Add to Cart"
 * button from product listings within the RecyclerViews.
 */
public class HomeActivity extends AppCompatActivity implements ProductAdapter.OnAddToCartClickListener, CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "HomeActivity";

    // ViewModel
    private MainViewModel mainViewModel;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private ProductAdapter featuredProductsAdapter;

    // UI Elements
    private RecyclerView categoriesRecyclerView;
    private RecyclerView featuredRecyclerView;
    private ProgressBar progressBar;
    private EditText searchBar;
    private ScrollView scrollView;

    // Navigation elements (based on your XML IDs)
    private LinearLayout navHome; // This is the bottom navigation Home LinearLayout
    private LinearLayout navMessages; // This is the bottom navigation Messages LinearLayout
    private LinearLayout navAccount; // This is the bottom navigation Account LinearLayout
    private LinearLayout cartButton; // This will be the TOP-RIGHT ImageView with id="@+id/cartButton"


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Find UI Views from layout
        initializeViews();

        // Configure Adapters and RecyclerViews
        setupRecyclerViews();

        // Set up all click listeners
        setupListeners();

        // Observe LiveData from the ViewModel to update the UI
        observeViewModel();
    }

    /**
     * Initializes all the UI views from the layout file.
     */
    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        scrollView = findViewById(R.id.scrollView);
        searchBar = findViewById(R.id.searchBarTop);

        // IMPORTANT: Due to duplicate ID "cartButton" in activity_home.xml,
        // findViewById(R.id.cartButton) will find the FIRST instance, which is the ImageView at the top right.
        // The LinearLayout in the bottom navigation with the same ID will NOT be accessible via this ID.
        cartButton = findViewById(R.id.cartButton); // This will be the ImageView at the top right.

        // RecyclerViews
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        featuredRecyclerView = findViewById(R.id.featuredRecyclerView);

        // Bottom Navigation buttons
        navHome = findViewById(R.id.nav_home);
        navMessages = findViewById(R.id.nav_messages);
        navAccount = findViewById(R.id.nav_account);
        // Note: nav_addItem is present in your XML, but not declared/initialized here.
        // If you need it, add: private LinearLayout navAddItem; and navAddItem = findViewById(R.id.nav_addItem);
    }

    /**
     * Sets up the RecyclerViews with their LayoutManagers and Adapters.
     */
    private void setupRecyclerViews() {
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(new ArrayList<>(), this,this);
        categoriesRecyclerView.setAdapter(categoryAdapter);
        Log.d(TAG, "Categories RecyclerView and Adapter set up.");

        featuredRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        featuredProductsAdapter = new ProductAdapter(this, new ArrayList<>(), this);
        featuredRecyclerView.setAdapter(featuredProductsAdapter);
        Log.d(TAG, "Featured Products RecyclerView and Adapter set up.");
    }

    /**
     * Sets up all the click listeners for buttons and navigation items.
     */
    private void setupListeners() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mainViewModel.performSearch(s.toString());
                Log.d(TAG, "Search bar text changed: " + s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listener for the top-right cart button (ImageView)
        if (cartButton != null) {
            cartButton.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CartActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Disable animation
                Log.d(TAG, "Top-right Cart button clicked. Starting CartActivity.");
            });
        } else {
            Log.e(TAG, "Top-right Cart button (ImageView) is NULL. Check activity_home.xml for @id/cartButton.");
        }


        // Bottom Navigation Logic
        // Home button click: Refresh data and scroll to top
        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Log.d(TAG, "Bottom Nav: Home button clicked. Refreshing data.");
                mainViewModel.refreshData(); // Call refreshData on ViewModel
                scrollView.smoothScrollTo(0, 0); // Scroll to top
//                Toast.makeText(this, "Home data refreshed!", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "Bottom Nav: Home button (LinearLayout) is NULL. Check activity_home.xml for @id/nav_home.");
        }


        if (navMessages != null) {
            navMessages.setOnClickListener(v -> {
                Log.d(TAG, "Bottom Nav: Messages clicked.");
                Toast.makeText(this, "Messages clicked - Not implemented yet", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "Bottom Nav: Messages button (LinearLayout) is NULL. Check activity_home.xml for @id/nav_messages.");
        }


        if (navAccount != null) {
            navAccount.setOnClickListener(v -> {
                Log.d(TAG, "Bottom Nav: Account clicked! Attempting to start AccountActivity.");
                Intent intent = new Intent(HomeActivity.this, AccountActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // Disable animation
            });
        } else {
            Log.e(TAG, "Bottom Nav: Account button (LinearLayout) is NULL. Check activity_home.xml for @id/nav_account");
        }

        // The bottom navigation Cart button (LinearLayout with id="@+id/cartButton")
        // cannot be directly accessed here using findViewById(R.id.cartButton) because
        // the top-right ImageView with the same ID is found first.
        // If you need to make the bottom nav Cart button clickable, its ID in XML MUST be unique.
        // For example, change its ID in XML to @id/nav_cart_bottom.
    }

    /**
     * Observes LiveData from the MainViewModel to update the UI dynamically
     * when data changes.
     */
    private void observeViewModel() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "ProgressBar set to VISIBLE.");

        mainViewModel.getCategoriesLiveData().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryAdapter.updateCategories(categories);
                Log.d(TAG, "Categories LiveData observed. Categories loaded: " + categories.size());
            } else {
                Log.e(TAG, "Categories LiveData observed. No categories found or error loading them. List is null or empty.");
            }
        });

        mainViewModel.getProductsLiveData().observe(this, products -> {
            progressBar.setVisibility(View.GONE);
            Log.d(TAG, "ProgressBar set to GONE after products LiveData update.");
            if (products != null && !products.isEmpty()) {
                featuredProductsAdapter.setProducts(products);
                Log.d(TAG, "Products LiveData observed. Products loaded: " + products.size() + ". Adapter updated.");
            } else {
                Log.e(TAG, "Products LiveData observed. No products found or error loading them. List is null or empty.");
                // Optionally show noFeaturedItemsMessage if products list is empty
                // noFeaturedItemsMessage.setVisibility(View.VISIBLE);
            }
        });

        mainViewModel.getSearchResultsLiveData().observe(this, products -> {
            Log.d(TAG, "Search results LiveData observed. Search results updated: " + products.size());
            featuredProductsAdapter.setProducts(products);
        });
    }

    /**
     * This method is required by the ProductAdapter.OnAddToCartClickListener interface.
     * It handles the click event when a user taps the "Add to Cart" button on a product.
     *
     * @param product The ProductModel object of the item to be added to the cart.
     */
    @Override
    public void onAddToCartClick(ProductModel product) {
        Log.d(TAG, "Adding to cart: " + product.getTitle());
        CartItem item = new CartItem(product, 1);
        CartManager.getInstance().addItem(item);
        Toast.makeText(this, product.getTitle() + " added to cart!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Logs the current user out using Firebase Authentication and navigates to the LoginActivity.
     */
    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(HomeActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void onCategoryClick(CategoryModel category) {
        Toast.makeText(this, "Category clicked: " + category.getName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Category clicked: " + category.getName() + " (ID: " + category.getId() + "). Filtering products.");
        mainViewModel.filterProductsByCategory(String.valueOf(category.getId()));
    }
}
