package com.fams.model;

import java.time.LocalDate;

public class MedicalRecord {
    private int medicalId;
    private String studentRegNo;
    private String studentName;
    private String courseCode;
    private ComponentType componentType;
    private int sessionNumber;
    private LocalDate absentDate;
    private String referenceNo;
    private String reason;
    private LocalDate submissionDate;
    private MedicalStatus status;
    private String reviewedBy; // Technical Officer username or name
    private String imagePath;  // Path or filename of medical certificate image

    public MedicalRecord(int medicalId, String studentRegNo, String studentName, String courseCode, ComponentType componentType, int sessionNumber, LocalDate absentDate, String referenceNo, String reason, LocalDate submissionDate, MedicalStatus status, String reviewedBy, String imagePath) {
        this.medicalId = medicalId;
        this.studentRegNo = studentRegNo;
        this.studentName = studentName;
        this.courseCode = courseCode;
        this.componentType = componentType;
        this.sessionNumber = sessionNumber;
        this.absentDate = absentDate;
        this.referenceNo = referenceNo;
        this.reason = reason;
        this.submissionDate = submissionDate;
        this.status = status;
        this.reviewedBy = reviewedBy;
        this.imagePath = imagePath;
    }

    public MedicalRecord(int medicalId, String studentRegNo, String studentName, String courseCode, ComponentType componentType, int sessionNumber, LocalDate absentDate, String referenceNo, String reason, LocalDate submissionDate, MedicalStatus status, String reviewedBy) {
        this(medicalId, studentRegNo, studentName, courseCode, componentType, sessionNumber, absentDate, referenceNo, reason, submissionDate, status, reviewedBy, "");
    }

    public int getMedicalId() { return medicalId; }
    public void setMedicalId(int medicalId) { this.medicalId = medicalId; }

    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public ComponentType getComponentType() { return componentType; }
    public void setComponentType(ComponentType componentType) { this.componentType = componentType; }

    public int getSessionNumber() { return sessionNumber; }
    public void setSessionNumber(int sessionNumber) { this.sessionNumber = sessionNumber; }

    public LocalDate getAbsentDate() { return absentDate; }
    public void setAbsentDate(LocalDate absentDate) { this.absentDate = absentDate; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDate getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDate submissionDate) { this.submissionDate = submissionDate; }

    public MedicalStatus getStatus() { return status; }
    public void setStatus(MedicalStatus status) { this.status = status; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
