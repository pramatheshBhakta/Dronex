package com.example.dronexx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RouteMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DatabaseReference mDatabase;
    private DatabaseReference droneLocationRef;
    private GoogleMap mMap;
    private Marker droneMarker;
    private Handler handler;
    private static final int UPDATE_INTERVAL = 5000; // Update interval in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        droneLocationRef = FirebaseDatabase.getInstance().getReference().child("dronelocation");

        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Fetch pickup location
        mDatabase.child("pickup").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Fetch pickup location and plot route
                double pickupLat = dataSnapshot.child("latitude").getValue(Double.class);
                double pickupLng = dataSnapshot.child("longitude").getValue(Double.class);
                LatLng pickup = new LatLng(pickupLat, pickupLng);
                mMap.addMarker(new MarkerOptions().position(pickup).title("Pickup"));
                fetchDropLocation(pickup);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Start listening for drone location updates
        startDroneLocationUpdates();
    }

    private void fetchDropLocation(LatLng pickup) {
        mDatabase.child("drop").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.child("latitude").getValue() != null &&
                        dataSnapshot.child("longitude").getValue() != null) {
                    double dropLat = dataSnapshot.child("latitude").getValue(Double.class);
                    double dropLng = dataSnapshot.child("longitude").getValue(Double.class);
                    LatLng drop = new LatLng(dropLat, dropLng);
                    mMap.addMarker(new MarkerOptions().position(drop).title("Drop"));
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(pickup);
                    builder.include(drop);
                    LatLngBounds bounds = builder.build();
                    int padding = 100; // Padding in pixels from the map's edges
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                    plotRoute(pickup, drop);
                } else {
                    // Handle the case when the data is missing or null
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void plotRoute(LatLng pickup, LatLng drop) {
        // Implement your route plotting logic here
    }

    private void startDroneLocationUpdates() {
        droneLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double droneLat = dataSnapshot.child("latitude").getValue(Double.class);
                    double droneLng = dataSnapshot.child("longitude").getValue(Double.class);
                    LatLng droneLocation = new LatLng(droneLat, droneLng);
                    updateDroneMarker(droneLocation);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        // Schedule periodic updates
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startDroneLocationUpdates(); // Restart listening for updates after 5 seconds
            }
        }, UPDATE_INTERVAL);
    }

    private void updateDroneMarker(LatLng newLocation) {
        if (droneMarker != null) {
            droneMarker.setPosition(newLocation);
        } else {
            addDroneMarker(newLocation);
        }
    }

    private void addDroneMarker(LatLng position) {
        // Load the original drone icon bitmap
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.drnicn1);

        // Define desired width and height for the resized bitmap
        int width = 100; // Adjust this value as needed
        int height = 100; // Adjust this value as needed

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // Resize the bitmap
        float scaleWidth = ((float) width) / originalBitmap.getWidth();
        float scaleHeight = ((float) height) / originalBitmap.getHeight();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);

        // Create the BitmapDescriptor from the resized bitmap
        BitmapDescriptor droneIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

        // Add the marker to the map
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title("Drone")
                .icon(droneIcon)
                .anchor(0.5f, 0.5f); // Set anchor to center of the icon
        droneMarker = mMap.addMarker(markerOptions);
    }

}
