package com.example.dronexx;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class quickTrip extends AppCompatActivity {
    private double pickupLatitude;
    private double pickupLongitude;
    private double destinationLatitude;
    private double destinationLongitude;
    private Spinner pickupLocationSpinner;
    private Spinner destinationSpinner,customspinner;
    TextView txt1,txt2;

    private ImageButton backButton;

    // Firebase
    private DatabaseReference locationsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_trip);

        txt1= findViewById(R.id.txt3);
        txt2= findViewById(R.id.txt2);

        // Firebase setup
        locationsRef = FirebaseDatabase.getInstance().getReference().child("locations");

        // Initialize views
        pickupLocationSpinner = findViewById(R.id.pickupLocationSpinner);
        destinationSpinner = findViewById(R.id.destinationSpinner);

        backButton = findViewById(R.id.backButton);
        CheckBox predefinedTourCheckBox = findViewById(R.id.predefinedTourCheckBox);
        customspinner = findViewById(R.id.customTourOptionsSpinner);

        // Set up spinners
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(this,
                R.array.places_options, android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pickupLocationSpinner.setAdapter(locationAdapter);
        destinationSpinner.setAdapter(locationAdapter);


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
                // Show confirmation dialog
                new AlertDialog.Builder(quickTrip.this)
                        .setMessage("CONFIRM YOUR RIDE")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked Yes button

                                if (predefinedTourCheckBox.isChecked()) {
                                    fetchCoordinates();
                                } else {
                                    fetchCoordinatesofcustom();
                                }
                                String coordinatesMessage = "Pickup Latitude: " + pickupLatitude + "\n"
                                        + "Pickup Longitude: " + pickupLongitude + "\n"
                                        + "Destination Latitude: " + destinationLatitude + "\n"
                                        + "Destination Longitude: " + destinationLongitude;

// Show toast with all coordinates
                                Toast.makeText(quickTrip.this, coordinatesMessage, Toast.LENGTH_LONG).show();
                                updateCommandNode();

                                Intent intent = new Intent(quickTrip.this, RouteMapActivity.class);
                                intent.putExtra("pickupLatitude", pickupLatitude);
                                intent.putExtra("pickupLongitude", pickupLongitude);
                                intent.putExtra("destinationLatitude", destinationLatitude);
                                intent.putExtra("destinationLongitude", destinationLongitude);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });


        // Set onCheckedChangeListener for the predefinedTourCheckBox
        predefinedTourCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Show customTourOptionsSpinner and hide pickupLocationSpinner and destinationSpinner
                    customspinner.setVisibility(View.VISIBLE);
                    pickupLocationSpinner.setVisibility(View.GONE);
                    destinationSpinner.setVisibility(View.GONE);
                    txt1.setVisibility(View.GONE);
                    txt2.setVisibility(View.GONE);


                } else {
                    // Show pickupLocationSpinner and destinationSpinner and hide customTourOptionsSpinner
                    customspinner.setVisibility(View.GONE);
                    pickupLocationSpinner.setVisibility(View.VISIBLE);
                    destinationSpinner.setVisibility(View.VISIBLE);
                    txt1.setVisibility(View.VISIBLE);
                    txt2.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    private void fetchCoordinates() {
        String customOption = customspinner.getSelectedItem().toString();
        String pickupLocation;
        String destination;

        // Set pickup and destination based on the selected custom option
        switch (customOption) {
            case "AB2-AB3":
                pickupLocation = "AB2";
                destination = "AB3";
                break;
            case "TEST1-TEST2":
                pickupLocation = "TEST1";
                destination = "TEST2";
                break;
            case "AB3-CAFETARIA":
                pickupLocation = "AB3";
                destination = "CAFETERIA";
                break;
            case "STUDENT SQUARE - ADMIN":
                pickupLocation = "STUDENT_SQUARE_LIBRARY";
                destination = "ADMIN_BLOCK_ACADEMIC_BLOCK1";
                break;
            default:
                // Handle default case or error
                pickupLocation = "";
                destination = "";
                break;
        }
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
//                    String coordinatesMessage = "Pickup - Latitude: " + pickupLatitude + ", Longitude: " + pickupLongitude + "\n"
//                            + "Destination - Latitude: " + destinationLatitude + ", Longitude: " + destinationLongitude;
//                    Toast.makeText(quickTrip.this, coordinatesMessage, Toast.LENGTH_LONG).show();

                    // Update pickup node in Firebase
                    updateLocationInDatabase("pickup", pickupLatitude, pickupLongitude);

                    // Update drop node in Firebase
                    updateLocationInDatabase("drop", destinationLatitude, destinationLongitude);
                } else {
                    Toast.makeText(quickTrip.this, "Error: Locations not found in the database", Toast.LENGTH_SHORT).show();
                }
                pickupLatitude = dataSnapshot.child(pickupLocation).child("latitude").getValue(Double.class);
                pickupLongitude = dataSnapshot.child(pickupLocation).child("longitude").getValue(Double.class);
                destinationLatitude = dataSnapshot.child(destination).child("latitude").getValue(Double.class);
                destinationLongitude = dataSnapshot.child(destination).child("longitude").getValue(Double.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(quickTrip.this, "Failed to fetch locations: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchCoordinatesofcustom() {

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
//                    String coordinatesMessage = "Pickup - Latitude: " + pickupLatitude + ", Longitude: " + pickupLongitude + "\n"
//                            + "Destination - Latitude: " + destinationLatitude + ", Longitude: " + destinationLongitude;
//                    Toast.makeText(quickTrip.this, coordinatesMessage, Toast.LENGTH_LONG).show();

                    // Update pickup node in Firebase
                    updateLocationInDatabase("pickup", pickupLatitude, pickupLongitude);

                    // Update drop node in Firebase
                    updateLocationInDatabase("drop", destinationLatitude, destinationLongitude);
                } else {
                    Toast.makeText(quickTrip.this, "Error: Locations not found in the database", Toast.LENGTH_SHORT).show();
                }
                pickupLatitude = dataSnapshot.child(pickupLocation).child("latitude").getValue(Double.class);
                pickupLongitude = dataSnapshot.child(pickupLocation).child("longitude").getValue(Double.class);
                destinationLatitude = dataSnapshot.child(destination).child("latitude").getValue(Double.class);
                destinationLongitude = dataSnapshot.child(destination).child("longitude").getValue(Double.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(quickTrip.this, "Failed to fetch locations: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void updateCommandNode() {
        DatabaseReference commandRef = FirebaseDatabase.getInstance().getReference().child("commands");
        commandRef.setValue("waypoints");
        Toast.makeText(quickTrip.this, "Initiating Drone in ETA:20s", Toast.LENGTH_SHORT).show();
    }

    // Helper method to update location in Firebase database
    private void updateLocationInDatabase(String node, Double latitude, Double longitude) {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference().child(node);
        locationRef.child("latitude").setValue(latitude);
        locationRef.child("longitude").setValue(longitude);
//        Toast.makeText(quickTrip.this, "Location updated in " + node + " node", Toast.LENGTH_SHORT).show();
    }
}