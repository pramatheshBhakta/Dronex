package com.example.dronexx;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
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

import java.util.Arrays;
import java.util.List;

public class mapView extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;
    private Toolbar toolbar;
    private Button startNowButton;

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

        // Initialize Start Now Button
        startNowButton = findViewById(R.id.startNowButton);
        startNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Quick Tour activity
                startActivity(new Intent(mapView.this, quickTrip.class));
            }
        });
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

        // Initialize the Places client
        Places.initialize(getApplicationContext(), getString(R.string.places_api_key));
        PlacesClient placesClient = Places.createClient(this);

        // Specify the fields you want to retrieve
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ID);

        // Create a FindCurrentPlaceRequest with the specified fields
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Perform the request
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
