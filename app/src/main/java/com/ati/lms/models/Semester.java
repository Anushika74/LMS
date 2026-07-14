package com.ati.lms.models;

public class Semester {
    private int id;
    private int yearId;
    private String semesterName;

    public Semester() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getYearId() { return yearId; }
    public void setYearId(int yearId) { this.yearId = yearId; }

    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }
}