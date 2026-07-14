package com.ati.lms.models;

public class Notice {
    private int id;
    private int lecturerId;
    private String title;
    private String content;
    private String priority;
    private String createdAt;

    public Notice() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLecturerId() { return lecturerId; }
    public void setLecturerId(int lecturerId) { this.lecturerId = lecturerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}