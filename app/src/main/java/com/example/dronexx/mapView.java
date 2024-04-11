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
import androidx.appcompat.widget.SearchView;

import android.widget.CursorAdapter;
import android.widget.CursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.List;

public class mapView extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private MapView mapView;
    private Toolbar toolbar;
    private Button startNowButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FusedLocationProviderClient fusedLocationProviderClient;

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
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            } else if (id == R.id.nav_about) {
                // Open about activity
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
            } else if (id == R.id.nav_support) {
                // Open support activity
                startActivity(new Intent(getApplicationContext(), SupportActivity.class));
            } else if (id == R.id.nav_schedule) {
                // Open schedule activity
                startActivity(new Intent(getApplicationContext(), ScheduleActivity.class));
            }

            // Close the drawer
            drawerLayout.closeDrawers();
            return true;
        });

        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize Start Now Button
        startNowButton = findViewById(R.id.startNowButton);
        startNowButton.setOnClickListener(v -> {
            // Start the Quick Tour activity
            startActivity(new Intent(mapView.this, quickTrip.class));
        });

        // Set up the SearchView with predefined options
        // Set up the SearchView with predefined options
        SearchView searchView = findViewById(R.id.searchView);
        String[] predefinedOptions = {"Chapel", "StudentSquare", "Academic Block 1", "Academic Block 2", "Academic Block 3"};
        MatrixCursor cursor = new MatrixCursor(new String[]{"_id", "options"});
        for (int i = 0; i < predefinedOptions.length; i++) {
            cursor.addRow(new Object[]{i, predefinedOptions[i]});
        }

        String[] from = {"options"}; // Ensure this matches with the column name in the MatrixCursor
        int[] to = {android.R.id.text1}; // Use the built-in text1 resource ID defined in Android

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, android.R.layout.simple_list_item_1, cursor, from, to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        searchView.setSuggestionsAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle query submission
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle query text change
                return false;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // Handle suggestion selection
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // Handle suggestion click
                MatrixCursor cursor = (MatrixCursor) adapter.getCursor();
                cursor.moveToPosition(position);

                // Retrieve the selected option
                String selectedOption = cursor.getString(cursor.getColumnIndex("options"));

                // Set the selected option as the text in the search view
                searchView.setQuery(selectedOption, true);

                // Optionally, collapse the search view after selecting an item
                searchView.clearFocus();

                // Display a toast message
                Toast.makeText(mapView.this, "Selected option: " + selectedOption, Toast.LENGTH_SHORT).show();
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

        // Plotting GPS Coordinates on Long Press
        mMap.setOnMapLongClickListener(latLng -> {
            mMap.clear(); // Clear existing markers
            mMap.addMarker(new MarkerOptions().position(latLng));
            // Display the coordinates at the bottom left of the screen
            Toast.makeText(this, "Coordinates: " + latLng.latitude + ", " + latLng.longitude, Toast.LENGTH_SHORT).show();
        });

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

        // Initialize the Places client
        Places.initialize(getApplicationContext(), getString(R.string.places_api_key));
        PlacesClient placesClient = Places.createClient(this);

        // Specify the fields you want to retrieve
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);

        // Create a FindCurrentPlaceRequest with the specified fields
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Perform the request
        placesClient.findCurrentPlace(request).addOnSuccessListener((response) -> {
            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                Place place = placeLikelihood.getPlace();
                // Add a marker for the place
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
            }
        }).addOnFailureListener((exception) -> {
            Toast.makeText(this, "Place not found: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
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