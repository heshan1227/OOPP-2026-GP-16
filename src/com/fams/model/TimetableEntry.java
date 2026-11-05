package com.fams.model;

public class TimetableEntry {
    private int entryId;
    private String courseCode;
    private String courseName;
    private ComponentType componentType;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String venue;
    private String lecturerName;
    private String department;
    private String batch;
    private String academicPeriod;

    public TimetableEntry(int entryId, String courseCode, String courseName, ComponentType componentType,
                          String dayOfWeek, String startTime, String endTime, String venue, String lecturerName,
                          String department, String batch) {
        this(entryId, courseCode, courseName, componentType, dayOfWeek, startTime, endTime, venue, lecturerName, department, batch, "Semester 1 - 2026");
    }

    public TimetableEntry(int entryId, String courseCode, String courseName, ComponentType componentType,
                          String dayOfWeek, String startTime, String endTime, String venue, String lecturerName,
                          String department, String batch, String academicPeriod) {
        this.entryId = entryId; this.courseCode = courseCode; this.courseName = courseName;
        this.componentType = componentType; this.dayOfWeek = dayOfWeek; this.startTime = startTime;
        this.endTime = endTime; this.venue = venue; this.lecturerName = lecturerName;
        this.department = department; this.batch = batch; this.academicPeriod = academicPeriod;
    }

    public int getEntryId() { return entryId; } public void setEntryId(int v) { entryId = v; }
    public String getCourseCode() { return courseCode; } public void setCourseCode(String v) { courseCode = v; }
    public String getCourseName() { return courseName; } public void setCourseName(String v) { courseName = v; }
    public ComponentType getComponentType() { return componentType; } public void setComponentType(ComponentType v) { componentType = v; }
    public String getDayOfWeek() { return dayOfWeek; } public void setDayOfWeek(String v) { dayOfWeek = v; }
    public String getStartTime() { return startTime; } public void setStartTime(String v) { startTime = v; }
    public String getEndTime() { return endTime; } public void setEndTime(String v) { endTime = v; }
    public String getVenue() { return venue; } public void setVenue(String v) { venue = v; }
    public String getLecturerName() { return lecturerName; } public void setLecturerName(String v) { lecturerName = v; }
    public String getDepartment() { return department; } public void setDepartment(String v) { department = v; }
    public String getBatch() { return batch; } public void setBatch(String v) { batch = v; }
    public String getAcademicPeriod() { return academicPeriod; } public void setAcademicPeriod(String v) { academicPeriod = v; }
}
