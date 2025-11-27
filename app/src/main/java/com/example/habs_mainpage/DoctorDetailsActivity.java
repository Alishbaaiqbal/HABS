package com.example.habs_mainpage;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DoctorDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DoctorAdapter adapter;
    private final List<Doctor> doctorList = new ArrayList<>();

    private String hospitalName;
    private String hospitalUniqueId;   // 🔹 NEW: internal unique id per hospital

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctordetails);

        recyclerView = findViewById(R.id.recycler_doctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 Read hospital info from Intent
        hospitalName = getIntent().getStringExtra("hospital_name");
        if (hospitalName == null || hospitalName.isEmpty()) {
            hospitalName = "General Hospital";
        }

        // 🔹 Try to get a unique id from previous screen (e.g. placeId or lat_lng)
        //     If not provided, we fallback to hospitalName (works if names are unique)
        hospitalUniqueId = getIntent().getStringExtra("hospital_unique_id");
        if (hospitalUniqueId == null || hospitalUniqueId.isEmpty()) {
            hospitalUniqueId = hospitalName;
        }

        // 🔹 Load doctors for this hospital name from JSON
        loadDoctorsFromJSON(hospitalName);
    }

    private void loadDoctorsFromJSON(String selectedHospital) {
        try (InputStream inputStream = getAssets().open("DoctorDataset.json")) {
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            String jsonString = new String(buffer, StandardCharsets.UTF_8);

            JSONObject rootObject = new JSONObject(jsonString);
            String bestKey = findBestHospitalKey(rootObject, selectedHospital);

            doctorList.clear();

            if (bestKey != null && rootObject.has(bestKey)) {
                JSONArray doctorsArray = rootObject.getJSONArray(bestKey);
                Log.d("DoctorMatch", "✅ Matched hospital: " + bestKey + " (" + doctorsArray.length() + " doctors)");

                for (int i = 0; i < doctorsArray.length(); i++) {
                    JSONObject obj = doctorsArray.getJSONObject(i);
                    doctorList.add(new Doctor(
                            obj.optString("Doctor Name", "Unknown"),
                            obj.optString("Specialization", "N/A"),
                            obj.optString("Doctor Qualification", ""),
                            obj.optString("Experience(Years)", ""),
                            obj.optString("Total_Reviews", ""),
                            obj.optString("Patient Satisfaction Rate(%age)", ""),
                            obj.optString("Avg Time to Patients(mins)", ""),
                            obj.optString("Wait Time(mins)", ""),
                            obj.optString("Fee(PKR)", ""),
                            obj.optString("Timing", obj.optString("Timings", "")) // ✅ handles both "Timing" & "Timings"
                    ));
                }
            } else if (rootObject.has("General Hospital")) {
                JSONArray defaultDoctors = rootObject.getJSONArray("General Hospital");
                Log.w("DoctorMatch", "⚠ No match for " + selectedHospital + ". Using General Hospital doctors.");

                for (int i = 0; i < defaultDoctors.length(); i++) {
                    JSONObject obj = defaultDoctors.getJSONObject(i);
                    doctorList.add(new Doctor(
                            obj.optString("Doctor Name", "Unknown"),
                            obj.optString("Specialization", "N/A"),
                            obj.optString("Doctor Qualification", ""),
                            obj.optString("Experience(Years)", ""),
                            obj.optString("Total_Reviews", ""),
                            obj.optString("Patient Satisfaction Rate(%age)", ""),
                            obj.optString("Avg Time to Patients(mins)", ""),
                            obj.optString("Wait Time(mins)", ""),
                            obj.optString("Fee(PKR)", ""),
                            obj.optString("Timing", obj.optString("Timings", ""))
                    ));
                }
                Toast.makeText(this, "No exact match found — showing General Hospital doctors.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No matching hospital found in JSON.", Toast.LENGTH_SHORT).show();
            }

            // 🔹 Now that doctorList is filled, create adapter with hospital info
            adapter = new DoctorAdapter(
                    this,
                    doctorList,
                    hospitalName,
                    hospitalUniqueId   // 👈 yahi unique id AppointmentBookingActivity tak jayegi
            );
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            Log.e("DoctorDetails", "Error reading JSON", e);
            Toast.makeText(this, "Failed to load doctor data.", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ Improved hospital name matching
    private String findBestHospitalKey(JSONObject rootObject, String selectedHospital) {
        String target = cleanName(selectedHospital);
        String bestKey = null;
        double bestScore = 0.0;

        Iterator<String> keys = rootObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String cleanedKey = cleanName(key);

            if (cleanedKey.contains(target) || target.contains(cleanedKey)) {
                Log.d("DoctorMatch", "🔹 Partial match found: " + key);
                return key;
            }

            double score = tokenOverlapScore(target, cleanedKey);
            if (score > bestScore) {
                bestScore = score;
                bestKey = key;
            }
        }

        Log.d("DoctorMatch", "Best fuzzy match score: " + bestScore + " for key " + bestKey);
        return bestScore >= 0.4 ? bestKey : null;
    }

    private double tokenOverlapScore(String a, String b) {
        String[] ta = a.split("\\s+");
        String[] tb = b.split("\\s+");
        java.util.HashSet<String> sa = new java.util.HashSet<>();
        java.util.HashSet<String> sb = new java.util.HashSet<>();
        for (String s : ta) sa.add(s);
        for (String s : tb) sb.add(s);
        java.util.HashSet<String> inter = new java.util.HashSet<>(sa);
        inter.retainAll(sb);
        java.util.HashSet<String> union = new java.util.HashSet<>(sa);
        union.addAll(sb);
        return union.isEmpty() ? 0 : (double) inter.size() / union.size();
    }

    private String cleanName(String name) {
        return name == null ? "" :
                name.toLowerCase()
                        .replaceAll("[^a-z0-9\\s]", "")
                        .replaceAll("\\s+", " ")
                        .trim();
    }
}
