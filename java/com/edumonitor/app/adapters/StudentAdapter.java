package com.edumonitor.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edumonitor.app.R;

import java.util.List;
import java.util.Map;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private final List<Map<String, Object>> items;

    public StudentAdapter(List<Map<String, Object>> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);
        String name = (String) item.getOrDefault("userName", "Ученик");
        
        Object emailObj = item.get("email");
        String email = (emailObj != null && !((String)emailObj).isEmpty()) 
                ? (String) emailObj : "Нет данных об email";
                
        String subject = (String) item.getOrDefault("subject", "—");
        String grade = (String) item.getOrDefault("grade", "—");
        Object sc = item.get("score");
        int score = sc instanceof Long ? ((Long) sc).intValue() : sc instanceof Integer ? (Integer) sc : 0;

        holder.tvName.setText(name);
        holder.tvEmail.setText(email);
        holder.tvSubject.setText(subject + " · " + grade + " класс");
        holder.tvScore.setText(score + "%");
        holder.tvScore.setTextColor(score >= 60 ? 0xFF4CAF50 : 0xFFF44336);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvSubject, tvScore;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_student_name);
            tvEmail = v.findViewById(R.id.tv_student_email);
            tvSubject = v.findViewById(R.id.tv_student_subject);
            tvScore = v.findViewById(R.id.tv_student_score);
        }
    }
}
