package com.example.dronexx;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the scheduleDronex activity
        startActivity(new Intent(this, scheduleDronex.class));

        // Finish the current activity to prevent going back to it when pressing back button
        finish();
    }
}
