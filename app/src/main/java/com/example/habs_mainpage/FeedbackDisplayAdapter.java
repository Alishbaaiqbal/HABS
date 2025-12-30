package com.example.habs_mainpage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FeedbackDisplayAdapter
        extends RecyclerView.Adapter<FeedbackDisplayAdapter.Holder> {

    List<FeedbackItem> list;

    public FeedbackDisplayAdapter(List<FeedbackItem> list) {
        this.list = list;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder h, int position) {
        FeedbackItem f = list.get(position);

        h.patient.setText(f.patientName);
        h.rating.setText("Rating: " + f.rating);
        h.comment.setText(f.comment);
        h.date.setText(f.dateTime);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView patient, rating, comment, date;

        Holder(View v) {
            super(v);
            patient = v.findViewById(R.id.tvPatientName);
            rating = v.findViewById(R.id.tvRating);
            comment = v.findViewById(R.id.tvComment);
            date = v.findViewById(R.id.tvDate);
        }
    }
}
