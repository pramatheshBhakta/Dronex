package com.example.dronexx;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class multimediaIntercation extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ImageView multimediaView; // Reference to multimedia view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimedia_intercation);

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
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                    startActivity(new Intent(getApplicationContext(), scheduleDronex.class));
                }

                // Close the drawer
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Initialize multimedia view
        multimediaView = findViewById(R.id.multimedia_view);

        // Fetch multimedia content from Firebase and set it to the multimedia view
        fetchMultimediaContent();
    }

    private void fetchMultimediaContent() {
        // TODO: Implement fetching instructions/captions and multimedia content from Firebase
        // For demonstration purposes, let's display a toast message
        Toast.makeText(this, "Fetching multimedia content from Firebase...", Toast.LENGTH_SHORT).show();
        // You need to replace this with actual Firebase database queries to fetch content
        // and set it to the multimedia view
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
