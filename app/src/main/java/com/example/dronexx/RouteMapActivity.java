package com.example.dronexx;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RouteMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private DatabaseReference droneLocationRef;
    private DatabaseReference pickupLocationRef, dropLocationRef;
    private MarkerOptions pickupMarkerOptions, dropMarkerOptions, droneMarkerOptions;
    private LatLng pickupLatLng, dropLatLng, droneLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);
        droneLocationRef = FirebaseDatabase.getInstance().getReference().child("droneLocation");
        pickupLocationRef = FirebaseDatabase.getInstance().getReference().child("pickup");
        dropLocationRef = FirebaseDatabase.getInstance().getReference().child("drop");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        droneLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double droneLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double droneLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                    droneLatLng = new LatLng(droneLatitude, droneLongitude);

                    // Update the drone marker on the map
                    droneMarkerOptions.position(droneLatLng);
                    mMap.clear(); // Clear the map to remove old markers
                    mMap.addMarker(droneMarkerOptions);
                    // Redraw the route if needed
                    drawRoute(pickupLatLng, dropLatLng);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RouteMapActivity.this, "Failed to fetch drone location: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Retrieve pickup location from Firebase
        pickupLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double pickupLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double pickupLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                    pickupLatLng = new LatLng(pickupLatitude, pickupLongitude);

                    pickupMarkerOptions = new MarkerOptions().position(pickupLatLng).title("Pickup Location");
                    mMap.addMarker(pickupMarkerOptions);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RouteMapActivity.this, "Failed to fetch pickup location: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Retrieve drop location from Firebase
        dropLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double dropLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double dropLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                    dropLatLng = new LatLng(dropLatitude, dropLongitude);

                    dropMarkerOptions = new MarkerOptions().position(dropLatLng).title("Drop Location");
                    mMap.addMarker(dropMarkerOptions);

                    // Move camera to show both pickup and drop locations
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(pickupLatLng);
                    builder.include(dropLatLng);
                    LatLngBounds bounds = builder.build();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RouteMapActivity.this, "Failed to fetch drop location: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        droneMarkerOptions = new MarkerOptions().title("Drone Location");
        droneLatLng = new LatLng(0, 0); // Initial position
        droneMarkerOptions.position(droneLatLng);
        mMap.addMarker(droneMarkerOptions);

        // Draw route between pickup and drop locations
        drawRoute(pickupLatLng, dropLatLng);

        // Update drone location in real-time
        updateDroneLocation();
    }

    private void drawRoute(LatLng start, LatLng end) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + start.latitude + "," + start.longitude + "&destination=" + end.latitude + "," + end.longitude + "&key=YOUR_API_KEY";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray routes = response.getJSONArray("routes");
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                            String points = overviewPolyline.getString("points");
                            List<LatLng> decodePoly = PolyUtil.decode(points);
                            PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.addAll(decodePoly);
                            polylineOptions.width(12);
                            polylineOptions.color(Color.BLUE);
                            mMap.addPolyline(polylineOptions);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } ,new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                    }
                });

        // Add the request to the RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }
    private void updateDroneLocation() {
        droneLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    double droneLatitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double droneLongitude = dataSnapshot.child("longitude").getValue(Double.class);
                    droneLatLng = new LatLng(droneLatitude, droneLongitude);

                    // Update the drone marker on the map
                    droneMarkerOptions.position(droneLatLng);
                    mMap.clear(); // Clear the map to remove old markers
                    mMap.addMarker(droneMarkerOptions);
                    // Redraw the route if needed
                    drawRoute(pickupLatLng, dropLatLng);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RouteMapActivity.this, "Failed to fetch drop location: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

}
}