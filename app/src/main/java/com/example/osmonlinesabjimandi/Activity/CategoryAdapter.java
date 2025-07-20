package com.example.osmonlinesabjimandi.Activity;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.osmonlinesabjimandi.Model.CategoryModel;
import com.example.osmonlinesabjimandi.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private static final String TAG = "CategoryAdapter"; // For logging

    private List<CategoryModel> categories;
    private Context context; // Keep context if needed for things like starting new activities or toasts

    public CategoryAdapter(List<CategoryModel> categories, Context context) {
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.context = context;
    }

    /**
     * Updates the list of categories and notifies the adapter to refresh the UI.
     *
     * @param newCategories The new list of categories to display.
     */
    public void updateCategories(List<CategoryModel> newCategories) {
        this.categories.clear();
        if (newCategories != null) {
            this.categories.addAll(newCategories);
        }
        notifyDataSetChanged();
        Log.d(TAG, "Category list updated. New size: " + this.categories.size());
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categories.get(position);

        holder.categoryNameTextView.setText(category.getName());

        // Load image using Glide
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(category.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background) // Placeholder image
                    .error(R.drawable.ic_launcher_background)     // Error image if loading fails
                    .into(holder.categoryImageView);
        } else {
            // Fallback if no image URL
            holder.categoryImageView.setImageResource(R.drawable.ic_launcher_background);
            Log.w(TAG, "No image URL for category: " + category.getName());
        }

        // Set click listener for the entire category item
        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "Clicked category: " + category.getName(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Category clicked: " + category.getName() + ", ID: " + category.getId());
            // TODO: Implement action on category click, e.g., filter products in HomeActivity
            // You might use an interface/listener here, similar to ProductAdapter's OnAddToCartClickListener,
            // to communicate back to HomeActivity to perform product filtering.
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    /**
     * ViewHolder for individual category items.
     */
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImageView;
        TextView categoryNameTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views from item_category.xml
            categoryImageView = itemView.findViewById(R.id.categoryImageView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
        }
    }
}