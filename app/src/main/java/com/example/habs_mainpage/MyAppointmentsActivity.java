package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MyAppointmentsActivity extends AppCompatActivity {

    // 🔹 Three sections
    ListView listTodayAppointments, listUpcomingAppointments, listPreviousAppointments;

    ArrayList<String> todayDisplayList = new ArrayList<>();
    ArrayList<String> upcomingDisplayList = new ArrayList<>();
    ArrayList<String> previousDisplayList = new ArrayList<>();

    ArrayList<String> todayAppointmentIds = new ArrayList<>();

    DatabaseReference appointmentRef;
    String userId;

    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        listTodayAppointments = findViewById(R.id.listTodayAppointments);
        listUpcomingAppointments = findViewById(R.id.listUpcomingAppointments);
        listPreviousAppointments = findViewById(R.id.listPreviousAppointments);

        userId = FirebaseAuth.getInstance().getUid();

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        loadUserAppointments();

        // 🔥 ONLY today's appointment → Live Token Screen
        listTodayAppointments.setOnItemClickListener((parent, view, position, id) -> {
            String appointmentId = todayAppointmentIds.get(position);

            Intent intent = new Intent(
                    MyAppointmentsActivity.this,
                    DocterCurrentTokenActivity.class
            );
            intent.putExtra("appointmentId", appointmentId);
            startActivity(intent);
        });
    }

    private void loadUserAppointments() {

        appointmentRef.orderByChild("userId").equalTo(userId).get()
                .addOnSuccessListener(snapshot -> {

                    todayDisplayList.clear();
                    upcomingDisplayList.clear();
                    previousDisplayList.clear();
                    todayAppointmentIds.clear();

                    if (!snapshot.exists()) {
                        Toast.makeText(this, "No appointments found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Date todayDate;
                    try {
                        todayDate = sdf.parse(sdf.format(new Date())); // strip time
                    } catch (Exception e) {
                        return;
                    }

                    for (DataSnapshot appt : snapshot.getChildren()) {

                        String doctorName = safeGet(appt, "doctorName");
                        String dateStr = safeGet(appt, "date");
                        String slot = safeGet(appt, "slot");

                        Date apptDate;
                        try {
                            apptDate = sdf.parse(dateStr);
                        } catch (Exception e) {
                            continue;
                        }

                        String item =
                                "Doctor: " + doctorName +
                                        "\nDate: " + dateStr +
                                        "\nSlot: " + slot;

                        String appointmentId = safeGet(appt, "appointmentId");
                        if (appointmentId.isEmpty())
                            appointmentId = appt.getKey();

                        // 🔥 FINAL DATE LOGIC
                        if (apptDate.equals(todayDate)) {
                            // TODAY
                            todayDisplayList.add(item);
                            todayAppointmentIds.add(appointmentId);

                        } else if (apptDate.after(todayDate)) {
                            // FUTURE
                            upcomingDisplayList.add(item);

                        } else {
                            // PAST
                            previousDisplayList.add(item);
                        }
                    }

                    listTodayAppointments.setAdapter(
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_list_item_1,
                                    todayDisplayList
                            )
                    );

                    listUpcomingAppointments.setAdapter(
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_list_item_1,
                                    upcomingDisplayList
                            )
                    );

                    listPreviousAppointments.setAdapter(
                            new ArrayAdapter<>(
                                    this,
                                    android.R.layout.simple_list_item_1,
                                    previousDisplayList
                            )
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private String safeGet(DataSnapshot snap, String key) {
        String value = snap.child(key).getValue(String.class);
        return (value == null) ? "" : value;
    }
}
