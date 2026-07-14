package com.ati.lms.models;

public class Module {
    private int id;
    private int semesterId;
    private String moduleName;
    private String moduleCode;
    private int lecturerId;

    public Module() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSemesterId() { return semesterId; }
    public void setSemesterId(int semesterId) { this.semesterId = semesterId; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }

    public int getLecturerId() { return lecturerId; }
    public void setLecturerId(int lecturerId) { this.lecturerId = lecturerId; }
}