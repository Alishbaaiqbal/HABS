package com.example.habs_mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button btnGotoSignup, btnGotoLogin,btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGotoSignup = findViewById(R.id.btnSignup);
        btnGotoLogin = findViewById(R.id.btnLogin);
        //btnLogout = findViewById(R.id.btnLogout);

        btnGotoSignup.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        btnGotoLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
       // btnLogout.setOnClickListener(v -> {
         //   FirebaseAuth.getInstance().signOut();
           // Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
           // startActivity(new Intent(MainActivity.this, LoginActivity.class));
           // finish();
       // });
    }
}
