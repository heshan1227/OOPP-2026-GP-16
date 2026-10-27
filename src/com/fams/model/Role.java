package com.fams.model;

public enum Role {
    ADMIN("Administrator", "Full system administration & control"),
    LECTURER("Lecturer", "Course material management & academic view"),
    TECHNICAL_OFFICER("Technical Officer", "Attendance & Medical records management"),
    UNDERGRADUATE("Undergraduate", "Student portal - view attendance, medicals, timetables & notices");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
