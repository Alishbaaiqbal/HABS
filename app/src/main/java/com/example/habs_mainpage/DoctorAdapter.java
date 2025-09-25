package com.example.habs_mainpage;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private List<Doctor> doctorList;

    public DoctorAdapter(List<Doctor> doctorList) {
        this.doctorList = doctorList;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.doctorcard, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);

        holder.name.setText(doctor.name);
        holder.specialization.setText("Specialization: " + doctor.specialization);
        holder.timing.setText("Timing: " + doctor.timing);
        holder.fees.setText("Fee: " + doctor.fees + " PKR");
        holder.consultationTime.setText("Consultation: " + doctor.consultationTime + "min");

        // ✅ Show availability with boolean
        if (doctor.isAvailability()) {
            holder.availability.setText("Available");
            holder.availability.setTextColor(Color.parseColor("#2E7D32")); // Green
            holder.btnBook.setEnabled(true);
            holder.btnBook.setAlpha(1f);
        } else {
            holder.availability.setText("Not Available");
            holder.availability.setTextColor(Color.parseColor("#C62828")); // Red
            holder.btnBook.setEnabled(false);
            holder.btnBook.setAlpha(0.5f); // fade disabled button
        }

        // ✅ Book button logic
        holder.btnBook.setOnClickListener(v -> {
            if (doctor.isAvailability()) {
                // Doctor available → open AppointmentBookingActivity
                Intent intent = new Intent(v.getContext(), AppointmentBookingActivity.class);
                intent.putExtra("doctorName", doctor.name);
                intent.putExtra("specialization", doctor.specialization);
                intent.putExtra("fee", doctor.fees);
                intent.putExtra("timing", doctor.timing);
                intent.putExtra("consultationTime", doctor.consultationTime);
                v.getContext().startActivity(intent);
            } else {
                // Doctor not available → show message
                Toast.makeText(v.getContext(), "Doctor not available right now", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialization, timing, availability, fees, consultationTime;
        Button btnBook;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_doctor_name);
            specialization = itemView.findViewById(R.id.tv_specialization);
            timing = itemView.findViewById(R.id.tv_timing);
            availability = itemView.findViewById(R.id.tv_availability);
            fees = itemView.findViewById(R.id.tv_fee);
            consultationTime = itemView.findViewById(R.id.tv_consultation_time);
            btnBook = itemView.findViewById(R.id.btn_book);
        }
    }
}
