package com.example.habs_mainpage;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DoctorDetailsActivity extends AppCompatActivity {

    private List<Doctor> doctorList;
    private List<Doctor> filteredList;
    private DoctorAdapter adapter;

    Spinner spinnerTime, spinnerAvailability, spinnerSpecialization, spinnerFees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctordetails);

        String hospitalName = getIntent().getStringExtra("hospital_name");

        TextView title = findViewById(R.id.tv_hospital_title);
        title.setText("Doctors at " + hospitalName);

        RecyclerView recyclerView = findViewById(R.id.recycler_doctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- Hardcoded Doctor Data (with Consultation Time) ---
        doctorList = new ArrayList<>();
        doctorList.add(new Doctor("Dr. Ahmed Khan", "Cardiologist", "Morning", true, "2000", "15"));
        doctorList.add(new Doctor("Dr. Fatima Malik", "Dermatologist", "Afternoon", false, "1500", "20"));
        doctorList.add(new Doctor("Dr. Usman Ali", "ENT Specialist", "Evening", true, "2500", "10"));
        doctorList.add(new Doctor("Dr. Sara Ahmed", "Pediatrician", "Morning", true, "1000", "25"));
        doctorList.add(new Doctor("Dr. Ali Raza", "Neurologist", "Afternoon", false, "3000", "30"));
        doctorList.add(new Doctor("Dr. Ayesha Noor", "Gynecologist", "Evening", true, "2200", "20"));
        doctorList.add(new Doctor("Dr. Bilal Hussain", "Orthopedic Surgeon", "Morning", true, "3500", "25"));
        doctorList.add(new Doctor("Dr. Imran Sheikh", "General Physician", "Afternoon", true, "1200", "15"));
        doctorList.add(new Doctor("Dr. Nadia Khan", "Psychiatrist", "Evening", false, "2800", "40"));
        filteredList = new ArrayList<>(doctorList);
        adapter = new DoctorAdapter(filteredList);
        recyclerView.setAdapter(adapter);

        // --- Initialize Spinners ---
        spinnerTime = findViewById(R.id.spinner_time);
        spinnerAvailability = findViewById(R.id.spinner_availability);
        spinnerSpecialization = findViewById(R.id.spinner_specialization);
        spinnerFees = findViewById(R.id.spinner_fees);

        setupSpinners();
    }

    private void setupSpinners() {
        // Time filter
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Morning", "Afternoon", "Evening"});
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);

        // Availability filter
        ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Available", "Not Available"});
        availabilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAvailability.setAdapter(availabilityAdapter);

        // Specialization filter
        ArrayAdapter<String> specializationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Cardiologist", "Dermatologist", "ENT Specialist", "Pediatrician", "Neurologist"});
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialization.setAdapter(specializationAdapter);

        // Fees filter
        ArrayAdapter<String> feesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "≤1500", "1501-2500", "2501-3000"});
        feesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFees.setAdapter(feesAdapter);

        // Set listeners for all spinners
        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerTime.setOnItemSelectedListener(filterListener);
        spinnerAvailability.setOnItemSelectedListener(filterListener);
        spinnerSpecialization.setOnItemSelectedListener(filterListener);
        spinnerFees.setOnItemSelectedListener(filterListener);
    }

    private void applyFilters() {
        String selectedTime = spinnerTime.getSelectedItem().toString();
        String selectedAvailability = spinnerAvailability.getSelectedItem().toString();
        String selectedSpecialization = spinnerSpecialization.getSelectedItem().toString();
        String selectedFees = spinnerFees.getSelectedItem().toString();

        filteredList.clear();

        for (Doctor d : doctorList) {
            boolean matches = true;

            // Time
            if (!selectedTime.equals("All") && !d.timing.equals(selectedTime)) {
                matches = false;
            }

            // Availability
            if (!selectedAvailability.equals("All") && !d.isAvailability()) {
                matches = false;
            }

            // Specialization
            if (!selectedSpecialization.equals("All") && !d.specialization.equals(selectedSpecialization)) {
                matches = false;
            }

            // Fees
            if (!selectedFees.equals("All")) {
                int fee = Integer.parseInt(d.fees);
                switch (selectedFees) {
                    case "≤1500":
                        if (fee > 1500) matches = false;
                        break;
                    case "1501-2500":
                        if (fee < 1501 || fee > 2500) matches = false;
                        break;
                    case "2501-3000":
                        if (fee < 2501 || fee > 3000) matches = false;
                        break;
                }
            }

            if (matches) {
                filteredList.add(d);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
