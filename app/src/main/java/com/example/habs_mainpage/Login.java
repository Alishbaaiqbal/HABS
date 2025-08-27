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

import java.util.Objects;

public class Login extends AppCompatActivity {

    EditText editTextEmail, editTextPASSWORD;
    Button buttonLogin, btnGoToSignUp;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textViewForgotPassword; // Forgot Password TextView

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User already logged in, directly go to homepage
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        editTextEmail = findViewById(R.id.Email);
        editTextPASSWORD = findViewById(R.id.PASSWORD);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progressBar);
        textViewForgotPassword = findViewById(R.id.forgotPassword);

        // Forgot Password click
        textViewForgotPassword.setOnClickListener(v -> {
            String email = Objects.requireNonNull(editTextEmail.getText()).toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Login.this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Reset email sent to your email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Login.this, "Unable to send reset email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Login button click
        buttonLogin.setOnClickListener(v -> {
            String EMAIL = Objects.requireNonNull(editTextEmail.getText()).toString().trim();
            String PASSWORD = Objects.requireNonNull(editTextPASSWORD.getText()).toString().trim();

            if (TextUtils.isEmpty(EMAIL)) {
                Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(EMAIL).matches()) {
                Toast.makeText(Login.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(PASSWORD)) {
                Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(EMAIL, PASSWORD)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Login successful.", Toast.LENGTH_SHORT).show();
                            // Login success, open homepage (MainActivity)
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Signup button click: go to Signup screen
        btnGoToSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
