package com.edumonitor.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.edumonitor.app.student.SubjectSelectActivity;
import com.edumonitor.app.teacher.TeacherDashboardActivity;

import java.util.HashMap;
import java.util.Map;

public class RoleSelectActivity extends AppCompatActivity {

    private String uid, email;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_select);

        db = FirebaseFirestore.getInstance();
        uid = getIntent().getStringExtra("uid");
        email = getIntent().getStringExtra("email");

        if (uid == null) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) uid = auth.getCurrentUser().getUid();
        }

        CardView cardStudent = findViewById(R.id.card_student);
        CardView cardTeacher = findViewById(R.id.card_teacher);

        cardStudent.setOnClickListener(v -> selectRole("student", SubjectSelectActivity.class));
        cardTeacher.setOnClickListener(v -> selectRole("teacher", TeacherDashboardActivity.class));
    }

    private void selectRole(String role, Class<?> dest) {
        if (uid == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", uid);
        userData.put("email", email != null ? email : "");
        userData.put("role", role);
        userData.put("name", email != null ? email.split("@")[0] : "Пользователь");
        userData.put("grade", "7");

        db.collection("users").document(uid).set(userData)
                .addOnSuccessListener(v -> {
                    startActivity(new Intent(this, dest));
                    finish();
                })
                .addOnFailureListener(e -> {
                        android.util.Log.e("RoleSelect", "Error saving role", e);
                        Toast.makeText(this, "Ошибка сохранения роли: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
