package com.example.osmonlinesabjimandi.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.osmonlinesabjimandi.Model.CartItem;
import com.example.osmonlinesabjimandi.R;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private final CartItemListener listener;

    // Listener interface for handling interactions
    public interface CartItemListener {
        void onQuantityChanged(CartItem item);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, CartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem currentItem = cartItems.get(position);
        holder.bind(currentItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    // Method to update the list of items and refresh the RecyclerView
    public void updateItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView itemImageView;
        private final TextView nameTextView;
        private final TextView priceTextView;
        private final TextView quantityTextView;
        private final ImageButton incrementButton, decrementButton, deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.cartItemImageView);
            nameTextView = itemView.findViewById(R.id.cartItemNameTextView);
            priceTextView = itemView.findViewById(R.id.cartItemPriceTextView);
            quantityTextView = itemView.findViewById(R.id.cartItemQuantityTextView);
            incrementButton = itemView.findViewById(R.id.incrementButton);
            decrementButton = itemView.findViewById(R.id.decrementButton);
            deleteButton = itemView.findViewById(R.id.deleteItemButton);
        }

        public void bind(final CartItem item) {
            nameTextView.setText(item.getProduct().getTitle());
            priceTextView.setText(String.format(Locale.getDefault(), "NRS%.2f", item.getProduct().getPrice()));
            quantityTextView.setText(String.valueOf(item.getQuantity()));

            Glide.with(itemView.getContext())
                    .load(item.getProduct().getImagePath())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(itemImageView);

            // Click listener for deleting an item
            deleteButton.setOnClickListener(v -> listener.onItemRemoved(item));

            // Click listener for increasing quantity
            incrementButton.setOnClickListener(v -> {
                item.setQuantity(item.getQuantity() + 1);
                listener.onQuantityChanged(item);
            });

            // Click listener for decreasing quantity
            decrementButton.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    listener.onQuantityChanged(item);
                } else {
                    // If quantity is 1, decrementing removes the item
                    listener.onItemRemoved(item);
                }
            });
        }
    }
}