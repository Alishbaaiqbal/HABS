package com.example.habs_mainpage;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DocterCurrentTokenActivity extends AppCompatActivity {

    TextView tvYourToken, tvCurrentToken, tvWaitingTime;

    DatabaseReference appointmentRef;

    String appointmentId;
    String doctorCode = "", hospitalCode = "";
    String todayDate;

    int consultationMinutes = 10;

    Handler handler = new Handler();

    SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.US);
    SimpleDateFormat fullFormat =
            new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docter_current_token);

        // 🔹 UI ORDER FIXED
        tvYourToken = findViewById(R.id.tvYourToken);
        tvCurrentToken = findViewById(R.id.tvCurrentToken);
        tvWaitingTime = findViewById(R.id.tvWaitingTime);

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        todayDate = dateFormat.format(new Date());

        appointmentId = getIntent().getStringExtra("appointmentId");

        if (appointmentId == null || appointmentId.isEmpty()) {
            Toast.makeText(this, "Appointment not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadAppointmentSafely();
    }

    // 🔹 Find appointment safely
    private void loadAppointmentSafely() {

        appointmentRef.get().addOnSuccessListener(snapshot -> {

            for (DataSnapshot appt : snapshot.getChildren()) {

                String apptId =
                        appt.child("appointmentId").getValue(String.class);

                if (appointmentId.equals(apptId)) {

                    doctorCode = appt.child("doctorCode").getValue(String.class);
                    hospitalCode = appt.child("hospitalCode").getValue(String.class);

                    if (doctorCode == null || hospitalCode == null) {
                        Toast.makeText(this,
                                "Invalid appointment data",
                                Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    // 🔹 UI FIRST = YOUR TOKEN
                    tvYourToken.setText("Your Token:\n" + appointmentId);

                    loadDoctorTimingFromJson();
                    startLiveQueue();
                    return;
                }
            }

            Toast.makeText(this,
                    "Appointment not found",
                    Toast.LENGTH_LONG).show();
            finish();
        });
    }

    // 🔹 refresh every 15 sec
    private void startLiveQueue() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                calculateLiveQueue();
                handler.postDelayed(this, 15000);
            }
        }, 0);
    }

    // 🔹 FINAL LIVE QUEUE LOGIC (FIXED)
    private void calculateLiveQueue() {

        appointmentRef.get().addOnSuccessListener(snapshot -> {

            List<DataSnapshot> queue = new ArrayList<>();

            for (DataSnapshot appt : snapshot.getChildren()) {

                if (!doctorCode.equals(appt.child("doctorCode").getValue(String.class)))
                    continue;
                if (!hospitalCode.equals(appt.child("hospitalCode").getValue(String.class)))
                    continue;
                if (!todayDate.equals(appt.child("date").getValue(String.class)))
                    continue;

                queue.add(appt);
            }

            if (queue.isEmpty()) return;

            // 🔹 STRONG SLOT-TIME SORT (BUG FIX)
            Collections.sort(queue, (a, b) -> {
                long t1 = safeMillis(a);
                long t2 = safeMillis(b);
                return Long.compare(t1, t2);
            });

            // 🔹 CURRENT INDEX from TIME
            long now = System.currentTimeMillis();
            int currentIndex = 0;

            for (int i = 0; i < queue.size(); i++) {
                if (now >= safeMillis(queue.get(i))) {
                    currentIndex = i;
                }
            }

            String currentToken =
                    queue.get(currentIndex).child("appointmentId").getValue(String.class);

            int yourIndex = -1;
            for (int i = 0; i < queue.size(); i++) {
                if (appointmentId.equals(
                        queue.get(i).child("appointmentId").getValue(String.class))) {
                    yourIndex = i;
                    break;
                }
            }

            tvCurrentToken.setText("Current Token:\n" + currentToken);

            int wait =
                    Math.max(0, (yourIndex - currentIndex) * consultationMinutes);

            tvWaitingTime.setText("Estimated Time: " + wait + " mins");
        });
    }

    // 🔹 SLOT TIME NORMALIZATION (MAIN FIX)
    private long safeMillis(DataSnapshot appt) {
        try {
            String date = appt.child("date").getValue(String.class);
            String slot = appt.child("slot").getValue(String.class);

            if (date == null || slot == null) return 0;

            slot = slot.toUpperCase()
                    .replace("PM", " PM")
                    .replace("AM", " AM")
                    .replaceAll("\\s+", " ")
                    .trim();

            Date d = fullFormat.parse(date + " " + slot);
            return d.getTime();

        } catch (Exception e) {
            return 0;
        }
    }

    // 🔹 Doctor timing from JSON (UNCHANGED)
    private void loadDoctorTimingFromJson() {
        try {
            InputStream is = getAssets().open("DoctorDataset.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONObject root = new JSONObject(new String(buffer));
            JSONArray hospitals = root.names();

            for (int i = 0; i < hospitals.length(); i++) {
                JSONArray doctors = root.getJSONArray(hospitals.getString(i));

                for (int j = 0; j < doctors.length(); j++) {
                    JSONObject d = doctors.getJSONObject(j);

                    if (buildCode(d.getString("Doctor Name"))
                            .equals(doctorCode)) {
                        consultationMinutes =
                                d.getInt("Avg Time to Patients(mins)");
                        return;
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private String buildCode(String name) {
        String[] p = name.replace("Dr.", "").trim().split(" ");
        String c = "DR";
        for (String s : p) c += s.charAt(0);
        return c.length() > 4 ? c.substring(0, 4) : c;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
