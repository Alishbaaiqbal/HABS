package com.example.habs_mainpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentMethodActivity extends AppCompatActivity {

    TextView tvFee;
    RadioGroup rgPaymentMethod;
    RadioButton rbCash, rbOnline, rbBankTransfer;
    Button btnPayNow;

    DatabaseReference appointmentRef;

    String appointmentId;
    double fee;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paymentmethod);

        tvFee = findViewById(R.id.tvFee);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCash = findViewById(R.id.rbCash);
        rbOnline = findViewById(R.id.rbOnline);
        rbBankTransfer = findViewById(R.id.rbBankTransfer);
        btnPayNow = findViewById(R.id.btnPayNow);

        appointmentId = getIntent().getStringExtra("appointmentId");
        String feeStr = getIntent().getStringExtra("fee");

        if (appointmentId == null || appointmentId.isEmpty()) {
            Toast.makeText(this, "Invalid Appointment ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            fee = Double.parseDouble(feeStr);
        } catch (Exception e) {
            fee = 0;
        }

        tvFee.setText("Fee: Rs. " + String.format("%.2f", fee));

        appointmentRef = FirebaseDatabase.getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Appointments");

        btnPayNow.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedId);
        String paymentMethod = selectedRadio.getText().toString();

        btnPayNow.setEnabled(false);
        btnPayNow.setText("Processing...");

        if (selectedId == R.id.rbOnline) {
            // Simulate online payment success after 2 seconds
            btnPayNow.postDelayed(() -> onPaymentSuccess("TXN123456789"), 2000);
        } else {
            // For Cash or Bank Transfer, payment status remains pending
            savePaymentStatus(paymentMethod, "pending");
        }
    }

    private void onPaymentSuccess(String transactionId) {
        savePaymentStatus("Online Payment", "paid", transactionId);
    }

    // Overloaded method for pending payment (no transaction ID)
    private void savePaymentStatus(String paymentMethod, String status) {
        savePaymentStatus(paymentMethod, status, null);
    }

    private void savePaymentStatus(String paymentMethod, String status, String transactionId) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", status);
        updates.put("paymentMethod", paymentMethod);
        updates.put("paymentAmount", fee);
        updates.put("paymentTime", timestamp);
        if (transactionId != null) {
            updates.put("transactionId", transactionId);
        }

        appointmentRef.child(appointmentId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    if (status.equals("paid")) {
                        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Payment Pending - Please pay at hospital", Toast.LENGTH_SHORT).show();
                    }

                    // Open PaymentReceiptActivity with updated info
                    Intent intent = new Intent(PaymentMethodActivity.this, PaymentReceiptActivity.class);
                    intent.putExtra("appointmentId", appointmentId);
                    intent.putExtra("paymentStatus", status);
                    intent.putExtra("paymentMethod", paymentMethod);
                    intent.putExtra("totalAmount", fee);
                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Payment update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPayNow.setEnabled(true);
                    btnPayNow.setText("Pay Now");
                });
    }
}
