package com.example.dronexx;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class mapView extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private MapView mapView;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private Button startNowButton;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // Initialize Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Drawer Layout and Navigation View
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        // Set up the navigation drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle navigation view item clicks here
            int id = item.getItemId();

            // Handle each item's action accordingly
            if (id == R.id.nav_profile) {
                // Open profile activity
                startActivity(new Intent(getApplicationContext(), profile.class));
            } else if (id == R.id.nav_about) {
                // Open about activity
                startActivity(new Intent(getApplicationContext(), about.class));
            } else if (id == R.id.nav_schedule) {
                // Open schedule activity
                startActivity(new Intent(getApplicationContext(), scheduleDronex.class));
            } else if (id == R.id.nav_feedback) {
                // Open schedule activity
                startActivity(new Intent(getApplicationContext(), feedback.class));
            }

            // Close the drawer
            drawerLayout.closeDrawers();
            return true;
        });

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        startNowButton = findViewById(R.id.startNowButton);
        startNowButton.setOnClickListener(v -> {
            // Start the Quick Tour activity
            startActivity(new Intent(mapView.this, quickTrip.class));
        });

        // Set up the SearchView with predefined options
        SearchView searchView = findViewById(R.id.searchView);
        String[] predefinedOptions = {
                "TEST1",
                "TEST2",
                "CHAPEL",
                "AB3",
                "STUDENT_SQUARE_LIBRARY",
                "AB2",
                "CAFETERIA",
                "THROWBALL_COURT",
                "GROUND",
                "PARKING",
                "CANTEEN",
                "ADMIN_BLOCK_ACADEMIC_BLOCK1",
                "AMPHITHEATER",
                "GATE"
        };

        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "options"});
        for (int i = 0; i < predefinedOptions.length; i++) {
            cursor.addRow(new Object[]{i, predefinedOptions[i]});
        }

        String[] from = {"options"};
        int[] to = {android.R.id.text1};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_list_item_1, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        searchView.setSuggestionsAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                MatrixCursor cursor = (MatrixCursor) adapter.getCursor();
                cursor.moveToPosition(position);

                String selectedOption = cursor.getString(cursor.getColumnIndex("options"));

                searchView.setQuery(selectedOption, true);

                fetchCoordinatesFromFirebase(selectedOption);

                searchView.clearFocus();

                return true;
            }
        });

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set initial camera position to St. Joseph Engineering College
        LatLng stJosephLocation = new LatLng(12.910953699, 74.89856947);
        mMap.addMarker(new MarkerOptions().position(stJosephLocation).title("SJEC"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(stJosephLocation));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(stJosephLocation, 19f));

        // Enable location button
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Get current location and move camera
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            }
        });
    }

    private void fetchCoordinatesFromFirebase(String selectedOption) {
        DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("locations");
        locationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String locationName = snapshot.getKey();
                    if (locationName.equals(selectedOption)) {
                        double latitude = snapshot.child("latitude").getValue(Double.class);
                        double longitude = snapshot.child("longitude").getValue(Double.class);
                        LatLng coordinates = new LatLng(latitude, longitude);
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(coordinates).title(selectedOption));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 19));
                        return;
                    }
                }
                Toast.makeText(mapView.this, "Coordinates for " + selectedOption + " not found.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mapView.this, "Error fetching coordinates: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
