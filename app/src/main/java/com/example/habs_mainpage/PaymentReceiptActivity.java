package com.example.habs_mainpage;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentReceiptActivity extends AppCompatActivity {

    private TextView tvReceiptNumber, tvPaymentMethod, tvAmount, tvDateTime, tvStatus;
    private Button btnDone, btnDownload;

    private String appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paymentreceipt);

        // Initialize views
        tvReceiptNumber = findViewById(R.id.tvReceiptNumber);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvAmount = findViewById(R.id.tvAmount);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvStatus = findViewById(R.id.tvStatus);
        btnDone = findViewById(R.id.btnDone);
        btnDownload = findViewById(R.id.btnDownload);

        // Get data from Intent extras safely
        appointmentId = getIntent().getStringExtra("appointmentId");
        String paymentMethod = getIntent().getStringExtra("paymentMethod");
        double totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);
        String paymentStatus = getIntent().getStringExtra("paymentStatus");

        // Set receipt information with defaults
        tvReceiptNumber.setText(appointmentId != null ? appointmentId : "N/A");
        tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "N/A");
        tvAmount.setText("Rs. " + String.format(Locale.getDefault(), "%.2f", totalAmount));

        // Show current date & time (can be replaced with actual payment timestamp if available)
        String currentDateTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date());
        tvDateTime.setText(currentDateTime);

        // Set payment status text and color with better readability
        if ("paid".equalsIgnoreCase(paymentStatus)) {
            tvStatus.setText("PAID");
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            btnDownload.setEnabled(true);
        } else if ("pending".equalsIgnoreCase(paymentStatus)) {
            tvStatus.setText("PENDING");
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            btnDownload.setEnabled(false);
        } else {
            tvStatus.setText("UNKNOWN");
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            btnDownload.setEnabled(false);
        }

        // Done button finishes this activity and returns to previous screen
        btnDone.setOnClickListener(v -> finish());

        // Download button: placeholder, disables if no paid status
        btnDownload.setOnClickListener(v ->
                Toast.makeText(this, "Receipt download feature coming soon!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed() {
        // Default back behavior
        super.onBackPressed();
        finish();
    }
}
