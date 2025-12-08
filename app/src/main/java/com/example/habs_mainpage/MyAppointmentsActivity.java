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

import java.util.ArrayList;

public class MyAppointmentsActivity extends AppCompatActivity {

    ListView listViewAppointments;
    ArrayList<String> displayList = new ArrayList<>();
    ArrayList<String> appointmentIds = new ArrayList<>();

    DatabaseReference appointmentRef;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        listViewAppointments = findViewById(R.id.listAppointments);

        userId = FirebaseAuth.getInstance().getUid();

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        loadUserAppointments();

        listViewAppointments.setOnItemClickListener((adapterView, view, position, id) -> {
            String selectedAppointmentId = appointmentIds.get(position);

            Intent intent = new Intent(MyAppointmentsActivity.this, TokenGenerationActivity.class);
            intent.putExtra("appointmentId", selectedAppointmentId);
            startActivity(intent);
        });
    }

    private void loadUserAppointments() {

        appointmentRef.orderByChild("userId").equalTo(userId).get()
                .addOnSuccessListener(snapshot -> {

                    displayList.clear();
                    appointmentIds.clear();

                    if (!snapshot.exists()) {
                        Toast.makeText(this, "No appointments found.", Toast.LENGTH_SHORT).show();
                    }

                    for (DataSnapshot appt : snapshot.getChildren()) {

                        String doctorName = safeGet(appt, "doctorName");
                        String date = safeGet(appt, "date");
                        String slot = safeGet(appt, "slot");

                        String item =
                                "Doctor: " + doctorName +
                                        "\nDate: " + date +
                                        "\nSlot: " + slot;

                        displayList.add(item);

                        String appointmentId = safeGet(appt, "appointmentId");

                        if (appointmentId.isEmpty())
                            appointmentId = appt.getKey();

                        appointmentIds.add(appointmentId);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_list_item_1,
                            displayList
                    );

                    listViewAppointments.setAdapter(adapter);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private String safeGet(DataSnapshot snap, String key) {
        String value = snap.child(key).getValue(String.class);
        return (value == null) ? "" : value;
    }
}
