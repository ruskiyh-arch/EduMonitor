package com.edumonitor.app.models;

import com.google.firebase.Timestamp;

public class Result {
    private String userId;
    private String userName;
    private String subject;
    private String grade;
    private int score;           // percentage 0-100
    private int correctAnswers;
    private int totalQuestions;
    private Timestamp date;

    public Result() {}

    public Result(String userId, String userName, String subject, String grade,
                  int score, int correctAnswers, int totalQuestions, Timestamp date) {
        this.userId = userId;
        this.userName = userName;
        this.subject = subject;
        this.grade = grade;
        this.score = score;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.date = date;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
}
