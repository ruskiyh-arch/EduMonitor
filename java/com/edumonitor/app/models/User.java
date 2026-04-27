package com.edumonitor.app.models;

public class User {
    private String id;
    private String email;
    private String role;
    private String name;
    private String grade;

    public User() {}

    public User(String id, String email, String role, String name, String grade) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.name = name;
        this.grade = grade;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
