package com.example.osmonlinesabjimandi.Utils;

import android.util.Log;

import com.example.osmonlinesabjimandi.Model.CartItem;
import com.example.osmonlinesabjimandi.Model.ProductModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Singleton class to manage the shopping cart.
 * It holds a list of CartItem objects and provides methods to add, update, remove items,
 * and calculate totals. It now also persists cart data to Firebase Realtime Database.
 */
public class CartManager {

    private static final String TAG = "CartManager";
    private static CartManager instance;
    private List<CartItem> cartItems;
    private List<CartChangeListener> listeners;

    private FirebaseAuth mAuth;
    private DatabaseReference userCartRef; // Reference to the user's specific cart in DB
    private ValueEventListener firebaseCartListener; // To attach/detach listener

    // Private constructor to enforce Singleton pattern
    private CartManager() {
        cartItems = new ArrayList<>();
        listeners = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        // Set up Firebase listener for the cart when the user's auth state changes
        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                // User is logged in, set up DB reference and load cart
                setupFirebaseCartListener(currentUser.getUid());
            } else {
                // User logged out, clear in-memory cart and detach listener
                Log.d(TAG, "User logged out. Clearing in-memory cart and detaching Firebase listener.");
                cartItems.clear();
                if (userCartRef != null && firebaseCartListener != null) {
                    userCartRef.removeEventListener(firebaseCartListener);
                }
                userCartRef = null;
                firebaseCartListener = null;
                notifyCartChanged(); // Notify UI that cart is empty
            }
        });
    }

    /**
     * Sets up the Firebase Realtime Database listener for the current user's cart.
     * This method is called when a user logs in.
     */
    private void setupFirebaseCartListener(String userId) {
        // Detach previous listener if it exists to prevent duplicates
        if (userCartRef != null && firebaseCartListener != null) {
            userCartRef.removeEventListener(firebaseCartListener);
            Log.d(TAG, "Detached previous Firebase cart listener.");
        }

        userCartRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("myCart");
//        Log.d(TAG, "Firebase cart reference set to: " + userCartRef.getPath());

        firebaseCartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CartItem> loadedCartItems = new ArrayList<>();
                if (!dataSnapshot.exists()) {
                    Log.d(TAG, "Firebase Realtime DB: No cart data found for user.");
                } else {
                    Log.d(TAG, "Firebase Realtime DB: Cart data snapshot exists. Children count: " + dataSnapshot.getChildrenCount());
                }

                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    // We need to manually reconstruct CartItem because ProductModel is nested
                    // And we only store specific fields in the DB for cart items
                    Long productIdLong = itemSnapshot.child("productId").getValue(Long.class);
                    Integer quantityInt = itemSnapshot.child("quantity").getValue(Integer.class);
                    String productTitle = itemSnapshot.child("productTitle").getValue(String.class);
                    Double productPrice = itemSnapshot.child("productPrice").getValue(Double.class);
                    String imagePath = itemSnapshot.child("imagePath").getValue(String.class);

                    if (productIdLong != null && quantityInt != null && productTitle != null && productPrice != null && imagePath != null) {
                        // Reconstruct a ProductModel with only the necessary fields for CartItem display
                        ProductModel product = new ProductModel();
                        product.setId(productIdLong);
                        product.setTitle(productTitle);
                        product.setPrice(productPrice);
                        product.setImagePath(imagePath);

                        CartItem cartItem = new CartItem(product, quantityInt);
                        loadedCartItems.add(cartItem);
                        Log.d(TAG, "Loaded cart item from DB: " + productTitle + " x " + quantityInt);
                    } else {
                        Log.e(TAG, "Failed to parse cart item from DB for key: " + itemSnapshot.getKey() + ". Missing fields.");
                    }
                }
                cartItems.clear();
                cartItems.addAll(loadedCartItems);
                notifyCartChanged(); // Update UI after loading from DB
                Log.d(TAG, "In-memory cart updated with " + cartItems.size() + " items from Firebase DB.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase Realtime DB: Failed to load cart data. Error: " + databaseError.getMessage(), databaseError.toException());
            }
        };
        userCartRef.addValueEventListener(firebaseCartListener); // Attach the listener
    }


    /**
     * Returns the singleton instance of CartManager.
     */
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    /**
     * Adds an item to the cart. If the product already exists, its quantity is incremented.
     * Persists changes to Firebase Realtime Database.
     * @param newItem The CartItem to add.
     */
    public void addItem(CartItem newItem) {
        boolean found = false;
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(newItem.getProduct().getId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            cartItems.add(newItem);
        }
        saveCartToFirebase(); // Save to DB after modifying in-memory cart
        notifyCartChanged();
    }

    /**
     * Updates the quantity of an existing item in the cart.
     * If quantity becomes 0 or less, the item is removed.
     * Persists changes to Firebase Realtime Database.
     * @param updatedItem The CartItem with the new quantity.
     */
    public void updateItemQuantity(CartItem updatedItem) {
        Iterator<CartItem> iterator = cartItems.iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getProduct().getId().equals(updatedItem.getProduct().getId())) {
                if (updatedItem.getQuantity() <= 0) {
                    iterator.remove();
                } else {
                    item.setQuantity(updatedItem.getQuantity());
                }
                changed = true;
                break;
            }
        }
        if (changed) {
            saveCartToFirebase(); // Save to DB after modifying in-memory cart
            notifyCartChanged();
        }
    }

    /**
     * Removes an item from the cart.
     * Persists changes to Firebase Realtime Database.
     * @param itemToRemove The CartItem to remove.
     */
    public void removeItem(CartItem itemToRemove) {
        Iterator<CartItem> iterator = cartItems.iterator();
        boolean changed = false;
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getProduct().getId().equals(itemToRemove.getProduct().getId())) {
                iterator.remove();
                changed = true;
                break;
            }
        }
        if (changed) {
            saveCartToFirebase(); // Save to DB after modifying in-memory cart
            notifyCartChanged();
        }
    }

    /**
     * Clears all items from the cart.
     * Persists changes to Firebase Realtime Database.
     */
    public void clearCart() {
        cartItems.clear();
        saveCartToFirebase(); // Save empty cart to DB
        notifyCartChanged();
    }

    /**
     * Returns an unmodifiable list of current cart items.
     */
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems); // Return a copy to prevent external modification
    }

    /**
     * Calculates the total number of distinct items in the cart.
     */
    public int getCartItemCount() {
        return cartItems.size();
    }

    /**
     * Calculates the total quantity of all products in the cart.
     */
    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    /**
     * Calculates the total monetary amount of all items in the cart.
     */
    public double getTotalAmount() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }

    /**
     * Saves the current in-memory cart to Firebase Realtime Database.
     * This method is called after any modification to the cart.
     */
    private void saveCartToFirebase() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Cannot save cart to Firebase: User not logged in.");
            return;
        }
        if (userCartRef == null) {
            Log.e(TAG, "userCartRef is null. Firebase cart listener not set up correctly.");
            return;
        }

        Map<String, Map<String, Object>> firebaseCartData = new HashMap<>();
        for (CartItem item : cartItems) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("productId", item.getProduct().getId());
            itemData.put("quantity", item.getQuantity());
            itemData.put("productTitle", item.getProduct().getTitle());
            itemData.put("productPrice", item.getProduct().getPrice());
            itemData.put("imagePath", item.getProduct().getImagePath());
            // Use productId as the key for each item in Firebase
            firebaseCartData.put(String.valueOf(item.getProduct().getId()), itemData);
        }

        userCartRef.setValue(firebaseCartData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Cart saved to Firebase Realtime DB successfully.");
                    } else {
                        Log.e(TAG, "Failed to save cart to Firebase Realtime DB: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Interface for listening to cart changes.
     */
    public interface CartChangeListener {
        void onCartChanged();
    }

    /**
     * Adds a listener for cart changes.
     */
    public void addCartChangeListener(CartChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for cart changes.
     */
    public void removeCartChangeListener(CartChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the cart has changed.
     */
    private void notifyCartChanged() {
        for (CartChangeListener listener : listeners) {
            listener.onCartChanged();
        }
    }
}
