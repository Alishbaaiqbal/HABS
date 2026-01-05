package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TokenGenerationActivity extends AppCompatActivity {

    TextView tvTokenNumber, tvPatientName, tvDoctorName, tvDate, tvSlot, tvType;
    DatabaseReference appointmentRef;
    Button btnProceedPayment;

    String appointmentId;   // keep as field so we can reuse easily

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tokengeneration);

        // Bind UI
        tvTokenNumber = findViewById(R.id.tvToken);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDate = findViewById(R.id.tvDate);
        tvSlot = findViewById(R.id.tvSlot);
        tvType = findViewById(R.id.tvType);
        btnProceedPayment = findViewById(R.id.btnProceedPayment);

        // Firebase reference
        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        // Get appointmentId from previous screen
        appointmentId = getIntent().getStringExtra("appointmentId");

        if (appointmentId == null) {
            Toast.makeText(this, "Error: No Appointment ID received!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show token number (same as appointment ID)
        tvTokenNumber.setText("Your Token: " + appointmentId);

        // Fetch details from Firebase and then enable payment button
        fetchAppointmentDetails();
    }

    private void fetchAppointmentDetails() {
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
                    String patientEmail = snapshot.child("email").getValue(String.class); // may be null
                    String fee = snapshot.child("fee").getValue(String.class);            // fee
                    String patientUid = snapshot.child("userId").getValue(String.class);  // ⭐ NEW

                    // Fill UI
                    tvPatientName.setText(patientName);
                    tvDoctorName.setText("Dr. " + doctor);
                    tvDate.setText(date);
                    tvSlot.setText(slot);
                    tvType.setText(type);

                    // Button navigates to payment page
                    btnProceedPayment.setOnClickListener(v -> {
                        if (fee == null) {
                            Toast.makeText(this, "Fee is missing for this appointment.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Intent intent = new Intent(TokenGenerationActivity.this, PaymentMethodActivity.class);
                        intent.putExtra("appointmentId", appointmentId);
                        intent.putExtra("fee", fee);
                        intent.putExtra("patientName", patientName);
                        intent.putExtra("patientEmail", patientEmail);
                        intent.putExtra("patientUid", patientUid); // ⭐ SEND UID
                        startActivity(intent);
                    });

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load appointment: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
