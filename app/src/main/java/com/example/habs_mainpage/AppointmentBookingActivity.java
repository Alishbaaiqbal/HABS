package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AppointmentBookingActivity:
 * - Generates slots based on doctor's timings and avgTime.
 * - Creates appointment ID scoped by hospitalCode + doctorCode with O/I prefix.
 * - Prevents double-booking: a slot (date+time) for a given hospitalCode+doctorCode can be booked only once.
 * - Saves appointment to Firebase and creates a BookedSlots entry for quick existence check.
 * - Navigates to TokenGenerationActivity passing appointmentId and details.
 *
 * Expected intent extras when launching this activity:
 *   REQUIRED (for proper display):
 *     "doctorName", "specialization", "fee", "timing", "avgTime", "hospitalName"
 *
 *   RECOMMENDED (for proper unique slot logic):
 *     "hospitalUniqueId" → unique ID per physical hospital (e.g. placeId or lat_lng)
 *     "doctorCode"       → optional; if missing it will be derived from doctorName
 *
 *   If "hospitalUniqueId" is missing, hospitalName will be used as fallback.
 */
public class AppointmentBookingActivity extends AppCompatActivity {

    private static final String TAG = "AppointmentBooking";

    TextView tvDoctorName, tvSpecialization, tvFee, tvTiming;
    DatePicker datePicker;
    GridView gridSlots;
    EditText etReason, etPatientName, etContact;
    RadioGroup rgConsultationType; // Online / In-person
    CheckBox cbConsent;
    Button btnConfirm;

    String[] generatedSlots = new String[0];
    String selectedSlot = null;

    // Firebase reference (use your DB URL)
    private final String FIREBASE_DB_URL = "https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app";
    DatabaseReference appointmentRef, countersRef, bookedSlotsRefRoot;

    // extras
    String doctorName, specialization, fee, timing, avgTime;
    String hospitalName, hospitalCode, hospitalUniqueId, doctorCode;

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

        // Prevent past dates
        datePicker.setMinDate(System.currentTimeMillis());

        // Firebase references (explicit DB URL to avoid region mismatch)
        appointmentRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("Appointments");
        countersRef = FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("AppointmentCounters");
        bookedSlotsRefRoot = FirebaseDatabase.getInstance(FIREBASE_DB_URL).getReference("BookedSlots");

        // ---------- READ EXTRAS ----------
        doctorName       = getIntent().getStringExtra("doctorName");
        specialization   = getIntent().getStringExtra("specialization");
        fee              = getIntent().getStringExtra("fee");
        timing           = getIntent().getStringExtra("timing");          // e.g. "7:00pm-9:00pm"
        avgTime          = getIntent().getStringExtra("avgTime");         // e.g. "15" or "15 mins"
        hospitalName     = getIntent().getStringExtra("hospitalName");
        hospitalUniqueId = getIntent().getStringExtra("hospitalUniqueId"); // NEW: internal unique ID
        doctorCode       = getIntent().getStringExtra("doctorCode");

        // ---------- FALLBACKS ----------
        if (doctorName == null) doctorName = "Unknown";
        if (specialization == null) specialization = "General";
        if (fee == null) fee = "0";
        if (timing == null) timing = "";
        if (avgTime == null) avgTime = "15";

        // Display name (what user sees)
        if (hospitalName == null || hospitalName.trim().isEmpty()) {
            hospitalName = "General Hospital";
        }

        // Internal UNIQUE ID (what booking logic uses)
        // For dataset hospitals: you should pass hospitalUniqueId = real hospital name or ID.
        // For unknown/general hospitals: you should pass hospitalUniqueId = placeId or lat_lng from Maps.
        if (hospitalUniqueId == null || hospitalUniqueId.trim().isEmpty()) {
            // fallback: treat hospitalName as unique id (will group same-name hospitals together)
            hospitalUniqueId = hospitalName;
        }

        // Now hospitalCode ALWAYS comes from the unique ID, not from display name
        hospitalCode = sanitizeCode(hospitalUniqueId);

        // Doctor code: if not sent, derive from doctorName
        if (doctorCode == null || doctorCode.isEmpty()) {
            doctorCode = sanitizeCode(doctorName);
        }

        Log.d(TAG, "onCreate: hospitalName=" + hospitalName
                + ", hospitalUniqueId=" + hospitalUniqueId
                + ", hospitalCode=" + hospitalCode
                + ", doctorCode=" + doctorCode);

        // ---------- SHOW DOCTOR INFO ----------
        tvDoctorName.setText("Dr. " + doctorName);
        tvSpecialization.setText("Specialization: " + specialization);
        tvFee.setText("Fee: Rs. " + fee);
        tvTiming.setText("Available: " + (timing.isEmpty() ? "Not specified" : timing));

