package com.example.habs_mainpage;

public class Hospital {
    public String name;
    public String distance;
    public String status;
    public String placeId;        // Google API ID
    public String firebaseId;     // Firebase Hospital ID (H001, H002, etc.)

    public Hospital() { }

    public Hospital(String name, String distance, String status, String placeId) {
        this.name = name;
        this.distance = distance;
        this.status = status;
        this.placeId = placeId;
        this.firebaseId = null;
    }

    public Hospital(String name, String distance, String status, String placeId, String firebaseId) {
        this.name = name;
        this.distance = distance;
        this.status = status;
        this.placeId = placeId;
        this.firebaseId = firebaseId;
    }
}