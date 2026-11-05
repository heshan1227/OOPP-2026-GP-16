package com.fams.model;

import java.time.LocalDate;

public class AttendanceRecord {
    private int recordId;
    private String studentRegNo;
    private String studentName;
    private String courseCode;
    private String courseName;
    private ComponentType componentType; // THEORY or PRACTICAL
    private int sessionNumber; // 1 to 15
    private LocalDate sessionDate;
    private boolean isPresent;
    private boolean hasMedicalSubmitted;
    private boolean isMedicalApproved;

    public AttendanceRecord(int recordId, String studentRegNo, String studentName, String courseCode, String courseName, ComponentType componentType, int sessionNumber, LocalDate sessionDate, boolean isPresent, boolean hasMedicalSubmitted, boolean isMedicalApproved) {
        this.recordId = recordId;
        this.studentRegNo = studentRegNo;
        this.studentName = studentName;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.componentType = componentType;
        this.sessionNumber = sessionNumber;
        this.sessionDate = sessionDate;
        this.isPresent = isPresent;
        this.hasMedicalSubmitted = hasMedicalSubmitted;
        this.isMedicalApproved = isMedicalApproved;
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public ComponentType getComponentType() { return componentType; }
    public void setComponentType(ComponentType componentType) { this.componentType = componentType; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public boolean isPresent() { return isPresent; }
    public void setPresent(boolean present) { isPresent = present; }

    public boolean isHasMedicalSubmitted() { return hasMedicalSubmitted; }
    public void setHasMedicalSubmitted(boolean hasMedicalSubmitted) { this.hasMedicalSubmitted = hasMedicalSubmitted; }

    public boolean isMedicalApproved() { return isMedicalApproved; }
    public void setMedicalApproved(boolean medicalApproved) { isMedicalApproved = medicalApproved; }

    public String getStatusText() {
        if (isPresent) return "Present";
        if (hasMedicalSubmitted) {
            return isMedicalApproved ? "Absent (Approved Medical)" : "Absent (Medical Pending/Rejected)";
        }
        return "Absent";
    }
}
