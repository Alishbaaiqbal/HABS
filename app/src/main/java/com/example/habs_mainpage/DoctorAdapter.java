package com.example.habs_mainpage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        holder.availability.setText("Availability: " + doctor.availability);
        holder.fees.setText("Fee: " + doctor.fees + " PKR");
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView name, specialization, timing, availability, fees;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_doctor_name);
            specialization = itemView.findViewById(R.id.tv_specialization);
            timing = itemView.findViewById(R.id.tv_timing);
            availability = itemView.findViewById(R.id.tv_availability);
            fees = itemView.findViewById(R.id.tv_fee);
        }
    }
}
