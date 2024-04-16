package com.example.dronexx;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class feedback extends AppCompatActivity {

    private EditText feedbackEditText;
    private Button submitButton;
    private DatabaseReference feedbackRef;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        feedbackRef = database.getReference("feedback");

        // Initialize Views
        feedbackEditText = findViewById(R.id.editTextFeedback);
        submitButton = findViewById(R.id.buttonSubmit);
        ratingBar = findViewById(R.id.ratingBar);

        // Submit button click listener
        submitButton.setOnClickListener(view -> submitFeedback());
    }

    private void submitFeedback() {
        String feedbackText = feedbackEditText.getText().toString().trim();
        float ratingValue = ratingBar.getRating();

        if (!feedbackText.isEmpty()) {
            // Push new feedback and rating to Firebase
            Feedback feedback = new Feedback(feedbackText, ratingValue);

            feedbackRef.push().setValue(feedback)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(feedback.this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                        feedbackEditText.setText("");
                        ratingBar.setRating(0); // Reset rating bar
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(feedback.this, "Failed to submit feedback. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
        }
    }


        public static class Feedback {
            private String feedbackText;
            private float rating;

            public Feedback() {
                // Default constructor required for Firebase
            }

            public Feedback(String feedbackText, float rating) {
                this.feedbackText = feedbackText;
                this.rating = rating;
            }

            public String getFeedbackText() {
                return feedbackText;
            }

            public float getRating() {
                return rating;
            }
        }

    }

