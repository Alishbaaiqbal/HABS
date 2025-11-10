package com.example.habs_mainpage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    // ✅ Your Google API key
    private static final String API_KEY = "AIzaSyD0LZrCsWehM4x9opUS7gKPQobUUzvBJKA";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView currentAddressTextView;

    private RecyclerView recyclerView;
    private HospitalAdapter adapter;
    private final List<Hospital> hospitalList = new ArrayList<>();

    private boolean isLocationFetched = false; // Prevent multiple calls

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

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
            getLastKnownLocation();
        }
    }

    // ✅ Fetch only one location and then show hospitals
    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && !isLocationFetched) {
                isLocationFetched = true; // prevent multiple updates
                updateMapAndFetchHospitals(location);
            } else {
                Toast.makeText(this, "Unable to detect your location. Please enable GPS.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapAndFetchHospitals(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        LatLng userLatLng = new LatLng(lat, lng);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLatLng).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f));

        updateAddressTextView(location);
        fetchNearbyHospitals(lat, lng);
    }

    private void updateAddressTextView(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                currentAddressTextView.setText("Your Location: " + addresses.get(0).getAddressLine(0));
            } else {
                currentAddressTextView.setText("Address not found");
            }
        } catch (Exception e) {
            currentAddressTextView.setText("Error getting address");
        }
    }

    // ✅ Fetch hospitals using Google Places API
    private void fetchNearbyHospitals(double lat, double lng) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                + "?location=" + lat + "," + lng
                + "&radius=5000"
                + "&type=hospital"
                + "&keyword=hospital"
                + "&key=" + API_KEY;

        Log.d(TAG, "Google API URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        hospitalList.clear();
                        mMap.clear();

                        JSONArray results = response.getJSONArray("results");
                        if (results.length() == 0) {
                            Toast.makeText(this, "No hospitals found nearby.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        List<Hospital> tempList = new ArrayList<>();

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject obj = results.getJSONObject(i);
                            String name = obj.optString("name", "Unknown");
                            JSONObject loc = obj.getJSONObject("geometry").getJSONObject("location");
                            double hospitalLat = loc.getDouble("lat");
                            double hospitalLng = loc.getDouble("lng");

                            float[] dist = new float[1];
                            Location.distanceBetween(lat, lng, hospitalLat, hospitalLng, dist);
                            float distanceKm = dist[0] / 1000f;

                            if (distanceKm > 5.0f) continue; // ✅ show only 5 km radius

                            String distance = String.format(Locale.US, "%.1f km", distanceKm);
                            Hospital hospital = new Hospital(name, distance, "Open", "");
                            tempList.add(hospital);

                            // Marker
                            LatLng hospitalLatLng = new LatLng(hospitalLat, hospitalLng);
                            mMap.addMarker(new MarkerOptions().position(hospitalLatLng).title(name));
                        }

                        // ✅ Sort by distance (nearest first)
                        Collections.sort(tempList, Comparator.comparingDouble(h -> {
                            try {
                                return Double.parseDouble(h.distance.replace(" km", ""));
                            } catch (Exception e) {
                                return 0;
                            }
                        }));

                        hospitalList.addAll(tempList);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(this,
                                hospitalList.size() + " hospitals within 5 km",
                                Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        Toast.makeText(this, "Error reading data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "API Error: " + error.getMessage());
                    Toast.makeText(this, "Failed to load hospitals", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
