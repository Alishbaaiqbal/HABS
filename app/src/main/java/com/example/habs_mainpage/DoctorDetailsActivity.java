package com.example.habs_mainpage;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DoctorDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctordetails);

        String hospitalName = getIntent().getStringExtra("hospital_name");

        TextView title = findViewById(R.id.tv_hospital_title);
        title.setText("Doctors at " + hospitalName);

        RecyclerView recyclerView = findViewById(R.id.recycler_doctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Doctor> doctorList = new ArrayList<>();
        doctorList.add(new Doctor("Dr. Ahmed", "Cardiologist", "10:00 AM - 2:00 PM"));
        doctorList.add(new Doctor("Dr. Fatima", "Dermatologist", "11:00 AM - 3:00 PM"));
        doctorList.add(new Doctor("Dr. Usman", "ENT Specialist", "12:00 PM - 4:00 PM"));

        DoctorAdapter adapter = new DoctorAdapter(doctorList);
        recyclerView.setAdapter(adapter);
    }
}