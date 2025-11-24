package com.example.habs_mainpage;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TokenGenerationActivity extends AppCompatActivity {

    TextView tvTokenNumber, tvPatientName, tvDoctorName, tvDate, tvSlot, tvType;
    DatabaseReference appointmentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tokengeneration);

        // Bind UI
        tvTokenNumber = findViewById(R.id.tvTokenNumber);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDate = findViewById(R.id.tvDate);
        tvSlot = findViewById(R.id.tvSlot);
        tvType = findViewById(R.id.tvType);

        // Firebase reference
        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        // Get appointmentId from previous screen
        String appointmentId = getIntent().getStringExtra("appointmentId");

        if (appointmentId == null) {
            Toast.makeText(this, "Error: No Appointment ID received!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show token number (same appointment ID)
        tvTokenNumber.setText("Your Token: " + appointmentId);

        // Fetch details from Firebase
        fetchAppointmentDetails(appointmentId);
    }

    private void fetchAppointmentDetails(String appointmentId) {
        appointmentRef.child(appointmentId).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Appointment not found!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Read values
                    String patientName = snapshot.child("patientName").getValue(String.class);
                    String doctor = snapshot.child("doctorName").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);
                    String slot = snapshot.child("slot").getValue(String.class);
                    String type = snapshot.child("type").getValue(String.class);

                    // Fill UI
                    tvPatientName.setText(patientName);
                    tvDoctorName.setText("Dr. " + doctor);
                    tvDate.setText(date);
                    tvSlot.setText(slot);
                    tvType.setText(type);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
