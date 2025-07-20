package com.example.osmonlinesabjimandi.Model;

import java.io.Serializable;

/**
 * Represents an item in the shopping cart, holding a ProductModel and its quantity.
 */
public class CartItem implements Serializable {
    private ProductModel product;
    private int quantity;

    public CartItem() {
        // Default constructor for Firebase or other serialization
    }

    public CartItem(ProductModel product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public ProductModel getProduct() {
        return product;
    }

    public void setProduct(ProductModel product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // You might want to add equals() and hashCode() if you plan to use
    // CartItem in collections that rely on object equality (e.g., HashMaps, HashSets)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return product != null ? product.getId().equals(cartItem.product.getId()) : cartItem.product == null;
    }

    @Override
    public int hashCode() {
        return product != null ? product.getId().hashCode() : 0;
    }
}
