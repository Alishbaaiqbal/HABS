package com.example.habs_mainpage;

public class Doctor {
    private String name;
    private String specialization;
    private String qualification;
    private String experience;
    private String totalReviews;
    private String satisfactionRate;
    private String avgTime;
    private String waitTime;
    private String fee;
    private String doctorLink;
    private String timing; // ✅ New field

    public Doctor(String name, String specialization, String qualification,
                  String experience, String totalReviews, String satisfactionRate,
                  String avgTime, String waitTime, String fee, String timing) { // ✅ include timing
        this.name = name;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experience = experience;
        this.totalReviews = totalReviews;
        this.satisfactionRate = satisfactionRate;
        this.avgTime = avgTime;
        this.waitTime = waitTime;
        this.fee = fee;
        this.timing = timing;
    }

    // ✅ Getters
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getQualification() { return qualification; }
    public String getExperience() { return experience; }
    public String getTotalReviews() { return totalReviews; }
    public String getSatisfactionRate() { return satisfactionRate; }
    public String getAvgTime() { return avgTime; }
    public String getWaitTime() { return waitTime; }
    public String getFee() { return fee; }
    public String getDoctorLink() { return doctorLink; }
    public String getTiming() { return timing; } // ✅ added getter

    // ✅ Setters
    public void setDoctorLink(String doctorLink) { this.doctorLink = doctorLink; }
    public void setTiming(String timing) { this.timing = timing; } // ✅ added setter
}
