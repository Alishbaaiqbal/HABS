package com.example.habs_mainpage;

public class Appointment {
        public String patientName, doctorName, date, slot, type, token;

        public Appointment() {}

        public Appointment(String token, String patientName, String doctorName,
                           String date, String slot, String type) {
            this.token = token;
            this.patientName = patientName;
            this.doctorName = doctorName;
            this.date = date;
            this.slot = slot;
            this.type = type;
        }

}