        // ---------- GENERATE SLOTS ----------
        generatedSlots = generateSlotsFromTiming(timing, avgTime);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.time_slot, R.id.tvSlot, generatedSlots);
        gridSlots.setAdapter(adapter);

        if (generatedSlots.length == 0) {
            Toast.makeText(this, "No slots available for this doctor.", Toast.LENGTH_SHORT).show();
        }

        // Slot selection
        gridSlots.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedSlot = generatedSlots[i];
            // highlight selected view
            for (int j = 0; j < adapterView.getChildCount(); j++) {
                adapterView.getChildAt(j).setActivated(false);
            }
            view.setActivated(true);
            Toast.makeText(this, "Selected: " + selectedSlot, Toast.LENGTH_SHORT).show();
        });

        // ---------- CONFIRM BUTTON ----------
        btnConfirm.setOnClickListener(v -> {
            if (!cbConsent.isChecked()) {
                Toast.makeText(this, "Please agree to the terms.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Enter patient name & contact", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedTypeId = rgConsultationType.getCheckedRadioButtonId();
            if (selectedTypeId == -1) {
                Toast.makeText(this, "Select consultation type (Online/In-person).", Toast.LENGTH_SHORT).show();
                return;
            }

            String typePrefix = (selectedTypeId == R.id.rbOnline) ? "O" : "I";
            String appointmentType = (selectedTypeId == R.id.rbOnline) ? "Online" : "In-person";

            // Compose date string
            Calendar selDate = Calendar.getInstance();
            selDate.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            String formattedDate = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(selDate.getTime());

            // Disable button while saving
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Booking...");

            // Check if slot already booked for this doctor in THIS HOSPITAL on THIS DATE
            String slotKey = slotToKey(selectedSlot);
            DatabaseReference checkRef = bookedSlotsRefRoot
                    .child(hospitalCode)
                    .child(doctorCode)
                    .child(formattedDate)
                    .child(slotKey);

            checkRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    // Slot already taken in this hospital
                    Toast.makeText(AppointmentBookingActivity.this,
                            "Slot already booked in this hospital. Please choose another slot.",
                            Toast.LENGTH_LONG).show();
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Confirm Appointment");
                } else {
                    // Not booked -> proceed to reserve and save appointment
                    reserveAndSaveAppointment(typePrefix, appointmentType, patientName, contact, reason,
                            formattedDate, selectedSlot, slotKey);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(AppointmentBookingActivity.this,
                        "Failed to check slot availability: " + e.getMessage(), Toast.LENGTH_LONG).show();
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm Appointment");
            });
        });
    }

    private void reserveAndSaveAppointment(String typePrefix, String appointmentType,
                                           String patientName, String contact, String reason,
                                           String formattedDate, String selectedSlot, String slotKey) {

        // Use a per-hospital-per-doctor counter to create friendly sequential IDs
        DatabaseReference thisCounterRef = countersRef.child(hospitalCode).child(doctorCode);

        thisCounterRef.get().addOnSuccessListener(counterSnap -> {
            long current = 0;
            try {
                if (counterSnap.exists()) {
                    Object val = counterSnap.getValue();
                    if (val instanceof Long) current = (Long) val;
                    else if (val instanceof Integer) current = ((Integer) val);
                    else current = Long.parseLong(String.valueOf(val));
                }
            } catch (Exception e) {
                current = 0;
            }
            long newCounter = current + 1;
            // Update counter
            thisCounterRef.setValue(newCounter);

            String appointmentId = typePrefix + "-" + shortCode(hospitalCode) + "-" + shortCode(doctorCode) + "-" + newCounter;

            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("appointmentId", appointmentId);
            appointmentData.put("doctorName", doctorName);
            appointmentData.put("hospitalName", hospitalName);
            appointmentData.put("specialization", specialization);
            appointmentData.put("fee", fee);
            appointmentData.put("date", formattedDate);
            appointmentData.put("slot", selectedSlot);
            appointmentData.put("patientName", patientName);
            appointmentData.put("contact", contact);
            appointmentData.put("reason", reason);
            appointmentData.put("status", "Pending");
            appointmentData.put("type", appointmentType);
            appointmentData.put("hospitalCode", hospitalCode);
            appointmentData.put("doctorCode", doctorCode);

            // Prepare booked slot ref to mark this slot as occupied
            DatabaseReference slotRef = bookedSlotsRefRoot
                    .child(hospitalCode)
                    .child(doctorCode)
                    .child(formattedDate)
                    .child(slotKey);

            // First write appointment, then mark slot
            appointmentRef.child(appointmentId).setValue(appointmentData)
                    .addOnSuccessListener(aVoid -> {
                        // mark slot
                        slotRef.setValue(appointmentId).addOnSuccessListener(aVoid2 -> {
                            Toast.makeText(AppointmentBookingActivity.this,
                                    "✅ Appointment " + appointmentId + " booked!", Toast.LENGTH_SHORT).show();

                            // Start TokenGenerationActivity with necessary extras
                            Intent intent = new Intent(AppointmentBookingActivity.this, TokenGenerationActivity.class);
                            intent.putExtra("appointmentId", appointmentId);
                            intent.putExtra("doctorName", doctorName);
                            intent.putExtra("hospitalName", hospitalName);
                            intent.putExtra("date", formattedDate);
                            intent.putExtra("slot", selectedSlot);
                            intent.putExtra("type", appointmentType);
                            startActivity(intent);

                            // re-enable and finish
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("Confirm Appointment");
                            finish();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(AppointmentBookingActivity.this,
                                    "Saved appointment but failed to reserve slot: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            btnConfirm.setEnabled(true);
                            btnConfirm.setText("Confirm Appointment");
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AppointmentBookingActivity.this,
                                "Failed to save appointment: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnConfirm.setEnabled(true);
                        btnConfirm.setText("Confirm Appointment");
                    });

        }).addOnFailureListener(e -> {
            Toast.makeText(AppointmentBookingActivity.this,
                    "Failed to get counter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnConfirm.setEnabled(true);
            btnConfirm.setText("Confirm Appointment");
        });
    }

    /**
     * Generate slots from doctor timing string and avgTime.
     * timing examples: "7:00pm-9:00pm", "09:00 AM - 12:00 PM", "9:00am-11:30am"
     */
    private String[] generateSlotsFromTiming(String timing, String avgTimeStr) {
        try {
            if (timing == null || timing.trim().isEmpty()) return new String[0];

            // Normalize: remove spaces then ensure format like "7:00PM-9:00PM"
            String t = timing.trim().replaceAll("\\s+", "");
            // Insert space before am/pm
            t = t.replaceAll("(?i)(am|pm)", " $1");
            // Ensure we have hyphen separating start-end (it should)
            String[] parts = t.split("-");
            if (parts.length < 2) return new String[0];

            String startRaw = parts[0].trim();
            String endRaw = parts[1].trim();

            // Parse consultation minutes from avgTimeStr (e.g. "15", "15 mins")
            int consultMins = 15;
            try {
                consultMins = Integer.parseInt(avgTimeStr.replaceAll("[^0-9]", ""));
                if (consultMins <= 0) consultMins = 15;
            } catch (Exception ignored) {
            }

            SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.US);

            startRaw = startRaw.toUpperCase();
            endRaw = endRaw.toUpperCase();

            // Add :00 if missing
            if (!startRaw.contains(":"))
                startRaw = startRaw.replaceAll("^(\\d+)(\\s*AM|\\s*PM)$", "$1:00$2");
            if (!endRaw.contains(":"))
                endRaw = endRaw.replaceAll("^(\\d+)(\\s*AM|\\s*PM)$", "$1:00$2");

            Date start = fmt.parse(startRaw);
            Date end = fmt.parse(endRaw);

            if (start == null || end == null) return new String[0];

            Calendar c = Calendar.getInstance();
            c.setTime(start);
            Calendar cEnd = Calendar.getInstance();
            cEnd.setTime(end);

            List<String> slots = new ArrayList<>();
            while (c.getTimeInMillis() + consultMins * 60L * 1000L <= cEnd.getTimeInMillis()) {
                slots.add(fmt.format(c.getTime()));
                c.add(Calendar.MINUTE, consultMins);
            }

            Log.d(TAG, "Generated slots for timing=" + timing + " slots=" + slots);
            return slots.toArray(new String[0]);

        } catch (ParseException pe) {
            Log.e(TAG, "SlotGen Parse Error: " + pe.getMessage());
            return new String[0];
        } catch (Exception e) {
            Log.e(TAG, "SlotGen Error: " + e.getMessage());
            return new String[0];
        }
    }

    // Convert a slot string into a safe key for Firebase child (replace non-alphanum)
    private String slotToKey(String slot) {
        if (slot == null) return "slot_unknown";
        return slot.replaceAll("[^A-Za-z0-9]", "_");
    }

    // Short helper to create a simple code (remove spaces and non-alphanum)
    private String sanitizeCode(String s) {
        if (s == null) return "X";
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    // produce short uppercase code for nicer ID
    private String shortCode(String s) {
        String cleaned = sanitizeCode(s);
        if (cleaned.length() <= 4) return cleaned.toUpperCase();
        return cleaned.substring(0, Math.min(4, cleaned.length())).toUpperCase();
    }
}
