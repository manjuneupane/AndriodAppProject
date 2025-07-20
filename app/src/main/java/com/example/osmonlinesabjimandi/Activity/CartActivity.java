package com.example.osmonlinesabjimandi.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.osmonlinesabjimandi.Adapter.CartAdapter;
import com.example.osmonlinesabjimandi.Model.CartItem;
import com.example.osmonlinesabjimandi.R;
import com.example.osmonlinesabjimandi.Utils.CartManager;

import java.util.List;
import java.util.Locale;

// Implement both the CartManager listener and the CartAdapter listener
public class CartActivity extends AppCompatActivity implements CartManager.CartChangeListener, CartAdapter.CartItemListener {

    // UI elements
    private RecyclerView cartRecyclerView;
    private TextView emptyCartMessage;
    private TextView totalItemsTextView;
    private TextView cartTotalTextView;
    private Button checkoutButton;

    private CartAdapter cartAdapter;
    private CartManager cartManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize UI elements
        cartRecyclerView = findViewById(R.id.cartItemsRecyclerView);
        emptyCartMessage = findViewById(R.id.emptyCartMessage);
        totalItemsTextView = findViewById(R.id.totalItemsTextView);
        cartTotalTextView = findViewById(R.id.totalAmountTextView);
        checkoutButton = findViewById(R.id.checkoutButton);

        // Get the singleton instance of CartManager
        cartManager = CartManager.getInstance();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup Checkout Button Listener
        checkoutButton.setOnClickListener(v -> {
            if (cartManager.getCartItemCount() > 0) {
                Toast.makeText(this, "Proceeding to checkout!", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to your Checkout Activity
            } else {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });

        updateCartDisplay();
    }

    private void setupRecyclerView() {
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Pass 'this' as the listener to the adapter's constructor
        cartAdapter = new CartAdapter(cartManager.getCartItems(), this);
        cartRecyclerView.setAdapter(cartAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register this activity as a listener for cart data changes
        cartManager.addCartChangeListener(this);
        updateCartDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister listener to prevent memory leaks
        cartManager.removeCartChangeListener(this);
    }

    /**
     * This callback from CartManager triggers a UI refresh when the cart data changes.
     */
    @Override
    public void onCartChanged() {
        updateCartDisplay();
    }

    /**
     * Updates all UI elements based on the current state of the CartManager.
     */
    private void updateCartDisplay() {
        List<CartItem> currentCartItems = cartManager.getCartItems();

        // Update the adapter with the latest data
        if (cartAdapter != null) {
            cartAdapter.updateItems(currentCartItems);
        }

        // Toggle visibility of the empty cart message vs. the RecyclerView
        if (currentCartItems.isEmpty()) {
            emptyCartMessage.setVisibility(View.VISIBLE);
            cartRecyclerView.setVisibility(View.GONE);
            checkoutButton.setEnabled(false);
        } else {
            emptyCartMessage.setVisibility(View.GONE);
            cartRecyclerView.setVisibility(View.VISIBLE);
            checkoutButton.setEnabled(true);
        }

        // Update total item count and total price
        totalItemsTextView.setText(String.format(Locale.getDefault(), "%d items", cartManager.getTotalQuantity()));
        cartTotalTextView.setText(String.format(Locale.getDefault(), "NRS%.2f", cartManager.getTotalAmount()));
    }

    // --- Methods from CartAdapter.CartItemListener ---

    /**
     * Called from the adapter when a user changes an item's quantity.
     * It delegates the action to the CartManager.
     */
    @Override
    public void onQuantityChanged(CartItem item) {
        cartManager.updateItemQuantity(item);
    }

    /**
     * Called from the adapter when a user removes an item.
     * It delegates the action to the CartManager.
     */
    @Override
    public void onItemRemoved(CartItem item) {
        cartManager.removeItem(item);
        Toast.makeText(this, item.getProduct().getTitle() + " removed from cart", Toast.LENGTH_SHORT).show();
    }
}