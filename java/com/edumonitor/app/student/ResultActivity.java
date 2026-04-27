package com.edumonitor.app.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import com.edumonitor.app.R;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        int score = getIntent().getIntExtra("score", 0);
        int correct = getIntent().getIntExtra("correct", 0);
        int total = getIntent().getIntExtra("total", 10);
        String subjectName = getIntent().getStringExtra("subjectName");

        ArrayList<String> qTexts = getIntent().getStringArrayListExtra("qTexts");
        ArrayList<String> qCorrects = getIntent().getStringArrayListExtra("qCorrects");
        ArrayList<String> qUserAns = getIntent().getStringArrayListExtra("qUserAns");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Результат");

        TextView tvScore = findViewById(R.id.tv_score);
        TextView tvCorrect = findViewById(R.id.tv_correct);
        TextView tvFeedback = findViewById(R.id.tv_feedback);
        PieChart pieChart = findViewById(R.id.pie_chart);
        Button btnRetry = findViewById(R.id.btn_retry);
        Button btnHome = findViewById(R.id.btn_home);
        LinearLayout llDetailedResults = findViewById(R.id.ll_detailed_results);

        tvScore.setText(score + "%");
        tvCorrect.setText("Правильных ответов: " + correct + " из " + total);

        if (score >= 90) tvFeedback.setText("Отлично! Превосходный результат! 🎉");
        else if (score >= 70) tvFeedback.setText("Хорошо! Есть над чем поработать 👍");
        else if (score >= 50) tvFeedback.setText("Удовлетворительно. Повторите материал 📚");
        else tvFeedback.setText("Нужно больше практики. Не сдавайся! 💪");

        setupPieChart(pieChart, correct, total - correct);

        // Populate detailed results
        if (qTexts != null && qCorrects != null && qUserAns != null) {
            for (int i = 0; i < qTexts.size(); i++) {
                CardView card = new CardView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 16);
                card.setLayoutParams(params);
                card.setRadius(12f);
                card.setCardElevation(2f);

                LinearLayout innerLayout = new LinearLayout(this);
                innerLayout.setOrientation(LinearLayout.VERTICAL);
                innerLayout.setPadding(32, 32, 32, 32);

                TextView tvQ = new TextView(this);
                tvQ.setText((i + 1) + ". " + qTexts.get(i));
                tvQ.setTextSize(16f);
                tvQ.setTextColor(Color.parseColor("#212121"));
                tvQ.setPadding(0, 0, 0, 16);

                TextView tvAns = new TextView(this);
                boolean isCorrect = qCorrects.get(i).equals(qUserAns.get(i));
                
                if (isCorrect) {
                    tvAns.setText("✅ Ваш ответ: " + qUserAns.get(i));
                    tvAns.setTextColor(Color.parseColor("#4CAF50")); // Green
                } else {
                    tvAns.setText("❌ Ваш ответ: " + qUserAns.get(i) + "\nПравильный: " + qCorrects.get(i));
                    tvAns.setTextColor(Color.parseColor("#F44336")); // Red
                }
                tvAns.setTextSize(14f);

                innerLayout.addView(tvQ);
                innerLayout.addView(tvAns);
                card.addView(innerLayout);
                llDetailedResults.addView(card);
            }
        }

        btnRetry.setOnClickListener(v -> finish());
        btnHome.setOnClickListener(v -> {
            finishAffinity();
            startActivity(new Intent(this, SubjectSelectActivity.class));
        });
    }

    private void setupPieChart(PieChart chart, int correct, int wrong) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(correct, "Верно"));
        entries.add(new PieEntry(wrong, "Неверно"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(0xFF4CAF50, 0xFFF44336);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(0xFFFFFFFF);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        chart.setData(data);
        chart.setHoleRadius(45f);
        chart.setTransparentCircleRadius(50f);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(true);
        chart.setDrawEntryLabels(false);
        chart.setCenterText("Результат");
        chart.setCenterTextSize(14f);
        chart.animateY(800);
        chart.invalidate();
    }
}
