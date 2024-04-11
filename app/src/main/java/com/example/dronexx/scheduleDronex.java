package com.example.dronexx;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class scheduleDronex extends AppCompatActivity {

    private Spinner pickupLocationSpinner;
    private Spinner destinationSpinner;
    private Spinner rideTypeSpinner;
    private EditText dateInput;
    private EditText timeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule_dronex);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pickupLocationSpinner = findViewById(R.id.pickupLocationSpinner);
        destinationSpinner = findViewById(R.id.destinationSpinner);
        rideTypeSpinner = findViewById(R.id.rideTypeSpinner);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);
        Button scheduleNowButton = findViewById(R.id.scheduleNowButton);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set click listener for the "SCHEDULE NOW" button
        scheduleNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get selected values from spinners and inputs
                String pickupLocation = pickupLocationSpinner.getSelectedItem().toString();
                String destination = destinationSpinner.getSelectedItem().toString();
                String rideType = rideTypeSpinner.getSelectedItem().toString();
                String date = dateInput.getText().toString();
                String time = timeInput.getText().toString();

                // Save the schedule to Firebase (You need to implement this part)
                saveScheduleToFirebase(pickupLocation, destination, rideType, date, time);
            }
        });
    }

    private void saveScheduleToFirebase(String pickupLocation, String destination, String rideType, String date, String time) {
        // Here you would implement the logic to save the schedule data to Firebase database
        // For demonstration, let's just display a toast message
        String scheduleMessage = "Drone scheduled for " + date + " at " + time + " from " + pickupLocation + " to " + destination + " (" + rideType + ")";
        Toast.makeText(this, scheduleMessage, Toast.LENGTH_SHORT).show();
    }
}
