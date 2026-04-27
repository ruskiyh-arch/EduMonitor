package com.edumonitor.app.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.edumonitor.app.LoginActivity;
import com.edumonitor.app.R;
import com.edumonitor.app.adapters.StudentAdapter;
import com.edumonitor.app.models.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherDashboardActivity extends AppCompatActivity {

    private static final String[] SUBJECTS_FILTER = {"Все предметы", "Математика", "Русский язык", "Английский язык"};
    private static final String[] GRADES_FILTER = {"Все классы", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvTotalStudents, tvAvgScore;
    private Spinner spinnerSubject, spinnerGrade;
    private FirebaseFirestore db;

    private final List<Map<String, Object>> allResults = new ArrayList<>();
    private final List<Map<String, Object>> filteredResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Панель учителя");

        tvTotalStudents = findViewById(R.id.tv_total_students);
        tvAvgScore = findViewById(R.id.tv_avg_score);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_students);
        spinnerSubject = findViewById(R.id.spinner_subject);
        spinnerGrade = findViewById(R.id.spinner_grade);
        FloatingActionButton fab = findViewById(R.id.fab_analytics);
        TextView btnLogout = findViewById(R.id.btn_logout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(filteredResults);
        recyclerView.setAdapter(adapter);

        setupSpinners();
        loadResults();

        fab.setOnClickListener(v -> startActivity(new Intent(this, AnalyticsActivity.class)));
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void setupSpinners() {
        ArrayAdapter<String> subjAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, SUBJECTS_FILTER);
        subjAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjAdapter);

        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, GRADES_FILTER);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrade.setAdapter(gradeAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { applyFilter(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        spinnerSubject.setOnItemSelectedListener(filterListener);
        spinnerGrade.setOnItemSelectedListener(filterListener);
    }

    private void loadResults() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("results")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    allResults.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Map<String, Object> r = doc.getData();
                        r.put("docId", doc.getId());
                        allResults.add(r);
                    }
                    progressBar.setVisibility(View.GONE);
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                });
    }

    private void applyFilter() {
        String subjectFilter = spinnerSubject.getSelectedItemPosition() == 0
                ? null : SUBJECTS_FILTER[spinnerSubject.getSelectedItemPosition()];
        String gradeFilter = spinnerGrade.getSelectedItemPosition() == 0
                ? null : GRADES_FILTER[spinnerGrade.getSelectedItemPosition()];

        filteredResults.clear();
        // Keep only latest result per student per subject
        Map<String, Map<String, Object>> latestPerStudent = new HashMap<>();
        for (Map<String, Object> r : allResults) {
            String subject = (String) r.get("subject");
            String gradeVal = (String) r.get("grade");
            if (subjectFilter != null && !subjectFilter.equals(subject)) continue;
            if (gradeFilter != null && !gradeFilter.equals(gradeVal)) continue;

            String userId = (String) r.get("userId");
            String key = userId + "_" + (subjectFilter != null ? subjectFilter : subject);
            if (!latestPerStudent.containsKey(key)) {
                latestPerStudent.put(key, r);
            }
        }
        filteredResults.addAll(latestPerStudent.values());

        // Update stats
        int total = filteredResults.size();
        int sumScore = 0;
        for (Map<String, Object> r : filteredResults) {
            Object s = r.get("score");
            if (s instanceof Long) sumScore += ((Long) s).intValue();
            else if (s instanceof Integer) sumScore += (Integer) s;
        }
        int avg = total > 0 ? sumScore / total : 0;

        tvTotalStudents.setText(String.valueOf(total));
        tvAvgScore.setText(avg + "%");

        adapter.notifyDataSetChanged();
    }
}
