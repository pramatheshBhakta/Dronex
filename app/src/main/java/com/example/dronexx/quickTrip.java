package com.example.dronexx;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dronexx.R;

public class quickTrip extends AppCompatActivity {

    private Spinner pickupLocationSpinner;
    private Spinner destinationSpinner;
    private Spinner rideTypeSpinner;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_trip);

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
    }
}
