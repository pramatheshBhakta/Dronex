package com.example.dronexx;

import static android.content.ContentValues.TAG;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class mapView extends AppCompatActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        private MapView mapView;
        private Toolbar toolbar;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map_view);

            // Initialize Toolbar
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // Initialize MapView
            mapView = findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
        }
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Set initial camera position to St. Joseph Engineering College
            LatLng stJosephLocation = new LatLng(12.910953699 , 74.89856947);
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

            // Initialize the Places client
            Places.initialize(getApplicationContext(), "AIzaSyDdI1l4BzaKigjIUdl5HZBsBsghfJJZXao");
            PlacesClient placesClient = Places.createClient(this);

            // Specify the fields you want to retrieve
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ID);

            // Create a FindCurrentPlaceRequest with the specified fields
            FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

            // Perform the request
            placesClient.findCurrentPlace(request).addOnSuccessListener(response -> {
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    Place place = placeLikelihood.getPlace();
                    // Add a marker for the place
                    mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName()));
                }
            }).addOnFailureListener(exception -> {
                if (exception instanceof ApiException) {
                    Log.e(TAG, "Place not found: " + ((ApiException) exception).getStatusCode());
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
