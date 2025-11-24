package com.example.habs_mainpage;

public class TokenHelper {
    public static void generateTokenAtomic(String firebaseUrl, String hospitalId, String doctorId, String appointmentId, String patientName, long requestedScheduledMs, int consultationTime, String source, TokenHelper.TokenCallback tokenCallback) {
    }

    public static abstract class TokenCallback {
        public abstract void onSuccess(String label, long scheduledMs);

        public abstract void onFailure(String error);
    }
}
