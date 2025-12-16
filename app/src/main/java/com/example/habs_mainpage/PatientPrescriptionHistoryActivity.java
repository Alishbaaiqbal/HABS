package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class PatientPrescriptionHistoryActivity extends AppCompatActivity {

    ListView listView;

    ArrayList<String> displayList = new ArrayList<>();
    ArrayList<String> appointmentIds = new ArrayList<>();

    DatabaseReference prescriptionRef;

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

        prescriptionRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Prescriptions");

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

        prescriptionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                displayList.clear();
                appointmentIds.clear();

                if (!snapshot.exists()) {
                    displayList.add("No prescriptions found");
                    refreshList();
                    return;
                }

                for (DataSnapshot snap : snapshot.getChildren()) {

                    if (!snap.exists()) continue;

                    String appointmentId = snap.getKey();
                    String doctor = snap.child("doctorName").getValue(String.class);
                    String diagnosis = snap.child("diagnosis").getValue(String.class);
                    String patient = snap.child("patientName").getValue(String.class);

                    if (appointmentId == null) continue;

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
