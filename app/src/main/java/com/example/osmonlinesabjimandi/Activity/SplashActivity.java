package com.example.osmonlinesabjimandi.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Handler;

import com.example.osmonlinesabjimandi.R;
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Declare FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final int SPLASH_DURATION = 3000; // 3 seconds
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            Intent intent;
            // Check if the user is currently signed in
            if (mAuth.getCurrentUser() != null) {
                // User is signed in, go to HomeActivity
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            } else {
                // No user is signed in, go to LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish(); // Close SplashActivity so user can't go back to it
        }, SPLASH_DURATION);
    }
}
