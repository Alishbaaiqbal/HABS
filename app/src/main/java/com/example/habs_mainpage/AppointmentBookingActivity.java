package com.example.habs_mainpage;

import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.content.Intent;

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

    DatabaseReference appointmentRef, counterRef;

    // hospital / doctor info
    String hospitalName;
    String hospitalCode;   // from hospitalUniqueId
    String doctorCode;     // generated from doctorName

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointmentbooking);

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

        // disable past dates
        datePicker.setMinDate(System.currentTimeMillis());

        // Firebase references
        appointmentRef = FirebaseDatabase.getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");
        counterRef = FirebaseDatabase.getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("AppointmentCounter");

        // Doctor details from DoctorAdapter
        String name = getIntent().getStringExtra("doctorName");
        String specialization = getIntent().getStringExtra("specialization");
        String fee = getIntent().getStringExtra("fee");
        String timing = getIntent().getStringExtra("timing");
        String avgTime = getIntent().getStringExtra("avgTime");

        // Hospital info from DoctorAdapter
        hospitalName = getIntent().getStringExtra("hospitalName");
        hospitalCode = getIntent().getStringExtra("hospitalUniqueId"); // we treat this as code

        if (hospitalCode == null || hospitalCode.isEmpty()) {
            hospitalCode = "GENH";   // default if missing
        }

        // doctorCode generated from doctorName
        doctorCode = buildDoctorCode(name);  // e.g. "Dr. Sara Ahmed" -> "DRSA"
        if (doctorCode == null || doctorCode.isEmpty()) {
            doctorCode = "DRXX";
        }

        tvDoctorName.setText("Dr. " + name);
        tvSpecialization.setText("Specialization: " + specialization);
        tvFee.setText("Fee: Rs. " + fee);
        tvTiming.setText("Available: " + timing);

        generatedSlots = generateSlots(timing, avgTime);
        if (generatedSlots.length == 0) {
            Toast.makeText(this, "No time slots available for this doctor.", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.time_slot, R.id.tvSlot, generatedSlots);
        gridSlots.setAdapter(adapter);

        gridSlots.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedSlot = generatedSlots[i];
            for (int j = 0; j < adapterView.getChildCount(); j++) {
                adapterView.getChildAt(j).setActivated(false);
            }
            view.setActivated(true);
            Toast.makeText(this, "Selected slot: " + selectedSlot, Toast.LENGTH_SHORT).show();
        });

        btnConfirm.setOnClickListener(v -> {
            if (!cbConsent.isChecked()) {
                Toast.makeText(this, "Please agree to the terms and conditions.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedSlot == null) {
                Toast.makeText(this, "Please select a time slot.", Toast.LENGTH_SHORT).show();
                return;
            }

            String patientName = etPatientName.getText().toString().trim();
            String contact = etContact.getText().toString().trim();
            String reason = etReason.getText().toString().trim();

            if (patientName.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Please enter patient name and contact number.", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedTypeId = rgConsultationType.getCheckedRadioButtonId();
            if (selectedTypeId == -1) {
                Toast.makeText(this, "Please select consultation type.", Toast.LENGTH_SHORT).show();
                return;
            }

            String typePrefix;
            String appointmentType;

            // I = in-person, O = online
            if (selectedTypeId == R.id.rbOnline) {
                typePrefix = "O";
                appointmentType = "Online";
            } else {
                typePrefix = "I";
                appointmentType = "In-person";
            }

            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(selectedDate.getTime());

            saveAppointmentToFirebase(
                    patientName,
                    contact,
                    reason,
                    name,
                    specialization,
                    fee,
                    formattedDate,
                    selectedSlot,
                    typePrefix,
                    appointmentType,
                    timing
            );
        });
    }

    // doctor name -> code like DRSA, DRKA etc.
    private String buildDoctorCode(String doctorName) {
        if (doctorName == null || doctorName.trim().isEmpty()) return "";

        String clean = doctorName.replace("Dr.", "")
                .replace("Dr", "")
                .trim()
                .toUpperCase(Locale.US);

        String[] parts = clean.split("\\s+");
        StringBuilder sb = new StringBuilder("DR");
        for (String p : parts) {
            if (!p.isEmpty()) {
                sb.append(p.charAt(0));
            }
        }

        String code = sb.toString();
        if (code.length() > 4) {
            code = code.substring(0, 4);
        }
        return code;
    }

    private String[] generateSlots(String timing, String avgTimeStr) {
        try {
            if (timing == null || !timing.contains("-")) {
                Log.e("SlotGen", "Invalid timing: " + timing);
                return new String[0];
            }

            timing = timing.replaceAll("(?i)am", " AM")
                    .replaceAll("(?i)pm", " PM")
                    .replaceAll("\\s*", "");
            timing = timing.replace("-", " - ");

            String[] parts = timing.split("-");
            if (parts.length < 2) return new String[0];

            String startStr = parts[0].trim();
            String endStr = parts[1].trim();

            int consultationMinutes = 15;
            try {
                consultationMinutes = Integer.parseInt(avgTimeStr.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);

            startStr = startStr.toUpperCase().replace("AM", " AM").replace("PM", " PM").trim();
            endStr = endStr.toUpperCase().replace("AM", " AM").replace("PM", " PM").trim();

            Date startDate = sdf.parse(startStr);
            Date endDate = sdf.parse(endStr);

            if (startDate == null || endDate == null) return new String[0];

            List<String> slots = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(endDate);

            while (cal.getTimeInMillis() + (consultationMinutes * 60L * 1000L) <= calEnd.getTimeInMillis()) {
                slots.add(sdf.format(cal.getTime()));
                cal.add(Calendar.MINUTE, consultationMinutes);
            }

            Log.d("SlotGen", "Slots: " + slots);
            return slots.toArray(new String[0]);
        } catch (Exception e) {
            Log.e("SlotGen", "Error: " + e.getMessage());
            return new String[0];
        }
    }

    private long combineDateAndSlotToMillis(DatePicker datePicker, String slot) {
        try {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year = datePicker.getYear();

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            Date time = timeFormat.parse(slot);

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);

            if (time != null) {
                Calendar t = Calendar.getInstance();
                t.setTime(time);

                cal.set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, t.get(Calendar.MINUTE));
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }

            return cal.getTimeInMillis();

        } catch (Exception e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    // STRICT slot check: if check fails → no booking
    private void saveAppointmentToFirebase(String patientName, String contact, String reason,
                                           String doctorName, String specialization, String fee,
                                           String date, String slot, String typePrefix,
                                           String appointmentType, String timing) {

        // same hospital + same doctor + same date + same time → must be unique
        String slotKey = hospitalCode + "_" + doctorCode + "_" + date + "_" + slot;

        appointmentRef.orderByChild("slotKey").equalTo(slotKey)
                .get()
                .addOnSuccessListener(snap -> {

                    if (snap.exists()) {
                        // slot already taken
                        Toast.makeText(this,
                                "This time slot is already booked for this doctor at this hospital.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // slot is free → create appointment
                    createAppointmentInFirebase(
                            patientName, contact, reason,
                            doctorName, specialization, fee,
                            date, slot, typePrefix, appointmentType,
                            timing, slotKey
                    );

                })
                .addOnFailureListener(e -> {
                    Log.e("SlotCheck", "Slot check failed: " + e.getMessage(), e);
                    Toast.makeText(this,
                            "Could not verify this time slot. Please try again in a moment.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void createAppointmentInFirebase(String patientName, String contact, String reason,
                                             String doctorName, String specialization, String fee,
                                             String date, String slot, String typePrefix,
                                             String appointmentType, String timing, String slotKey) {

        counterRef.get().addOnSuccessListener(snapshot -> {
            long currentCount = snapshot.exists() ? snapshot.getValue(Long.class) : 0;
            long newCount = currentCount + 1;
            counterRef.setValue(newCount);

            // I-HOSP-DRXX-1 / O-HOSP-DRXX-2 etc.
            String customId = typePrefix + "-" + hospitalCode + "-" + doctorCode + "-" + newCount;

            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("appointmentId", customId);
            appointmentData.put("hospitalName", hospitalName);
            appointmentData.put("hospitalCode", hospitalCode);
            appointmentData.put("doctorCode", doctorCode);
            appointmentData.put("doctorName", doctorName);
            appointmentData.put("specialization", specialization);
            appointmentData.put("fee", fee);
            appointmentData.put("date", date);
            appointmentData.put("slot", slot);
            appointmentData.put("slotKey", slotKey);
            appointmentData.put("patientName", patientName);
            appointmentData.put("contact", contact);
            appointmentData.put("reason", reason);
            appointmentData.put("status", "Pending");
            appointmentData.put("type", appointmentType); // Online / In-person

            appointmentRef.child(customId).setValue(appointmentData)
                    .addOnSuccessListener(aVoid -> {

                        Toast.makeText(this,
                                "Appointment " + customId + " booked successfully.",
                                Toast.LENGTH_SHORT).show();

                        try {
                            Intent intent = new Intent(AppointmentBookingActivity.this,
                                    TokenGenerationActivity.class);
                            intent.putExtra("appointmentId", customId);
                            startActivity(intent);
                        } catch (Exception e) {
                            Log.e("AppointmentBooking", "Failed to start TokenGenerationActivity", e);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Error while saving appointment: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> {
            Log.e("AppointmentBooking", "Counter read failed: " + e.getMessage(), e);
            Toast.makeText(this,
                    "Error while generating appointment number. Please try again.",
                    Toast.LENGTH_SHORT).show();
        });
    }
}
