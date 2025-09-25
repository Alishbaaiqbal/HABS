package com.example.habs_mainpage;

public class Doctor {
    String name, specialization, timing, fees;
    int consultationTime;
    private boolean availability;
    public Doctor(String name, String specialization, String timing, boolean availability, String fees, String consultationTime) {
        this.name = name;
        this.specialization = specialization;
        this.timing = timing;
        this.availability = availability;
        this.fees = fees;
        this.consultationTime = Integer.parseInt(consultationTime);
    }
    public boolean isAvailability() { return availability; }
}
