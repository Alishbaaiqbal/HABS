package com.example.habs_mainpage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder> {

    private final List<Hospital> hospitalList;
    private final Context context;

    public HospitalAdapter(Context context, List<Hospital> hospitalList) {
        this.context = context;
        this.hospitalList = hospitalList;
    }

    @Override
    public HospitalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.homcard, parent, false);
        return new HospitalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HospitalViewHolder holder, int position) {
        Hospital hospital = hospitalList.get(position);

        holder.name.setText(hospital.name);
        holder.distance.setText(hospital.distance);

        // ✅ Set hospital status color
        if (hospital.status == null || hospital.status.equalsIgnoreCase("Status unknown")) {
            holder.status.setText("Hours not available");
            holder.status.setTextColor(Color.parseColor("#808080"));
        } else if (hospital.status.equalsIgnoreCase("Open")) {
            holder.status.setText("Open Now");
            holder.status.setTextColor(Color.parseColor("#2E7D32"));
        } else if (hospital.status.equalsIgnoreCase("Closed")) {
            holder.status.setText("Closed");
            holder.status.setTextColor(Color.parseColor("#C62828"));
        } else {
            holder.status.setText(hospital.status);
            holder.status.setTextColor(Color.parseColor("#000000"));
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DoctorDetailsActivity.class);

            // 👀 1) Display name for UI (jo tum already sahi bhej rahi ho)
            intent.putExtra("hospital_name", hospital.name);

            // 👀 2) INTERNAL UNIQUE ID for logic
            //    - If firebaseId available → best option
            //    - Warna hospital name se fallback
            String uniqueId;
            if (hospital.firebaseId != null && !hospital.firebaseId.isEmpty()) {
                uniqueId = hospital.firebaseId;
            } else {
                uniqueId = hospital.name;   // fallback (only safe if names are unique)
            }

            // yahi woh key hai jo DoctorDetailsActivity + AppointmentBookingActivity use kareinge
            intent.putExtra("hospital_unique_id", uniqueId);

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return hospitalList.size();
    }

    static class HospitalViewHolder extends RecyclerView.ViewHolder {
        TextView name, distance, status;

        public HospitalViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_hospital_name);
            distance = itemView.findViewById(R.id.tv_distance);
            status = itemView.findViewById(R.id.tv_status);
        }
    }
}
