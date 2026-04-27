package com.edumonitor.app.models;

import java.util.List;

public class TestModel {
    private String id;
    private String subject;
    private List<Question> questions;

    public TestModel() {}

    public TestModel(String id, String subject, List<Question> questions) {
        this.id = id;
        this.subject = subject;
        this.questions = questions;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
