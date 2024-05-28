package com.example.dronexx;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class scheduleDronex extends AppCompatActivity {
    private Spinner pickupLocationSpinner, dropLocationSpinner;
    private Button selectTimeButton, scheduleTripButton;
    private Calendar selectedDateTime;

    private DatabaseReference scheduledTripsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_dronex);

        pickupLocationSpinner = findViewById(R.id.pickupLocationSpinner);
        dropLocationSpinner = findViewById(R.id.dropLocationSpinner);
        selectTimeButton = findViewById(R.id.selectTimeButton);
        scheduleTripButton = findViewById(R.id.scheduleTripButton);

        scheduledTripsRef = FirebaseDatabase.getInstance().getReference().child("scheduledTrips");

        selectTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        scheduleTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleTrip();
            }
        });
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        selectedDateTime = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void scheduleTrip() {
        String pickupLocation = pickupLocationSpinner.getSelectedItem().toString();
        String dropLocation = dropLocationSpinner.getSelectedItem().toString();
        String scheduledTime = selectedDateTime.getTime().toString();

        // Construct a meaningful node name for the scheduled trip
        String tripName = "Trip_" + pickupLocation + "_to_" + dropLocation + "_" + scheduledTime;

        DatabaseReference newTripRef = scheduledTripsRef.child(tripName);
        newTripRef.setValue(new ScheduledTrip(pickupLocation, dropLocation, scheduledTime))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(scheduleDronex.this, "Trip Scheduled Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(scheduleDronex.this, "Failed to Schedule Trip", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public static class ScheduledTrip {
        public String pickupLocation;
        public String dropLocation;
        public String scheduledTime;

        public ScheduledTrip() {
        }

        public ScheduledTrip(String pickupLocation, String dropLocation, String scheduledTime) {
            this.pickupLocation = pickupLocation;
            this.dropLocation = dropLocation;
            this.scheduledTime = scheduledTime;
        }
    }
}
