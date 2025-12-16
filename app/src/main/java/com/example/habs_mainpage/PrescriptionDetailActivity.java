package com.example.habs_mainpage;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PrescriptionDetailActivity extends AppCompatActivity {

    TextView tvDoctor, tvDiagnosis, tvMedicines;

    DatabaseReference prescriptionRef;
    String appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_detail);

        tvDoctor = findViewById(R.id.tvDoctorName);
        tvDiagnosis = findViewById(R.id.tvDiagnosis);
        tvMedicines = findViewById(R.id.tvMedicines);

        appointmentId = getIntent().getStringExtra("appointmentId");

        prescriptionRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Prescriptions");

        loadDetails();
    }

    private void loadDetails() {

        prescriptionRef.child(appointmentId).get().addOnSuccessListener(snapshot -> {

            String doctor = snapshot.child("doctorName").getValue(String.class);
            String diagnosis = snapshot.child("diagnosis").getValue(String.class);

            tvDoctor.setText("Doctor: Dr. " + doctor);
            tvDiagnosis.setText("Diagnosis: " + diagnosis);

            StringBuilder meds = new StringBuilder();

            for (DataSnapshot medSnap : snapshot.child("medicines").getChildren()) {
                meds.append("• ")
                        .append(medSnap.child("name").getValue(String.class))
                        .append(" | ")
                        .append(medSnap.child("dosage").getValue(String.class))
                        .append(" | ")
                        .append(medSnap.child("frequency").getValue(String.class))
                        .append(" | ")
                        .append(medSnap.child("duration").getValue(String.class))
                        .append("\n\n");
            }

            tvMedicines.setText(meds.toString());
        });
    }
}
