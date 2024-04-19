package com.example.dronexx;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RouteMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DatabaseReference mDatabase;
    private GoogleMap mMap;
    private Marker droneMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mDatabase.child("pickup").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.moveCamera(cu);
                    plotRoute(pickup, drop);
                    // Add drone marker at pickup location
                    addDroneMarker(pickup);
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
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + pickup.latitude + "," + pickup.longitude + "&destination=" + drop.latitude + "," + drop.longitude + "&key=AIzaSyDdI1l4BzaKigjIUdl5HZBsBsghfJJZXao";

        new FetchRouteTask().execute(url);
    }

    private void addDroneMarker(LatLng position) {
        // Add drone marker at the specified position
        if (droneMarker != null) {
            droneMarker.remove();
        }

        // Load the bitmap with desired size
        Bitmap droneBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.drnicn1);
        int width = droneBitmap.getWidth();
        int height = droneBitmap.getHeight();

        // Define desired width and height
        int newWidth = 100; // Adjust this value as needed
        int newHeight = 100; // Adjust this value as needed

        // Calculate the scale factor
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // Resize the bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        // Recreate the new bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(droneBitmap, 0, 0, width, height, matrix, true);

        // Create the BitmapDescriptor from the resized bitmap
        BitmapDescriptor droneIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

        // Add the marker to the map
        MarkerOptions markerOptions = new MarkerOptions().position(position).icon(droneIcon);
        droneMarker = mMap.addMarker(markerOptions);
    }


    private class FetchRouteTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String data = "";
            try {
                HttpURLConnection connection = null;
                URL url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder stringBuilder = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                stream.close();
                data = stringBuilder.toString();
            } catch (Exception e) {
                Log.d("plotRoute", "Exception downloading URL: " + e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("plotRoute", "Received data: " + result); // Log the received data for debugging
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(android.graphics.Color.BLUE);
                lineOptions.geodesic(true);

            }

            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
                // Update drone marker position to the starting point of the route
                if (!points.isEmpty()) {
                    addDroneMarker(points.get(0));
                }
            }
        }
    }

    public class DirectionsJSONParser {
        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
            List<List<HashMap<String, String>>> routes = new ArrayList<>();
            JSONArray jRoutes = null;
            JSONArray jLegs = null;
            JSONArray jSteps = null;

            try {
                jRoutes = jObject.getJSONArray("routes");

                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List<HashMap<String, String>> path = new ArrayList<>();

                    for (int j = 0; j < jLegs.length(); j++) {
                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude));
                                hm.put("lng", Double.toString((list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }
}
