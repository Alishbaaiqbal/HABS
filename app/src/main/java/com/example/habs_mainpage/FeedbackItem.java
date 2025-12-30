package com.example.habs_mainpage;

public class FeedbackItem {

    public String doctorName;
    public String patientName;
    public int rating;
    public String comment;
    public String dateTime;

    public FeedbackItem() {}

    public FeedbackItem(String doctorName, String patientName,
                        int rating, String comment, String dateTime) {

        this.patientName = patientName;
        this.rating = rating;
        this.comment = comment;
        this.dateTime = dateTime;
    }
}
