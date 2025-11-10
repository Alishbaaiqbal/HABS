package com.example.habs_mainpage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    private final Context context;
    private final List<Doctor> doctorList;

    public DoctorAdapter(Context context, List<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
    }

    @Override
    public DoctorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doctorcard, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DoctorViewHolder holder, int position) {
        Doctor doctor = doctorList.get(position);

        holder.name.setText(doctor.getName());
        holder.specialization.setText("Specialization: " + doctor.getSpecialization());
        holder.qualification.setText("Qualification: " + doctor.getQualification());
        holder.experience.setText("Experience: " + doctor.getExperience() + " years");
        holder.reviews.setText("Reviews: " + doctor.getTotalReviews());
        holder.satisfaction.setText("Satisfaction: " + doctor.getSatisfactionRate() + "%");
        holder.avgTime.setText("Avg Time: " + doctor.getAvgTime() + " mins");
        holder.waitTime.setText("Wait Time: " + doctor.getWaitTime() + " mins");
        holder.fee.setText("Fee: Rs. " + doctor.getFee());

        // ✅ Show timing from JSON
        if (doctor.getTiming() != null && !doctor.getTiming().isEmpty()) {
            holder.timing.setText("Timing: " + doctor.getTiming());
        } else {
            holder.timing.setText("Timing: Not available");
        }

        holder.btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(context, AppointmentBookingActivity.class);
            intent.putExtra("doctorName", doctor.getName());
            intent.putExtra("specialization", doctor.getSpecialization());
            intent.putExtra("fee", doctor.getFee());
            intent.putExtra("timing", doctor.getTiming());

            // ✅ Directly use Avg Time instead of consultationTime
            String avgTime = doctor.getAvgTime(); // e.g. "15 mins"
            intent.putExtra("avgTime", avgTime);

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    public static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialization, qualification, experience, reviews, satisfaction,
                avgTime, waitTime, fee, timing;
        Button btnBook;

        public DoctorViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_doctor_name);
            specialization = itemView.findViewById(R.id.tv_specialization);
            qualification = itemView.findViewById(R.id.tv_qualification);
            experience = itemView.findViewById(R.id.tv_experience);
            reviews = itemView.findViewById(R.id.tv_reviews);
            satisfaction = itemView.findViewById(R.id.tv_satisfaction);
            avgTime = itemView.findViewById(R.id.tv_avg_time);
            waitTime = itemView.findViewById(R.id.tv_wait_time);
            fee = itemView.findViewById(R.id.tv_fee);
            timing = itemView.findViewById(R.id.tv_timing); // ✅ added
            btnBook = itemView.findViewById(R.id.btn_book);
        }
    }
}
