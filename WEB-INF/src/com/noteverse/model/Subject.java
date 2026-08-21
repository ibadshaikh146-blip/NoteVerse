package com.noteverse.model;

public class Subject {
    private int subjectId;
    private String subjectName;
    private int semester;

    public Subject() {}

    public Subject(int subjectId, String subjectName, int semester) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.semester = semester;
    }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
}
