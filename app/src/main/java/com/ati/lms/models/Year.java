package com.ati.lms.models;

public class Year {
    private int id;
    private int courseId;
    private String yearName;

    public Year() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public String getYearName() { return yearName; }
    public void setYearName(String yearName) { this.yearName = yearName; }
}