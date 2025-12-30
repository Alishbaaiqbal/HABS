package com.example.habs_mainpage;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PatientProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            tvEmail.setText(user.getEmail());
            tvName.setText(user.getDisplayName() != null
                    ? user.getDisplayName()
                    : "Patient");
        }
    }
}
