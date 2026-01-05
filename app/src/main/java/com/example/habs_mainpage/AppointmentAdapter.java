package com.example.habs_mainpage;

import android.content.Context;        // ✅ REQUIRED
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppointmentAdapter
        extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    private final List<Appointment> list;
    private final Context context;

    public AppointmentAdapter(Context context, List<Appointment> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {

        Appointment a = list.get(position);

        h.token.setText(a.token);
        h.patient.setText(a.patientName);
        h.doctor.setText(a.doctorName);
        h.date.setText(a.date);
        h.slot.setText(a.slot);
        h.type.setText(a.type);

        h.btnCreatePrescription.setOnClickListener(v -> {
            Intent i = new Intent(context, CreatePrescriptionActivity.class);
            i.putExtra("appointmentId", a.token);
            i.putExtra("patientName", a.patientName);
            i.putExtra("doctorName", a.doctorName);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView token, patient, doctor, date, slot, type;
        Button btnCreatePrescription;

        ViewHolder(View v) {
            super(v);
            token = v.findViewById(R.id.tvToken);
            patient = v.findViewById(R.id.tvPatient);
            doctor = v.findViewById(R.id.tvDoctor);
            date = v.findViewById(R.id.tvDate);
            slot = v.findViewById(R.id.tvSlot);
            type = v.findViewById(R.id.tvType);
            btnCreatePrescription = v.findViewById(R.id.btnCreatePrescription);
        }
    }
}
