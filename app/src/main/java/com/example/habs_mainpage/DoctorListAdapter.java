package com.example.habs_mainpage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DoctorListAdapter
        extends RecyclerView.Adapter<DoctorListAdapter.Holder> {

    public interface OnDoctorClick {
        void onClick(DoctorItem doctor);
    }

    private final List<DoctorItem> list;
    private final OnDoctorClick listener;

    public DoctorListAdapter(List<DoctorItem> list, OnDoctorClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        // ✅ USE CUSTOM DOCTOR CARD
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        DoctorItem d = list.get(position);

        h.name.setText(d.doctorName);
        h.code.setText("Code: " + d.doctorCode);

        h.itemView.setOnClickListener(v -> listener.onClick(d));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView name, code;

        Holder(View v) {
            super(v);
            name = v.findViewById(R.id.tvDoctorName);
            code = v.findViewById(R.id.tvDoctorCode);
        }
    }
}
