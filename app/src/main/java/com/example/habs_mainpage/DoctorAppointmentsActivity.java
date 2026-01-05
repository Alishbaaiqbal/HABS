package com.example.habs_mainpage;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorAppointmentsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AppointmentAdapter adapter;
    List<Appointment> appointmentList = new ArrayList<>();

    DatabaseReference appointmentRef;

    String hospitalCode;
    String doctorCode;
    String doctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointments);

        hospitalCode = getIntent().getStringExtra("hospitalCode");
        doctorCode = getIntent().getStringExtra("doctorCode");
        doctorName = getIntent().getStringExtra("doctorName");

        if (hospitalCode == null || doctorCode == null) {
            Toast.makeText(this, "Invalid doctor data", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView tvHeader = findViewById(R.id.tvDoctorHeader);
        tvHeader.setText("Appointments of " + doctorName);

        recyclerView = findViewById(R.id.recyclerDoctorAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ FIXED ADAPTER CALL
        adapter = new AppointmentAdapter(this, appointmentList);
        recyclerView.setAdapter(adapter);

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        loadDoctorAppointments();
    }

    private void loadDoctorAppointments() {

        appointmentRef.get().addOnSuccessListener(snapshot -> {

            appointmentList.clear();

            for (DataSnapshot child : snapshot.getChildren()) {

                String hc = child.child("hospitalCode").getValue(String.class);
                String dc = child.child("doctorCode").getValue(String.class);

                if (!hospitalCode.equalsIgnoreCase(hc)) continue;
                if (!doctorCode.equalsIgnoreCase(dc)) continue;

                String token = child.getKey();
                String patient = child.child("patientName").getValue(String.class);
                String date = child.child("date").getValue(String.class);
                String slot = child.child("slot").getValue(String.class);
                String type = child.child("type").getValue(String.class);
                String doctor = child.child("doctorName").getValue(String.class);

                appointmentList.add(
                        new Appointment(token, patient, doctor, date, slot, type)
                );
            }

            Collections.sort(appointmentList, (a1, a2) -> {
                try {
                    SimpleDateFormat sdf =
                            new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.US);
                    Date d1 = sdf.parse(a1.date + " " + a1.slot);
                    Date d2 = sdf.parse(a2.date + " " + a2.slot);
                    return d2.compareTo(d1);
                } catch (Exception e) {
                    return 0;
                }
            });

            adapter.notifyDataSetChanged();
        });
    }
}