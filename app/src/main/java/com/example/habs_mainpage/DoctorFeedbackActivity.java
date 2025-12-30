package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DoctorFeedbackActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FeedbackDisplayAdapter adapter;
    private final List<FeedbackItem> feedbackList = new ArrayList<>();

    private TextView tvAvgRating, tvDoctorTitle;
    private Button btnGiveFeedback;

    private DatabaseReference feedbackRef;

    private String doctorCode;
    private String doctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_feedback);

        // 🔹 Get data from intent
        doctorCode = getIntent().getStringExtra("doctorCode");
        doctorName = getIntent().getStringExtra("doctorName");

        if (doctorCode == null || doctorCode.trim().isEmpty()) {
            Toast.makeText(this,
                    "Invalid doctor feedback request",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 🔹 Views
        tvDoctorTitle = findViewById(R.id.tvDoctorTitle);
        tvAvgRating = findViewById(R.id.tvAvgRating);
        recyclerView = findViewById(R.id.recyclerFeedback);
        btnGiveFeedback = findViewById(R.id.btnGiveFeedback);

        // 🔹 Title (NAME, not code)
        if (doctorName != null && !doctorName.isEmpty()) {
            tvDoctorTitle.setText("Feedback for " + doctorName);
        } else {
            tvDoctorTitle.setText("Doctor Feedback");
        }

        // 🔹 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FeedbackDisplayAdapter(feedbackList);
        recyclerView.setAdapter(adapter);

        // 🔹 Firebase reference (SAFE: doctorCode only)
        FirebaseDatabase database =
                FirebaseDatabase.getInstance(
                        "https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app");

        feedbackRef = database
                .getReference("Feedback")
                .child(doctorCode);

        // 🔹 Load feedback
        loadFeedback();

        // 🔹 GIVE FEEDBACK BUTTON
        btnGiveFeedback.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DoctorFeedbackActivity.this,
                    FeedbackFormActivity.class
            );

            intent.putExtra("doctorCode", doctorCode); // 🔑 Firebase
            intent.putExtra("doctorName", doctorName); // 👤 UI

            startActivity(intent);
        });
    }

    private void loadFeedback() {

        feedbackRef.get()
                .addOnSuccessListener(snapshot -> {

                    feedbackList.clear();

                    float totalRating = 0;
                    int count = 0;

                    for (DataSnapshot child : snapshot.getChildren()) {

                        FeedbackItem feedback =
                                child.getValue(FeedbackItem.class);

                        if (feedback == null) continue;

                        feedbackList.add(feedback);
                        totalRating += feedback.rating;
                        count++;
                    }

                    if (count > 0) {
                        float avg = totalRating / count;
                        tvAvgRating.setText(
                                String.format(
                                        Locale.US,
                                        "⭐ %.1f (%d reviews)",
                                        avg,
                                        count
                                )
                        );
                    } else {
                        tvAvgRating.setText("No reviews yet");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load feedback",
                                Toast.LENGTH_SHORT).show()
                );
    }
}
