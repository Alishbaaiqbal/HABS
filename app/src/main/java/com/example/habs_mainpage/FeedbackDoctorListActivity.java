package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeedbackDoctorListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FeedbackDoctorAdapter adapter;

    List<DoctorItem> doctorList = new ArrayList<>();

    DatabaseReference appointmentRef;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_doctor_list);

        recyclerView = findViewById(R.id.recyclerDoctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FeedbackDoctorAdapter(doctorList, doctor -> {

            Intent intent = new Intent(
                    FeedbackDoctorListActivity.this,
                    DoctorFeedbackActivity.class
            );

            // ✅ CORRECT DATA FLOW
            intent.putExtra("doctorCode", doctor.doctorCode); // Firebase
            intent.putExtra("doctorName", doctor.doctorName); // UI

            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        userId = FirebaseAuth.getInstance().getUid();

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        loadBookedDoctors();
    }

    private void loadBookedDoctors() {

        appointmentRef.orderByChild("userId").equalTo(userId)
                .get().addOnSuccessListener(snapshot -> {

                    doctorList.clear();
                    Set<String> addedDoctors = new HashSet<>();

                    for (DataSnapshot child : snapshot.getChildren()) {

                        String doctorCode =
                                child.child("doctorCode").getValue(String.class);
                        String doctorName =
                                child.child("doctorName").getValue(String.class);

                        if (doctorCode == null || doctorName == null)
                            continue;

                        if (addedDoctors.contains(doctorCode))
                            continue;

                        addedDoctors.add(doctorCode);

                        // ✅ FIXED CONSTRUCTOR ORDER
                        doctorList.add(
                                new DoctorItem(doctorName, doctorCode)
                        );
                    }

                    adapter.notifyDataSetChanged();

                    if (doctorList.isEmpty()) {
                        Toast.makeText(
                                this,
                                "No doctors found for feedback",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
