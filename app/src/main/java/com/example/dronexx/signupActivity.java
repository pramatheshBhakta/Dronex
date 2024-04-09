package com.example.dronexx;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class signupActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private EditText emailEditText, passwordEditText, reenterPasswordEditText;
    private TextView Sign;
    private Button signUpButton;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup2);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        reenterPasswordEditText = findViewById(R.id.reenterPasswordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        Sign=findViewById(R.id.signInTextView);

        Sign.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });

        signUpButton.setOnClickListener(v -> {
            // Handle sign up with email and password
            signUpWithEmailPassword();
        });
    }

    private void signUpWithEmailPassword() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String reenteredPassword = reenterPasswordEditText.getText().toString();

        // Check if email, password, and reentered password are provided
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return;
        }
        if (TextUtils.isEmpty(reenteredPassword)) {
            reenterPasswordEditText.setError("Please re-enter your password");
            return;
        }
        if (!password.equals(reenteredPassword)) {
            reenterPasswordEditText.setError("Passwords do not match");
            return;
        }

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign up success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(signupActivity.this, "Sign up successful.",
                                Toast.LENGTH_SHORT).show();
                        // Add any additional actions upon successful sign-up
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(signupActivity.this, "Authentication failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}