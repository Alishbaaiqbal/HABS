package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppointmentBookingActivity extends AppCompatActivity {

    TextView tvDoctorName, tvSpecialization, tvFee, tvTiming;
    DatePicker datePicker;
    GridView gridSlots;
    EditText etReason, etPatientName, etContact;
    RadioGroup rgConsultationType;
    CheckBox cbConsent;
    Button btnConfirm;

    String[] generatedSlots;
    String selectedSlot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointmentbooking);

        // Bind UI
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvSpecialization = findViewById(R.id.tvSpecialization);
        tvFee = findViewById(R.id.tvFee);
        tvTiming = findViewById(R.id.tvTiming);
        datePicker = findViewById(R.id.datePicker);
        gridSlots = findViewById(R.id.gridSlots);
        etReason = findViewById(R.id.etReason);
        etPatientName = findViewById(R.id.etPatientName);
        etContact = findViewById(R.id.etContact);
        rgConsultationType = findViewById(R.id.rgConsultationType);
        cbConsent = findViewById(R.id.cbConsent);
        btnConfirm = findViewById(R.id.btnConfirm);

        // ❌ Past dates disable
        datePicker.setMinDate(System.currentTimeMillis());

        // Intent se data receive karo
        Intent intent = getIntent();
        String name = intent.getStringExtra("doctorName");
        String specialization = intent.getStringExtra("specialization");
        String fee = intent.getStringExtra("fee");
        String timing = intent.getStringExtra("timing");
        int consultationTime = intent.getIntExtra("consultationTime", 15);

        // Doctor info set kar do
        tvDoctorName.setText("Dr. " + name);
        tvSpecialization.setText("Specialization: " + specialization);
        tvFee.setText("Fee: Rs. " + fee);
        tvTiming.setText("Available: " + timing);

        // Slots generate karo
        generatedSlots = generateSlots(timing, consultationTime);

        // ✅ Custom adapter (highlight support)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.time_slot, R.id.tvSlot, generatedSlots);
        gridSlots.setAdapter(adapter);

        // Slot select listener with highlight
        gridSlots.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedSlot = generatedSlots[i];

            // ❌ Reset all slots
            for (int j = 0; j < adapterView.getChildCount(); j++) {
                adapterView.getChildAt(j).setActivated(false);
            }

            // ✅ Highlight selected slot
            view.setActivated(true);

            Toast.makeText(this, "Selected: " + selectedSlot, Toast.LENGTH_SHORT).show();
        });

        // Confirm button
        btnConfirm.setOnClickListener(v -> {
            if (!cbConsent.isChecked()) {
                Toast.makeText(this, "Please agree to terms", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedSlot == null) {
                Toast.makeText(this, "Please select a slot", Toast.LENGTH_SHORT).show();
                return;
            }

            String patientName = etPatientName.getText().toString().trim();
            String contact = etContact.getText().toString().trim();

            if (patientName.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Enter patient name & contact", Toast.LENGTH_SHORT).show();
                return;
            }

            // Final success message
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(selectedDate.getTime());

            Toast.makeText(this, "Appointment booked for " + patientName +
                    " on " + formattedDate + " at " + selectedSlot, Toast.LENGTH_LONG).show();

            finish();
        });
    }

    // Slots generator
    private String[] generateSlots(String timing, int consultationMinutes) {
        try {
            String range = timing;

            if (!timing.contains("-")) {
                String t = timing.toLowerCase();
                if (t.contains("morning")) range = "09:00 AM - 12:00 PM";
                else if (t.contains("afternoon")) range = "12:00 PM - 04:00 PM";
                else if (t.contains("evening")) range = "04:00 PM - 08:00 PM";
                else return new String[0];
            }

            String[] parts = range.split("-");
            if (parts.length < 2) return new String[0];

            String startStr = parts[0].trim();
            String endStr = parts[1].trim();

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            Date startDate = sdf.parse(startStr);
            Date endDate = sdf.parse(endStr);

            long consultMs = consultationMinutes * 60L * 1000L;
            List<String> slots = new ArrayList<>();

            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);

            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(endDate);

            while (cal.getTimeInMillis() + consultMs <= calEnd.getTimeInMillis()) {
                slots.add(sdf.format(cal.getTime()));
                cal.add(Calendar.MINUTE, consultationMinutes);
            }

            return slots.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return new String[0];
        }
    }
}
