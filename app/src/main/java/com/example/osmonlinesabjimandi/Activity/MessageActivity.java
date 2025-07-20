//package com.example.osmonlinesabjimandi;
//
//import android.os.Bundle;
//import com.google.android.material.bottomnavigation.BottomNavigationView;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.navigation.NavController;
//import androidx.navigation.Navigation;
//import androidx.navigation.ui.AppBarConfiguration;
//import androidx.navigation.ui.NavigationUI;
//import com.example.osmonlinesabjimandi.databinding.ActivityMessageBinding;
//
//public class MessageActivity extends AppCompatActivity {
//
//    private ActivityMessageBinding binding;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        binding = ActivityMessageBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
////
////        BottomNavigationView navView = findViewById(R.id.navView);
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.messagesToolbar);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//
//    }
//}



package com.example.osmonlinesabjimandi.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.osmonlinesabjimandi.R; // Make sure this import matches your package

     class MessagesActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_message);

            ImageView backButton = findViewById(R.id.backButton);
            if (backButton != null) {
                backButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed(); // This will go back to the previous activity (HomeActivity)
                    }
                });
            }

            // Initialize any other UI elements specific to MessagesActivity here
            // For example, if you add a RecyclerView for chat list:
//             RecyclerView chatListRecyclerView = findViewById(R.id.chatListRecyclerView);
//             chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//            chatListRecyclerView.setAdapter(new ChatListAdapter(chatData)); // You'd create ChatListAdapter later
        }
    }

