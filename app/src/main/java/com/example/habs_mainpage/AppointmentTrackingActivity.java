package com.example.habs_mainpage;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AppointmentTrackingActivity extends AppCompatActivity {

    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList = new ArrayList<>();

    DatabaseReference appointmentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_tracking);

        RecyclerView recyclerView = findViewById(R.id.recyclerAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppointmentAdapter(appointmentList);
        recyclerView.setAdapter(adapter);

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        loadAppointments();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadAppointments() {
        appointmentRef.get().addOnSuccessListener(snapshot -> {
            appointmentList.clear();

            for (DataSnapshot child : snapshot.getChildren()) {
                String token = child.getKey();
                String patientName = child.child("patientName").getValue(String.class);
                String doctor = child.child("doctorName").getValue(String.class);
                String date = child.child("date").getValue(String.class);
                String slot = child.child("slot").getValue(String.class);
                String type = child.child("type").getValue(String.class);

                appointmentList.add(new Appointment(token, patientName, doctor, date, slot, type));
            }

            adapter.notifyDataSetChanged();
        });
    }
}
