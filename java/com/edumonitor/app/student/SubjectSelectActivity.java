package com.edumonitor.app.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.edumonitor.app.LoginActivity;
import com.edumonitor.app.R;

public class SubjectSelectActivity extends AppCompatActivity {

    private static final String[] SUBJECTS = {"Математика", "Русский язык", "Английский язык"};
    private static final String[] SUBJECT_IDS = {"math", "russian", "english"};
    private static final String[] GRADES = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};

    private String selectedSubject = null;
    private String selectedSubjectId = null;
    private CardView cardMath, cardRussian, cardEnglish;
    private Spinner spinnerGrade;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_select);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Выбор теста");

        cardMath = findViewById(R.id.card_math);
        cardRussian = findViewById(R.id.card_russian);
        cardEnglish = findViewById(R.id.card_english);
        spinnerGrade = findViewById(R.id.spinner_grade);
        Button btnStart = findViewById(R.id.btn_start_test);
        Button btnLogout = findViewById(R.id.btn_logout);

        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, GRADES);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrade.setAdapter(gradeAdapter);
        spinnerGrade.setSelection(6); // default: 7th grade (index 6)

        cardMath.setOnClickListener(v -> selectSubject(0));
        cardRussian.setOnClickListener(v -> selectSubject(1));
        cardEnglish.setOnClickListener(v -> selectSubject(2));

        btnStart.setOnClickListener(v -> startTest());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void selectSubject(int index) {
        selectedSubject = SUBJECTS[index];
        selectedSubjectId = SUBJECT_IDS[index];
        CardView[] cards = {cardMath, cardRussian, cardEnglish};
        
        // Reset all cards
        for (CardView card : cards) {
            card.setCardElevation(4);
            card.setCardBackgroundColor(android.graphics.Color.WHITE);
            // Reset text color to dark gray
            android.widget.LinearLayout layout = (android.widget.LinearLayout) card.getChildAt(0);
            android.widget.TextView tvText = (android.widget.TextView) layout.getChildAt(1);
            tvText.setTextColor(android.graphics.Color.parseColor("#212121"));
        }

        // Highlight selected
        cards[index].setCardElevation(12);
        cards[index].setCardBackgroundColor(android.graphics.Color.parseColor("#3F51B5")); // Dark blue
        
        // Change text color to white for contrast
        android.widget.LinearLayout layout = (android.widget.LinearLayout) cards[index].getChildAt(0);
        android.widget.TextView tvText = (android.widget.TextView) layout.getChildAt(1);
        tvText.setTextColor(android.graphics.Color.WHITE);
    }

    private void startTest() {
        if (selectedSubjectId == null) {
            Toast.makeText(this, "Выберите предмет", Toast.LENGTH_SHORT).show();
            return;
        }
        String grade = GRADES[spinnerGrade.getSelectedItemPosition()];

        // Load test from Firestore then launch TestActivity
        db.collection("tests").document(selectedSubjectId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Тест не найден", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(this, TestActivity.class);
                    intent.putExtra("subjectId", selectedSubjectId);
                    intent.putExtra("subjectName", selectedSubject);
                    intent.putExtra("grade", grade);
                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Ошибка загрузки теста", Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
