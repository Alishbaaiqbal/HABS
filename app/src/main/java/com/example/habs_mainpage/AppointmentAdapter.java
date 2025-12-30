package com.example.habs_mainpage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppointmentAdapter
        extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public interface OnItemClick {
        void onClick(int position);
    }

    private final List<Appointment> list;
    private OnItemClick listener = null;

    // 🔹 UPDATED CONSTRUCTOR
    public AppointmentAdapter(List<Appointment> list, Object o) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        Appointment a = list.get(position);

        h.token.setText(a.token);
        h.patient.setText(a.patientName);
        h.doctor.setText(a.doctorName);
        h.date.setText(a.date);
        h.slot.setText(a.slot);
        h.type.setText(a.type);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView token, patient, doctor, date, slot, type;

        ViewHolder(View v) {
            super(v);
            token = v.findViewById(R.id.tvToken);
            patient = v.findViewById(R.id.tvPatient);
            doctor = v.findViewById(R.id.tvDoctor);
            date = v.findViewById(R.id.tvDate);
            slot = v.findViewById(R.id.tvSlot);
            type = v.findViewById(R.id.tvType);
        }
    }
}
