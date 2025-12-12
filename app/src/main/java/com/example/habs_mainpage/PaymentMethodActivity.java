package com.example.habs_mainpage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PaymentMethodActivity extends AppCompatActivity {

    TextView tvDoctorFee, tvServiceCharge, tvTotalAmount;
    RadioGroup rgPaymentMethod;
    RadioButton rbCash, rbJazzCash, rbEasyPaisa, rbBankTransfer;
    Button btnPayNow;

    DatabaseReference appointmentRef;

    String appointmentId;
    double fee;             // doctor fee
    double serviceCharge;   // service fee
    double totalAmount;     // fee + serviceCharge

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@NotNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paymentmethod);

        // 🔹 Bind views (IDs must match your XML)
        tvDoctorFee = findViewById(R.id.tvDoctorFee);
        tvServiceCharge = findViewById(R.id.tvServiceCharge);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCash = findViewById(R.id.rbCash);
        rbJazzCash = findViewById(R.id.rbJazzCash);
        rbEasyPaisa = findViewById(R.id.rbEasyPaisa);
        rbBankTransfer = findViewById(R.id.rbBankTransfer);

        btnPayNow = findViewById(R.id.btnPayNow);

        // 🔹 Data from previous screen
        appointmentId = getIntent().getStringExtra("appointmentId");
        String feeStr = getIntent().getStringExtra("fee");

        if (appointmentId == null || appointmentId.isEmpty()) {
            Toast.makeText(this, "Invalid appointment. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (feeStr == null || feeStr.isEmpty()) {
            Toast.makeText(this, "Fee not received. Please go back and try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            fee = Double.parseDouble(feeStr);
        } catch (Exception e) {
            fee = 0;
        }

        // 🔹 Calculate charges (service charge can be adjusted)
        serviceCharge = 50;                // e.g. Rs. 50 platform/service fee
        totalAmount = fee + serviceCharge;

        // 🔹 Show breakdown on UI
        tvDoctorFee.setText("Rs. " + String.format(Locale.getDefault(), "%.2f", fee));
        tvServiceCharge.setText("Rs. " + String.format(Locale.getDefault(), "%.2f", serviceCharge));
        tvTotalAmount.setText("Rs. " + String.format(Locale.getDefault(), "%.2f", totalAmount));

        // 🔹 Firebase reference
        appointmentRef = FirebaseDatabase
                .getInstance("https://fyp-maju-default-rtdb.asia-southeast1.firebasedatabase.app")
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

        if (selectedId == R.id.rbCash) {
            // 🟡 Cash at hospital → payment remains pending
            savePaymentStatus(paymentMethod, "pending");
        } else {
            // 🟢 JazzCash / EasyPaisa / Bank Transfer → simulate online success
            String txnId = generateTransactionId();
            btnPayNow.postDelayed(() -> onPaymentSuccess(paymentMethod, txnId), 2000);
        }
    }

    /**
     * 🔑 Har transaction ke liye unique ID generate karta hai.
     * Format: TXN-{appointmentId}-{timestamp}-{5digitRandom}
     */
    private String generateTransactionId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 90000) + 10000; // 5 digit random
        return "TXN-" + appointmentId + "-" + timestamp + "-" + random;
    }

    private void onPaymentSuccess(String paymentMethod, String transactionId) {
        // Online-type payment success
        savePaymentStatus(paymentMethod, "paid", transactionId);
    }

    // Overloaded method for pending (no transaction ID)
    private void savePaymentStatus(String paymentMethod, String status) {
        savePaymentStatus(paymentMethod, status, null);
    }

    private void savePaymentStatus(String paymentMethod, String status, String transactionId) {

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", status);         // "paid" / "pending"
        updates.put("paymentMethod", paymentMethod);  // Cash / JazzCash / EasyPaisa / Bank Transfer
        updates.put("paymentAmount", totalAmount);    // fee + service
        updates.put("paymentTime", timestamp);

        // 🔹 Update overall appointment status
        if ("paid".equals(status)) {
            updates.put("status", "Confirmed");    // payment done → appointment confirmed
        } else {
            updates.put("status", "Pending");      // still pending payment (cash)
        }

        if (transactionId != null) {
            updates.put("transactionId", transactionId);
        }

        appointmentRef.child(appointmentId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {

                    if ("paid".equals(status)) {
                        Toast.makeText(this,
                                "Payment successful. Your appointment is confirmed.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Payment marked as pending. Please pay at the hospital.",
                                Toast.LENGTH_SHORT).show();
                    }

                    // 👉 Go to receipt screen (if you have this Activity)
                    Intent intent = new Intent(PaymentMethodActivity.this, PaymentReceiptActivity.class);
                    intent.putExtra("appointmentId", appointmentId);
                    intent.putExtra("paymentStatus", status);
                    intent.putExtra("paymentMethod", paymentMethod);
                    intent.putExtra("totalAmount", totalAmount);
                    if (transactionId != null) {
                        intent.putExtra("transactionId", transactionId);
                    }
                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Payment update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPayNow.setEnabled(true);
                    btnPayNow.setText("PAY NOW");
                });
    }
}
