package com.example.osmonlinesabjimandi.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.osmonlinesabjimandi.Model.CategoryModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainRepository {

    private DatabaseReference categoryRef;

    public MainRepository() {
        categoryRef = FirebaseDatabase.getInstance().getReference("Categories");
    }

    public MutableLiveData<List<CategoryModel>> getCategories() {
        MutableLiveData<List<CategoryModel>> categoriesLiveData = new MutableLiveData<>();

        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CategoryModel> categories = new ArrayList<>();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    CategoryModel model = categorySnapshot.getValue(CategoryModel.class);
                    if (model != null) {
                        categories.add(model);
                    }
                }
                categoriesLiveData.setValue(categories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                categoriesLiveData.setValue(null);
            }
        });

        return categoriesLiveData;
    }
}
