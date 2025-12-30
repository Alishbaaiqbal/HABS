package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyReceiptsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    AppointmentAdapter adapter;
    List<Appointment> receiptList = new ArrayList<>();

    DatabaseReference appointmentRef;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_receipts);

        recyclerView = findViewById(R.id.recyclerReceipts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ✅ Adapter with click callback
        adapter = new AppointmentAdapter(receiptList,null);
        recyclerView.setAdapter(adapter);

        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        userId = FirebaseAuth.getInstance().getUid();

        loadReceipts();
    }

    private void loadReceipts() {

        appointmentRef.orderByChild("userId").equalTo(userId)
                .get().addOnSuccessListener(snapshot -> {

                    receiptList.clear();

                    for (DataSnapshot child : snapshot.getChildren()) {

                        String paymentStatus =
                                child.child("paymentStatus").getValue(String.class);

                        if (!"paid".equalsIgnoreCase(paymentStatus))
                            continue;

                        String token =
                                child.child("appointmentId").getValue(String.class);
                        if (token == null) token = child.getKey();

                        String patient =
                                child.child("patientName").getValue(String.class);
                        String doctor =
                                child.child("doctorName").getValue(String.class);
                        String date =
                                child.child("date").getValue(String.class);
                        String slot =
                                child.child("slot").getValue(String.class);
                        String paymentMethod =
                                child.child("paymentMethod").getValue(String.class);

                        receiptList.add(
                                new Appointment(
                                        token,
                                        patient,
                                        doctor,
                                        date,
                                        slot,
                                        paymentMethod
                                )
                        );
                    }

                    // ✅ SORT BY DATE (NEWEST FIRST)
                    Collections.sort(receiptList, (a, b) -> {
                        try {
                            SimpleDateFormat sdf =
                                    new SimpleDateFormat("dd MMM yyyy", Locale.US);
                            Date d1 = sdf.parse(a.date);
                            Date d2 = sdf.parse(b.date);
                            return d2.compareTo(d1);
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    adapter.notifyDataSetChanged();
                });
    }

    // ✅ SAFE CLICK METHOD
    private void openReceipt(int position) {

        Appointment a = receiptList.get(position);

        Intent intent =
                new Intent(this, PaymentReceiptActivity.class);

        intent.putExtra("appointmentId", a.token);
        intent.putExtra("paymentMethod", a.type);
        intent.putExtra("totalAmount", 0.0);
        intent.putExtra("paymentStatus", "paid");

        startActivity(intent);
    }
}
