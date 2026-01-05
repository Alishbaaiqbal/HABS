package com.example.habs_mainpage;

import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.*;

public class CreatePrescriptionActivity extends AppCompatActivity {

    TextView tvPatientInfo;
    EditText etDiagnosis, etMedName, etDosage, etFrequency, etDuration;
    Button btnSave;

    DatabaseReference appointmentRef, prescriptionRef;

    String appointmentId;
    String patientName = "";
    String patientContact = "";
    String doctorName = "";
    String doctorCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_prescription);

        tvPatientInfo = findViewById(R.id.tvSelectedPatient);
        etDiagnosis = findViewById(R.id.etDiagnosis);
        etMedName = findViewById(R.id.etMedicineName);
        etDosage = findViewById(R.id.etDosage);
        etFrequency = findViewById(R.id.etFrequency);
        etDuration = findViewById(R.id.etDuration);
        btnSave = findViewById(R.id.btnSavePrescription);

        appointmentId = getIntent().getStringExtra("appointmentId");

        if (appointmentId == null) {
            Toast.makeText(this, "Appointment missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        prescriptionRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Prescriptions");

        // 🔥 LOAD FULL APPOINTMENT DETAILS (JUST LIKE OLD CODE)
        loadAppointmentDetails();

        btnSave.setOnClickListener(v -> savePrescription());
    }

    private void loadAppointmentDetails() {

        appointmentRef.child(appointmentId).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Appointment not found", Toast.LENGTH_LONG).show();
                        finish();
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

        if (patientName.isEmpty() || doctorName.isEmpty()) {
            Toast.makeText(this, "Appointment data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String diagnosis = etDiagnosis.getText().toString().trim();
        String medName = etMedName.getText().toString().trim();

        if (diagnosis.isEmpty() || medName.isEmpty()) {
            Toast.makeText(this, "Diagnosis and medicine required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> prescription = new HashMap<>();
        prescription.put("appointmentId", appointmentId);
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

        prescription.put("medicines", Collections.singletonList(med));

        prescriptionRef.child(appointmentId)
                .setValue(prescription)
                .addOnSuccessListener(a ->
                        Toast.makeText(this,
                                "Prescription saved successfully",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private String safe(String v) {
        return v == null ? "-" : v;
    }
}