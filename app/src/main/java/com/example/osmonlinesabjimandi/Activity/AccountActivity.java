package com.example.osmonlinesabjimandi.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.osmonlinesabjimandi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView userNameTextView;
    private TextView userEmailTextView;

    // References to the clickable LinearLayout items
    private LinearLayout myOrdersItem;
    private LinearLayout myWalletItem;
    private LinearLayout myVouchersItem;
    private LinearLayout addItemItem; // This is the 'Add New Item' in Admin Tools
    private LinearLayout settingsItem;
    private LinearLayout helpCenterItem;
    private LinearLayout adminToolsSection;

    private Button logoutButton;

    // Bottom Navigation Items (NEWLY DECLARED)
    private LinearLayout navHomeBottom;
    private LinearLayout navMessagesBottom;
    private LinearLayout navCartBottom;
    private LinearLayout navAccountBottom; // Renamed for clarity in AccountActivity

    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    private static final String TAG = "AccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersDatabaseRef = database.getReference("users");

        profileImageView = findViewById(R.id.profile_picture);
        userNameTextView = findViewById(R.id.user_name);
        userEmailTextView = findViewById(R.id.user_email);

        myOrdersItem = findViewById(R.id.my_orders_item);
        myWalletItem = findViewById(R.id.my_wallet_item);
        myVouchersItem = findViewById(R.id.my_vouchers_item);
        addItemItem = findViewById(R.id.add_item_item);
        settingsItem = findViewById(R.id.settings_item);
        helpCenterItem = findViewById(R.id.help_center_item);
        adminToolsSection = findViewById(R.id.admin_tools_section);

        logoutButton = findViewById(R.id.logout_button);

        // Initialize Bottom Navigation Items (NEW)
        navHomeBottom = findViewById(R.id.nav_home); // Assuming this is the ID for Home in bottom nav
        navMessagesBottom = findViewById(R.id.nav_messages_bottom); // Assuming this is the ID for Messages in bottom nav
        navCartBottom = findViewById(R.id.nav_cart_bottom); // Assuming this is the ID for Cart in bottom nav
        navAccountBottom = findViewById(R.id.nav_account); // This is the current activity's nav item

        // Load user data
        loadUserData();

        // Set up click listeners for the main LinearLayout items
        setupMainListeners();

        // Set up click listeners for the bottom navigation items (NEW)
        setupBottomNavListeners();
    }

    private void setupMainListeners() {
        myOrdersItem.setOnClickListener(v -> {
            Toast.makeText(AccountActivity.this, "My Orders clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to MyOrdersActivity
        });

        myWalletItem.setOnClickListener(v -> {
            Toast.makeText(AccountActivity.this, "My Wallet clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to MyWalletActivity
        });

        myVouchersItem.setOnClickListener(v -> {
            Toast.makeText(AccountActivity.this, "My Vouchers clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to MyVouchersActivity
        });

        addItemItem.setOnClickListener(v -> {
            Toast.makeText(AccountActivity.this, "Add New Item clicked (Admin)", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to AddItemActivity (for Admins)
        });

        settingsItem.setOnClickListener(v -> {
            Toast.makeText(AccountActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show();
        });

        helpCenterItem.setOnClickListener(v -> {
            Toast.makeText(AccountActivity.this, "Help Center clicked", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> logoutUser());
    }

    // NEW METHOD: Setup listeners for bottom navigation
    private void setupBottomNavListeners() {
        navHomeBottom.setOnClickListener(v -> {
            Log.d(TAG, "Bottom Nav: Home clicked. Navigating to HomeActivity.");
            Intent intent = new Intent(AccountActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clears activity stack
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish(); // Finish AccountActivity to prevent going back
            overridePendingTransition(0, 0);
        });

        navCartBottom.setOnClickListener(v -> {
            Log.d(TAG, "Bottom Nav: Cart clicked. Navigating to CartActivity.");
            Intent intent = new Intent(AccountActivity.this, CartActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            // No finish() here if you want to allow back navigation to AccountActivity
        });

        navMessagesBottom.setOnClickListener(v -> {
            Log.d(TAG, "Bottom Nav: Messages clicked.");
            Toast.makeText(AccountActivity.this, "Messages - Not implemented yet", Toast.LENGTH_SHORT).show();
        });

        // The navAccountBottom click listener is implicitly handled by being in AccountActivity,
        // but you could add a Toast here if you want feedback when it's clicked.
        navAccountBottom.setOnClickListener(v -> {
            Log.d(TAG, "Bottom Nav: Account clicked (Current Activity).");
//            Toast.makeText(AccountActivity.this, "You are already on the Account page!", Toast.LENGTH_SHORT).show();
        });
    }


    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmailTextView.setText(currentUser.getEmail() != null && !currentUser.getEmail().isEmpty() ?
                    currentUser.getEmail() : getString(R.string.user_email_default));

            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.ic_default_profile_picture);
            }

            String userId = currentUser.getUid();
            usersDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);
                        Boolean isAdmin = dataSnapshot.child("isAdmin").getValue(Boolean.class);

                        if (fullName != null && !fullName.isEmpty()) {
                            userNameTextView.setText(fullName);
                            Log.d(TAG, "User Full Name from DB: " + fullName);
                        } else {
                            userNameTextView.setText(getString(R.string.user_name_default));
                            Log.d(TAG, "User Full Name not found in DB or is empty. Using default.");
                        }

                        if (isAdmin != null && isAdmin) {
                            adminToolsSection.setVisibility(View.VISIBLE);
                            Log.d(TAG, "User is Admin: true. Admin tools visible.");
                        } else {
                            adminToolsSection.setVisibility(View.GONE);
                            Log.d(TAG, "User is Admin: false or null. Admin tools hidden.");
                        }

                    } else {
                        Log.d(TAG, "User data not found in Realtime Database for UID: " + userId);
                        userNameTextView.setText(getString(R.string.user_name_default));
                        adminToolsSection.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Failed to load user data from Realtime DB: " + databaseError.getMessage());
                    userNameTextView.setText(getString(R.string.user_name_default));
                    adminToolsSection.setVisibility(View.GONE);
                }
            });

        } else {
            userNameTextView.setText(getString(R.string.guest_user_label));
            userEmailTextView.setText(getString(R.string.not_logged_in_label));
            profileImageView.setImageResource(R.drawable.ic_default_profile_picture);
            Toast.makeText(this, getString(R.string.no_user_logged_in_message), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(AccountActivity.this, getString(R.string.title_activity_message), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
