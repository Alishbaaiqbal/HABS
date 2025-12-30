package com.example.habs_mainpage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class HomePage extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HomePage";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private static final String API_KEY = "AIzaSyD0LZrCsWehM4x9opUS7gKPQobUUzvBJKA";

    private GoogleMap mMap;
    private Button btnMyAppointment;

    private FusedLocationProviderClient fusedLocationClient;
    private TextView currentAddressTextView;

    private RecyclerView recyclerView;
    private HospitalAdapter adapter;
    private final List<Hospital> hospitalList = new ArrayList<>();

    private boolean isLocationFetched = false;

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        currentAddressTextView = findViewById(R.id.tv_current_address);

        recyclerView = findViewById(R.id.recycler_hospitals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HospitalAdapter(this, hospitalList);
        recyclerView.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map_fragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnMyAppointment = findViewById(R.id.btnMyAppointment);
        btnMyAppointment.setOnClickListener(v -> {
            startActivity(new Intent(this, MyAppointmentsActivity.class));
        });

        // 🔹 FOOTER NAVIGATION SETUP
        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true; // already on home
            }

            if (id == R.id.nav_receipts) {
                startActivity(new Intent(this, MyReceiptsActivity.class));
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, PatientProfileActivity.class));
                return true;
            }
            if (id == R.id.nav_feedback) {
                startActivity(new Intent(this, FeedbackDoctorListActivity.class));
                return true;
            }


            return false;
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
            getLastKnownLocation();
        }
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null && !isLocationFetched) {
                        isLocationFetched = true;
                        updateMapAndFetchHospitals(location);
                    } else {
                        Toast.makeText(this,
                                "Unable to detect your location",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateMapAndFetchHospitals(Location location) {

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        LatLng userLatLng = new LatLng(lat, lng);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(userLatLng)
                .title("You are here"));

        mMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(userLatLng, 14f));

        updateAddressTextView(location);
        fetchNearbyHospitals(lat, lng);
    }

    private void updateAddressTextView(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1);

            if (addresses != null && !addresses.isEmpty()) {
                currentAddressTextView.setText(
                        "Your Location: " +
                                addresses.get(0).getAddressLine(0));
            } else {
                currentAddressTextView.setText("Address not found");
            }

        } catch (Exception e) {
            currentAddressTextView.setText("Error getting address");
        }
    }

    private void fetchNearbyHospitals(double lat, double lng) {

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                + "?location=" + lat + "," + lng
                + "&radius=5000"
                + "&type=hospital"
                + "&keyword=hospital"
                + "&key=" + API_KEY;

        Log.d(TAG, "Google API URL: " + url);

        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET,
                        url,
                        null,
                        response -> {

                            try {
                                hospitalList.clear();
                                mMap.clear();

                                JSONArray results =
                                        response.getJSONArray("results");

                                List<Hospital> tempList =
                                        new ArrayList<>();

                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject obj =
                                            results.getJSONObject(i);

                                    String name =
                                            obj.optString("name", "Unknown");

                                    JSONObject loc =
                                            obj.getJSONObject("geometry")
                                                    .getJSONObject("location");

                                    double hLat =
                                            loc.getDouble("lat");
                                    double hLng =
                                            loc.getDouble("lng");

                                    float[] dist = new float[1];
                                    Location.distanceBetween(
                                            lat, lng, hLat, hLng, dist);

                                    float km = dist[0] / 1000f;
                                    if (km > 5.0f) continue;

                                    String distance =
                                            String.format(Locale.US,
                                                    "%.1f km", km);

                                    tempList.add(
                                            new Hospital(name,
                                                    distance,
                                                    "Open", ""));

                                    mMap.addMarker(
                                            new MarkerOptions()
                                                    .position(
                                                            new LatLng(hLat, hLng))
                                                    .title(name));
                                }

                                Collections.sort(tempList,
                                        Comparator.comparingDouble(h ->
                                                Double.parseDouble(
                                                        h.distance.replace(" km", "")))
                                );

                                hospitalList.addAll(tempList);
                                adapter.notifyDataSetChanged();

                                Toast.makeText(this,
                                        hospitalList.size()
                                                + " hospitals within 5 km",
                                        Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                Toast.makeText(this,
                                        "Error reading data",
                                        Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            Toast.makeText(this,
                                    "Failed to load hospitals",
                                    Toast.LENGTH_SHORT).show();
                        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
