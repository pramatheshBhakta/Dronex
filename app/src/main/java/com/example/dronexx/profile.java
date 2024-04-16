package com.example.dronexx;

import static com.example.dronexx.R.id.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class profile extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView emailTextView;
    private Button logoutButton;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);


        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        profileImageView = findViewById(R.id.profileImageView);
        emailTextView = findViewById(R.id.emailTextView);
        logoutButton = findViewById(R.id.logoutButton);

        // Retrieve the current user's email address from Firebase Authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            emailTextView.setText(email);
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the user from Firebase Authentication
                mAuth.signOut();
                // Display a toast message indicating successful logout
                Toast.makeText(profile.this, "Logout Successful", Toast.LENGTH_SHORT).show();
                Intent it = new Intent(profile.this,MainActivity.class);
                startActivity(it);
                // Redirect the user to the login or signup activity
                // For example:
                // startActivity(new Intent(profile.this, LoginActivity.class));
                // or
                // startActivity(new Intent(profile.this, SignupActivity.class));
                // Finish the current activity
                finish();
            }
        });
    }
}
