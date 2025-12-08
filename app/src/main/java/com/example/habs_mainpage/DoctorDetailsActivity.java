package com.example.habs_mainpage;

import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DoctorDetailsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DoctorAdapter adapter;

    private final List<Doctor> doctorList = new ArrayList<>();
    private final List<Doctor> filteredDoctorList = new ArrayList<>();

    private Spinner spinnerTime, spinnerExperience, spinnerSpecialization, spinnerFees;
    private TextView tvHospitalTitle;

    private String hospitalName;
    private String hospitalUniqueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctordetails);

        recyclerView = findViewById(R.id.recycler_doctors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvHospitalTitle = findViewById(R.id.tv_hospital_title);
        spinnerTime = findViewById(R.id.spinner_time);
        spinnerExperience = findViewById(R.id.spinner_availability);  // same ID used
        spinnerSpecialization = findViewById(R.id.spinner_specialization);
        spinnerFees = findViewById(R.id.spinner_fees);

        hospitalName = getIntent().getStringExtra("hospital_name");
        if (hospitalName == null || hospitalName.isEmpty())
            hospitalName = "General Hospital";

        hospitalUniqueId = getIntent().getStringExtra("hospital_unique_id");
        if (hospitalUniqueId == null || hospitalUniqueId.isEmpty())
            hospitalUniqueId = hospitalName;

        tvHospitalTitle.setText("Doctors at " + hospitalName);

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
                JSONArray arr = rootObject.getJSONArray(bestKey);

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    doctorList.add(new Doctor(
                            obj.optString("Doctor Name", "Unknown"),
                            obj.optString("Specialization", "N/A"),
                            obj.optString("Doctor Qualification", ""),
                            obj.optString("Experience(Years)", "0"),
                            obj.optString("Total_Reviews", ""),
                            obj.optString("Patient Satisfaction Rate(%age)", ""),
                            obj.optString("Avg Time to Patients(mins)", ""),
                            obj.optString("Wait Time(mins)", ""),
                            obj.optString("Fee(PKR)", ""),
                            obj.optString("Timing", obj.optString("Timings", ""))
                    ));
                }
            }

            filteredDoctorList.clear();
            filteredDoctorList.addAll(doctorList);

            adapter = new DoctorAdapter(this, filteredDoctorList, hospitalName, hospitalUniqueId);
            recyclerView.setAdapter(adapter);

            setupFilterSpinners();

        } catch (Exception e) {
            Log.e("ERR", "JSON Load Error", e);
        }
    }

    // ---------------------- FILTER SPINNERS ----------------------

    private void setupFilterSpinners() {

        setupSpecializationSpinner();
        setupFeesSpinner();
        setupTimeSpinner();
        setupExperienceSpinner();

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int pos, long id) {
                applyFilters();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerSpecialization.setOnItemSelectedListener(listener);
        spinnerFees.setOnItemSelectedListener(listener);
        spinnerTime.setOnItemSelectedListener(listener);
        spinnerExperience.setOnItemSelectedListener(listener);
    }

    private void setupExperienceSpinner() {
        List<String> expOptions = new ArrayList<>();
        expOptions.add("Any Experience");
        expOptions.add("0 - 3 Years");
        expOptions.add("3 - 5 Years");
        expOptions.add("5 - 10 Years");
        expOptions.add("10+ Years");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, expOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExperience.setAdapter(adapter);
    }

    private void setupSpecializationSpinner() {
        List<String> specs = new ArrayList<>();
        specs.add("All Specializations");

        Set<String> set = new HashSet<>();
        for (Doctor d : doctorList) if (!d.getSpecialization().isEmpty()) set.add(d.getSpecialization());
        specs.addAll(set);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, specs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpecialization.setAdapter(adapter);
    }

    private void setupFeesSpinner() {
        List<String> list = List.of("Any Fee", "Under 500", "500 - 1000", "1000 - 1500", "1500+");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFees.setAdapter(adapter);
    }

    private void setupTimeSpinner() {
        List<String> list = List.of("Any Time", "Morning", "Afternoon", "Evening", "Night");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(adapter);
    }

    // ---------------------- FILTER LOGIC ----------------------

    private void applyFilters() {

        String spec = spinnerSpecialization.getSelectedItem().toString();
        String feeFilter = spinnerFees.getSelectedItem().toString();
        String timeFilter = spinnerTime.getSelectedItem().toString();
        String expFilter = spinnerExperience.getSelectedItem().toString();

        List<Doctor> result = new ArrayList<>();

        for (Doctor d : doctorList) {
            boolean ok = true;

            // SPECIALIZATION
            if (!spec.equals("All Specializations") &&
                    !d.getSpecialization().equalsIgnoreCase(spec))
                ok = false;

            // EXPERIENCE
            if (ok) {
                int exp = Integer.parseInt(d.getExperience().replaceAll("[^0-9]", "0"));

                switch (expFilter) {
                    case "0 - 3 Years":
                        if (!(exp >= 0 && exp <= 3)) ok = false;
                        break;
                    case "3 - 5 Years":
                        if (!(exp >= 3 && exp <=5)) ok = false;
                        break;
                    case "5 - 10 Years":
                        if (!(exp >=5 && exp <=10)) ok = false;
                        break;
                    case "10+ Years":
                        if (exp < 10) ok = false;
                        break;
                }
            }

            // FEES
            if (ok) {
                int fee;
                try { fee = Integer.parseInt(d.getFee().replaceAll("[^0-9]","0")); }
                catch(Exception e){ fee = 0; }

                switch (feeFilter) {
                    case "Under 500": if (fee >= 500) ok = false; break;
                    case "500 - 1000": if (fee < 500 || fee > 1000) ok = false; break;
                    case "1000 - 1500": if (fee < 1000 || fee > 1500) ok = false; break;
                    case "1500+": if (fee < 1500) ok = false; break;
                }
            }

            // TIME RANGE FILTER (EXACT TIME)
            if (ok && !timeFilter.equals("Any Time")) {
                if (!isDoctorAvailableInTimeSlot(d.getTiming(), timeFilter))
                    ok = false;
            }

            if (ok) result.add(d);
        }

        filteredDoctorList.clear();
        filteredDoctorList.addAll(result);
        adapter.notifyDataSetChanged();
    }

    // ---------------- TIME SLOT PARSER ----------------

    private boolean isDoctorAvailableInTimeSlot(String timing, String slot) {
        if (timing == null || timing.isEmpty()) return true;   // agar timing nahi hai to filter ignore

        try {
            // Example timing: "5:30pm-7:30pm"
            // 1) Spaces hatao, upper-case karo (5:30PM-7:30PM)
            String normalized = timing.replace(" ", "").toUpperCase(Locale.US);

            // 2) Hyphen pe split karo
            String[] parts = normalized.split("-");
            if (parts.length != 2) return true;

            String startStr = parts[0]; // "5:30PM"
            // String endStr  = parts[1]; // "7:30PM" (abhi use nahi kar rahe)

            // 3) Time parse karo: pattern "h:mma" => 5:30PM, 11:00AM, etc.
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("h:mma", Locale.US);
            java.util.Date startTime = fmt.parse(startStr);
            if (startTime == null) return true;

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(startTime);
            int hour24 = cal.get(java.util.Calendar.HOUR_OF_DAY); // 0–23

            switch (slot) {
                case "Morning":     // 6 AM – 12 PM
                    return hour24 >= 6 && hour24 < 12;

                case "Afternoon":   // 12 PM – 5 PM
                    return hour24 >= 12 && hour24 < 17;

                case "Evening":     // 5 PM – 10 PM
                    return hour24 >= 17 && hour24 < 22;

                default:
                    return true; // "Any Time" ya kuch aur ho to doctor dikhao
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Parsing fail ho jaye to filter ko ignore kar dete hain
            return true;
        }
    }


    // ---------------- MATCHING HELPERS (UNCHANGED) ----------------

    private String findBestHospitalKey(JSONObject rootObject, String selectedHospital) {
        String target = cleanName(selectedHospital);
        String bestKey = null;
        double bestScore = 0;

        Iterator<String> keys = rootObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String cleaned = cleanName(key);

            if (cleaned.contains(target) || target.contains(cleaned)) return key;

            double score = tokenOverlapScore(target, cleaned);
            if (score > bestScore) { bestScore = score; bestKey = key; }
        }

        return bestScore >= 0.4 ? bestKey : null;
    }

    private double tokenOverlapScore(String a, String b) {
        String[] ta = a.split("\\s+"), tb = b.split("\\s+");
        HashSet<String> s1 = new HashSet<>(), s2 = new HashSet<>();
        for (String s : ta) s1.add(s);
        for (String s : tb) s2.add(s);
        HashSet<String> inter = new HashSet<>(s1);
        inter.retainAll(s2);
        HashSet<String> uni = new HashSet<>(s1);
        uni.addAll(s2);
        return uni.isEmpty() ? 0 : (double) inter.size() / uni.size();
    }

    private String cleanName(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9\\s]","").trim();
    }
}
