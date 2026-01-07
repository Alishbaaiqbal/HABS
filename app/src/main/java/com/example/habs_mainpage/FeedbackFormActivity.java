package com.example.habs_mainpage;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedbackFormActivity extends AppCompatActivity {

    TextView tvDoctorName;
    RatingBar ratingBar;
    EditText etComment;
    Button btnSubmit;

    String doctorCode, doctorName;
    String patientId, patientName;

    DatabaseReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_form);

        tvDoctorName = findViewById(R.id.tvDoctorName);
        ratingBar = findViewById(R.id.ratingBar);
        etComment = findViewById(R.id.etComment);
        btnSubmit = findViewById(R.id.btnSubmitFeedback);

        // ✅ Intent data
        doctorCode = getIntent().getStringExtra("doctorCode");
        doctorName = getIntent().getStringExtra("doctorName");

        if (doctorCode == null || doctorCode.isEmpty()) {
            Toast.makeText(this, "Invalid doctor", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvDoctorName.setText("Feedback for " + doctorName);

        patientId = FirebaseAuth.getInstance().getUid();
        patientName = "Patient"; // optional later enhance

        // ✅ SAFE Firebase path (doctorCode ONLY)
        feedbackRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Feedback")
                .child(doctorCode)   // 🔐 SAFE KEY
                .child(patientId);  // one feedback per patient

        btnSubmit.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {

        int rating = (int) ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please give rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Please write comment", Toast.LENGTH_SHORT).show();
            return;
        }

        FeedbackItem feedback = new FeedbackItem(
                doctorName,          // 👤 UI display
                patientName,
                rating,
                comment,
                new SimpleDateFormat(
                        "dd MMM yyyy, hh:mm a",
                        Locale.US).format(new Date())
        );

        feedbackRef.setValue(feedback)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Feedback submitted successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}
