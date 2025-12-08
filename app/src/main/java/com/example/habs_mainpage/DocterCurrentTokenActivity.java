package com.example.habs_mainpage;

import android.os.Bundle;
import android.os.Handler;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DocterCurrentTokenActivity extends AppCompatActivity {

    Spinner spinnerDoctors;
    TextView tvToken, tvStatus;

    DatabaseReference appointmentRef;
    String userId;

    List<String> doctorList = new ArrayList<>();

    String selectedDoctor = "";

    Handler handler = new Handler();
    Runnable autoRefreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docter_current_token);

        spinnerDoctors = findViewById(R.id.spinnerDoctors);
        tvToken = findViewById(R.id.tvCurrentToken);
        tvStatus = findViewById(R.id.tvStatus);

        userId = FirebaseAuth.getInstance().getUid();

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        loadDoctorsForUser();
        setupAutoRefresh();
    }

    private void setupAutoRefresh() {
        autoRefreshRunnable = () -> {
            if (!selectedDoctor.isEmpty()) {
                loadCurrentToken(selectedDoctor);
            }
            handler.postDelayed(autoRefreshRunnable, 15000);
        };

        handler.postDelayed(autoRefreshRunnable, 15000);
    }

    private void loadDoctorsForUser() {
        appointmentRef.get().addOnSuccessListener(snapshot -> {

            doctorList.clear();

            for (DataSnapshot child : snapshot.getChildren()) {

                String doctor = safeGet(child, "doctorName");

                if (!doctor.isEmpty() && !doctorList.contains(doctor)) {
                    doctorList.add(doctor);
                }
            }

            if (doctorList.isEmpty()) {
                Toast.makeText(this, "No doctors found!", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctorList);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDoctors.setAdapter(adapter);

            spinnerDoctors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                    selectedDoctor = doctorList.get(position);
                    loadCurrentToken(selectedDoctor);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error loading doctors: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }


    private void loadCurrentToken(String doctorName) {

        appointmentRef.get().addOnSuccessListener(snapshot -> {

            long now = System.currentTimeMillis();
            long currentSlotIndex = now / (15 * 60 * 1000);

            String foundToken = null;

            for (DataSnapshot appt : snapshot.getChildren()) {

                String doctor = safeGet(appt, "doctorName");
                String date = safeGet(appt, "date");
                String slot = safeGet(appt, "slot");

                if (!doctor.equalsIgnoreCase(doctorName)) continue;

                long apptTime = convertToMillis(date, slot);
                if (apptTime == 0) continue;

                long apptIndex = apptTime / (15 * 60 * 1000);

                if (apptIndex == currentSlotIndex) {
                    foundToken = safeGet(appt, "appointmentId");
                    break;
                }
            }

            if (foundToken != null) {
                tvToken.setText("Current Token: " + foundToken);
                tvStatus.setText("Status: Calling Patient");
            } else {
                tvToken.setText("No Token");
                tvStatus.setText("Status: Waiting…");
            }

        });
    }



    private long convertToMillis(String date, String slot) {
        try {
            if (date == null || slot == null || date.isEmpty() || slot.isEmpty())
                return 0;

            // Slot will be "5:30 PM - 5:45 PM"
            if (slot.contains("-")) {
                slot = slot.split("-")[0].trim();  // Only first time
            }

            // Your DB date format = "27 Nov 2025"
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm a");

            String combined = date + " " + slot;   // EXAMPLE → "27 Nov 2025 5:30 PM"

            Date d = sdf.parse(combined);
            return d.getTime();

        } catch (Exception e) {
            return 0;
        }
    }


    private String safeGet(DataSnapshot snap, String key) {
        String value = snap.child(key).getValue(String.class);
        return value == null ? "" : value;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(autoRefreshRunnable);
    }
}
