package com.example.habs_mainpage;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private RadioButton radioPatient, radioHospital;
    private Button btnSignup;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    boolean passwordVisible = false;
    boolean confirmPasswordVisible = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // Firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views
        editName = findViewById(R.id.Name);
        editEmail = findViewById(R.id.email);
        editPassword = findViewById(R.id.password);
        editConfirmPassword = findViewById(R.id.confirmPassword);
        radioPatient = findViewById(R.id.radioPatient);
        radioHospital = findViewById(R.id.radioHospital);
        btnSignup = findViewById(R.id.btnSignup);

        // 🔥 Add show/hide password functionality
        setupToggleEye(editPassword, true);
        setupToggleEye(editConfirmPassword, false);

        btnSignup.setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!radioPatient.isChecked() && !radioHospital.isChecked()) {
                Toast.makeText(SignUpActivity.this, "Please select Patient or Hospital", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = radioPatient.isChecked() ? "Patient" : "Hospital";
            // 🔐 Hospital email validation
            if (role.equals("Hospital")) {
                if (!email.toLowerCase().endsWith("@hospital.com")) {
                    Toast.makeText(
                            SignUpActivity.this,
                            "Hospital signup requires an @hospital.com email",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
            }


            // Create Firebase user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignUpActivity.this, task -> {
                        if (task.isSuccessful()) {

                            String userId = mAuth.getCurrentUser().getUid();

                            // Save user data in Firestore
                            Map<String, Object> user = new HashMap<>();
                            user.put("name", name);
                            user.put("email", email);
                            user.put("role", role);

                            String collection = role.equals("Patient") ? "Patients" : "Hospitals";

                            db.collection(collection).document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignUpActivity.this, "Registered as " + role, Toast.LENGTH_SHORT).show();

                                        mAuth.signOut();

                                        // Redirect to Login
                                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(SignUpActivity.this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );

                        } else {
                            Toast.makeText(SignUpActivity.this, "Auth error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // 🔥 Function for Show/Hide Password Eye Icon
    @SuppressLint("ClickableViewAccessibility")
    private void setupToggleEye(EditText editText, boolean isPasswordField) {

        editText.setOnTouchListener((v, event) -> {

            final int DRAWABLE_END = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {

                    if (isPasswordField) passwordVisible = !passwordVisible;
                    else confirmPasswordVisible = !confirmPasswordVisible;

                    boolean visible = isPasswordField ? passwordVisible : confirmPasswordVisible;

                    if (visible) {
                        editText.setInputType(
                                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                editText.getCompoundDrawables()[0], null,
                                getDrawable(R.drawable.ic_eye_on), null
                        );
                    } else {
                        editText.setInputType(
                                android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );
                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                editText.getCompoundDrawables()[0], null,
                                getDrawable(R.drawable.ic_eye_off), null
                        );
                    }

                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
}