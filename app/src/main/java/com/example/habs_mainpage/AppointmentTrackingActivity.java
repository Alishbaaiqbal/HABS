package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import com.example.habs_mainpage.DoctorListAdapter;


public class AppointmentTrackingActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DoctorListAdapter adapter;

    List<DoctorItem> doctorList = new ArrayList<>();

    DatabaseReference appointmentRef;
    String hospitalCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_tracking);

        hospitalCode = getIntent().getStringExtra("hospitalCode");

        if (hospitalCode == null || hospitalCode.isEmpty()) {
            Toast.makeText(this, "Hospital not identified", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DoctorListAdapter(doctorList, doctor -> {
            Intent intent = new Intent(
                    AppointmentTrackingActivity.this,
                    DoctorAppointmentsActivity.class
            );
            intent.putExtra("hospitalCode", hospitalCode);
            intent.putExtra("doctorCode", doctor.doctorCode);
            intent.putExtra("doctorName", doctor.doctorName);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        loadDoctors();
    }

    private void loadDoctors() {

        appointmentRef.get().addOnSuccessListener(snapshot -> {

            doctorList.clear();

            for (DataSnapshot child : snapshot.getChildren()) {

                String hc = child.child("hospitalCode").getValue(String.class);
                String doctorCode = child.child("doctorCode").getValue(String.class);
                String doctorName = child.child("doctorName").getValue(String.class);

                if (!hospitalCode.equalsIgnoreCase(hc)) continue;
                if (doctorCode == null || doctorName == null) continue;

                boolean alreadyAdded = false;
                for (DoctorItem d : doctorList) {
                    if (d.doctorCode.equals(doctorCode)) {
                        alreadyAdded = true;
                        break;
                    }
                }

                if (!alreadyAdded) {
                    doctorList.add(new DoctorItem(doctorCode, doctorName));
                }
            }

            adapter.notifyDataSetChanged();
        });
    }
}
