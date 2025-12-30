package com.example.habs_mainpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPASSWORD;
    Button buttonLogin;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    ProgressBar progressBar;
    TextView textView; // Forgot Password

    @Override
    public void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            routeUser(currentUser);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.Email);
        editTextPASSWORD = findViewById(R.id.PASSWORD);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.forgotPassword);

        // 🔹 Forgot Password
        textView.setOnClickListener(v -> {
            String email = Objects.requireNonNull(editTextEmail.getText())
                    .toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset email sent.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Unable to send reset email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 🔹 LOGIN BUTTON
        buttonLogin.setOnClickListener(v -> {

            String EMAIL = Objects.requireNonNull(editTextEmail.getText())
                    .toString().trim();
            String PASSWORD = Objects.requireNonNull(editTextPASSWORD.getText())
                    .toString().trim();

            if (TextUtils.isEmpty(EMAIL)) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(EMAIL).matches()) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(PASSWORD)) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(EMAIL, PASSWORD)
                    .addOnCompleteListener(task -> {

                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            routeUser(mAuth.getCurrentUser());
                        } else {
                            Toast.makeText(this,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // 🔥 FINAL ROUTING LOGIC (MOST IMPORTANT)
    private void routeUser(FirebaseUser user) {

        String email = Objects.requireNonNull(user.getEmail()).toLowerCase();

        if (email.endsWith("@hospital.com")) {
            // 🔹 HOSPITAL LOGIN → fetch REAL hospital name
            firestore.collection("Hospitals")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {

                        if (doc.exists()) {
                            String hospitalName = doc.getString("name");
                            // example: "FM General Hospital"

                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    AppointmentTrackingActivity.class
                            );
                            intent.putExtra("hospitalCode", hospitalName);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this,
                                    "Hospital record not found",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        } else {
            // 🔹 PATIENT LOGIN
            startActivity(new Intent(
                    LoginActivity.this,
                    HomePage.class
            ));
            finish();
        }
    }
}
