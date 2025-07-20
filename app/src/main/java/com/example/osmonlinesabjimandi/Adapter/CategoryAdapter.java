package com.example.osmonlinesabjimandi.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.osmonlinesabjimandi.Model.CategoryModel; // Make sure this import is correct
import com.example.osmonlinesabjimandi.R; // R file for your project resources

import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of categories.
 * It binds CategoryModel data to the views defined in item_category.xml.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryModel> categories;
    private Context context;
    private OnCategoryClickListener listener; // Listener for category clicks

    /**
     * Interface for handling category item clicks.
     * The Activity or Fragment using this adapter should implement this.
     */
    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryModel category);
    }

    /**
     * Constructor for the CategoryAdapter.
     *
     * @param categories The list of CategoryModel objects to display.
     * @param context The context from the calling activity/fragment.
     * @param listener The listener for category item clicks.
     */
    public CategoryAdapter(List<CategoryModel> categories, Context context, OnCategoryClickListener listener) {
        this.categories = categories;
        this.context = context;
        this.listener = listener;
    }

    /**
     * Provides a reference to the views for each data item.
     * This is used to cache the views within the item layout for fast access.
     */
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImageView;
        TextView categoryNameTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize UI elements from item_category.xml
            categoryImageView = itemView.findViewById(R.id.categoryImageView);
            categoryNameTextView = itemView.findViewById(R.id.categoryNameTextView);
        }
    }

    /**
     * Called when RecyclerView needs a new {@link CategoryViewHolder} of the given type to represent
     * an item.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new CategoryViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for a single category item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link CategoryViewHolder#itemView} to reflect the item at the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel category = categories.get(position);

        // Set category name
        holder.categoryNameTextView.setText(category.getName());

        // Load category image using Glide
        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(category.getImageUrl())
                    .placeholder(R.drawable.placeholder_image) // Placeholder image while loading
                    .error(R.drawable.error_image)         // Image to display if loading fails
                    .into(holder.categoryImageView);
        } else {
            // If image path is null or empty, set a default placeholder
            holder.categoryImageView.setImageResource(R.drawable.placeholder_image);
        }

        // Set click listener for the entire item view
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return categories.size();
    }

    /**
     * Updates the list of categories and notifies the RecyclerView to refresh.
     *
     * @param newCategories The new list of CategoryModel objects.
     */
    public void updateCategories(List<CategoryModel> newCategories) {
        this.categories.clear(); // Clear existing data
        this.categories.addAll(newCategories); // Add new data
        notifyDataSetChanged(); // Notify adapter that data has changed
    }
}
