package com.example.habs_mainpage;

public class DoctorItem {

    public String doctorName;
    public String doctorCode;
    public float avgRating;
    public int feedbackCount;

    public DoctorItem() {}

    public DoctorItem(String name, String code) {
        this.doctorName = name;
        this.doctorCode = code;
        this.avgRating = 0;
        this.feedbackCount = 0;
    }
}
