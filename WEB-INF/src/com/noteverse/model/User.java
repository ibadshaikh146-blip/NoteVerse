package com.noteverse.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String fullName;
    private String email;
    private String rollNumber;
    private int semester;
    private String passwordHash;
    private String role; // STUDENT or ADMIN
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String fullName, String email, String rollNumber, int semester,
                String passwordHash, String role, Timestamp createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.rollNumber = rollNumber;
        this.semester = semester;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + fullName + "', email='" + email +
               "', roll='" + rollNumber + "', semester=" + semester + ", role='" + role + "'}";
    }
}
