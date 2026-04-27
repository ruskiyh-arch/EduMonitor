package com.edumonitor.app.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.edumonitor.app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsActivity extends AppCompatActivity {

    private static final String[] SUBJECTS_FILTER = {"Все предметы", "Математика", "Русский язык", "Английский язык"};
    private static final String[] GRADES_FILTER = {"Все классы", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
    private static final String[] ALL_SUBJECTS = {"Математика", "Русский язык", "Английский язык"};

    private BarChart barChart;
    private PieChart pieChart;
    private LineChart lineChart;
    private Spinner spinnerSubject, spinnerGrade;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    private final List<Map<String, Object>> allResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Аналитика");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        barChart = findViewById(R.id.bar_chart);
        pieChart = findViewById(R.id.pie_chart);
        lineChart = findViewById(R.id.line_chart);
        spinnerSubject = findViewById(R.id.spinner_subject);
        spinnerGrade = findViewById(R.id.spinner_grade);
        progressBar = findViewById(R.id.progress_bar);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    PieEntry pe = (PieEntry) e;
                    boolean isPassed = pe.getLabel().equals("Сдали");
                    
                    String subjFilter = spinnerSubject.getSelectedItemPosition() == 0 ? "all"
                            : SUBJECTS_FILTER[spinnerSubject.getSelectedItemPosition()];
                    String gradeFilter = spinnerGrade.getSelectedItemPosition() == 0 ? "all"
                            : GRADES_FILTER[spinnerGrade.getSelectedItemPosition()];

                    android.content.Intent intent = new android.content.Intent(AnalyticsActivity.this, FilteredStudentsActivity.class);
                    intent.putExtra("subjectFilter", subjFilter);
                    intent.putExtra("gradeFilter", gradeFilter);
                    intent.putExtra("isPassed", isPassed);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected() {}
        });

        setupSpinners();
        loadAndRender();

        toolbar.setNavigationOnClickListener(v -> finish());
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

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { renderCharts(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        spinnerSubject.setOnItemSelectedListener(listener);
        spinnerGrade.setOnItemSelectedListener(listener);
    }

    private void loadAndRender() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("results")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    allResults.clear();
                    for (QueryDocumentSnapshot doc : snap) allResults.add(doc.getData());
                    progressBar.setVisibility(View.GONE);
                    renderCharts();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                });
    }

    private List<Map<String, Object>> getFiltered() {
        String subjFilter = spinnerSubject.getSelectedItemPosition() == 0 ? null
                : SUBJECTS_FILTER[spinnerSubject.getSelectedItemPosition()];
        String gradeFilter = spinnerGrade.getSelectedItemPosition() == 0 ? null
                : GRADES_FILTER[spinnerGrade.getSelectedItemPosition()];

        Map<String, Map<String, Object>> latestPerStudent = new HashMap<>();
        for (Map<String, Object> r : allResults) {
            String subject = (String) r.get("subject");
            String gradeVal = (String) r.get("grade");
            if (subjFilter != null && !subjFilter.equals(subject)) continue;
            if (gradeFilter != null && !gradeFilter.equals(gradeVal)) continue;
            
            String userId = (String) r.get("userId");
            // If "All subjects" is selected, we keep latest per student per subject
            // If specific subject is selected, we keep latest per student for that subject
            String key = userId + "_" + (subjFilter != null ? subjFilter : subject);
            if (!latestPerStudent.containsKey(key)) {
                latestPerStudent.put(key, r);
            }
        }
        return new ArrayList<>(latestPerStudent.values());
    }

    private void renderCharts() {
        List<Map<String, Object>> data = getFiltered();
        renderBarChart(data);
        renderPieChart(data);
        renderLineChart(data);
    }

    // BarChart: average score per subject
    private void renderBarChart(List<Map<String, Object>> data) {
        Map<String, List<Integer>> subjectScores = new HashMap<>();
        for (String s : ALL_SUBJECTS) subjectScores.put(s, new ArrayList<>());

        for (Map<String, Object> r : data) {
            String subj = (String) r.get("subject");
            Object sc = r.get("score");
            int scoreVal = sc instanceof Long ? ((Long) sc).intValue() : sc instanceof Integer ? (Integer) sc : 0;
            if (subjectScores.containsKey(subj)) subjectScores.get(subj).add(scoreVal);
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int idx = 0;
        for (String subj : ALL_SUBJECTS) {
            List<Integer> scores = subjectScores.get(subj);
            float avg = scores == null || scores.isEmpty() ? 0 :
                    scores.stream().mapToInt(i -> i).sum() / (float) scores.size();
            entries.add(new BarEntry(idx, avg));
            labels.add(subj.length() > 8 ? subj.substring(0, 7) + "." : subj);
            idx++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Средний балл (%)");
        dataSet.setColors(0xFF3F51B5, 0xFF009688, 0xFFFF9800);
        dataSet.setValueTextSize(12f);

        barChart.setData(new BarData(dataSet));
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMaximum(100f);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(800);
        barChart.invalidate();
    }

    // PieChart: pass (>=60%) vs fail (<60%)
    private void renderPieChart(List<Map<String, Object>> data) {
        if (data.isEmpty()) {
            pieChart.clear();
            pieChart.invalidate();
            return;
        }

        int pass = 0, fail = 0;
        for (Map<String, Object> r : data) {
            Object sc = r.get("score");
            int scoreVal = sc instanceof Long ? ((Long) sc).intValue() : sc instanceof Integer ? (Integer) sc : 0;
            if (scoreVal >= 60) pass++; else fail++;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        
        if (pass > 0) {
            entries.add(new PieEntry(pass, "Сдали"));
            colors.add(0xFF4CAF50); // Green
        }
        if (fail > 0) {
            entries.add(new PieEntry(fail, "Не сдали"));
            colors.add(0xFFF44336); // Red
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(0xFFFFFFFF);
        dataSet.setSliceSpace(2f);
        
        // Use an integer formatter to display "2 чел." instead of "2.00"
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value) + " чел.";
            }
        });

        pieChart.setData(new PieData(dataSet));
        pieChart.setHoleRadius(40f);
        pieChart.setCenterText("Успеваемость");
        pieChart.setCenterTextSize(12f);
        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    // LineChart: scores over time (sorted by index as proxy)
    private void renderLineChart(List<Map<String, Object>> data) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            Object sc = data.get(i).get("score");
            int scoreVal = sc instanceof Long ? ((Long) sc).intValue() : sc instanceof Integer ? (Integer) sc : 0;
            entries.add(new Entry(i, scoreVal));
        }

        if (entries.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Динамика баллов");
        dataSet.setColor(0xFF3F51B5);
        dataSet.setCircleColor(0xFF3F51B5);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        lineChart.setData(new LineData(dataSet));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setAxisMaximum(100f);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateX(800);
        lineChart.invalidate();
    }
}
