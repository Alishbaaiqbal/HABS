package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PatientPrescriptionHistoryActivity extends AppCompatActivity {

    ListView listView;

    ArrayList<String> displayList = new ArrayList<>();
    ArrayList<String> appointmentIds = new ArrayList<>();

    DatabaseReference prescriptionRef;
    DatabaseReference appointmentRef;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_prescription_history);

        listView = findViewById(R.id.listViewPrescriptions);
        if (listView == null) {
            Toast.makeText(this, "ListView not found in layout", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 🔑 Get current logged-in patient ID
        currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // References
        prescriptionRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Prescriptions");

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments"); // ✅ Correct path

        // Set empty adapter first
        listView.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        ));

        loadHistory();

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= appointmentIds.size()) return;
            String appointmentId = appointmentIds.get(position);
            if (appointmentId == null) return;

            Intent intent = new Intent(
                    PatientPrescriptionHistoryActivity.this,
                    PrescriptionDetailActivity.class
            );
            intent.putExtra("appointmentId", appointmentId);
            startActivity(intent);
        });
    }

    private void loadHistory() {
        // First, get all appointments for this user
        appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Set<String> userAppointmentIds = new HashSet<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String userId = snap.child("userId").getValue(String.class);
                    if (currentUserId.equals(userId)) {
                        userAppointmentIds.add(snap.getKey());
                    }
                }

                if (userAppointmentIds.isEmpty()) {
                    displayList.clear();
                    displayList.add("No prescriptions found");
                    refreshList();
                    return;
                }

                // Now get prescriptions for only these appointments
                prescriptionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        displayList.clear();
                        appointmentIds.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String appointmentId = snap.getKey();
                            if (!userAppointmentIds.contains(appointmentId)) continue;

                            String doctor = snap.child("doctorName").getValue(String.class);
                            String diagnosis = snap.child("diagnosis").getValue(String.class);
                            String patient = snap.child("patientName").getValue(String.class);

                            appointmentIds.add(appointmentId);
                            displayList.add(
                                    "Patient: " + safe(patient) +
                                            "\nDr. " + safe(doctor) +
                                            "\nDiagnosis: " + safe(diagnosis)
                            );
                        }

                        if (displayList.isEmpty()) {
                            displayList.add("No prescriptions found");
                        }

                        refreshList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        displayList.clear();
                        displayList.add("Failed to load prescriptions");
                        refreshList();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                displayList.clear();
                displayList.add("Failed to load appointments");
                refreshList();
            }
        });
    }

    private void refreshList() {
        listView.setAdapter(new ArrayAdapter<>(
                PatientPrescriptionHistoryActivity.this,
                android.R.layout.simple_list_item_1,
                displayList
        ));
    }

    private String safe(String v) {
        return v == null ? "-" : v;
    }
}
