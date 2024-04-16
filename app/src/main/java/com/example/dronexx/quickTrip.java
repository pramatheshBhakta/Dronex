package com.example.dronexx;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dronexx.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class quickTrip extends AppCompatActivity {
    private Spinner pickupLocationSpinner;
    private Spinner destinationSpinner;
    private Spinner rideTypeSpinner;
    private ImageButton backButton;

    // Firebase
    private DatabaseReference locationsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_trip);

        // Firebase setup
        locationsRef = FirebaseDatabase.getInstance().getReference().child("locations");

        // Initialize views
        pickupLocationSpinner = findViewById(R.id.pickupLocationSpinner);
        destinationSpinner = findViewById(R.id.destinationSpinner);
        rideTypeSpinner = findViewById(R.id.rideTypeSpinner);
        backButton = findViewById(R.id.backButton);

        // Set up spinners
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(this,
                R.array.places_options, android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pickupLocationSpinner.setAdapter(locationAdapter);
        destinationSpinner.setAdapter(locationAdapter);

        ArrayAdapter<CharSequence> rideTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.ride_types, android.R.layout.simple_spinner_item);
        rideTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rideTypeSpinner.setAdapter(rideTypeAdapter);

        // Set onClickListener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Set onClickListener for the button to fetch coordinates
        findViewById(R.id.tourNowButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchCoordinates();
            }
        });
    }

    private void fetchCoordinates() {
        String pickupLocation = pickupLocationSpinner.getSelectedItem().toString();
        String destination = destinationSpinner.getSelectedItem().toString();

        // Fetch latitude and longitude for pickup and destination from Firebase database
        locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Double pickupLatitude = dataSnapshot.child(pickupLocation).child("latitude").getValue(Double.class);
                    Double pickupLongitude = dataSnapshot.child(pickupLocation).child("longitude").getValue(Double.class);
                    Double destinationLatitude = dataSnapshot.child(destination).child("latitude").getValue(Double.class);
                    Double destinationLongitude = dataSnapshot.child(destination).child("longitude").getValue(Double.class);

                    // Show toast message indicating pickup and destination
                    String message = "Travelling from " + pickupLocation + " to " + destination;
                    Toast.makeText(quickTrip.this, message, Toast.LENGTH_LONG).show();

                    // Show toast message with latitude and longitude
                    String coordinatesMessage = "Pickup - Latitude: " + pickupLatitude + ", Longitude: " + pickupLongitude + "\n"
                            + "Destination - Latitude: " + destinationLatitude + ", Longitude: " + destinationLongitude;
                    Toast.makeText(quickTrip.this, coordinatesMessage, Toast.LENGTH_LONG).show();

                    // Update pickup node in Firebase
                    updateLocationInDatabase("pickup", pickupLatitude, pickupLongitude);

                    // Update drop node in Firebase
                    updateLocationInDatabase("drop", destinationLatitude, destinationLongitude);
                } else {
                    Toast.makeText(quickTrip.this, "Error: Locations not found in the database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(quickTrip.this, "Failed to fetch locations: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to update location in Firebase database
    private void updateLocationInDatabase(String node, Double latitude, Double longitude) {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child(node);
        locationRef.child("latitude").setValue(latitude);
        locationRef.child("longitude").setValue(longitude);
        Toast.makeText(quickTrip.this, "Location updated in " + node + " node", Toast.LENGTH_SHORT).show();
    }
}