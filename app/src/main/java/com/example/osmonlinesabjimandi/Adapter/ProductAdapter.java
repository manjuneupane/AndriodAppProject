package com.example.osmonlinesabjimandi.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.osmonlinesabjimandi.Activity.ProductDetailActivity;
import com.example.osmonlinesabjimandi.Model.ProductModel;
import com.example.osmonlinesabjimandi.R;

import java.util.List;
import java.util.Locale; // For price formatting

/**
 * RecyclerView Adapter for displaying a list of products.
 * It binds ProductModel data to the views defined in item_product.xml.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<ProductModel> products;
    private OnAddToCartClickListener onAddToCartClickListener; // Listener for "Add to Cart" button

    /**
     * Interface for handling "Add to Cart" button clicks.
     * The Activity or Fragment using this adapter should implement this.
     */
    public interface OnAddToCartClickListener {
        void onAddToCartClick(ProductModel product);
    }

    /**
     * Constructor for the ProductAdapter.
     *
     * @param context The context from the calling activity/fragment.
     * @param products The list of ProductModel objects to display.
     * @param onAddToCartClickListener The listener for "Add to Cart" button clicks.
     */
    public ProductAdapter(Context context, List<ProductModel> products, OnAddToCartClickListener onAddToCartClickListener) {
        this.context = context;
        this.products = products;
        this.onAddToCartClickListener = onAddToCartClickListener;
    }

    /**
     * Provides a reference to the views for each data item.
     * This is used to cache the views within the item layout for fast access.
     */
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productNameTextView;
        TextView productPriceTextView;
        Button addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize UI elements from item_product.xml
            productImageView = itemView.findViewById(R.id.productImageView);
            productNameTextView = itemView.findViewById(R.id.productNameTextView);
            productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
    }

    /**
     * Called when RecyclerView needs a new {@link ProductViewHolder} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ProductViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single product item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link ProductViewHolder#itemView} to reflect the item at the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = products.get(position);

        // Set product title
        holder.productNameTextView.setText(product.getTitle());

        // Set product price, formatted as "NRS XX.XX per KG"
        holder.productPriceTextView.setText(String.format(Locale.getDefault(), "NRS %.2f per KG", product.getPrice()));

        // Load product image using Glide
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            Glide.with(context)
                    .load(product.getImagePath())
                    .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                    .error(R.drawable.error_image)         // Image to display if loading fails
                    .into(holder.productImageView);
        } else {
            // If image path is null or empty, set a default placeholder
            holder.productImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listener for the "Add to Cart" button
        holder.addToCartButton.setOnClickListener(v -> {
            if (onAddToCartClickListener != null) {
                onAddToCartClickListener.onAddToCartClick(product);
            }
        });

        // REMOVED: Set click listener for the entire product item to go to details
        // The user explicitly requested to stop clickable products, only allowing "Add to Cart"
        /*
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            // Pass the product ID to the ProductDetailActivity
            // Ensure ProductModel.getId() returns a String or convert it
            intent.putExtra("productId", String.valueOf(product.getId()));
            context.startActivity(intent);
        });
        */
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return products.size();
    }

    /**
     * Updates the list of products and notifies the RecyclerView to refresh.
     * This method is used by the ViewModel observers.
     *
     * @param newProducts The new list of ProductModel objects.
     */
    public void setProducts(List<ProductModel> newProducts) {
        this.products.clear(); // Clear existing data
        this.products.addAll(newProducts); // Add new data
        notifyDataSetChanged(); // Notify adapter that data has changed
    }
}
