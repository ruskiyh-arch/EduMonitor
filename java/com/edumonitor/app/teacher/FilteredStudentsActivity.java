package com.edumonitor.app.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.edumonitor.app.R;
import com.edumonitor.app.adapters.StudentAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilteredStudentsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FirebaseFirestore db;

    private final List<Map<String, Object>> filteredResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_students);

        String subjFilter = getIntent().getStringExtra("subjectFilter");
        String gradeFilter = getIntent().getStringExtra("gradeFilter");
        boolean isPassed = getIntent().getBooleanExtra("isPassed", true);

        if ("all".equals(subjFilter)) subjFilter = null;
        if ("all".equals(gradeFilter)) gradeFilter = null;

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            String title = isPassed ? "Сдали тест" : "Не сдали тест";
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerView = findViewById(R.id.recycler_filtered_students);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(filteredResults);
        recyclerView.setAdapter(adapter);

        loadResults(subjFilter, gradeFilter, isPassed);
    }

    private void loadResults(String subjectFilter, String gradeFilter, boolean isPassed) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("results")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Map<String, Object>> allResults = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Map<String, Object> r = doc.getData();
                        allResults.add(r);
                    }

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

                    filteredResults.clear();
                    for (Map<String, Object> r : latestPerStudent.values()) {
                        Object sc = r.get("score");
                        int scoreVal = sc instanceof Long ? ((Long) sc).intValue() : sc instanceof Integer ? (Integer) sc : 0;
                        boolean userPassed = scoreVal >= 60;
                        if (userPassed == isPassed) {
                            filteredResults.add(r);
                        }
                    }

                    progressBar.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();

                    if (filteredResults.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                });
    }
}
