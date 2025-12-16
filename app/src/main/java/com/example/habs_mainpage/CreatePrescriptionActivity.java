package com.example.habs_mainpage;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.*;

public class CreatePrescriptionActivity extends AppCompatActivity {

    Spinner spinnerAppointments;
    TextView tvPatientInfo;
    EditText etDiagnosis, etMedName, etDosage, etFrequency, etDuration;
    Button btnSave;

    DatabaseReference appointmentRef, prescriptionRef;

    String selectedAppointmentId = null;
    String patientName = "";
    String patientContact = "";
    String doctorName = "";
    String doctorCode = "";

    List<String> appointmentIds = new ArrayList<>();
    List<String> spinnerItems = new ArrayList<>();
    ArrayAdapter<String> spinnerAdapter;

    boolean ignoreFirstSelection = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_prescription);

        spinnerAppointments = findViewById(R.id.spinnerAppointments);
        tvPatientInfo = findViewById(R.id.tvSelectedPatient);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etMedName = findViewById(R.id.etMedicineName);
        etDosage = findViewById(R.id.etDosage);
        etFrequency = findViewById(R.id.etFrequency);
        etDuration = findViewById(R.id.etDuration);
        btnSave = findViewById(R.id.btnSavePrescription);

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        prescriptionRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Prescriptions");

        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerItems
        );
        spinnerAppointments.setAdapter(spinnerAdapter);

        spinnerAppointments.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {

                if (ignoreFirstSelection) {
                    ignoreFirstSelection = false;
                    return;
                }

                if (position == 0) {
                    selectedAppointmentId = null;
                    tvPatientInfo.setText("No appointment selected");
                    return;
                }

                if (position < 0 || position >= appointmentIds.size()) return;

                selectedAppointmentId = appointmentIds.get(position);
                loadAppointmentDetails(selectedAppointmentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnSave.setOnClickListener(v -> savePrescription());

        loadPendingAppointments();
    }

    private void loadPendingAppointments() {

        appointmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                appointmentIds.clear();
                spinnerItems.clear();

                appointmentIds.add("NONE");
                spinnerItems.add("Select Appointment");

                for (DataSnapshot snap : snapshot.getChildren()) {

                    String status = snap.child("status").getValue(String.class);
                    if (!"Pending".equalsIgnoreCase(status)) continue;

                    String appointmentId = snap.getKey();
                    String patient = snap.child("patientName").getValue(String.class);
                    String date = snap.child("date").getValue(String.class);
                    String slot = snap.child("slot").getValue(String.class);

                    if (appointmentId == null) continue;

                    appointmentIds.add(appointmentId);
                    spinnerItems.add(
                            appointmentId + " | " +
                                    safe(patient) + " | " +
                                    safe(date) + " | " +
                                    safe(slot)
                    );
                }

                spinnerAdapter.notifyDataSetChanged();

                if (appointmentIds.size() == 1) {
                    Toast.makeText(CreatePrescriptionActivity.this,
                            "No pending appointments found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreatePrescriptionActivity.this,
                        "Failed to load appointments",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAppointmentDetails(String appointmentId) {

        appointmentRef.child(appointmentId).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        tvPatientInfo.setText("Appointment not found");
                        return;
                    }

                    patientName = snapshot.child("patientName").getValue(String.class);
                    patientContact = snapshot.child("contact").getValue(String.class);
                    doctorName = snapshot.child("doctorName").getValue(String.class);
                    doctorCode = snapshot.child("doctorCode").getValue(String.class);

                    tvPatientInfo.setText(
                            "Patient: " + safe(patientName) +
                                    " (" + safe(patientContact) + ")"
                    );
                });
    }

    private void savePrescription() {

        if (selectedAppointmentId == null) {
            Toast.makeText(this, "Select an appointment first", Toast.LENGTH_SHORT).show();
            return;
        }

        String diagnosis = etDiagnosis.getText().toString().trim();
        String medName = etMedName.getText().toString().trim();

        if (diagnosis.isEmpty() || medName.isEmpty()) {
            Toast.makeText(this, "Diagnosis and medicine required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> prescription = new HashMap<>();
        prescription.put("appointmentId", selectedAppointmentId);
        prescription.put("doctorName", doctorName);
        prescription.put("doctorCode", doctorCode);
        prescription.put("patientName", patientName);
        prescription.put("diagnosis", diagnosis);
        prescription.put("createdAt", System.currentTimeMillis());

        Map<String, String> med = new HashMap<>();
        med.put("name", medName);
        med.put("dosage", etDosage.getText().toString().trim());
        med.put("frequency", etFrequency.getText().toString().trim());
        med.put("duration", etDuration.getText().toString().trim());

        List<Map<String, String>> meds = new ArrayList<>();
        meds.add(med);

        prescription.put("medicines", meds);

        prescriptionRef.child(selectedAppointmentId)
                .setValue(prescription)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this,
                                "Prescription saved successfully",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to save prescription",
                                Toast.LENGTH_SHORT).show());
    }

    private String safe(String v) {
        return v == null ? "-" : v;
    }
}
