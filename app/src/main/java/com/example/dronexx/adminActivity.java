package com.example.dronexx;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class adminActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set onClickListener for ARM Button
        Button armButton = findViewById(R.id.armButton);
        armButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("arm");
            }
        });

        // Set onClickListener for DISARM Button
        Button disarmButton = findViewById(R.id.disarmButton);
        disarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("disarm");
            }
        });

        // Set onClickListener for LOITER Button
        Button loiterButton = findViewById(R.id.loiterButton);
        loiterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("loiter");
            }
        });

        // Set onClickListener for RTL (Return to Launch) Button
        Button rtlButton = findViewById(R.id.rtlButton);
        rtlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("rtl");
            }
        });

        // Set onClickListener for TAKEOFF Button
        Button takeoffButton = findViewById(R.id.takeoffButton);
        takeoffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("takeoff");
            }
        });

        // Set onClickListener for LAND Button
        Button landButton = findViewById(R.id.landButton);
        landButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("land");
            }
        });

        // Set onClickListener for ALT_HOLD Button
        Button altHoldButton = findViewById(R.id.altHoldButton);
        altHoldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("alt_hold");
            }
        });
        Button hardButton = findViewById(R.id.hardButton);
        hardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("hard");
            }
        });

        Button None= findViewById(R.id.noneButton);
        None.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("None");
            }
        });

        // Set onClickListener for WAYPOINT MISSION Button
        Button waypointButton = findViewById(R.id.waypointButton);
        waypointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("waypoint_mission");
            }
        });
    }

    // Method to send command to Firebase
    private void sendCommand(String command) {
        // Write command to Firebase Realtime Database
        mDatabase.child("commands").setValue(command);
    }

    // Method to show dialog for entering coordinates
    private void showCoordinateInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(adminActivity.this);
        builder.setTitle("Enter Waypoint Coordinates");

        // Set up the input fields
        final EditText latitudeInput = new EditText(adminActivity.this);
        latitudeInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        latitudeInput.setHint("Latitude");

        final EditText longitudeInput = new EditText(adminActivity.this);
        longitudeInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        longitudeInput.setHint("Longitude");

        LinearLayout layout = new LinearLayout(adminActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(latitudeInput);
        layout.addView(longitudeInput);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String latitudeStr = latitudeInput.getText().toString().trim();
                String longitudeStr = longitudeInput.getText().toString().trim();

                if (!latitudeStr.isEmpty() && !longitudeStr.isEmpty()) {
                    double latitude = Double.parseDouble(latitudeStr);
                    double longitude = Double.parseDouble(longitudeStr);

                    // Write the coordinates to Firebase Realtime Database
                    mDatabase.child("waypoints").child("latitude").setValue(latitude);
                    mDatabase.child("waypoints").child("longitude").setValue(longitude);

                    Toast.makeText(adminActivity.this, "Waypoint coordinates saved to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(adminActivity.this, "Please enter valid coordinates", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
