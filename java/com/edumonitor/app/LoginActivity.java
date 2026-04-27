package com.edumonitor.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.edumonitor.app.student.SubjectSelectActivity;
import com.edumonitor.app.teacher.TeacherDashboardActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private android.widget.TextView tvTitle, tvToggleMode;
    private android.widget.ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail    = findViewById(R.id.loginInput);
        etPassword = findViewById(R.id.passwordInput);
        btnLogin   = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progress_bar);
        
        tvTitle = findViewById(R.id.tv_title);
        tvToggleMode = findViewById(R.id.tv_toggle_mode);

        // Auto-login if session exists
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkRoleAndRedirect(currentUser.getUid());
        }

        btnLogin.setOnClickListener(v -> {
            if (isLoginMode) {
                loginUser();
            } else {
                registerUser();
            }
        });
        
        tvToggleMode.setOnClickListener(v -> toggleMode());
    }

    private boolean isLoginMode = true;

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            tvTitle.setText("Вход");
            btnLogin.setText("Войти");
            tvToggleMode.setText("Нет аккаунта? Зарегистрируйся");
        } else {
            tvTitle.setText("Регистрация");
            btnLogin.setText("Создать аккаунт");
            tvToggleMode.setText("Уже есть аккаунт? Войти");
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            shakeView(TextUtils.isEmpty(email) ? etEmail : etPassword);
            return;
        }
        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> checkRoleAndRedirect(r.getUser().getUid()))
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    shakeView(etPassword);
                });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Пароль — минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> {
                    showLoading(false);
                    Intent i = new Intent(this, RoleSelectActivity.class);
                    i.putExtra("uid", r.getUser().getUid());
                    i.putExtra("email", email);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Ошибка регистрации: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /** After login — check Firestore for stored role. If present, redirect directly. */
    private void checkRoleAndRedirect(String uid) {
        showLoading(true);
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    showLoading(false);
                    if (doc.exists() && doc.getString("role") != null) {
                        redirectByRole(doc.getString("role"));
                    } else {
                        Intent i = new Intent(this, RoleSelectActivity.class);
                        i.putExtra("uid", uid);
                        startActivity(i);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Intent i = new Intent(this, RoleSelectActivity.class);
                    i.putExtra("uid", uid);
                    startActivity(i);
                    finish();
                });
    }

    private void redirectByRole(String role) {
        Intent i = "teacher".equals(role)
                ? new Intent(this, TeacherDashboardActivity.class)
                : new Intent(this, SubjectSelectActivity.class);
        startActivity(i);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        tvToggleMode.setEnabled(!show);
    }

    private void shakeView(View v) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        v.startAnimation(shake);
    }
}
